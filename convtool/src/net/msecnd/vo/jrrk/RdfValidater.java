package net.msecnd.vo.jrrk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import net.msecnd.vo.jrrk.util.CalendarUtil;
import net.msecnd.vo.jrrk.util.FileUtil;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.LiteralRequiredException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileManager;

/**
 * バリデーター/値の型入れ/リソース化
 * 
 * 
 */
public class RdfValidater {

	/** 一般のRDFスキーマのディレクトリ */
	private String rdfSchemaDirName;
	/** 共通のRDFスキーマのディレクトリ */
	private String commonRdfSchemaDirName;

	/** 無視URLリストのファイル名 */
	private String ignoreURIListDir;

	/** 出力先 */
	private String outputDir;

	/** エラー出力先 */
	private String errorOuputDir;

	/** エラーページのエンコード */
	private String outputFileEncode;

	private String ignoreListEncode = "UTF-8";

	/** 型チェックをするかどうか */
	private boolean typeCheck;

	/** エラー時に標準出力に出力するか(同期処理用) */
	private boolean outError;

	private String outputEncode = "UTF-8";

	/** バリデート比較部分(key:uri val:type */
	private Map<String, String> checkUriTypeMap;

	/** リソースへの書き換え用 */
	private Set<String> resourceUriSet;

	/** 無視対象（URI) */
	private Set<String> ignoreUriSet;
	/** 無視対象(ドメイン) */
	private Set<String> ignoreHostSet;

	/**  型チェック部分 URIとタイプの指定リスト */
	private static Map<String, String> urlTypeMap;

	/**
	 * 埋め込むURIのベース
	 */
	private String resourceUriBase;

	// 初期化エラー
	private List<String> initErrorList;

	private boolean debugLog;

	public static final String TYPE_STRING = "STRING";
	public static final String TYPE_INT = "INT";

	public static final String TYPE_RESOURCE = "RESOURCE";

	public static final String TYPE_FLOAT = "FLOAT";

	public static final String TYPE_DATETIME = "DATETIME";
	public static final String TYPE_DATE = "DATE";
	public static final String TYPE_TIME = "TIME";
	
	public static final String NS_RDFS = "http://www.w3.org/2000/01/rdf-schema#";

	public static final String NS_XSD = "http://www.w3.org/2001/XMLSchema#";

	// // rdf:typeのURI(rdf:typeの値格納用)
	// private static String RDFRES =
	// "http://www.w3.org/1999/02/22-rdf-syntax-ns#resouce";

	static {
		urlTypeMap = new HashMap<String, String>();
		urlTypeMap.put(NS_RDFS + "Literal", TYPE_STRING);

		//
		urlTypeMap.put(NS_XSD + "int", TYPE_INT);
		urlTypeMap.put(NS_XSD + "decimal", TYPE_INT);
		urlTypeMap.put(NS_XSD + "float", TYPE_FLOAT);


		urlTypeMap.put(NS_XSD + "dateTime", TYPE_DATETIME);
		urlTypeMap.put(NS_XSD + "dateTIme", TYPE_DATETIME); // jrrkのミスタイプ
		urlTypeMap.put(NS_XSD + "integer", TYPE_INT);
		urlTypeMap.put(NS_XSD + "string", TYPE_STRING);
		urlTypeMap.put(NS_XSD + "time", TYPE_TIME);

		
		urlTypeMap.put(NS_XSD + "date", TYPE_DATE);
		
		// 上記以外はリソース扱いにする。
		// ただし、未指定はString
	}

	/**
	 * 
	 * @param outputDir
	 * @param errorOuputDir
	 * @param rdfSchemaDirName
	 * @param commonRdfSchemaDirName
	 * @param ignoreURIListFilePath
	 * @param typeCheck
	 * @throws IOException
	 * @throws
	 */
	public RdfValidater(String outputDir, String errorOuputDir,
			String rdfSchemaDirName, String commonRdfSchemaDirName,
			String ignoreURIListDir, String outputFileEncode,
			boolean typeCheck, boolean outError, boolean debugLog,
			String resourceUriBase) throws IOException {
		this.outputDir = outputDir;
		this.errorOuputDir = errorOuputDir;

		this.rdfSchemaDirName = rdfSchemaDirName;
		this.ignoreURIListDir = ignoreURIListDir;
		this.commonRdfSchemaDirName = commonRdfSchemaDirName;
		this.outputFileEncode = outputFileEncode;
		this.debugLog = debugLog;

		this.typeCheck = typeCheck;
		this.outError = outError;
		this.resourceUriBase = resourceUriBase;

		init();
	}

