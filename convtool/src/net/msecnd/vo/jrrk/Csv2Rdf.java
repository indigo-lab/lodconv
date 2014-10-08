package net.msecnd.vo.jrrk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.msecnd.vo.jrrk.sdf.*;
import net.msecnd.vo.jrrk.util.CsvUtil;
import net.msecnd.vo.jrrk.util.FileUtil;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

/**
 * CSVをRDF(XML)へ変換する。(Jenaライブラリ版)
 * 
 * 
 */
public class Csv2Rdf {

	// key:略称 val:URL
	private HashMap<String, String> prefixMap = new HashMap<String, String>();

	private File inputFileInst;
	private File inputJsonFileInst;

	private String outputFile, errorPath;
	private String inputFileEncode;

	/** ドキュメントのベースURL(内部でファイル名を足す） */
	private String baseUrl;

	/** エラー時に標準出力に出力するか(同期処理用) */
	private boolean outError;

	/** 空タグでも出力するか */
	private boolean outVoidTag;

	private String outputEncode = "UTF-8";

	private boolean debugLog;
	/** カラム順とURI(展開済み)のリスト */
	private List<String[]> columnUrlList;

	/** ダブリンコアのURI(SDFのメタ用) */
	private static String DCURI = "http://purl.org/dc/elements/1.1/";

	/** rdf:typeのURI(rdf:typeの値格納用) */
	private static String AURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	/**
	 * 
	 * @param inputFileInst
	 * @param inputFileEncode
	 * @param outputFile
	 * @param errorFile
	 */
	public Csv2Rdf(File inputFileInst, File inputJsonFileInst,
			String inputFileEncode, String outputFile, String errorPath,
			String baseUrl, boolean debugLog, boolean outVoidTag) {
		this.baseUrl = baseUrl;
		this.inputFileInst = inputFileInst;
		this.outputFile = outputFile;
		this.inputFileEncode = inputFileEncode;
		this.errorPath = errorPath;
		this.debugLog = debugLog;
		this.outVoidTag = outVoidTag;
		this.inputJsonFileInst = inputJsonFileInst;
		prefixMap = new HashMap<String, String>();

		columnUrlList = new ArrayList<String[]>();
	}

	/**
	 * 
	 * @param inputFile
	 * @param inputFileEncode
	 * @param outputFile
	 * @param errorFile
	 */
	public Csv2Rdf(String inputFile, String inputJsonFile,
			String inputFileEncode, String outputFile, String errorFile,
			String baseUrl, boolean debugLog, boolean outVoidTag) {
		this(new File(inputFile), (inputJsonFile == null ? null : new File(
				inputJsonFile)), inputFileEncode, outputFile, errorFile,
				baseUrl, debugLog, outVoidTag);
	}

