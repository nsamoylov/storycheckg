package com.nicksamoylov.storycheck.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    private static final Logging log = Logging.getLog("DateUtils");    
    private static final long MILLISECS_PER_DAY = 1000*60*60*24;    
    private static final String DF = "yyyy-MMM-dd";
    private static final String TF = "yyyy-MMM-dd HH:mm:ss";
    private static final String PF = "MM/dd/yyyy";

    private DateUtils (){
    }
    public static String formatTime (Date date){
        if(date==null) return "-";
        SimpleDateFormat f = new SimpleDateFormat(TF);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format (date);
    }
    public static String formatDate (Date date){
        if(date==null) return "-";
        SimpleDateFormat f = new SimpleDateFormat(DF);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format (date);
    }
    public static DateFormat dateFormatter (){
        return new SimpleDateFormat(DF);
    }
    public static DateFormat timeFormatter (){
        return new SimpleDateFormat(TF);
    }
    public static String formatTime (long t){
        if(t==0) return "-";
        return new SimpleDateFormat(TF).format (new Date (t));
    }
    public static String formatTime (Long t){
        if(t==null) return "-";
        return new SimpleDateFormat(TF).format (new Date (t));
    }
    public static Date lastMidnight (){
        Calendar cal = Calendar.getInstance ();
        cal.set (Calendar.HOUR_OF_DAY, 0);
        cal.set (Calendar.MINUTE, 0);
        cal.set (Calendar.SECOND, 0);
        cal.set (Calendar.MILLISECOND, 0);
        return cal.getTime ();
    }
    public static String lastMidnightString (){
        return new SimpleDateFormat(DF).format(lastMidnight());
    }
    public static Date nextMidnight (){
        Calendar cal = Calendar.getInstance ();
        cal.add (Calendar.DATE, 1);
        cal.set (Calendar.HOUR_OF_DAY, 0);
        cal.set (Calendar.MINUTE, 0);
        cal.set (Calendar.SECOND, 0);
        cal.set (Calendar.MILLISECOND, 0);
        return cal.getTime ();
    }
    public static String nextMidnightString (){
        return new SimpleDateFormat(DF).format(nextMidnight());
    }
	public static Date publishingFormatToDate(String s) {
		Date result;
		try {
			result = new Date(new SimpleDateFormat(PF).parse(s)
					.getTime());
		} catch (ParseException e) {
			result = new Date();
		}
		return result;
	}
	public static String calendarToPublishingFormat(Calendar calendar) {
		return millisToPublishingFormat(calendar.getTimeInMillis());
	}	
    public static String millisToPublishingFormat(long millis){
        return new SimpleDateFormat(PF).format (new Date(millis));
    }
    public static boolean isValidDate(Date date){
       Date testDate = null;
//       log.debug("String value of date is : " + df.format(date));
       Date maxDate;
       try{
    	 DateFormat df = new SimpleDateFormat(DF);
    	 maxDate = new Date(df.parse("01-Jan-2036").getTime());
//    	 log.debug("String value of max date is : " + df.format(maxDate));
//    	 log.debug("String value of Max limit comparision is : " + date.compareTo(maxDate));
    	 if (date.compareTo(maxDate) > 0){
    		 return false;
    	 }
         testDate = df.parse(df.format(date));
       }
       catch (ParseException e){
    	 log.warn("Error while parsing. Invalid date string : " + date);
         return false;
       }
       DateFormat df = new SimpleDateFormat(DF);
//       log.debug("String value of test date is : " + df.format(testDate));
       if(!df.format(testDate).equals(df.format(date))){
    	   return false;
       }
       return true;

    } // end isValidDate
    public static final String timeDiff(Date start, Date stop){
    	String result = null;
    	long diff = stop.getTime() - start.getTime();
    	if(diff == 0){
    		result = "less than 1 ms";
    	}
    	else{
    		long hrs = diff/1000/60/60;
    		long min = (diff - hrs*60*60*1000)/1000/60;
    		long sec = (diff - hrs*60*60*1000 - min*60*1000)/1000;
    		long msc = diff - hrs*60*60*1000 - min*60*1000 - sec*1000;
    		result = hrs+":"+(min>9?min:"0"+min)+":"+(sec>9?sec:"0"+sec)+":"+(msc>9?(msc>99?msc:"0"+msc):"00"+msc);
    	}
    	return result;
    }
    public static long diffInDays(Date startDate, Date endDate) {
    	Calendar end = Calendar.getInstance();
    	end.setTime(endDate);
    	Calendar start = Calendar.getInstance();
    	start.setTime(startDate);
        return diffInDays(start, end);
    }
    public static long diffInDays(Calendar start, Calendar end) {
        long endL   =  end.getTimeInMillis() +  end.getTimeZone().getOffset(  end.getTimeInMillis() );
        long startL =  start.getTimeInMillis() + start.getTimeZone().getOffset( start.getTimeInMillis() );
        return (endL - startL) / MILLISECS_PER_DAY;
    }


}