	/**
	 * 指定されたRDFSファイルを読み込んで、URIと型のMapを作成する。
	 * 
	 * @param rdfsFile
	 * @return
	 */
	private Map<String, String> getUriTypeFromRdfs(File rdfsFile) {
		Map<String, String> retMap = new HashMap<String, String>();
		writeErrorLog("Loading:" + rdfsFile.getAbsolutePath());

		try {

			String exp = FileUtil.getFileExtension(rdfsFile.getName());
			String loadFileType;
			if ("rdf".equalsIgnoreCase(exp)) {
				loadFileType = "RDF/XML";
			} else if ("ttl".equalsIgnoreCase(exp)) {
				loadFileType = "TURTLE";
			} else {
				return retMap;
			}

			InputStream in = FileManager.get().open(rdfsFile.getAbsolutePath());
			Model model = ModelFactory.createDefaultModel();
			model.read(in, null, loadFileType);

			in.close();
			//
			StmtIterator allStIter = model.listStatements();
			while (allStIter.hasNext()) {

				Statement sta = allStIter.next();
				// sout.println("S:s:" + sta.toString());
				Resource subject = sta.getSubject();
				Property property = sta.getPredicate();

				String propUri = property.getURI();
				String objStr = sta.getObject().toString();
//
//				System.out.println("S:Resource  :" + subject.toString());
//				System.out.println("S:Property:" + propUri);
//				System.out.println("S:Object:" + objStr);
//
				String typeStr = TYPE_STRING;
				if ("http://www.w3.org/2000/01/rdf-schema#range"
						.equals(propUri) && objStr != null) {
					typeStr = urlTypeMap.get(objStr);
					if (typeStr == null) {
//						 System.out.println("Resource: " +subject.toString()  +"/" + objStr );
						// 定義があるが、所定以外はRes
						typeStr = TYPE_RESOURCE;
						// writeErrorLog("TempSet:" +subject.toString() + " "
						// +objStr + " -> STRING" );
					} else {// 有った場合
						//writeErrorLog("Set:" + subject.toString() + " "
						// + objStr + " -> " + typeStr);
					}
				} else {
					// 定義無しはString
					// writeErrorLog("TempSet:" +subject.toString() + " "
					// +objStr + " -> STRING" );
				}
				String oldType = retMap.get(subject.toString());
				if (oldType == null || oldType.equals(TYPE_STRING)) {
					retMap.put(subject.toString(), typeStr);
				}

			}
		} catch (Exception e) {
			writeErrorLog("RDFスキーマの読み込みに失敗しました。:" + rdfsFile + " "
					+ e.getMessage());
			initErrorList.add("RDFスキーマの読み込みに失敗しました。:" + rdfsFile + " "
					+ e.getMessage());

			// e.printStackTrace();
			// System.err.println(e.getMessage());
			return retMap;
		}
		return retMap;
	}

