package com.netentsec.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.elasticsearch.common.joda.time.DateTime;

public class TimeUtil {

	public static String longToString(long time, String format){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		Date dt = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(dt);
	}
	
	public static long dateToLong(String time, String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			long timeStart = sdf.parse(time).getTime();
			return timeStart;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
		}
		return 0;
	}
	
	public static String StringToString(String time, String format){
		String tmp = "";
		if(time.indexOf(".") != -1){
			String[] dt = time.split("\\.");
			if(dt.length > 1)
				tmp = TimeUtil.longToString(TimeUtil.dateToLong(dt[0], "yyyy-MM-dd HH:mm:ss"), format);
			else
				return "";
		}else{
			long times = TimeUtil.dateToLong(time, "yyyy-MM-dd HH:mm:ss");
			if(times == 0)
				return "";
			tmp = TimeUtil.longToString(times, format);
		}
		return tmp;
	}
	
	public static String Random4(){
		int count = 4;
		StringBuffer sb = new StringBuffer();
		String str = "0123456789";
		Random r = new Random();
		for(int i = 0; i < count; i ++){
			int num = r.nextInt(str.length());
			sb.append(str.charAt(num));
			str = str.replace((str.charAt(num) + ""), "");
		}
		return sb.toString();
	}
	
	public static String getCurrent(){
		Date now = new Date();
		SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");//可以方便地修改日期格式
		String current = datetimeFormat.format(now);
		return current;
	}
	
	public static void main(String[] args){
		System.out.println(TimeUtil.Random4());
		String file_name = "1471425600_008844";
		String[] name_time = file_name.split("_");
		System.out.println("```````````" + TimeUtil.longToString(Long.parseLong(name_time[0] + "000"), "yyyyMMdd"));
		
		Date date = new Date();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		int k = 30;
		long ol = (long)k * 24 * 60 * 60 * 1000;
		System.out.println("两天前的日期：" + df.format(new Date(date.getTime() - ol)));
		long a = Long.valueOf("1472649900000");
//		long b = 1463987256445l;
//		if(a > b){
//		//	a = b;
//		}
//		Date date = new Date();
//		String tmp = "2016-02-22 13:49:48.498087";
		System.out.println("*****" + TimeUtil.longToString(a, "yyyy-MM-dd'T'HH:mm:ssXXX"));
		String time = TimeUtil.StringToString("2016-02-22 13:49:48", "yyyy-MM-dd'T'HH:mm:ssXXX");
		if(time != ""){
			System.out.println(time);
		}else{
			System.out.println("NO");
		}
		System.out.println("------" + TimeUtil.StringToString("2462959972000", "yyyy-MM-dd'T'HH:mm:ssXXX"));
		System.out.println(TimeUtil.longToString(TimeUtil.dateToLong("2016-02-22 13:49:48", "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd'T'HH:mm:ssXXX"));
		System.out.println("==========" + UUID.randomUUID());
		//System.out.println(b);
	}
}
