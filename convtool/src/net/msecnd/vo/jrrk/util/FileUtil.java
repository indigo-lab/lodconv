package net.msecnd.vo.jrrk.util;

import java.io.File;

/**
 * 
 * 
 */
public class FileUtil {

	public static final String FS = File.separator;

	private FileUtil() {
	};

	/**
	 * パスからファイル名を取得する
	 * 
	 * @param name
	 *            ファイルパス
	 * @param del
	 *            パス区切り文字列
	 * @return ファイル名
	 */
	public static String getFileName(String name, char del) {
		int dotPos = name.lastIndexOf(del);
		if (dotPos <= 0) {
			return "";
		}
		return name.substring(dotPos + 1);
	}

	/**
	 * ファイルの拡張子を取得する
	 * 
	 * @param name
	 *            ファイル名
	 * @return ファイルの拡張子
	 */
	public static String getFileExtension(String name) {
		int dotPos = name.lastIndexOf('.');
		if (dotPos <= 0) {
			return "";
		}
		return name.substring(dotPos + 1).toLowerCase();
	}

	/**
	 * ファイルの本体名を取得する。
	 * 
	 * @parma name ファイル名
	 * @return ファイルの本体名
	 */
	public static String getFileBaseName(String name) {
		int dotPos = name.lastIndexOf('.');
		if (dotPos < 0) {
			return name;
		}
		return name.substring(0, dotPos);
	}


	/**
	 * 不等号等をエンコードした文字列を返す
	 * @param val
	 * @return
	 */
	public static String getXMLEncodeStr(String val){
		if(val != null){
			String s1 = val.replace("&", "&amp;");
			String s2 = s1.replace("<", "&lt;").replace(">", "&gt;");
			return s2.replace("\"", "&quot;");
		}
		return val;
	}
	
}