	/**
	 * RDFスキーマをロードして 初期化を行う。
	 * 
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void init() throws IOException {
		checkUriTypeMap = new HashMap<String, String>();
		ignoreUriSet = new HashSet<String>();
		ignoreHostSet = new HashSet<String>();

		initErrorList = new ArrayList<String>();

		// スキーマ
		File[] schemaListFiles = new File(rdfSchemaDirName).listFiles();
		for (File scf : schemaListFiles) {
			checkUriTypeMap.putAll(getUriTypeFromRdfs(scf));
		}

		// 共通スキーマ
		File[] commonSchemaListFiles = new File(commonRdfSchemaDirName)
				.listFiles();
		for (File scf : commonSchemaListFiles) {
			checkUriTypeMap.putAll(getUriTypeFromRdfs(scf));
		}
		// Resourceへの書き換えリスト作成
		resourceUriSet = new HashSet<String>();
		String[] uriSet = checkUriTypeMap.keySet().toArray(new String[0]);
		for (String uri : uriSet) {
			String type = checkUriTypeMap.get(uri);
			if (TYPE_RESOURCE.equals(type)) {
				resourceUriSet.add(uri);
			}
		}

		// 無視リスト
		String[] ignoreURIListList = new File(ignoreURIListDir).list();
		for (String scf : ignoreURIListList) {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(ignoreURIListDir
							+ File.separator + scf), ignoreListEncode));
			String str;
			while ((str = br.readLine()) != null) {
				if (str == null || str.length() == 0) {
					continue;
				}
				str.trim();
				if (str.startsWith("#")) {
					continue;
				}
				String uri = str;
				if (uri.endsWith("*")) {
					String host;
					try {
						host = new URI(uri).getHost();
					} catch (URISyntaxException e) {
						e.printStackTrace();
						continue;
					}
					ignoreHostSet.add(host);
				} else {
					ignoreUriSet.add(uri);
				}
			}
			br.close();
		}
	}

	/**
	 * newModel に対して、origSta の PropをreplaceResで置き換えるて登録する。
	 * 
	 * @param origSta
	 * @param propUri
	 * @param replaceRes
	 * @param newModel
	 */
	private void replaceProp(Model newModel, Statement origSta,
			RDFNode replaceRes) {
		// 元の目的語でPropを作る
		Property property = origSta.getPredicate();
		String propUri = property.getURI();

		Property ordUriProp = newModel.createProperty(propUri);

		// 元の主語のURIを取得
		Resource subject = origSta.getSubject();
		String ownUri = subject.getURI();
		if (ownUri == null) {
			// 無名ノードが親の時
			AnonId anonId = subject.getId();
			Resource rs = newModel.createResource(anonId);
			Statement newS = newModel.createStatement(rs, ordUriProp,
					replaceRes);
			newModel.add(newS);
		} else {
			Resource rs = newModel.getResource(ownUri);
			if (rs == null) {
				rs = newModel.createResource(ownUri);
				Statement newS = newModel.createStatement(rs, ordUriProp,
						replaceRes);
				newModel.add(newS);
			} else {
				rs.addProperty(ordUriProp, replaceRes);
			}
		}

	}

	/**
	 * 
	 * @param inputFile
	 * @return
	 */
	public int exec(File inputFile) {
		return exec(inputFile.getAbsolutePath());
	}