	/**
	 * @return 0:正常 1:変換失敗 2:IO失敗
	 * @throws IOException
	 */
	public int exec() {
		try {
			String fileBaseName = FileUtil.getFileBaseName(inputFileInst
					.getName());
			List<String> errorList = new ArrayList<String>();
			PrintStream sout = null;
			try {
				sout = new PrintStream(System.out, true, outputEncode);
			} catch (UnsupportedEncodingException e1) {
			}

			int linecount = 0;
			try {
				BufferedReader ordBr = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(inputFileInst),
								inputFileEncode));

				Model model = ModelFactory.createDefaultModel();

				// .txtはtab, .csvはカンマ(Excel仕様に合わせる）
				boolean tabCsv = "txt".equalsIgnoreCase(FileUtil
						.getFileExtension(inputFileInst.getName()));
				boolean ttl = "ttl".equalsIgnoreCase(FileUtil
						.getFileExtension(inputFileInst.getName()));
				// SDFに関してはCSV+JSONの時のみだが、txt+JSONも含める
				boolean sdf = inputJsonFileInst != null
						&& inputJsonFileInst.exists();

				boolean hasCSVHeader = false;

				if (ttl) {
					InputStream in = FileManager.get().open(
							inputFileInst.getAbsolutePath());

					model.read(in, null, "TURTLE");
					// ////////////////////////////////////////////////////////////////

					StmtIterator allStIter = model.listStatements();

					Set<Statement> deleteStatement = new HashSet<Statement>();

					while (allStIter.hasNext()) {
						Statement sta = allStIter.next();
						sout.println("S:s:" + sta.toString());

						try {
							Resource subject = sta.getSubject();
							Property property = sta.getPredicate();

							String propUri = property.getURI();
							String objStr = sta.getObject().toString();

							sout.println("S:Resource  :" + subject.toString());
							sout.println("S:Property:" + propUri);
							sout.println("S:Object:" + objStr);

							
							if (subject.toString().startsWith("file")) {
								deleteStatement.add(sta);

								String onwUrl;
								if (baseUrl.endsWith("/")) {
									onwUrl = baseUrl + fileBaseName + ".rdf";
								} else {
									onwUrl = baseUrl + "/" + fileBaseName
											+ ".rdf";
								}
								Resource rs = model.getResource(onwUrl);
								if (rs == null) {
									rs = model.createResource(onwUrl);
								}
								Property p = model.createProperty(propUri);
								rs.addProperty(p, objStr);
								writeErrorLog("add:" + rs.toString());

							}
						} catch (Exception e) {
							errorList.add("TTLファイルのフォーマットが不正です。");
							e.printStackTrace();
						}
					}
					// 削除のために回す。

					for (Statement sta : deleteStatement) {
						model.remove(sta);
					}

					// ////////////////////////////////////////////////////////////////

				} else {
					String onwUrl;
					String ownBaseUrl;
					if (baseUrl.endsWith("/")) {
						onwUrl = baseUrl + fileBaseName + ".rdf";
						ownBaseUrl = baseUrl + fileBaseName + "/";
					} else {
						onwUrl = baseUrl + "/" + fileBaseName + ".rdf";
						ownBaseUrl = baseUrl + "/" + fileBaseName + "/";
					}

					if (sdf) {
						// SDFの暫定対応
						// RDFグラフは、先頭カラムに対してStar型になる。入れ子等は出来ない。
						// NameSpaceのprefix処理はしてない
						// 先頭カラムの値を主語にしている/先頭カラムのfieldNameは使わない
						// URIはuriの値を使う。

						FileReader reader = new FileReader(inputJsonFileInst);
						Gson gson = new Gson();
						SDFBase sdfBase = gson.fromJson(reader, SDFBase.class);
						reader.close();
						String name = sdfBase.getName();
						String title = sdfBase.getTitle();
						model.setNsPrefix("asdf", ownBaseUrl);
						Resource rs = model.createResource(onwUrl);

						// SDF -> DC
						// name->title
						// title->Description

						Property nameP = model.createProperty(DCURI + "title");
						rs.addProperty(nameP, name);
						if (title != null) {
							Property titleP = model.createProperty(DCURI
									+ "description");
							rs.addProperty(nameP, titleP);
						}

						List<String> filedNameList = new ArrayList<String>();

						List<SDFResources> sdfSDFResources = sdfBase
								.getResources();
						for (SDFResources res : sdfSDFResources) {
							// 仕様上はSDFResourcesが複数あり、
							// それぞれに複数csvが入る場合があるが、常に先頭のみ見る(pathは見ない)

							List<SDFField> fields = res.getSchema().getFields();
							for (int i = 0; i < fields.size(); i++) {
								// 型は見ない
								// URI決定方法 uriがあれば使う
								// なければ、nameを使う
								// nameがuriでない場合は、自分のUri+p+nameを使う。
								// nameもuriもない場合は、idを使う
								// name or uri = uri

								SDFField field = fields.get(i);
								String fieldName = field.getName();
								String typeStr = field.getType();
								String fUrl = field.getUri();
								String id = field.getId();

								if (fUrl == null
										|| fUrl.trim().trim().length() == 0) {
									if (fieldName == null
											|| fieldName.trim().length() == 0) {
										fieldName = id;
									} else {
										fieldName = fieldName.trim();
									}
									if (!fieldName.startsWith("http")) {
										fUrl = ownBaseUrl + "p" + fieldName;
									} else {
										fUrl = fieldName;
									}
								}
								filedNameList.add(fUrl);
							}
						}
						// CSVパース
						// 先頭カラムを主語にする。この時先頭カラムのfieldNameは見ない

						String fstr;

						StringBuffer bf = new StringBuffer();
						while ((fstr = ordBr.readLine()) != null) {
							bf.append(fstr + "\n");
						}
						ordBr.close();

						List<List<String>> stsListList = CsvUtil
								.getCsvValList(bf.toString());

						boolean first = true;
						for (List<String> stsList : stsListList) {
							linecount++;
							if (stsList == null || stsList.size() == 0) {
								continue;
							}
							if (first) {
								first = false;
								continue;
							}

							String[] sts = stsList.toArray(new String[0]);

							// データ行
							String col = sts[0];
							String url;
							if (!col.startsWith("http")) { 
								url = ownBaseUrl + "v" + col;
							} else {
								url = col;
							}

							Resource dataRs = model.createResource(url);

							for (int i = 1; i < sts.length; i++) {
								String val = sts[i];
								String js = filedNameList.get(i);
								Property p = model.createProperty(js);
								dataRs.addProperty(p, val);
							}

						}

					} else {

						String fstr;

						StringBuffer bf = new StringBuffer();
						while ((fstr = ordBr.readLine()) != null) {
							bf.append(fstr + "\n");
						}
						ordBr.close();

						List<List<String>> stsListList;

						if (tabCsv) {
							stsListList = CsvUtil.getTsvValList(bf.toString());
						} else {
							// カンマ区切り
							stsListList = CsvUtil.getCsvValList(bf.toString());
						}

						for (List<String> stsList : stsListList) {

							linecount++;
							if (stsList == null || stsList.size() == 0) {
								continue;
							}
							String[] sts = stsList.toArray(new String[0]);

							String s0 = sts[0].trim();
							if (s0.startsWith("@prefix")) { // prefix宣言

								hasCSVHeader = true;

								// @prefix uc: <http://www.uidcenter.org/vocab#>
								// .
								String preUrl = s0.substring(8);
								int commaPos = preUrl.indexOf(':');
								String pre = preUrl.substring(0, commaPos)
										.trim();
								String urlStrwA = preUrl
										.substring(commaPos + 1);
								int lPos = urlStrwA.indexOf('<');
								int mPos = urlStrwA.indexOf('>');

								String urlStr = urlStrwA.substring(lPos + 1,
										mPos);

								// sout.println(pre + " <- " + urlStr);
								prefixMap.put(pre, urlStr);
								model.setNsPrefix(pre, urlStr);

								continue;
							} else if (s0.startsWith("_:this")) {
								// このドキュメントの定義。
								// _:this dc:title "test1"; aaa:bbb "test2" . ,
								String jsStr = s0.substring(7).trim();

								String[] poStrs = jsStr.split(";");
								// 外からの値を入れる。+自分ファイル名.rdf

								Resource rs = model.createResource(onwUrl);

								for (String colVal : poStrs) { // dc:title
																// "test1"
									String[] po = colVal.trim().split(" ");
									int pos = po[0].indexOf(':');
									if (pos <= 0) {
										continue;
									}
									String pre = po[0].substring(0, pos);
									String exp = po[0].substring(pos + 1)
											.trim();
									String url = prefixMap.get(pre) + exp;
									Property p = model.createProperty(url);
									
									rs.addProperty(p,
											po[1].replaceAll("\"", ""));
								}
								// sout.println(jsStr);

							} else if (s0.startsWith("\"_:this")) { // ExcelからCSVを作る場合に、発生することがある
								// 基本的には上と同じ。
								String tJsStr = s0
										.substring(8, s0.length() - 1);

								String jsStr = tJsStr.replaceAll("\"\"", "\"");

								String[] poStrs = jsStr.split(";");
								Resource rs = model.createResource(onwUrl);

								for (String colVal : poStrs) { // dc:title
																// "test1"
									String[] po = colVal.trim().split(" ");
									int pos = po[0].indexOf(':');
									if (pos <= 0) {
										continue;
									}
									String pre = po[0].substring(0, pos);
									String exp = po[0].substring(pos + 1)
											.trim();
									String url = prefixMap.get(pre) + exp;
									Property p = model.createProperty(url);
									// 
									String addStr = po[1].replaceAll("\"", "");
									rs.addProperty(p, addStr);
								}

								// sout.println(jsStr);

							} else if (s0.startsWith("@comment")) { // コメント：無視
								continue;
							} else if (s0.length() == 0) {// 空：無視
								continue;

							} else if (s0.startsWith("@column")) {

								hasCSVHeader = true;

								// タイトル行

								for (int i = 1; i < sts.length; i++) {
									String colVal = sts[i];
									String[] uris = colVal.split(";");
									if (uris.length == 0) {// コメント行扱い
										columnUrlList.add(new String[0]);
										break;
									}

									String[] setUris = new String[uris.length];
									for (int j = 0; j < uris.length; j++) {
										String val = uris[j];
										int pos = val.indexOf(':');
										if (pos <= 0) {
											// columnUrlList.add(new String[0]);
											break;
										}
										String pre = val.substring(0, pos);
										String exp = val.substring(pos + 1);
										String url = prefixMap.get(pre) + exp;
										setUris[j] = url;
									}
									columnUrlList.add(setUris);
								}
							} else {
								// データ行
								String col = sts[0];
								// ID
								int pos = col.indexOf(':');
								if (pos <= 0) {
									continue;
								}
								String pre = col.substring(0, pos);
								String exp = col.substring(pos + 1);
								String url = prefixMap.get(pre) + exp;

								Resource ordRs = model.createResource(url);
								// key:uriuri... var:res
								HashMap<String, Resource> uriResMap = new HashMap<String, Resource>();

								// data
								for (int i = 1; i < sts.length; i++) {
									Resource currentRs = ordRs;
									String val = sts[i];
									if (!outVoidTag && val.length() == 0) {
										continue;
									}

									// System.out.println(val);

									if (i > columnUrlList.size()) {
										break;
									}

									String[] jss = columnUrlList.get(i - 1);

									if (jss == null || jss.length == 0) {
										continue;
									}

									// 深掘りの場合
									String connetJs = "";
									for (int j = 0; j < jss.length; j++) {
										String js = jss[j];
										if (js == null
												|| js.trim().length() == 0) {
											continue; // コメント行
										}
										if (j != jss.length - 1) {
											connetJs = connetJs + js;

											// tagname:name
											// でも指定可に。
											String realUri = js;
											int cPos = js.indexOf(":");
											int eCPos = js.lastIndexOf(":");
											if (cPos != eCPos) {
												realUri = realUri.substring(0,
														eCPos);
											}

											// System.out.println(cPos + "-" +
											// eCPos + " " + realUri );

											Resource chVoidRs = uriResMap
													.get(connetJs);
											if (chVoidRs == null) {
												// オブジェクトを挟む場合
												chVoidRs = model
														.createResource();
												uriResMap.put(connetJs,
														chVoidRs);
											}
											Property p = model
													.createProperty(realUri);
											currentRs.addProperty(p, chVoidRs);
											currentRs = chVoidRs;

										} else {

											// rdf:type
											if (AURI.equals(js)) {
												// rdf:type (a)
												String typeUri;
												if (val.startsWith("http")) {
													typeUri = val;
												} else {
													int typePos = val
															.indexOf(':');
													if (typePos <= 0) {
														continue;
													}

													String typePre = val
															.substring(0,
																	typePos);
													String typeExp = val
															.substring(typePos + 1);
													typeUri = prefixMap
															.get(typePre)
															+ typeExp;
												}

												Resource rc = model
														.createResource(typeUri);
												Property p2 = model
														.createProperty(AURI);
												currentRs.addProperty(p2, rc);

											} else {
												Property p = model
														.createProperty(js);
												currentRs.addProperty(p, val);
											}

										}
									}
								}
							}
						}
					}
				}
				// 
				// Writer w2 = new OutputStreamWriter(new
				// FileOutputStream(outputFile+".ttl"),
				// "UTF-8");
				// model.write(w2, "TURTLE");

				// 

				if (!ttl && !sdf && !hasCSVHeader) {
					errorList.add("ヘッダ情報のないCSVファイルのため、変換できません。");
				}

				// XML書き出し
				Writer w = new OutputStreamWriter(new FileOutputStream(
						outputFile), "UTF-8");
				model.write(w, "RDF/XML");

				// ファイルサイズ確認
				File outFile = new File(outputFile);

				long size = outFile.length();
				if (size < LodConvert.VOID_RDF_SIZE) {
					errorList.add("出力ファイルの中身がありません");
					outFile.delete();
				}

			} catch (Exception e) {
				e.printStackTrace();
				errorList.add("ファイルのフォーマットが不正です。" + inputFileInst.getName()
						+ " " + linecount + "行目 (" + e.getMessage() + " )");
				// errorList.add(e.getMessage());
			}

