package com.nicksamoylov.storycheck.domain.test.results;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class AnswerResult {
    private int id;
    private int testerId;
    private int questionId;
	private String answerKey;
    
	public static final String DB_TABLE = "Results_Answers";
	public static final String DB_ID_GEN_NAME = "Results_Answers_id";
	public static final String DB_ID = "id";
	public static final String DB_TESTER_ID = "tester_id";
	public static final String DB_QUESTION_ID = "question_id";
	public static final String DB_ANSWER_KEY = "answer_key";
	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_ANSWER_KEY, new Integer(256));
	}

	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_TESTER_ID);
		DB_COLUMNS_INSERT.add(DB_QUESTION_ID);
		DB_COLUMNS_INSERT.add(DB_ANSWER_KEY);
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
	public static final String SQL_SELECT_BY_TESTER_ID_AND_QUESTION_ID = "select * from " + DB_TABLE 
			+ " where "+DB_TESTER_ID+"=:"+DB_TESTER_ID+" and "+ DB_QUESTION_ID+"=:"+DB_QUESTION_ID;
	
	public static final String sqlCountAnswersByTesterIdsAndQuestionId(List<String> testerIds, int questionId){
		return "select count(*) from " + DB_TABLE 
				+ " where "+DB_TESTER_ID+" in("+DisplayUtils.commaSeparated(testerIds)+") and "+ DB_QUESTION_ID+"="+questionId;
	}
	public static final String sqlCountAnswersByTesterIdsQuestionIdAndAnswerKey(List<String> testerIds, int questionId, String answerKey){
		return "select count (*) from " + DB_TABLE 
				+ " where "+DB_TESTER_ID+" in("+DisplayUtils.commaSeparated(testerIds)+") and "	+ DB_QUESTION_ID+"="+questionId 
				+" and "+DB_ANSWER_KEY+"='"+answerKey+"'";
	}
	public AnswerResult() {
		super();
	}
	public AnswerResult(int id, int testerId, int questionId, String answerKey) {
		super();
		this.id = id;
		this.testerId = testerId;
		this.questionId = questionId;
		this.answerKey = fitDb(answerKey, DB_ANSWER_KEY);
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_TESTER_ID, testerId);
		values.put(DB_QUESTION_ID, questionId);
		values.put(DB_ANSWER_KEY, answerKey);
		return values;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getTesterId() {
		return testerId;
	}
	public void setTesterId(int testerId) {
		this.testerId = testerId;
	}
	public int getQuestionId() {
		return questionId;
	}
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	public String getAnswerKey() {
		return answerKey;
	}
	public void setAnswerKey(String answerKey) {
		this.answerKey = fitDb(answerKey, DB_ANSWER_KEY);
	}
	@Override
	public String toString() {
		return "AnswerResult [id=" + id + ", testerId="
				+ testerId + ", questionId=" + questionId + ", answerKey=" + answerKey + "]";
	}

}
