package com.nicksamoylov.storycheck.domain.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;
import com.nicksamoylov.storycheck.utils.Utils;

public class Test {
	private int id;
	private int writerId;
	@NotEmpty
    @Length(min=2, max=64)
	private String name;
	private String status="";
	private Set<Integer> testgroupsId = new HashSet<Integer>();
	private String testTextLength = TEXT_LENGTH_SHORT;
	private String emailText="";
	
	public static final String DB_TABLE = "tests";
	public static final String DB_ID_GEN_NAME = "tests_id";
	public static final String DB_ID = "id";
	public static final String DB_WRITER_ID = "writer_id";
	public static final String DB_NAME = "name";
	public static final String DB_STATUS = "status";
	public static final String DB_TEXT_LENGTH="text_length";
	public static final String DB_EMAIL_TEXT="emailtext";
	public static final String DB_MODIFIED_DATE="modified_date";
	public static final int DB_EMAIL_MIN_LENGTH = 20;
	public static final int DB_EMAIL_MAX_LENGTH = 1100;
	
	public static final String STATUS_NO_GROUPS_NO_TEXT = "test.status.nogroups.notext"; 
	public static final String STATUS_NO_GROUPS_NO_IMAGE = "test.status.nogroups.noimage"; 
	public static final String STATUS_PARTIAL_TEXT = "test.status.partialtext"; 
	public static final String STATUS_PARTIAL_IMAGE = "test.status.partialimage"; 
	public static final String STATUS_NO_EMAIL = "test.status.noemail"; 
	public static final String STATUS_READY = "test.status.ready"; 
	
