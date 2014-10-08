package net.msecnd.vo.jrrk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CsvUtil {
	
	/**
	 * 指定されたTab区切りCSVの中身から、各行をListにしたListを取得する(全体) "は""でエスケープされていること前提
	 * 文字列前後の""は除去する
	 * 
	 * 
	 */
	public static List<List<String>> getTsvValList(String tsvStr) {

		List<List<String>> retList = new ArrayList<List<String>>();
		List<String> lineList = new ArrayList<String>();

		char[] strs = tsvStr.toCharArray();
		char[] retStrs = new char[strs.length];
		boolean inStr = false;

		// 一度文字列中の改行コードを\nから\fに、\tを\b変えておく

		for (int i = 0; i < strs.length; i++) {
			char c = strs[i];
			if (c == '\"') {
				inStr = !inStr;
			}
			if (inStr) {
				if (c == '\n') {
					retStrs[i] = '\f';
				} else if (c == '\t') {
					retStrs[i] = '\b';
				} else {
					retStrs[i] = strs[i];
				}
			} else {
				retStrs[i] = strs[i];
			}
		}
		String newCsvStr = new String(retStrs);
		StringTokenizer wSt = new StringTokenizer(newCsvStr, "\n");

		while (wSt.hasMoreTokens()) {
			StringTokenizer lSt = new StringTokenizer(wSt.nextToken(), "\t",
					true);
			boolean lastComma = true;

			boolean lineFirst = true;
			while (lSt.hasMoreTokens()) {
				String str = lSt.nextToken();
				if (lineFirst) {
					if (str.startsWith("#")) {
						lineList = new ArrayList<String>();
						break;
					}
				}
				lineFirst = false;
				if (str.startsWith("\"") && str.endsWith("\"")) {
					str = str.substring(1, str.length() - 1);
				}
				// \tが連続してきた場合対応
				// 前も\tなら連続なので空文字がデータ
				if (str.equals("\t")) {
					if (lastComma) {
						str = "";
					} else {
						// 前が文字列なら区切り文字なので読み飛ばし
						lastComma = true;
						continue;
					}
				} else {
					lastComma = false;
				}

				String chStr = str.replace('\f', '\n').replace('\b', '\t')
						.replace("\"\"", "\"");
				// System.out.println("o: "+ str);
				// System.out.println("n: "+ chStr);

				lineList.add(chStr);
			}
			if (lineList.size() != 0) {
				retList.add(lineList);
				lineList = new ArrayList<String>();
			}
		}
		return retList;
	}
	
	

	/**
	 * 指定されたカンマ区切りCSVの中身から、各行をListにしたListを取得する(全体) "は""でエスケープされていること前提
	 * 文字列前後の""は除去する
	 * 
	 * 
	 */
	public static List<List<String>> getCsvValList(String csvStr) {

		List<List<String>> retList = new ArrayList<List<String>>();
		List<String> lineList = new ArrayList<String>();

		char[] strs = csvStr.toCharArray();
		char[] retStrs = new char[strs.length];
		boolean inStr = false;

		// 一度文字列中の改行コードを\nから\tに、,を\b変えておく

		for (int i = 0; i < strs.length; i++) {
			char c = strs[i];
			if (c == '\"') {
				inStr = !inStr;
			}
			if (inStr) {
				if (c == '\n') {
					retStrs[i] = '\t';
				} else if (c == ',') {
					retStrs[i] = '\b';
				} else {
					retStrs[i] = strs[i];
				}
			} else {
				retStrs[i] = strs[i];
			}
		}
		String newCsvStr = new String(retStrs);
		StringTokenizer wSt = new StringTokenizer(newCsvStr, "\n");

		while (wSt.hasMoreTokens()) {
			StringTokenizer lSt = new StringTokenizer(wSt.nextToken(), ",",
					true);
			boolean lastComma = true;

			boolean lineFirst = true;
			while (lSt.hasMoreTokens()) {
				String str = lSt.nextToken();
				if (lineFirst) {
					if (str.startsWith("#")) {
						lineList = new ArrayList<String>();
						break;
					}
				}
				lineFirst = false;
				if (str.startsWith("\"") && str.endsWith("\"")) {
					str = str.substring(1, str.length() - 1);
				}
				// ,が連続してきた場合対応
				// 前も,なら連続なので空文字がデータ
				if (str.equals(",")) {
					if (lastComma) {
						str = "";
					} else {
						// 前が文字列なら区切り文字なので読み飛ばし
						lastComma = true;
						continue;
					}
				} else {
					lastComma = false;
				}

				String chStr = str.replace('\t', '\n').replace('\b', ',')
						.replace("\"\"", "\"");
				// System.out.println("o: "+ str);
				// System.out.println("n: "+ chStr);

				lineList.add(chStr);
			}
			if (lineList.size() != 0) {
				retList.add(lineList);
				lineList = new ArrayList<String>();
			}
		}
		return retList;
	}

	/**
	 * 指定されたカンマ区切りCSVの中身から、各行をListにしたListを取得する(一行) "は""でエスケープされていること前提
	 * 文字列前後の""は除去する
	 * 
	 * 
	 */
	public static List<String> getCsvLineValList(String csvStr) {

		List<String> lineList = new ArrayList<String>();

		char[] strs = csvStr.toCharArray();
		char[] retStrs = new char[strs.length];
		boolean inStr = false;

		// 一度文字列中の改行コードを\nから\tに、,を\b変えておく

		for (int i = 0; i < strs.length; i++) {
			char c = strs[i];
			if (c == '\"') {
				inStr = !inStr;
			}
			if (inStr) {
				if (c == '\n') {
					retStrs[i] = '\t';
				} else if (c == ',') {
					retStrs[i] = '\b';
				} else {
					retStrs[i] = strs[i];
				}
			} else {
				retStrs[i] = strs[i];
			}
		}
		String newCsvStr = new String(retStrs);

		StringTokenizer lSt = new StringTokenizer(newCsvStr, ",", true);
		boolean lastComma = true;

		boolean lineFirst = true;
		while (lSt.hasMoreTokens()) {
			String str = lSt.nextToken();
			if (lineFirst) {
				if (str.startsWith("#")) {
					lineList = new ArrayList<String>();
					break;
				}
			}
			lineFirst = false;
			if (str.startsWith("\"") && str.endsWith("\"")) {
				str = str.substring(1, str.length() - 1);
			}
			// ,が連続してきた場合対応
			// 前も,なら連続なので空文字がデータ
			if (str.equals(",")) {
				if (lastComma) {
					str = "";
				} else {
					// 前が文字列なら区切り文字なので読み飛ばし
					lastComma = true;
					continue;
				}
			} else {
				lastComma = false;
			}

			String chStr = str.replace('\t', '\n').replace('\b', ',')
					.replace("\"\"", "\"");
			lineList.add(chStr);
		}
		return lineList;
	}
}
