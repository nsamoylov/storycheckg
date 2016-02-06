package com.nicksamoylov.storycheck.domain.test.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nicksamoylov.storycheck.domain.test.Testgroup;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.StringUtils;
import com.nicksamoylov.storycheck.utils.Utils;

public class TestgroupResult {
	private static Logging log = Logging.getLog("TestgroupResult");
	private int id;
	private int testId;
	private String name;
	private String text = "";
	private String size = "0";
	private String numberAnswered = "0";
	private Map<Integer, String[]> questionsAnsweresMap = new HashMap<Integer, String[]>();

	public static final String DB_TABLE = "Results_Testgroups";
	public static final String DB_ID_GEN_NAME = "Results_Testgroups_id";
	public static final String DB_ID = "id";
	public static final String DB_TEST_ID = "test_id";
	public static final String DB_NAME = "name";
	public static final String DB_SIZE = "size";
	public static final String DB_TEXT = "text";
	
	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_NAME, new Integer(64));
		DB_COLUMN_SIZES.put(DB_TEXT, new Integer(1100));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_TEST_ID);
		DB_COLUMNS_INSERT.add(DB_NAME);
		DB_COLUMNS_INSERT.add(DB_SIZE);
		DB_COLUMNS_INSERT.add(DB_TEXT);
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
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_BY_TEST_ID_AND_NAME = "select * from " + DB_TABLE 
			+ " where "+DB_TEST_ID+"=:"+DB_TEST_ID+ " and "+DB_NAME+"=:"+DB_NAME;
	public static final String SQL_SELECT_BY_TEST_ID_ORDER_BY_NAME= "select * from " + DB_TABLE 
			+ " where "+DB_TEST_ID+"=:"+DB_TEST_ID+" order by "+DB_NAME;
	public static final String SQL_SELECT_IDS_BY_TEST_ID = "select "+DB_ID+" from " + DB_TABLE 
			+ " where "+DB_TEST_ID+"=:"+DB_TEST_ID;
	
	public TestgroupResult() {
		super();
	}
	public TestgroupResult(int id, int testId, String name, int size, String text) {
		super();
		this.id = id;
		this.testId = testId;
		this.name = fitDb(name, DB_NAME);
		this.size = Integer.toString(size);
		this.text = fitDb(text, DB_TEXT);
	}
	public TestgroupResult(int testId, Testgroup tg, int size) {
		super();
		this.testId = testId;
		this.name = fitDb(tg.getName(), DB_NAME);
		this.size = Integer.toString(size);
		this.text = fitDb(tg.getText(), DB_TEXT);
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_TEST_ID, testId);
		values.put(DB_NAME, name);
		values.put(DB_SIZE, Integer.parseInt(size));
		values.put(DB_TEXT, text);
		return values;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getTestId() {
		return testId;
	}
	public void setTestId(int testId) {
		this.testId = testId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = fitDb(name, DB_NAME);
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = fitDb(text, DB_TEXT);
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getNumberAnswered() {
		return numberAnswered;
	}
	public void setNumberAnswered(String numberAnswered) {
		this.numberAnswered = numberAnswered;
	}
	public String getCompleteness() {
		String nt = (this.size==null?"10":this.size);
		String na = (this.numberAnswered==null?"0":this.numberAnswered);
		return Utils.calculatePercent(na, nt)+"% ("+na+"/"+nt+")";
	}
	public void setQuestionAnswers(int questionOrder, String[] answerOptionSelected){
		//log.debug("*********************set tg="+id+": questionOrder="+questionOrder+", answerOptionSelected="+DisplayUtils.commaSeparated(answerOptionSelected));
		this.questionsAnsweresMap.put(questionOrder, answerOptionSelected);
	}
	public String getCompleteness(int questionOrder, int answerOrder) {
		//log.debug("*********************get questionOrder="+questionOrder+", answerOrder="+answerOrder);
		String[] a = questionsAnsweresMap.get(questionOrder);
		String b = (a==null?"0":a[answerOrder-1]);
		String nt = (this.size==null?"10":this.size);
		return Utils.calculatePercent(b, nt)+"% ("+b+"/"+nt+")";
	}
	@Override
	public String toString() {
		return "TestgroupResult [id=" + id + ", testId=" + testId + ", name=" + name 
				+ ", size=" + size + ", numberAnswered=" + numberAnswered + ", text.length=" + text.length() + "]";
	}

}
