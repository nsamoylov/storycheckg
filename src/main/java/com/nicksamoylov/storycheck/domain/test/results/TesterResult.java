package com.nicksamoylov.storycheck.domain.test.results;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nicksamoylov.storycheck.domain.users.Tester;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class TesterResult {
	private int id;
	private int testgroupId;
	private String testerToken;
	private String email;
	private String firstname;
	private String lastname;
	private String language;
	private boolean independent;
	private String comments;
    private long timeSpentSec;
    private Date answeredDate;
	
	private static final String DB_TABLE = "Results_Testers";
	private static final String DB_ID_GEN_NAME = "Results_Testers_id";
	public static final String DB_ID = "id";
	public static final String DB_TESTGROUP_ID = "testgroup_id";
	public static final String DB_TESTER_TOKEN = "tester_token";
	public static final String DB_EMAIL = "email";
	public static final String DB_FIRSTNAME = "firstname";
	public static final String DB_LASTNAME = "lastname";
	public static final String DB_LANG = "lang";
	public static final String DB_INDEPENDENT = "independent";
	public static final String DB_COMMENTS = "comments";
	public static final String DB_TIME_SPENT_SEC = "time_spent_sec";
	public static final String DB_ANSWERED_DATE = "answered_date";
	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_TESTER_TOKEN, new Integer(64));
		DB_COLUMN_SIZES.put(DB_EMAIL, new Integer(256));
		DB_COLUMN_SIZES.put(DB_FIRSTNAME, new Integer(128));
		DB_COLUMN_SIZES.put(DB_LASTNAME, new Integer(128));
		DB_COLUMN_SIZES.put(DB_LANG, new Integer(16));
		DB_COLUMN_SIZES.put(DB_COMMENTS, new Integer(1100));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_TESTGROUP_ID);
		DB_COLUMNS_INSERT.add(DB_TESTER_TOKEN);
		DB_COLUMNS_INSERT.add(DB_EMAIL);
		DB_COLUMNS_INSERT.add(DB_FIRSTNAME);
		DB_COLUMNS_INSERT.add(DB_LASTNAME);
		DB_COLUMNS_INSERT.add(DB_LANG);
		DB_COLUMNS_INSERT.add(DB_INDEPENDENT);
		DB_COLUMNS_INSERT.add(DB_COMMENTS);
		DB_COLUMNS_INSERT.add(DB_TIME_SPENT_SEC);
		DB_COLUMNS_INSERT.add(DB_ANSWERED_DATE);
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
	public static final String SQL_INSERT = "insert into "	+ DB_TABLE
			+ " ("+DB_ID+","+DB_COLUMNS_INSERT_SORTED+")"
			+ " values(nextval(\'"+DB_ID_GEN_NAME+"\'), "+DB_COLUMNS_INSERT_WITH_PREF_SORTED+")";
	public static final String SQL_SELECT_BY_TESTGROUP_ID = "select * from " + DB_TABLE 
			+ " where "+DB_TESTGROUP_ID+"=:"+DB_TESTGROUP_ID;
	public static final String SQL_SELECT_BY_TESTER_TOKEN = "select * from " + DB_TABLE 
			+ " where "+DB_TESTER_TOKEN+"=:"+DB_TESTER_TOKEN;
	public static final String SQL_SELECT_COMMENTS_BY_GROUP_ID = "select "+DB_COMMENTS+" from " + DB_TABLE 
			+ " where "+DB_TESTGROUP_ID+"=:"+DB_TESTGROUP_ID+ " and "+DB_COMMENTS+" is NOT NULL";
	public static final String SQL_UPDATE_BY_ID = "update "+DB_TABLE+" set " + DB_COMMENTS+"=:"+DB_COMMENTS 
			+ ", " + DB_TIME_SPENT_SEC+"=:"+DB_TIME_SPENT_SEC + ", " + DB_ANSWERED_DATE+"=:"+DB_ANSWERED_DATE
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String sqlSelectTesterIdsByGroupIds(List<String> tgIds){
		return "select "+DB_ID+" from " + DB_TABLE + " where "+DB_TESTGROUP_ID+" in("+DisplayUtils.commaSeparated(tgIds)+")";
	}

	public TesterResult() {
		super();
	}
	public TesterResult(Tester tester, int testgroupId, String testerToken, boolean independent) {
		super();
		this.testgroupId = testgroupId;
		this.testerToken = fitDb(testerToken, DB_TESTER_TOKEN);
		this.email = fitDb(tester.getEmail(), DB_EMAIL);
		this.firstname = fitDb(tester.getFirstname(), DB_FIRSTNAME);
		this.lastname = fitDb(tester.getLastname(), DB_LASTNAME);
		this.language = tester.getLanguage();
		this.independent = independent;
	}
	public TesterResult(int id, int testgroupId, String testerToken,
			String email, String firstname, String lastname, String language,
			boolean independent, String comments, long timeSpentSec, Date answeredDate) {
		super();
		this.id = id;
		this.testgroupId = testgroupId;
		this.testerToken = fitDb(testerToken, DB_TESTER_TOKEN);
		this.email = fitDb(email, DB_EMAIL);
		this.firstname = fitDb(firstname, DB_FIRSTNAME);
		this.lastname = fitDb(lastname, DB_LASTNAME);
		this.language = language;
		this.independent = independent;
		this.comments = fitDb(comments, DB_COMMENTS);
		this.timeSpentSec = timeSpentSec;
		this.answeredDate = answeredDate;
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_TESTGROUP_ID, testgroupId);
		values.put(DB_TESTER_TOKEN, testerToken);
		values.put(DB_EMAIL, email);
		values.put(DB_FIRSTNAME, firstname);
		values.put(DB_LASTNAME, lastname);
		values.put(DB_LANG, language);
		values.put(DB_INDEPENDENT, independent);
		values.put(DB_COMMENTS, comments);
		values.put(DB_TIME_SPENT_SEC, timeSpentSec);
		values.put(DB_ANSWERED_DATE, answeredDate);
		return values;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getTestgroupId() {
		return testgroupId;
	}
	public void setTestgroupId(int testgroupId) {
		this.testgroupId = testgroupId;
	}
	public String getTesterToken() {
		return testerToken;
	}
	public void setTesterToken(String testerToken) {
		this.testerToken = fitDb(testerToken, DB_TESTER_TOKEN);
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = fitDb(email, DB_EMAIL);
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
	public boolean isIndependent() {
		return independent;
	}
	public void setIndependent(boolean independent) {
		this.independent = independent;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = fitDb(comments, DB_COMMENTS);
	}
	public long getTimeSpentSec() {
		return timeSpentSec;
	}
	public void setTimeSpentSec(long timeSpentSec) {
		this.timeSpentSec = timeSpentSec;
	}
	public Date getAnsweredDate() {
		return answeredDate;
	}
	public void setAnsweredDate(Date answeredDate) {
		this.answeredDate = answeredDate;
	}
	@Override
	public String toString() {
		return "TesterResult [id=" + id + ", testgroupId=" + testgroupId
				+ ", testerToken=" + testerToken + ", email=" + email
				+ ", firstname=" + firstname + ", lastname=" + lastname
				+ ", language=" + language + ", independent=" + independent
				+ "]";
	}

}
