package com.nicksamoylov.storycheck.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class News {
	public static final String AREA_TEST="Test";
	public static final String AREA_HOME="Home";
	
	private int id;
	private String area;
	private String lang;
	private String text;
	private Timestamp createdDate;
	
	public static final String DB_TABLE = "news";
	public static final String DB_ID_GEN_NAME = "news_id";
	public static final String DB_ID = "id";
	public static final String DB_AREA = "area";
	public static final String DB_LANG = "lang";
	public static final String DB_TEXT = "text";
	public static final String DB_DATE = "created_date";
	
	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_AREA, new Integer(20));
		DB_COLUMN_SIZES.put(DB_TEXT, new Integer(256));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v, DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_AREA);
		DB_COLUMNS_INSERT.add(DB_TEXT);
		DB_COLUMNS_INSERT.add(DB_LANG);
	}
	private static final Set<String> DB_COLUMNS_UPDATE = new HashSet<String>();
	static {
		DB_COLUMNS_UPDATE.add(DB_ID);
		DB_COLUMNS_UPDATE.add(DB_AREA);
		DB_COLUMNS_UPDATE.add(DB_TEXT);
		DB_COLUMNS_UPDATE.add(DB_LANG);
	}
	private static final String DB_COLUMNS_INSERT_WITH_PREF_SORTED;
	private static final List<String> DB_COLUMNS_INSERT_WITH_PREF = new ArrayList<String>();
	private static final String DB_COLUMNS_INSERT_SORTED = DisplayUtils.commaSeparatedSorted(DB_COLUMNS_INSERT);;
	static {
		for(String n: DB_COLUMNS_INSERT){
			DB_COLUMNS_INSERT_WITH_PREF.add(":"+n);
		}
		DB_COLUMNS_INSERT_WITH_PREF_SORTED = DisplayUtils.commaSeparatedSorted(DB_COLUMNS_INSERT_WITH_PREF);
	}
	private static final String SET_DB_COLUMNS_UPDATE;
	static {
		List<String> SET = new ArrayList<String>();
		for(String n: DB_COLUMNS_UPDATE){
			SET.add(n+"=:"+n);
		}
		SET_DB_COLUMNS_UPDATE = DisplayUtils.commaSeparated(SET);
	}

	public static final String SQL_INSERT = "insert into "	+ DB_TABLE
			+ " ("+DB_ID+","+DB_COLUMNS_INSERT_SORTED+")"
			+ " values(nextval(\'"+DB_ID_GEN_NAME+"\'), "+DB_COLUMNS_INSERT_WITH_PREF_SORTED+")";
	public static final String SQL_UPDATE_BY_ID = "update " + DB_TABLE +" set " + SET_DB_COLUMNS_UPDATE 
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_IDS_BY_AREA_AND_LANG_ORDER_BY_ID = "select "+DB_ID+" from " + DB_TABLE 
			+ " where "+DB_AREA+"=:"+DB_AREA + " and "+DB_LANG+"=:"+DB_LANG + " order by " +DB_ID;
	public static final String SQL_SELECT_NEWS_BY_ID = "select "+DB_TEXT+" from " + DB_TABLE + " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_DELETE_BY_ID = "delete from " + DB_TABLE + " where "+DB_ID+"=:"+DB_ID;

	public News() {
		super();
	}

	public News(String area, String lang, String text) {
		super();
		this.area = fitDb(area, DB_AREA);
		this.lang = lang;
		this.text = fitDb(text, DB_TEXT);
	}

	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_AREA, area);
		values.put(DB_LANG, lang);
		values.put(DB_TEXT, text);
		return values;
	}	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = fitDb(area, DB_AREA);
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}
	
	

}
