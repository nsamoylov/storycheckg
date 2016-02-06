package com.nicksamoylov.storycheck.domain.test.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nicksamoylov.storycheck.domain.test.AnswerOption;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class QuestionResult {
	private int id;
	private int testId;
	private int questionOrder;
	private String questionEn;
	private String questionRu;
	private String language;
	private List<AnswerOption> answerOptions;
	
	public static final String DB_TABLE = "Results_Questions";
	public static final String DB_ID_GEN_NAME = "Results_Questions_id";
	public static final String DB_ID = "id";
	public static final String DB_TEST_ID = "test_id";
	public static final String DB_QUESTION_ORDER = "question_order";
	public static final String DB_QUESTION_EN = "question_en";
	public static final String DB_QUESTION_RU = "question_ru";
	public static final String DB_LANGUAGE = "lang";
	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_QUESTION_EN, new Integer(1100));
		DB_COLUMN_SIZES.put(DB_QUESTION_RU, new Integer(1100));
		DB_COLUMN_SIZES.put(DB_LANGUAGE, new Integer(16));
	}

	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_TEST_ID);
		DB_COLUMNS_INSERT.add(DB_QUESTION_ORDER);
		DB_COLUMNS_INSERT.add(DB_QUESTION_EN);
		DB_COLUMNS_INSERT.add(DB_QUESTION_RU);
		DB_COLUMNS_INSERT.add(DB_LANGUAGE);
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
	public static final String SQL_SELECT_BY_TEST_ID_AND_ORDER = "select * from " + DB_TABLE 
		    + " where "+DB_TEST_ID+"=:"+DB_TEST_ID +" and " +DB_QUESTION_ORDER+"=:"+DB_QUESTION_ORDER;
	public static final String SQL_SELECT_BY_TEST_ID_ORDERED = "select * from " + DB_TABLE 
		    + " where "+DB_TEST_ID+"=:"+DB_TEST_ID +" order by " +DB_QUESTION_ORDER;

	public QuestionResult() {
		super();
	}
	public QuestionResult(int id, int testId, int questionOrder, String questionEn, String questionRu, String language) {
		super();
		this.id = id;
		this.testId = testId;
		this.questionOrder = questionOrder;
		this.language = fitDb(language, DB_LANGUAGE);
		this.questionEn = fitDb(questionEn, DB_QUESTION_EN);
		this.questionRu = fitDb(questionRu, DB_QUESTION_RU);
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_TEST_ID, testId);
		values.put(DB_QUESTION_ORDER, questionOrder);
		values.put(DB_QUESTION_EN, questionEn);
		values.put(DB_QUESTION_RU, questionRu);
		values.put(DB_LANGUAGE, language);
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
	public int getQuestionOrder() {
		return questionOrder;
	}
	public void setQuestionOrder(int questionOrder) {
		this.questionOrder = questionOrder;
	}
	public String getQuestionEn() {
		return questionEn;
	}
	public void setQuestionEn(String questionEn) {
		this.questionEn = fitDb(questionEn, DB_QUESTION_EN);
	}
	public String getQuestionRu() {
		return questionRu;
	}
	public void setQuestionRu(String questionRu) {
		this.questionRu = fitDb(questionRu, DB_QUESTION_RU);
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = fitDb(language, DB_LANGUAGE);
	}
	public List<AnswerOption> getAnswerOptions() {
		return answerOptions;
	}
	public void setAnswerOptions(List<AnswerOption> answerOptions) {
		this.answerOptions = answerOptions;
	}
	@Override
	public String toString() {
		return "Question [id=" + id + ", testId=" + testId
				+ ", questionOrder=" + questionOrder + ", questionEn=" + questionEn 
				+ ", questionRu=" + questionRu + ", language=" + language + "]";
	}

}
