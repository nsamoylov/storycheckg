package com.nicksamoylov.storycheck.domain.users;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.nicksamoylov.storycheck.domain.test.results.TesterResult;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;
import com.nicksamoylov.storycheck.utils.Utils;

public class User {
	private int id;
	@NotEmpty
	@Email
    private String username;
    private String password;
    private String passwordAgain;
	@NotEmpty
    @Length(min=2)
	private String firstname;
	@NotEmpty
    @Length(min=2)
	private String lastname;
	private String street;
	private String city;
	private String state;
	private String zipcode;
	private String country;
	private String language;
	private int emailsCountToday = 0;
	private Date emailsCountDate;
	private int emailsCountTotal = 0;
    private boolean enabled;
	private Date modifiedDate;
	private Set<String> roles = new HashSet<String>();
	
	public static final String LANG_EN = "lang.en"; 
	public static final String LANG_RU = "lang.ru"; 
	public static final String LANG_BOTH = "lang.both"; 
	public static final List<String> LANG_OPTIONS = new ArrayList<String>();
	static {
		LANG_OPTIONS.add(LANG_EN);
		LANG_OPTIONS.add(LANG_RU);
		LANG_OPTIONS.add(LANG_BOTH);
	}

	public static final String DB_TABLE = "users";
	public static final String DB_ID_GEN_NAME = "users_id";
	public static final String DB_ID = "id";
	public static final String DB_USERNAME = "username";
	public static final String DB_PASSWORD = "password";
	public static final String DB_FIRSTNAME = "firstname";
	public static final String DB_LASTNAME = "lastname";
	public static final String DB_STREET = "street";
	public static final String DB_CITY = "city";
	public static final String DB_STATE = "state";
	public static final String DB_ZIPCODE = "zipcode";
	public static final String DB_COUNTRY = "country";
	public static final String DB_LANG = "lang";
	public static final String DB_EMAILS_COUNT_TODAY = "emails_count_today";
	public static final String DB_EMAILS_COUNT_DATE="emails_count_date";
	public static final String DB_EMAILS_COUNT_TOTAL = "emails_count_total";
	public static final String DB_ENABLED = "enabled";
	public static final String DB_MODIFIED_DATE="modified_date";

	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_USERNAME, new Integer(256));
		DB_COLUMN_SIZES.put(DB_PASSWORD, new Integer(45));
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
		DB_COLUMNS_INSERT.add(DB_USERNAME);
		DB_COLUMNS_INSERT.add(DB_PASSWORD);
		DB_COLUMNS_INSERT.add(DB_FIRSTNAME);
		DB_COLUMNS_INSERT.add(DB_LASTNAME);
		DB_COLUMNS_INSERT.add(DB_STREET);
		DB_COLUMNS_INSERT.add(DB_CITY);
		DB_COLUMNS_INSERT.add(DB_STATE);
		DB_COLUMNS_INSERT.add(DB_ZIPCODE);
		DB_COLUMNS_INSERT.add(DB_COUNTRY);
		DB_COLUMNS_INSERT.add(DB_ENABLED);
		DB_COLUMNS_INSERT.add(DB_LANG);
	}
	private static final Set<String> DB_COLUMNS_UPDATE = new HashSet<String>();
	static {
		DB_COLUMNS_UPDATE.add(DB_ID);
		DB_COLUMNS_UPDATE.add(DB_USERNAME);
		DB_COLUMNS_UPDATE.add(DB_PASSWORD);
		DB_COLUMNS_UPDATE.add(DB_FIRSTNAME);
		DB_COLUMNS_UPDATE.add(DB_LASTNAME);
		DB_COLUMNS_UPDATE.add(DB_STREET);
		DB_COLUMNS_UPDATE.add(DB_CITY);
		DB_COLUMNS_UPDATE.add(DB_STATE);
		DB_COLUMNS_UPDATE.add(DB_ZIPCODE);
		DB_COLUMNS_UPDATE.add(DB_COUNTRY);
		DB_COLUMNS_UPDATE.add(DB_LANG);
		DB_COLUMNS_UPDATE.add(DB_EMAILS_COUNT_TODAY);
		DB_COLUMNS_UPDATE.add(DB_EMAILS_COUNT_DATE);
		DB_COLUMNS_UPDATE.add(DB_EMAILS_COUNT_TOTAL);
		DB_COLUMNS_UPDATE.add(DB_ENABLED);
		DB_COLUMNS_UPDATE.add(DB_MODIFIED_DATE);
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

	public static final String DB_TABLE_TEMP = "users_temp";
	public static final String SQL_INSERT_TEMP = "insert into "	+ DB_TABLE_TEMP
			+ " ("+DB_ID+","+DB_USERNAME+","+DB_PASSWORD+")"
			+ " values(:"+DB_ID+", :"+DB_USERNAME+", :"+DB_PASSWORD+")";
	public static final String SQL_SELECT_USERNAME_FROM_TEMP = "select "+ DB_USERNAME + " from " + DB_TABLE_TEMP
		    + " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_PASSWORD_FROM_TEMP_BY_ID = "select "+ DB_PASSWORD + " from " + DB_TABLE_TEMP
		    + " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_ID_BY_PASSWORD_FROM_TEMP = "select "+ DB_ID + " from " + DB_TABLE_TEMP
		    + " where "+DB_PASSWORD+"=:"+DB_PASSWORD;
	public static final String SQL_SELECT_DATE_FROM_TEMP = "select created_date from " + DB_TABLE_TEMP
		    + " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_DELETE_USER_TEMP_BY_ID = "delete from " + DB_TABLE_TEMP
		    + " where "+DB_ID+"=:"+DB_ID;
	
	public static final String SQL_INSERT = "insert into "	+ DB_TABLE
			+ " ("+DB_ID+","+DB_COLUMNS_INSERT_SORTED+")"
			+ " values(nextval(\'"+DB_ID_GEN_NAME+"\'), "+DB_COLUMNS_INSERT_WITH_PREF_SORTED+")";
	public static final String SQL_UPDATE_BY_ID = "update " + DB_TABLE +" set " + SET_DB_COLUMNS_UPDATE 
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_UPDATE_USERNAME_BY_ID = "update " + DB_TABLE +" set " + DB_USERNAME+"=:"+DB_USERNAME 
			+ ", "+DB_MODIFIED_DATE+"=:"+DB_MODIFIED_DATE + " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_UPDATE_MODIFIED_DATE_BY_ID = "update " + DB_TABLE +" set " + DB_MODIFIED_DATE+"=:"+DB_MODIFIED_DATE 
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_UPDATE_EMAIL_COUNT_BY_ID = "update " + DB_TABLE +" set " + DB_EMAILS_COUNT_TODAY+"=:"+DB_EMAILS_COUNT_TODAY 
			+ "," +DB_EMAILS_COUNT_DATE+"=:"+DB_EMAILS_COUNT_DATE+ "," +DB_EMAILS_COUNT_TOTAL+"=:"+DB_EMAILS_COUNT_TOTAL
			+ "," +DB_MODIFIED_DATE+"=:"+DB_MODIFIED_DATE + " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_USER_BY_ID = "select * from " + DB_TABLE 
		    + " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_USER_BY_USERNAME = "select * from " + DB_TABLE 
		    + " where "+DB_USERNAME+"=:"+DB_USERNAME;
	public static final String SQL_DELETE_BY_USERNAME = "delete from " + DB_TABLE + " where "+DB_USERNAME+"=:"+DB_USERNAME;

	public static List<String> getRoleOptions(){
		return Role.getRoleOptions();
	}

	public User() {
		super();
	}
	public User(Locale locale) {
		super();
		this.language = Utils.isRussian(locale)?LANG_RU:LANG_EN;
	}
	public User(final int id, final String username) {
		super();
		this.id = id;
		this.username = username;
	}
	public User(TesterResult testerResult) {
		super();
		this.username = testerResult.getEmail();
		this.firstname = testerResult.getFirstname();
		this.lastname = testerResult.getLastname();
		this.language = testerResult.getLanguage();
		this.roles = new HashSet<String>();
		this.roles.add("tester answer");
	}
	public User(String username, String password, String firstname, String lastname) {
		super();
		this.username = fitDb(username, DB_USERNAME);
		this.password = fitDb(password, DB_PASSWORD);
		this.firstname = fitDb(firstname, DB_FIRSTNAME);
		this.lastname = fitDb(lastname, DB_LASTNAME);
	}
	public User(final int id, String username, String firstname, String lastname, String street, String city, String state, 
			String zipcode, String country, final String language, final int emailsCountToday, final Date emailsCountDate, 
			final int emailsCountTotal,	final Date modifiedDate, final boolean enabled) {
		super();
		this.id = id;
		this.username = fitDb(username, DB_USERNAME);
		this.firstname = fitDb(firstname, DB_FIRSTNAME);
		this.lastname = fitDb(lastname, DB_LASTNAME);
		this.street = fitDb(street, DB_STREET);
		this.city = fitDb(city, DB_CITY);
		this.state = fitDb(state, DB_STATE);
		this.zipcode = fitDb(zipcode, DB_ZIPCODE);
		this.country = fitDb(country, DB_COUNTRY);
		this.language = language;
		this.emailsCountToday = emailsCountToday;
		this.emailsCountDate = emailsCountDate;
		this.emailsCountTotal = emailsCountTotal;
		this.enabled = enabled;
		this.modifiedDate = modifiedDate;
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_USERNAME, username);
		values.put(DB_PASSWORD, password);
		values.put(DB_FIRSTNAME, firstname);
		values.put(DB_LASTNAME, lastname);
		values.put(DB_STREET, street);
		values.put(DB_CITY, city);
		values.put(DB_STATE, state);
		values.put(DB_ZIPCODE, zipcode);
		values.put(DB_COUNTRY, country);
		values.put(DB_LANG, language);
		values.put(DB_EMAILS_COUNT_TODAY, emailsCountToday);
		values.put(DB_EMAILS_COUNT_DATE, emailsCountDate);
		values.put(DB_EMAILS_COUNT_TOTAL, emailsCountTotal);
		values.put(DB_ENABLED, enabled);
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = fitDb(password, DB_PASSWORD);
	}
	public String getPasswordAgain() {
		return passwordAgain;
	}
	public void setPasswordAgain(String password) {
		this.passwordAgain = fitDb(password, DB_PASSWORD);
	}
	public final String getFirstname() {
		return firstname;
	}
	public final void setFirstname(final String firstname) {
		this.firstname = fitDb(firstname, DB_FIRSTNAME);
	}
	public final String getLastname() {
		return lastname;
	}
	public final void setLastname(final String lastname) {
		this.lastname = fitDb(lastname, DB_LASTNAME);
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public boolean speaksLanguage(boolean isRussian) {
		return LANG_BOTH.equals(language) || (isRussian?LANG_RU.equals(language):LANG_EN.equals(language));
	}
	public static String getLanguage(boolean isRussian) {
		return isRussian?LANG_RU:LANG_EN;
	}
	public static String getLanguageBoth() {
		return LANG_BOTH;
	}
	public static List<String> getLanguageOptions(){
		return LANG_OPTIONS;
	}	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public final Set<String> getRoles() {
		return roles;
	}
	public final void setRoles(Set<String> roles) {
		this.roles = roles;
	}	
	public final boolean hasRole(String role){
		return roles.contains(Role.getRoleName(role));
	}
	public final boolean isWriter(){
		return roles.contains(Role.ROLE_WRITER);
	}
	public final boolean isEditor(){
		return roles.contains(Role.ROLE_EDITOR);
	}
	public final boolean isTester(){
		return roles.contains(Role.ROLE_TESTER);
	}
	public final boolean isTranslator(){
		return roles.contains(Role.ROLE_TRANSLATOR);
	}
	public final boolean isProofreader(){
		return roles.contains(Role.ROLE_PROOFREADER);
	}
	public final String getRoleClass(String role){
		int index = 0;
		switch (role) {
		case Role.ROLE_WRITER:
			index = 1;
			break;
		case Role.ROLE_TESTER:
			if(isWriter()){
				index++;
			}
			if(isTester()){
				index++;
			}
			break;
		case Role.ROLE_PROOFREADER:
			if(isWriter()){
				index++;
			}
			if(isTester()){
				index++;
			}
			if(isProofreader()){
				index++;
			}
			break;
		case Role.ROLE_EDITOR:
			if(isWriter()){
				index++;
			}
			if(isTester()){
				index++;
			}
			if(isProofreader()){
				index++;
			}
			if(isEditor()){
				index++;
			}
			break;
		case Role.ROLE_TRANSLATOR:
			if(isWriter()){
				index++;
			}
			if(isTester()){
				index++;
			}
			if(isProofreader()){
				index++;
			}
			if(isEditor()){
				index++;
			}
			if(isTranslator()){
				index++;
			}
			break;
		default:
			throw new RuntimeException("Role "+role+" has to be added to the switch in User.java");
		}
		return index% 2 == 0?"pale-green":"pale-blue";
	}
	public int getEmailsCountToday() {
		return emailsCountToday;
	}
	public void setEmailsCountToday(int emailsCountToday) {
		this.emailsCountToday = emailsCountToday;
	}
	public Date getEmailsCountDate() {
		return emailsCountDate;
	}
	public void setEmailsCountDate(Date emailsCountDate) {
		this.emailsCountDate = emailsCountDate;
	}
	public boolean isEmailsCountSameDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(emailsCountDate).equals(sdf.format(new Date()));
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
	public String display() {
		return firstname+" " + lastname + " (" + username + ") "+language+" ("+DisplayUtils.commaSeparated(roles)+")";
	}

	@Override
	public String toString() {
		return "User [" + id + ", " + username + ", " + firstname + ", " + lastname   
				+ ", lang=" + language + ", emailsToday=" + emailsCountToday + ", emailsTotal=" + emailsCountTotal 
	           + ", roles=(" + DisplayUtils.commaSeparatedSorted(roles) + "), enabled=" + enabled + "]";
	}

	public final String getStreet() {
		return street;
	}

	public final void setStreet(String street) {
		this.street = fitDb(street, DB_STREET);
	}

	public final String getCity() {
		return city;
	}

	public final void setCity(String city) {
		this.city = fitDb(city, DB_CITY);
	}

	public final String getState() {
		return state;
	}

	public final void setState(String state) {
		this.state = fitDb(state, DB_STATE);
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = fitDb(zipcode, DB_ZIPCODE);
	}

	public final String getCountry() {
		return country;
	}

	public final void setCountry(String country) {
		this.country = fitDb(country, DB_COUNTRY);
	}


}