			if (errorList.size() > 0) {
				// エラー書き出し。
				BufferedWriter errW = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(errorPath),
								outputEncode));
				errW.write("<?xml version=\"1.0\" encoding=\"" + outputEncode
						+ "\" ?>\n");
				errW.write("<?xml-stylesheet type=\"text/xsl\" href=\"/style/err.xsl\" ?>\n");

				errW.write("<div class=\"error\">\n");
				for (String errLine : errorList) {
					String outStr = "<p>" + FileUtil.getXMLEncodeStr(errLine)
							+ "</p>";
					errW.write(outStr + "\n");
					if (outError) {
						sout.println(outStr);
					}
				}
				errW.write("</div>");
				errW.flush();
				errW.close();
				return LodConvert.RETCODE_CONV_NG;
			} else {
				return LodConvert.RETCODE_OK;
			}

		} catch (Exception e) {
			writeErrorLog(e.getMessage());
			return LodConvert.RETCODE_SYSTEM_NG;
		}
	}

	/**
	 * 
	 * @param s
	 */
	private void writeErrorLog(String s) {
		if (debugLog) {
			System.err.println(this.getClass().getName() + ":" + s);
		}
	}

	/**
	 * @param args
	 *            [0] inputFile [1] inputFileEncode [2] outFile [3] errorFile
	 *            [4] baseUrl [5] あればjson
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws Exception, IOException {
		String jsonFile = args.length > 5 ? args[5] : null;

		Csv2Rdf csv2Rdf = new Csv2Rdf(args[0], jsonFile, args[1], args[2],
				args[3], args[4], true, false);
		csv2Rdf.exec();
	}
}