	/**
	 * バリデートを行う
	 * 
	 * @throws IOException
	 * @return 0:正常 1:変換失敗 2:IO失敗
	 */
	public int exec(String inputFilePath) {

		Set<String> errorUriSet = new HashSet<String>();
		Set<String> warnrUriSet = new HashSet<String>();

		List<String> errorList = new ArrayList<String>();
		errorList.addAll(initErrorList);

		String inputFileName = FileUtil.getFileName(inputFilePath,
				File.separatorChar);
		String inputFileBaseName = FileUtil.getFileBaseName(inputFileName);
		String exp = FileUtil.getFileExtension(inputFileName);
		String loadFileType;
		if ("rdf".equalsIgnoreCase(exp)) {
			loadFileType = "RDF/XML";
		} else if ("ttl".equalsIgnoreCase(exp)) {
			loadFileType = "TURTLE";
		} else {
			writeErrorLog(inputFileName + " はサポートされていないファイル形式です。");
			errorList.add(inputFileName + " はサポートされていないファイル形式です。");
			return LodConvert.RETCODE_SYSTEM_NG;
		}

		PrintStream sout = null;
		try {
			sout = new PrintStream(System.out, true, outputEncode);
		} catch (UnsupportedEncodingException e1) {
		}

		InputStream in = FileManager.get().open(inputFilePath);
		if (in == null) {
			writeErrorLog(inputFileName + " が見つかりません。");
			return LodConvert.RETCODE_SYSTEM_NG;
		}

		Model model = ModelFactory.createDefaultModel();
		Model model2 = ModelFactory.createDefaultModel();
		model.read(in, null, loadFileType);

		try {
			in.close();
		} catch (IOException e1) {
		}

		Map<String, String> prefixMap = model.getNsPrefixMap();
		model2.setNsPrefixes(prefixMap);

		//
		StmtIterator allStIter = model.listStatements();

		while (allStIter.hasNext()) {
			Statement sta = allStIter.next();
			// sout.println("s:" + sta.toString());

			try {
				Property property = sta.getPredicate();

				// sout.println("Property:" + property.getURI());
				String uri = property.getURI();
				if (ignoreUriSet.contains(uri)) {
					if (warnrUriSet.add(uri)) {
						writeErrorLog("バリデート非対象のURIです。:" + uri);
					}
					model2.add(sta);
					continue;
				}
				Literal literal = null;

				try {
					literal = sta.getLiteral();
				} catch (LiteralRequiredException e) { // 元々Resourceで入ってるのはここにくる
					// if (warnrUriSet.add(uri)) {
					// writeErrorLog("リテラル取得不可:" + uri + ")");
					// }
					model2.add(sta);
					continue;
				}

				// sout.println("Literal:" + literal.getString());
				String value = literal.getString();

				String host = new URI(uri).getHost();
				if (ignoreHostSet.contains(host)) {
					if (warnrUriSet.add(uri)) {
						writeErrorLog("バリデート非対象のホスト名です。:" + host + "(" + uri
								+ ")");
					}
					model2.add(sta);
					continue;
				}

				String typeStr = checkUriTypeMap.get(uri);
				boolean isResType = resourceUriSet.contains(uri);
				if (typeStr != null) {
					// OK

					if (!typeCheck) {
						// writeErrorLog("URIバリデートOK :" + uri);
					} else {
						// 型チェック部分。
						boolean chkTypeOk = checkVal(value, typeStr);
						if (chkTypeOk) {
							writeErrorLog("URI/値の型 バリデートOK:" + uri + " "
									+ value + " (" + typeStr + ")");
						} else {
							writeErrorLog("値の型 バリデートNG:" + uri + " " + value
									+ " (" + typeStr + ")");
							errorList.add("値の型 バリデートNG:" + uri + " " + value
									+ " (" + typeStr + ")");
							model2.add(sta); // 本来NG時はいらないが。
							continue;
						}
					}
				} else {
					// エラー処理部分
					if (errorUriSet.add(uri)) {
						errorList.add("URIバリデート失敗:" + uri);
					}
					continue;
				}
				if (!isResType) {
					// 書き換えずに抜ける
					// ---------------------------------------------
					//
					//

					Literal newL = null;
					try {
						if (typeStr.equals(TYPE_INT)) {
//							System.out.println("INT");
							value = value.replace("\"", "").replace(",","");
							int iv = Integer.parseInt(value);
							newL = model2.createTypedLiteral(iv);
						} else if (typeStr.equals(TYPE_FLOAT)) {
//							System.out.println("FLOAT");
							value = value.replace("\"", "").replace(",","");
							float fv = Float.parseFloat(value);
							newL = model2.createTypedLiteral(fv);
						} else if (typeStr.equals(TYPE_DATE)) {
//							System.out.println("DATE:" + value + "(" + uri);
							RDFDatatype daterd = new BaseDatatype(NS_XSD + "date");
							newL =model2.createTypedLiteral(value, daterd);
							
						} else if (typeStr.equals(TYPE_TIME)) {
//							System.out.println("TIME:" + value + "(" + uri);
							RDFDatatype daterd = new BaseDatatype(NS_XSD + "time");
							newL =model2.createTypedLiteral(value, daterd);
							
						} else if (typeStr.equals(TYPE_DATETIME)) {
							System.out.println("DATETIME:" + value + "(" + uri);
							Calendar cv = CalendarUtil.getCalendar(value, TimeZone.getTimeZone("GMT+9"));
							newL = model2.createTypedLiteral(cv);
						}
					} catch (Exception e) {
//						System.out.println(e.getMessage() + " " + uri + " "  + value);
						//	変換失敗の場合。newLをnullにして型指定の追加をしない
						newL = null;
					}

					if (newL != null) {
						replaceProp(model2, sta, newL);
						continue;
					}

					// ------------------------------

					model2.add(sta);
					continue;
				} else {
					// 書き換え
					// 本来リテラルではなくURIなので、リテラルからURIを作る

					boolean isUri = validateHTTP_URI(value);
					String newUri;
					if (!isUri) { // "^[]<>\|{} CR LF tab '#%;/?:@&=+$s

						String replaceVal = value
								.replaceAll(
										"[\\\"\\^\\[\\]\\(\\)\\'\\<\\>\\\\|\\{\\}\r\n`#%;/?:@&=\\+$,]",
										"_");

						newUri = resourceUriBase + replaceVal;
						// newUri = resourceUriBase + URLEncoder.encode(value,
						// outputEncode);
						if (!validateHTTP_URI(newUri)) {
							// URI化できないものはそのまま
							System.out.println("Cannot to Resource:" + value
									+ " (" + uri);
							model2.add(sta);
							continue;
						}

					} else {
						newUri = value;
					}
					// System.out.println(newUri);
					// Resourceに入れるオブジェクトを作る

					Resource propRes = model2.createResource(newUri);
					replaceProp(model2, sta, propRes);
				}

			} catch (Exception e) {
				e.printStackTrace();
				errorList.add(e.getMessage());
			}
		}

		try {

			// // 
			// Writer w2 = new OutputStreamWriter(new FileOutputStream(
			// outputDir + File.separator + "debug_" + inputFileName), "UTF-8");
			// model2.write(w2, "TURTLE");
			// 

			if (errorList.size() > 0) {
				// エラー書き出し。
				// BufferedWriter errW = new BufferedWriter(
				// new OutputStreamWriter(new FileOutputStream(
				// errorOuputDir + File.separator
				// + inputFileBaseName + ".xml"),
				// outputFileEncode));
				// errW.write("<?xml version=\"1.0\" encoding=\""
				// + outputFileEncode + "\" ?>\n");
				// errW.write("<?xml-stylesheet type=\"text/xsl\" href=\"/style/err.xsl\" ?>\n");
				// errW.write("<div class=\"error\">\n");
				// for (String errLine : errorList) {
				// String outStr = "<p>" + FileUtil.getXMLEncodeStr(errLine)
				// + "</p>";
				// errW.write(outStr + "\n");
				// if (outError) {
				// sout.println(outStr);
				// }
				// }
				// errW.write("</div>");
				//
				// errW.flush();
				// errW.close();
				return LodConvert.RETCODE_VILADATE_NG;
			} else {
				Writer w = new OutputStreamWriter(new FileOutputStream(
						outputDir + File.separator + inputFileName), "UTF-8");
				model2.write(w, "RDF/XML");

				return LodConvert.RETCODE_OK;
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorList.add(e.getMessage());

			return LodConvert.RETCODE_SYSTEM_NG;
		} finally {
			try {
				if (errorList.size() > 0) {
					// エラー書き出し。
					BufferedWriter errW = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(
									errorOuputDir + File.separator
											+ inputFileBaseName + ".xml"),
									outputFileEncode));
					errW.write("<?xml version=\"1.0\" encoding=\""
							+ outputFileEncode + "\" ?>\n");
					errW.write("<?xml-stylesheet type=\"text/xsl\" href=\"/style/err.xsl\" ?>\n");
					errW.write("<div class=\"error\">\n");
					for (String errLine : errorList) {
						String outStr = "<p>"
								+ FileUtil.getXMLEncodeStr(errLine) + "</p>";
						errW.write(outStr + "\n");
						if (outError) {
							sout.println(outStr);
						}
					}
					errW.write("</div>");

					errW.flush();
					errW.close();
					return LodConvert.RETCODE_VILADATE_NG;
				}
			} catch (JenaException e) {
				e.printStackTrace();
				return LodConvert.RETCODE_VILADATE_NG;
			} catch (Exception e) {
				e.printStackTrace();
				return LodConvert.RETCODE_SYSTEM_NG;
			}
		}

	}

	public static boolean validateHTTP_URI(String uri) {
		final URL url;
		try {
			url = new URL(uri);
		} catch (Exception e1) {
			return false;
		}
		return "http".equals(url.getProtocol());
	}

	/**
	 * 
	 * @param val
	 * @param valType
	 * @return
	 */
	private boolean checkVal(String val, String valType) {

		if (TYPE_STRING.equals(valType)) {
			return true;
		}

		if (TYPE_INT.equals(valType)) {
			try {
				Integer.parseInt(val);
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}
		if (TYPE_FLOAT.equals(valType)) {
			try {
				Double.parseDouble(val);
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}
		return true;
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
	 * 
	 * @param args
	 *            [0]inputFile [1]outputDir [2]errorOutputDir [3]rdfSchemaDir
	 *            [4] commonRdfSchemaDir [5] igonreURIListDir [6]value validater
	 *            [7] resourceBaseUri
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		RdfValidater rdfValidater = new RdfValidater(args[1], args[2], args[3],
				args[4], args[5], "UTF-8", Boolean.valueOf(args[6]), true,
				true, args[7]);

		rdfValidater.exec(args[0]);
	}

}
