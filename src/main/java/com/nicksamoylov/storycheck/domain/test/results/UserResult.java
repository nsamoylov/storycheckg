package com.nicksamoylov.storycheck.domain.test.results;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nicksamoylov.storycheck.domain.users.User;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class UserResult  {
	private int id;
    private String username;
	private String firstname;
	private String lastname;
	private String street;
	private String city;
	private String state;
	private String zipcode;
	private String country;
	private String language;
	private int emailsCountTotal = 0;
	private Date modifiedDate;

	public static final String DB_TABLE = "Results_Users";
	public static final String DB_ID_GEN_NAME = "Results_Users_id";
	public static final String DB_ID = "id";
	public static final String DB_USERNAME = "username";
	public static final String DB_FIRSTNAME = "firstname";
	public static final String DB_LASTNAME = "lastname";
	public static final String DB_STREET = "street";
	public static final String DB_CITY = "city";
	public static final String DB_STATE = "state";
	public static final String DB_ZIPCODE = "zipcode";
	public static final String DB_COUNTRY = "country";
	public static final String DB_LANG = "lang";
	public static final String DB_EMAILS_COUNT_TOTAL = "emails_count_total";
	public static final String DB_MODIFIED_DATE="modified_date";
	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_USERNAME, new Integer(256));
		DB_COLUMN_SIZES.put(DB_FIRSTNAME, new Integer(128));
		DB_COLUMN_SIZES.put(DB_LASTNAME, new Integer(128));
		DB_COLUMN_SIZES.put(DB_STREET, new Integer(256));
		DB_COLUMN_SIZES.put(DB_CITY, new Integer(128));
		DB_COLUMN_SIZES.put(DB_STATE, new Integer(64));
		DB_COLUMN_SIZES.put(DB_ZIPCODE, new Integer(10));
		DB_COLUMN_SIZES.put(DB_COUNTRY, new Integer(64));
		DB_COLUMN_SIZES.put(DB_LANG, new Integer(16));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v, DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_ID);
		DB_COLUMNS_INSERT.add(DB_USERNAME);
		DB_COLUMNS_INSERT.add(DB_FIRSTNAME);
		DB_COLUMNS_INSERT.add(DB_LASTNAME);
		DB_COLUMNS_INSERT.add(DB_STREET);
		DB_COLUMNS_INSERT.add(DB_CITY);
		DB_COLUMNS_INSERT.add(DB_STATE);
		DB_COLUMNS_INSERT.add(DB_ZIPCODE);
		DB_COLUMNS_INSERT.add(DB_COUNTRY);
		DB_COLUMNS_INSERT.add(DB_LANG);
		DB_COLUMNS_INSERT.add(DB_EMAILS_COUNT_TOTAL);
	}
	private static final Set<String> DB_COLUMNS_UPDATE = new HashSet<String>();
	static {
		DB_COLUMNS_UPDATE.add(DB_MODIFIED_DATE);
		DB_COLUMNS_UPDATE.addAll(DB_COLUMNS_INSERT);
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
			+ " ("+DB_COLUMNS_INSERT_SORTED+") values("+DB_COLUMNS_INSERT_WITH_PREF_SORTED+")";
	public static final String SQL_SELECT_BY_USER_ID = "select * from " + DB_TABLE + " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_UPDATE_BY_ID = "update " + DB_TABLE +" set " + SET_DB_COLUMNS_UPDATE 
			+ " where "+DB_ID+"=:"+DB_ID;

	public UserResult() {
		super();
	}
	public UserResult(User user) {
		super();
		setUserData(user);
	}
	public UserResult(int id, String username, String firstname, String lastname, String language, int emailsCountTotal) {
		super();
		this.id = id;
		this.username = fitDb(username, DB_USERNAME);
		this.firstname = fitDb(firstname, DB_FIRSTNAME);
		this.lastname = fitDb(lastname, DB_LASTNAME);
		this.language = fitDb(language, DB_LANG);
		this.emailsCountTotal = emailsCountTotal;
	}
	public void setUserData(User user) {
		this.id = user.getId();
		this.username = fitDb(user.getUsername(), DB_USERNAME);
		this.firstname = fitDb(user.getFirstname(), DB_FIRSTNAME);
		this.lastname = fitDb(user.getLastname(), DB_LASTNAME);
		this.language = fitDb(user.getLanguage(), DB_LANG);
		this.emailsCountTotal = user.getEmailsCountTotal();
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_USERNAME, username);
		values.put(DB_FIRSTNAME, firstname);
		values.put(DB_LASTNAME, lastname);
		values.put(DB_STREET, street);
		values.put(DB_CITY, city);
		values.put(DB_STATE, state);
		values.put(DB_ZIPCODE, zipcode);
		values.put(DB_COUNTRY, country);
		values.put(DB_LANG, language);
		values.put(DB_EMAILS_COUNT_TOTAL, emailsCountTotal);
		values.put(DB_MODIFIED_DATE, new Date());
		return values;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = fitDb(username, DB_USERNAME);
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = fitDb(firstname, DB_FIRSTNAME);
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = fitDb(lastname, DB_LASTNAME);
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = fitDb(language, DB_LANG);
	}
	public int getEmailsCountTotal() {
		return emailsCountTotal;
	}
	public void setEmailsCountTotal(int emailsCountTotal) {
		this.emailsCountTotal = emailsCountTotal;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	@Override
	public String toString() {
		return "UserResult [" + id + "," + username + "," + firstname + "," + lastname   
				+ ", lang=" + language + ", emailsTotal=" + emailsCountTotal + "]";
	}

}
