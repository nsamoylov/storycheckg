package com.nicksamoylov.storycheck.domain.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class AnswerOption {
    private int id;
    private int questionId;
    private int answerOrder;
	private String answerKey;
	private String answerEn;
	private String answerRu;
    
	public static final String DB_TABLE = "Results_Answer_options";
	public static final String DB_ID_GEN_NAME = "Results_Answer_options_id";
	public static final String DB_ID = "id";
	public static final String DB_QUESTION_ID = "question_id";
	public static final String DB_ANSWER_ORDER = "answer_order";
	public static final String DB_ANSWER_KEY = "answer_key";
	public static final String DB_ANSWER_EN = "answer_en";
	public static final String DB_ANSWER_RU = "answer_ru";
	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_ANSWER_KEY, new Integer(256));
		DB_COLUMN_SIZES.put(DB_ANSWER_EN, new Integer(256));
		DB_COLUMN_SIZES.put(DB_ANSWER_RU, new Integer(256));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_QUESTION_ID);
		DB_COLUMNS_INSERT.add(DB_ANSWER_ORDER);
		DB_COLUMNS_INSERT.add(DB_ANSWER_KEY);
		DB_COLUMNS_INSERT.add(DB_ANSWER_EN);
		DB_COLUMNS_INSERT.add(DB_ANSWER_RU);
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
	public static final String SQL_SELECT_BY_QUESTION_ID_ORDERED = "select * from " + DB_TABLE 
			+ " where "+DB_QUESTION_ID+"=:"+DB_QUESTION_ID+ " order by "+DB_ANSWER_ORDER;
/*	public static final String getSelectAnswerByQuestionIdOrderAndKey(boolean isRussian){
		return"select "+(isRussian?DB_ANSWER_RU:DB_ANSWER_EN)+" from " + DB_TABLE 
			+ " where "+DB_QUESTION_ID+"=:"+DB_QUESTION_ID+ " and "+DB_ANSWER_ORDER+"=:"+DB_ANSWER_ORDER
			+ " and "+DB_ANSWER_KEY+"=:"+DB_ANSWER_KEY;
	}
*/	
	public AnswerOption() {
		super();
	}
	public AnswerOption(int id, int questionId, int answerOrder,
			String answerKey, String answerEn, String answerRu) {
		super();
		this.id = id;
		this.questionId = questionId;
		this.answerOrder = answerOrder;
		this.answerKey = fitDb(answerKey, DB_ANSWER_KEY);
		this.answerEn = fitDb(answerEn, DB_ANSWER_EN);
		this.answerRu = fitDb(answerRu, DB_ANSWER_RU);
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_QUESTION_ID, questionId);
		values.put(DB_ANSWER_ORDER, answerOrder);
		values.put(DB_ANSWER_KEY, answerKey);
		values.put(DB_ANSWER_EN, answerEn);
		values.put(DB_ANSWER_RU, answerRu);
		return values;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public int getAnswerOrder() {
		return answerOrder;
	}

	public void setAnswerOrder(int answerOrder) {
		this.answerOrder = answerOrder;
	}

	public String getAnswerKey() {
		return answerKey;
	}

	public void setAnswerKey(String answerKey) {
		this.answerKey = fitDb(answerKey, DB_ANSWER_KEY);
	}

	public String getAnswerEn() {
		return answerEn;
	}

	public void setAnswerEn(String answerEn) {
		this.answerEn = fitDb(answerEn, DB_ANSWER_EN);
	}

	public String getAnswerRu() {
		return answerRu;
	}

	public void setAnswerRu(String answerRu) {
		this.answerEn = fitDb(answerRu, DB_ANSWER_RU);
	}

	@Override
	public String toString() {
		return "AnswerOptions [id=" + id + ", questionId=" + questionId
				+ ", answerOrder=" + answerOrder + ", answerKey=" + answerKey
				+ ", answerEn=" + answerEn + ", answerRu=" + answerRu + "]";
	}

}
