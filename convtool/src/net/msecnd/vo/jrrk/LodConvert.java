package net.msecnd.vo.jrrk;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import net.msecnd.vo.jrrk.util.FileUtil;

/**
 * cronのbashスクリプトから呼びだられるコンバート+バリデート部分の Java側呼び出し部分の実装
 * 
 * 
 */
public class LodConvert {

	public static final int RETCODE_OK = 0;
	public static final int RETCODE_CONV_NG = 1;
	public static final int RETCODE_VILADATE_NG = 2;
	public static final int RETCODE_SYSTEM_NG = -1;

	public static final long VOID_RDF_SIZE = 200;

	/** 変換対象ファイルの拡張子*/
	private static final String[] CONVERT_EXP = { "csv", "txt", "ttl" };

	/** 変換を行わずにバリデートのみ行う拡張子 */
	private static final String[] NOCONVERT_EXP = { "rdf" };

	// 上記以外は無視する。

	/** 変換対象、バリデートのみ対象のリスト */
	private static Set<String> convertExpSet, noConvertExpSet;

	private static String outputEncode = "UTF-8";

	static {
		convertExpSet = new HashSet<String>();
		for (String s : CONVERT_EXP) {
			convertExpSet.add(s);
		}

		noConvertExpSet = new HashSet<String>();
		for (String s : NOCONVERT_EXP) {
			noConvertExpSet.add(s);
		}
	}