	public static final String TEXT_LENGTH_SHORT = "test.text.short"; 
	public static final String TEXT_LENGTH_LONG = "test.text.long"; 
	public static final String TEXT_LENGTH_IMAGE = "test.image"; 
	public static final List<String> TEST_TEXT_LENGTH_OPTIONS = new ArrayList<String>();
	static {
		TEST_TEXT_LENGTH_OPTIONS.add(TEXT_LENGTH_SHORT);
		TEST_TEXT_LENGTH_OPTIONS.add(TEXT_LENGTH_LONG);
		TEST_TEXT_LENGTH_OPTIONS.add(TEXT_LENGTH_IMAGE);
	}
	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_NAME, new Integer(100));
		DB_COLUMN_SIZES.put(DB_STATUS, new Integer(64));
		DB_COLUMN_SIZES.put(DB_TEXT_LENGTH, new Integer(20));
		DB_COLUMN_SIZES.put(DB_EMAIL_TEXT, new Integer(1100));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_WRITER_ID);
		DB_COLUMNS_INSERT.add(DB_NAME);
		DB_COLUMNS_INSERT.add(DB_STATUS);
		DB_COLUMNS_INSERT.add(DB_TEXT_LENGTH);
	}
	private static final Set<String> DB_COLUMNS_UPDATE = new HashSet<String>();
	static {
		DB_COLUMNS_UPDATE.add(DB_ID);
		DB_COLUMNS_UPDATE.add(DB_EMAIL_TEXT);
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
			+ " ("+DB_ID+","+DB_COLUMNS_INSERT_SORTED+")"
			+ " values(nextval(\'"+DB_ID_GEN_NAME+"\'), "+DB_COLUMNS_INSERT_WITH_PREF_SORTED+")";
	public static final String SQL_COUNT_BY_WRITER_ID = "select count (*) from " + DB_TABLE 
		    + " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID;
	public static final String SQL_UPDATE_BY_ID = "update " + DB_TABLE +" set " + SET_DB_COLUMNS_UPDATE 
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_UPDATE_STATUS_BY_ID = "update " + DB_TABLE +" set "+DB_STATUS+"=:" + DB_STATUS 
			+ ","+DB_MODIFIED_DATE+"=:" + DB_MODIFIED_DATE+" where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_ALL_BY_WRITER_ID_ORDER_BY_NAME = "select * from " + DB_TABLE 
		    + " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID +" order by " +DB_NAME;
	public static final String SQL_SELECT_BY_WRITER_ID_AND_ID = "select * from " + DB_TABLE 
			+ " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID +" and "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_BY_WRITER_ID_AND_NAME = "select * from " + DB_TABLE 
			+ " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID +" and "+DB_NAME+"=:"+DB_NAME;
	public static final String SQL_DELETE_BY_ID = "delete from " + DB_TABLE + " where "+DB_ID+"=:"+DB_ID;

	public static final String DB_TEST_MEMBERS_TABLE = "test_members";
	public static final String DB_TEST_MEMBERS_TEST_ID = "test_id";
	public static final String DB_TEST_MEMBERS_TESTGROUP_ID = "testgroup_id";
	public static final String DB_TEST_MEMBERS_TEXT = "text";

	private static final String SQL_SELECT_TEST_IDS_BY_TESTGROUP_ID = "select "+DB_TEST_MEMBERS_TEST_ID+" from " + DB_TEST_MEMBERS_TABLE 
		    + " where "+DB_TEST_MEMBERS_TESTGROUP_ID+"=:"+DB_TEST_MEMBERS_TESTGROUP_ID;
	public static final String SQL_SELECT_BY_TESTGROUP_IDS_ORDER_BY_NAME = "select * from " + DB_TABLE 
		    + " where "+DB_ID+" in("+SQL_SELECT_TEST_IDS_BY_TESTGROUP_ID +") order by " +DB_NAME;
	public static final String SQL_SELECT_TEXT_BY_TEST_ID_AND_TESTGROUP_ID = "select "+DB_TEST_MEMBERS_TEXT+" from " + DB_TEST_MEMBERS_TABLE 
		    + " where "+DB_TEST_MEMBERS_TEST_ID+"=:"+DB_TEST_MEMBERS_TEST_ID 
		    + " and "+DB_TEST_MEMBERS_TESTGROUP_ID+"=:"+DB_TEST_MEMBERS_TESTGROUP_ID;
	public static final String SQL_UPDATE_TEXT_BY_TEST_ID_AND_TESTGROUP_ID = "update " + DB_TEST_MEMBERS_TABLE
			+ " set "+DB_TEST_MEMBERS_TEXT+"=:"+DB_TEST_MEMBERS_TEXT
		    + " where "+DB_TEST_MEMBERS_TEST_ID+"=:"+DB_TEST_MEMBERS_TEST_ID 
		    + " and "+DB_TEST_MEMBERS_TESTGROUP_ID+"=:"+DB_TEST_MEMBERS_TESTGROUP_ID;
	
	public static final String SQL_SELECT_TESTGROUP_IDS_BY_TEST_ID = "select "+DB_TEST_MEMBERS_TESTGROUP_ID+" from " + DB_TEST_MEMBERS_TABLE 
		    + " where "+DB_TEST_MEMBERS_TEST_ID+"=:"+DB_TEST_MEMBERS_TEST_ID;
	public static final String SQL_INSERT_MEMBER = "insert into "+ DB_TEST_MEMBERS_TABLE
			+ " ("+DB_TEST_MEMBERS_TEST_ID+","+DB_TEST_MEMBERS_TESTGROUP_ID+")"
			+ " values(:"+DB_TEST_MEMBERS_TEST_ID+", :"+DB_TEST_MEMBERS_TESTGROUP_ID+")";
	public static final String SQL_DELETE_ALL_MEMBERS = "delete from " + DB_TEST_MEMBERS_TABLE 
			+ " where "+DB_TEST_MEMBERS_TEST_ID+"=:"+DB_TEST_MEMBERS_TEST_ID;
	public static final String SQL_DELETE_ALL_MEMBERSHIP = "delete from " + DB_TEST_MEMBERS_TABLE 
			+ " where "+DB_TEST_MEMBERS_TESTGROUP_ID+"=:"+DB_TEST_MEMBERS_TESTGROUP_ID;
	public static final String sqlDeleteGroupMembersByIds(Set<Integer> ids){
		return "delete from " + DB_TEST_MEMBERS_TABLE + " where "+DB_TEST_MEMBERS_TEST_ID+"=:"+DB_TEST_MEMBERS_TEST_ID
				+" and "+DB_TEST_MEMBERS_TESTGROUP_ID	+ " in("+DisplayUtils.commaSeparatedSorted(ids)+")";
	}
	private static final String contentDir = Utils.getPropertyContent();
	public static final String getTextfileDirName(final int writerId){
		return contentDir+File.separatorChar+"writer"+writerId;
	}
	public static final String getTextfileName(final int testId, final int tgId){
		return "test"+testId+"tg"+tgId;
	}
	public static final File getFile(final int writerId, final int testId, final int tgId){
		return new File(Test.getTextfileDirName(writerId),Test.getTextfileName(testId, tgId));
	}
	public static final File getApplicationImageFile(final String imageName){
		return new File(contentDir, imageName);
	}

	public Test() {
		super();
	}

	public Test(int id, int writerId, String name, String status, String testTextLength, String emailText) {
		super();
		this.id = id;
		this.writerId = writerId;
		this.name = fitDb(name, DB_NAME);
		this.status = fitDb(status, DB_STATUS);
		this.testTextLength = testTextLength;
		this.emailText = fitDb(emailText, DB_EMAIL_TEXT);
	}

	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_WRITER_ID, writerId);
		values.put(DB_NAME, name);
		values.put(DB_STATUS, status);
		values.put(DB_EMAIL_TEXT, emailText);
		values.put(DB_TEXT_LENGTH, testTextLength);
		values.put(DB_MODIFIED_DATE, new Date());
		return values;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setWriterId(int writerId) {
		this.writerId = writerId;
	}

	public int getWriterId() {
		return writerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = fitDb(name, DB_NAME);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = fitDb(status, DB_STATUS);
	}

    public Set<Integer> getTestgroupsId() {
		if(this.testgroupsId == null){
			this.testgroupsId = new HashSet<Integer>();
		}
		return testgroupsId;
	}

	public void setTestgroupsId(Set<Integer> testgroupsId) {
		if(this.testgroupsId == null){
			this.testgroupsId = new HashSet<Integer>();
		}
		this.testgroupsId.addAll(testgroupsId);
	}

	public void addTestgroupId(int testgroupId) {
		if(this.testgroupsId == null){
			this.testgroupsId = new HashSet<Integer>();
		}
		this.testgroupsId.add(testgroupId);
	}

	public String getTestTextLength() {
		return testTextLength;
	}

	public void setTestTextLength(String testTextLength) {
		this.testTextLength = testTextLength;
	}
	
	public List<String> getTestTextLengthOptions(){
		return TEST_TEXT_LENGTH_OPTIONS;
	}
	
	public boolean isTextShort(){
		return TEXT_LENGTH_SHORT.equals(this.testTextLength);
	}

	public boolean isTextLong(){
		return TEXT_LENGTH_LONG.equals(this.testTextLength);
	}
	
	public boolean isImage(){
		return TEXT_LENGTH_IMAGE.equals(this.testTextLength);
	}
	
	public String getEnctype(){
		return isTextShort()?"application/x-www-form-urlencoded":"multipart/form-data";
	}

	public String getEmailText() {
		return emailText;
	}

	public void setEmailText(String emailText) {
		this.emailText = (emailText==null?"":emailText.trim());
	}

	public boolean isEmailTextEmpty(){
		return this.emailText == null || this.emailText.length() == 0;
	}

	public boolean isReady(){
		return STATUS_READY.equals(this.status);
	}

	@Override
	public String toString() {
		return "Test [id=" + id + ", writerId=" + writerId + ", name=" + name
				+ ", testTextLength=" + testTextLength + ", status=" + status 
				+ ", email.length()=" + (emailText==null?null:emailText.length()) + ", tgIds=" + testgroupsId + "]";
	}

 }
