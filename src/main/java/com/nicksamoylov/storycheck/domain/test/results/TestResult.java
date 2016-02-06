package com.nicksamoylov.storycheck.domain.test.results;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nicksamoylov.storycheck.domain.test.Test;
import com.nicksamoylov.storycheck.utils.DateUtils;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.StringUtils;
import com.nicksamoylov.storycheck.utils.Utils;

public class TestResult {
	private static Logging log = Logging.getLog("TestResult");
	private int id;
	private int userId;
    private String name;
	private String testTextLength;
	private String emailText="";
	private String questionsToken="";
	private Date createdDate;
	private Date completedDate;
	private String numberTesters = "0";
	private String numberAnswered = "0";

	public static final String DB_TABLE = "Results_Tests";
	public static final String DB_ID_GEN_NAME = "Results_Tests_id";
	public static final String DB_ID = "id";
	public static final String DB_USER_ID = "user_id";
	public static final String DB_NAME = "name";
	public static final String DB_TEXT_LENGTH="text_length";
	public static final String DB_EMAIL_TEXT="email_text";
	public static final String DB_QUESTIONS_TOKEN="Questions_token";
	public static final String DB_CREATED_DATE="created_date";
	public static final String DB_COMPLETED_DATE="completed_date";
	public static final String DB_NUMBER_TESTERS="number_testers";
	public static final String DB_NUMBER_ANSWERED="number_answered";
	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_NAME, new Integer(64));
		DB_COLUMN_SIZES.put(DB_TEXT_LENGTH, new Integer(20));
		DB_COLUMN_SIZES.put(DB_EMAIL_TEXT, new Integer(2048));
		DB_COLUMN_SIZES.put(DB_QUESTIONS_TOKEN, new Integer(64));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_USER_ID);
		DB_COLUMNS_INSERT.add(DB_NAME);
		DB_COLUMNS_INSERT.add(DB_TEXT_LENGTH);
		DB_COLUMNS_INSERT.add(DB_EMAIL_TEXT);
		DB_COLUMNS_INSERT.add(DB_QUESTIONS_TOKEN);
		DB_COLUMNS_INSERT.add(DB_NUMBER_TESTERS);
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
	public static final String SQL_SELECT_BY_ID = "select * from " + DB_TABLE 
			+ " where "+DB_ID+"=:"+DB_ID + " and "+DB_USER_ID+"=:"+DB_USER_ID;
	public static final String SQL_SELECT_BY_QUESTIONS_TOKEN = "select * from " + DB_TABLE 
			+ " where "+DB_QUESTIONS_TOKEN+"=:"+DB_QUESTIONS_TOKEN;
	public static final String SQL_SELECT_BY_USER_ID_ORDER_NAME_DATE = "select * from " + DB_TABLE 
			+ " where "+DB_USER_ID+"=:"+DB_USER_ID+" order by "+DB_NAME+","+DB_CREATED_DATE;
	public static final String SQL_UPDATE_BY_ID = "update "+DB_TABLE+" set " + DB_COMPLETED_DATE+"=:"+DB_COMPLETED_DATE 
			+ ", " + DB_NUMBER_ANSWERED+"=:"+DB_NUMBER_ANSWERED
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_UPDATE_EMAILTEXT_BY_ID = "update "+DB_TABLE+" set " + DB_EMAIL_TEXT+"=:"+DB_EMAIL_TEXT
			+ " where "+DB_ID+"=:"+DB_ID;

	private static final String contentDir = Utils.getPropertyContent()+File.separatorChar+Utils.getPropertyResults();
	public static final String getTextfileDirName(final int userId){
		return contentDir+File.separatorChar+"user"+userId;
	}
	public static final String getTextfileName(final int testId, final int tgId){
		return "test"+testId+"tg"+tgId;
	}
	public static final File getFile(final int userId, final int testId, final int tgId){
		return new File(getTextfileDirName(userId),Test.getTextfileName(testId, tgId));
	}
	public TestResult() {
		super();
	}
	public TestResult(Test test, int userId, String questionsToken, int emailsCount) {
		super();
		this.userId = userId;
		this.name = fitDb(test.getName(), DB_NAME);
		this.testTextLength = test.getTestTextLength();
		this.questionsToken = questionsToken;
		this.numberTesters = Integer.toString(emailsCount);
	}
	public TestResult(int id, int userId, String name, String testTextLength, String emailText, 
			String questionsToken, Date createdDate, Date completedDate, String numberTesters, String numberAnswered) {
		super();
		this.id = id;
		this.userId = userId;
		this.name = fitDb(name, DB_NAME);
		this.testTextLength = testTextLength;
		this.emailText = fitDb(emailText, DB_EMAIL_TEXT);
		this.questionsToken = questionsToken;
		this.createdDate = createdDate;
		this.completedDate = completedDate;
		this.numberTesters = numberTesters;
		this.numberAnswered = numberAnswered;
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_USER_ID, userId);
		values.put(DB_NAME, name);
		values.put(DB_EMAIL_TEXT, emailText);
		values.put(DB_TEXT_LENGTH, testTextLength);
		values.put(DB_QUESTIONS_TOKEN, questionsToken);
		values.put(DB_COMPLETED_DATE, completedDate);
		values.put(DB_NUMBER_TESTERS, Integer.parseInt(numberTesters));
		values.put(DB_NUMBER_ANSWERED, Integer.parseInt(numberAnswered==null?"0":numberAnswered));
		return values;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getName() {
		return fitDb(name, DB_NAME);
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTestTextLength() {
		return testTextLength;
	}
	public void setTestTextLength(String testTextLength) {
		this.testTextLength = testTextLength;
	}
	public boolean isImage(){
		return Test.TEXT_LENGTH_IMAGE.equals(this.testTextLength);
	}
	public boolean isTextShort(){
		return Test.TEXT_LENGTH_SHORT.equals(this.testTextLength);
	}
	public String getEmailText() {
		return emailText;
	}
	public void setEmailText(String emailText) {
		this.emailText = fitDb(emailText, DB_EMAIL_TEXT);
	}
	public String getQuestionsToken() {
		return questionsToken;
	}
	public void setQuestionsToken(String questionsToken) {
		this.questionsToken = questionsToken;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getCreatedDateString() {
		return DateUtils.formatTime(createdDate);
	}
	public String getCompletedDateString() {
		return DateUtils.formatTime(completedDate);
	}
	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
	}
	public String getNumberTesters() {
		return numberTesters;
	}
	public void setNumberTesters(String numberTesters) {
		this.numberTesters = numberTesters;
	}
	public String getNumberAnswered() {
		return numberAnswered;
	}
	public void setNumberAnswered(String numberAnswered) {
		this.numberAnswered = numberAnswered;
	}
	public String getCompleteness() {
		String nt = (this.numberTesters==null?"10":this.numberTesters);
		String na = (this.numberAnswered==null?"0":this.numberAnswered);
		if(nt==na){
			return getCompletedDateString();
		}
		else {
			return Utils.calculatePercent(na, nt)+"% ("+na+"/"+nt+")";
		}
	}
	@Override
	public String toString() {
		return "TestResult [id=" + id + ", userId=" + userId + ", name=" + name
				+ ", testTextLength=" + testTextLength + ", emailText.length="
				+ emailText.length() + ", questionsToken=" + questionsToken + ", testers=" + numberTesters 
				+ ", completedDate=" + completedDate + ", completeness=" + getCompleteness() + "]";
	}
	
}
