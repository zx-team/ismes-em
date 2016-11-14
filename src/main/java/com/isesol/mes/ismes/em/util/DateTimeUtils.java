package com.isesol.mes.ismes.em.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class DateTimeUtils {
	
	private static Logger log4j = Logger.getLogger(DateTimeUtils.class);
	
	/**
	 * weekendsList ---List<String>  周末的 数组
	 * @param start
	 * @param end
	 * @return
	 */
	public static Map<String,Object> weekdaysList(String start,String end){
		Map<String,Object> returnMap = new HashMap<String, Object>();
		Date startDate = string2Date(start);
		Calendar startCal = Calendar.getInstance(); 
		startCal.setTime(startDate);
		Date endDate = string2Date(end);
		Calendar endCal = Calendar.getInstance(); 
		endCal.setTime(endDate);
		
		List<String> weekendsList = new ArrayList<String>();
		while(startCal.getTime().compareTo(endCal.getTime()) < 1){
			int weekday = startCal.get(Calendar.DAY_OF_WEEK);
			String month = String.valueOf(startCal.get(Calendar.MONTH) + 1);
			month = month.length() == 1 ? "0"+month :month;
			String day = String.valueOf(startCal.get(Calendar.DAY_OF_MONTH));
			day = day.length() == 1 ? "0"+day :day;
			String str = String.valueOf(startCal.get(Calendar.YEAR)) + "-" +
					month + "-" + day;
			if(weekday == Calendar.SATURDAY){
				weekendsList.add(str);
			}
			if(weekday == Calendar.SUNDAY){
				weekendsList.add(str);
			}
			
			startCal.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		returnMap.put("weekendsList", weekendsList);
		return returnMap;
	}
	
	public static Date string2Date(String timeStr){
	    if(!timeStr.contains(":")){
	    	timeStr = timeStr + " 00:00:00";
	    }
	    String format ="";
	    if(timeStr.contains("-")){
	    	format = "yyyy-MM-dd HH:mm:ss";
	    }
	    if(timeStr.contains("/")){
	    	format = "yyyy/MM/dd HH:mm:ss";
	    }
	    SimpleDateFormat formatter=new SimpleDateFormat(format);  
	    try {
			return formatter.parse(timeStr);
		} catch (ParseException e) {
			log4j.info("时间转换出现异常;;;"+timeStr);
			log4j.error(e.getMessage());
			return null;
		} 
	}
	
	public static String date2String(Date time,String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(time);
	}
	
	
	public static Date getOneDayBeginTime(Date date){
		return DateTimeUtils.string2Date(DateTimeUtils.date2String(date, "yyyy-MM-dd")) ;
	}
	
	public static Date getOneDayEndTime(Date date){
		Calendar calendar = Calendar.getInstance(); 
		calendar.setTime(date);
		
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
		month = month.length() == 1 ? "0"+month :month;
		String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
		day = day.length() == 1 ? "0"+day :day;
		String str = String.valueOf(calendar.get(Calendar.YEAR)) + "-" +
				month + "-" + day;
		return DateTimeUtils.string2Date(str) ;
	}
}
