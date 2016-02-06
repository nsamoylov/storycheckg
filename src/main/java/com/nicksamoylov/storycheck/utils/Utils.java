package com.nicksamoylov.storycheck.utils;

import java.util.Locale;
import java.util.Properties;

public class Utils {
	public static String calculatePercent(String fractionInt, String totalInt){
		String fraction = fractionInt==null?"0":fractionInt;
		int p = Math.round(Integer.parseInt(fraction) * 100f / Integer.parseInt(totalInt));
		return Integer.toString(p);
	}
	
	private static final String LANG_EN="en";
	private static final String LANG_RU="ru";
	public static final String lang(boolean isRussian){
		return isRussian?LANG_RU:LANG_EN;
	}
	public static final boolean isRussian(Locale locale) {
    	return locale.getLanguage().equals(getLocale(true).getLanguage());
    }
	public static final Locale getLocale(boolean isRussian){
		return isRussian?new Locale("ru", "", ""):new Locale("en", "", "");
	}
    
    private static Properties properties = loadProperties();        		
    public static final String getPropertyServerUrl(){
    	return properties.getProperty("server.url");
    }
    public static final String getEnvironmentPrefix(){
    	String env = properties.getProperty("environment");
    	return "prod".equals(env)?"":"L:";
    }
    public static final boolean isAdminEmailSendOff(){
    	return "false".equals(properties.getProperty("admin.email.send"));
    }
    public static final String getPropertyContent(){
    	return properties.getProperty("content");
    }
    public static final String getPropertyResults(){
    	return properties.getProperty("results");
    }
    public static final String getPropertyAdminEmail(){
    	return properties.getProperty("admin.email");
    }
    public static final String getPropertyAdminEmailFrom(){
    	return properties.getProperty("admin.email");
    }
    public static final int getPropertyEmailLimitPerDay(){
    	return Integer.parseInt(properties.getProperty("emails.limit.per.day"));
    }
    private static final Properties loadProperties() {
        properties = new Properties();
        try {
            properties.load(FileUtils.class.getClassLoader().getResourceAsStream("storycheck.properties"));
        } catch (final Exception e) {
            throw new RuntimeException("Exception loading properties", e);
        } 
        return properties;
    }


}
