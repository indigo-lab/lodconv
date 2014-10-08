package net.msecnd.vo.jrrk.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.sun.xml.internal.txw2.IllegalAnnotationException;

public class CalendarUtil {
	/**
	 * 
	 * @param timeStr
	 * @param tz
	 * @return
	 */
	public static Calendar getCalendar(String timeStr, TimeZone tz) throws IllegalArgumentException{
		
		if(timeStr == null){
			return null;
		}
		try{
			if(timeStr.length() <= 10){
				return getCalendarFromShortString( timeStr,  tz);
			}else {
				return getCalendarFromString( timeStr,  tz);
			}
		}catch(Exception e){
			throw new IllegalAnnotationException(e.getMessage());
		}
	}
	
	/**
	 * 2011-01-05T13:22:56Z (GMT)形式の時間を変換する
	 * 2011-01-05 13:22:56 でもOK
	 * @param timeStr
	 * @param shoftms
	 * @return
	 */
	public static Calendar getCalendarFromString(String timeStr, TimeZone tz) {
		// 2011-01-05T13:22:56Z
		// YYYY-MM-DDTHH:MM:SS:Z(GMT)
		String yyyy = timeStr.substring(0, 4);
		
		//	MM,DDが1桁の時の対応
		int shift = 0;
		
		String mm = timeStr.substring(5, 7);
		if(mm.endsWith("/")){
			mm = mm.substring(0,1);
			shift++;
		}
		
		String dd = timeStr.substring(8 - shift, 10- shift);
		if(dd.endsWith(" ")|| dd.endsWith("T")){
			dd = mm.substring(0,1);
			shift++;
		}
		
		
		String hh = timeStr.substring(11- shift, 13- shift);
		//	HHが1桁の時の対応
		if(hh.endsWith(":")){
			hh = hh.substring(0,1);
			shift++;
		}
		//	minは2桁
		String minmm = timeStr.substring(14- shift, 16- shift);
		
		String sec;
		if(timeStr.length() >= (17- shift)){
			sec = timeStr.substring(17- shift, 19- shift);
		}else{
			sec = "0";
		}


		Calendar cal = new GregorianCalendar(tz);
		cal.set(Calendar.YEAR, Integer.valueOf(yyyy));
		cal.set(Calendar.MONTH, Integer.valueOf(mm) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dd));

		cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hh));
		cal.set(Calendar.MINUTE, Integer.valueOf(minmm));
		cal.set(Calendar.SECOND, Integer.valueOf(sec));
		return cal;
	}
	
	/**
	 * 2011-01-05形式の時間を変換する
	 * @param timeStr
	 * @param shoftms
	 * @return
	 */
	public static Calendar getCalendarFromShortString(String timeStr, TimeZone tz) {
		// 2011-01-05
		// YYYY-MM-DD
		int shift = 0;
		String yyyy = timeStr.substring(0, 4);
		String mm = timeStr.substring(5, 7);
		try{
			int t = Integer.parseInt(mm);
		}catch(Exception e){
			shift = 1;
			mm = mm.substring(0,1);
		}
		
		String dd = timeStr.substring(8-shift);

		
		Calendar cal = new GregorianCalendar(tz);
		cal.set(Calendar.YEAR, Integer.valueOf(yyyy));
		cal.set(Calendar.MONTH, Integer.valueOf(mm) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dd));

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal;
	}
}