	/**
	 * @param args
	 *            [0] 読み込みディレクトリ(upload) ファイル名の場合はそのファイルのみ対象 [1]
	 *            テンポラリDir(変換後ファイル置き場） [2] 成功時書き込みディレクトリ (update) [3] エラーファイル置き場
	 *            (failed) [4] RDFS(専用)Dir [5] RDFS(汎用)Dir [6] 無視リストDir [7]
	 *            ドキュメントのNamesapce(基底URI) [8] 型チェックをするかどうか(true/false) [9]
	 *            有る場合、true の場合は詳細ログを出力する [10] 有る場合、true :空タグでも出力する [11]
	 *            有る場合、リテラルのリソース変換時に使うURI。ない場合は[7]を使う
	 * 
	 * 
	 */
	public static void main(String[] args) {

		int ret = 0;
		String inputDir = args[0];
		String tmpOutDir = args[1];
		String outputDir = args[2];
		String errorDir = args[3];
		String schemaDir = args[4];
		String commonSchemaDir = args[5];
		String ignoreUriDir = args[6];

		String baseUri = args[7];

		String resourceBaseUri = baseUri;
		boolean typeCheck = Boolean.valueOf(args[8]);

		boolean outVoidTag = false;
		boolean procSync = false;
		boolean debugLog = false;
		if (args.length >= 10) {
			debugLog = Boolean.valueOf(args[9]);
		}
		if (args.length >= 11) {
			outVoidTag = Boolean.valueOf(args[10]);
		}

		if (args.length >= 12) {
			resourceBaseUri = args[11];
		}

		PrintStream sout = null;
		try {
			sout = new PrintStream(System.out, true, outputEncode);
		} catch (UnsupportedEncodingException e1) {
		}

		// 入力値チェック

		File inputDirFile = new File(inputDir);
		if (!inputDirFile.exists()) {
			// 指定されたinputディレクトリがないとき
			System.out.println("not found:" + inputDir);
			System.exit(RETCODE_SYSTEM_NG);
		}

		File outputDirFile = new File(outputDir);
		if (!outputDirFile.exists()) {
			// 指定されたoutputディレクトリがないとき
			System.out.println("not found:" + outputDir);
			System.exit(RETCODE_SYSTEM_NG);
		}

		File[] fileList;
		// ファイル指定（同期処理）の場合
		if (inputDirFile.isFile()) {
			procSync = true;
			fileList = new File[1];
			fileList[0] = inputDirFile;
		} else { // ディレクトリの場合
			fileList = inputDirFile.listFiles();
		}

		RdfValidater rdfValidater = null;
		try {
			rdfValidater = new RdfValidater(outputDir, errorDir, schemaDir,
					commonSchemaDir, ignoreUriDir, "UTF-8", typeCheck,
					procSync, debugLog, resourceBaseUri);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(RETCODE_SYSTEM_NG);
		}

		for (File f : fileList) {
			String exp = FileUtil.getFileExtension(f.getName()).toLowerCase();
			String baseName = FileUtil.getFileBaseName(f.getName());

			// エラーファイル
			String errorFileName = errorDir + File.separator + baseName
					+ ".xml";

			// 中間ファイル
			String convertedFileName = tmpOutDir + File.separator + baseName
					+ ".rdf";

			// 最終ファイル
			String checkedOutputFileName = outputDir + File.separator
					+ baseName + ".rdf";

			long inputTs = f.lastModified();

			File outF = new File(checkedOutputFileName);
			if (!procSync
					&& (outF == null || (outF.exists() && inputTs < outF
							.lastModified()))) {
				// 生成済みの場合
				sout.println("[INFO]RDFファイルの方が新しいため変換を行いません。" + f.getName());
				continue;
			}

			// エラーファイルの方が新しい場合：変換に失敗するので処理しない

			File errorF = new File(errorFileName);
			if (!procSync
					&& (errorF == null || (errorF.exists() && inputTs < errorF
							.lastModified()))) {
				sout.println("[INFO]エラーファイルの方が新しいため変換を行いません。" + f.getName());
				continue;
			}

			if (convertExpSet.contains(exp)) {

				File jsonFile = new File(f.getParent() + File.separator
						+ baseName + ".json");

				// 変換処理+バリデート
				Csv2Rdf csv2Rdf = new Csv2Rdf(f, jsonFile, "UTF-8",
						convertedFileName, errorFileName, baseUri, debugLog,
						outVoidTag);
				ret = csv2Rdf.exec();
				if (ret == RETCODE_OK) {
					sout.println("<p>" + f.getName() + " の変換に成功しました。</p>");

					// if(typeCheck){
					// sout.println("<p>"+ f.getName() +
					// " のバリデート(URI,型チェック)を行います。</p>");
					// }else{
					// sout.println("<p>"+ f.getName() +
					// " のバリデート(URI)を行います。</p>");
					// }

					String validateTypeStr;
					if (typeCheck) {
						validateTypeStr = "バリデート(URI,型チェック)";
					} else {
						validateTypeStr = "バリデート(URI)";
					}

					ret = rdfValidater.exec(convertedFileName);
					if (ret == RETCODE_OK) {
						// new File(convertedFileName).delete();
						sout.println("<p>" + f.getName() + " の"
								+ validateTypeStr + " に成功しました。</p>");
						sout.println("<p>" + f.getName() + " の登録に成功しました。</p>");
					} else {
						sout.println("<p>" + f.getName() + " の"
								+ validateTypeStr + " に失敗しました。</p>");
					}
				} else {
					sout.println("<p>" + f.getName() + " の変換に失敗しました。</p>");
				}

			} else if (noConvertExpSet.contains(exp)) {
				// バリデートだけ
				// if(typeCheck){
				// sout.println("<p>"+ f.getName() +
				// " のバリデート(URI,型チェック)を行います。</p>");
				// }else{
				// sout.println("<p>"+ f.getName() + " のバリデート(URI)を行います。</p>");
				// }
				ret = rdfValidater.exec(f);

				String validateTypeStr;
				if (typeCheck) {
					validateTypeStr = "バリデート(URI,型チェック)";
				} else {
					validateTypeStr = "バリデート(URI)";
				}

				if (ret == RETCODE_OK) {
					sout.println("<p>" + f.getName() + " の" + validateTypeStr
							+ " に成功しました。</p>");
					sout.println("<p>" + f.getName() + " の登録に成功しました。</p>");
				} else {
					sout.println("<p>" + f.getName() + " の" + validateTypeStr
							+ " に失敗しました。</p>");
				}
			} else {
				// 対象外：同期の場合はエラーにする
				if (procSync) {
					sout.println("<p>" + f.getName()
							+ " は同期変換対象外ファイルのため変換を行いません。</p>");
					ret = RETCODE_CONV_NG;
				}
			}
		}
		System.exit(ret);
	}

}
