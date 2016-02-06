package com.nicksamoylov.storycheck.domain.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;

import com.nicksamoylov.storycheck.domain.test.results.QuestionResult;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.Utils;

public final class Questions{
	private static Logging log = Logging.getLog("Questions");
	private static String TEXT_LENGTH_SHORT_ABBR = "tts";
	private static String TEXT_LENGTH_LONG_ABBR  = "ttl";
	private static String TEXT_LENGTH_IMAGE_ABBR = "ti";
	private static final Map<String, String> TEXT_LENGTH_MAP = new HashMap<String, String>();
	static{
		TEXT_LENGTH_MAP.put(Test.TEXT_LENGTH_SHORT, TEXT_LENGTH_SHORT_ABBR);
		TEXT_LENGTH_MAP.put(Test.TEXT_LENGTH_LONG,  TEXT_LENGTH_LONG_ABBR);
		TEXT_LENGTH_MAP.put(Test.TEXT_LENGTH_IMAGE, TEXT_LENGTH_IMAGE_ABBR);		
	}
    private String questionsToken;
    private String testerToken;
    private String[] questions;
    private String[] questionKeys;
    private Map<Integer, String[][]> answerOptions;
    private String answer1;
    private String answer2;
    private String answer3;
    private String comments;
    private String textLengthAbbreviation;
    
	public Questions() {
		super();
	}
	public Questions(final String questionsToken, final String testerToken, final List<QuestionResult> qrs, final Locale locale) {
		super();
		this.questionsToken = questionsToken;
		this.testerToken = testerToken;
		this.answerOptions = new HashMap<Integer, String[][]>();		
		this.questions = new String[qrs.size()];
		boolean isRussian = Utils.isRussian(locale);
		int i = 1;
		for(QuestionResult qr:qrs){
			this.questions[i-1] = isRussian?qr.getQuestionRu():qr.getQuestionEn();
			String[][] a = new String[qr.getAnswerOptions().size()][2];
			int j = 0;
			for(AnswerOption ao:qr.getAnswerOptions()){
				a[j][0] = ao.getAnswerKey();
				a[j++][1] = isRussian?ao.getAnswerRu():ao.getAnswerEn();
				if(j==1){
					switch (i) {
					case 1:
						this.answer1 = ao.getAnswerKey();
						break;
					case 2:
						this.answer2 = ao.getAnswerKey();
						break;
					case 3:
						this.answer3 = ao.getAnswerKey();
						break;
					default:
						throw new RuntimeException("Add more answers in Questions.java");
					}
				}
			}
			this.answerOptions.put(i++, a);
		}
	}
	public Questions(String textLength, MessageSource messageSource, final Locale locale) {
		super();
		this.questionsToken = "abc";
		this.answerOptions = new HashMap<Integer, String[][]>();
		this.textLengthAbbreviation = textLength;
		if(TEXT_LENGTH_MAP.containsKey(textLength)){
			this.textLengthAbbreviation = TEXT_LENGTH_MAP.get(textLength);
		}
		int qn = TEXT_LENGTH_IMAGE_ABBR.equals(this.textLengthAbbreviation)?2:3;
		this.questions = new String[qn];
		this.questionKeys = new String[qn];
		int limit = qn+1;
		for(int i=1; i < limit; i++){
			this.questionKeys[i-1] = this.textLengthAbbreviation+".question."+i;
			this.questions[i-1] = messageSource.getMessage(this.questionKeys[i-1], new Object[0], locale);
			int limitJ = i<3?5:3;
			String[][] a = new String[limitJ-1][2];
			for(int j=1; j < limitJ; j++){
				a[j-1][0] = this.textLengthAbbreviation+".answer."+i+"."+j;
				a[j-1][1] = messageSource.getMessage(a[j-1][0], new Object[0], locale);
			}
			this.answerOptions.put(i, a);
			switch (i) {
			case 1:
				this.answer1 = a[0][0];
				break;
			case 2:
				this.answer2 = a[0][0];
				break;
			case 3:
				this.answer3 = a[0][0];
				break;
			default:
				throw new RuntimeException("Add more default answers in Questions.java");
			}
		}
	}
	public String getTextLengthAbbreviation() {
		return textLengthAbbreviation;
	}
	public void setTextLengthAbbreviation(String textLengthAbbreviation) {
		this.textLengthAbbreviation = textLengthAbbreviation;
	}
	public String getQuestionsToken() {
		return questionsToken;
	}
	public void setQuestionsToken(String questionsToken) {
		this.questionsToken = questionsToken;
	}
	public String getTesterToken() {
		return testerToken;
	}
	public void setTesterToken(String testerToken) {
		this.testerToken = testerToken;
	}
	public String[] getQuestions() {
		return questions;
	}
	public void setQuestions(String[] questions) {
		this.questions = questions;
	}
	public String[] getQuestionKeys() {
		return questionKeys;
	}
	public void setQuestionKeys(String[] questionKeys) {
		this.questionKeys = questionKeys;
	}
	public String[][] getAnswerOptions(int questionOrder) {
		return this.answerOptions.get(questionOrder);
	}
	public int getAnswerOptionsNumber(int questionOrder) {
		return getAnswerOptions(questionOrder).length;
	}
	public String getAnswerKey(int questionOrder, int answerOrder) {
		return this.answerOptions.get(questionOrder)[answerOrder-1][0];
	}
	public String getAnswerValue(int questionOrder, int answerOrder) {
		return this.answerOptions.get(questionOrder)[answerOrder-1][1];
	}
	public String getAnswer1() {
		return answer1;
	}
	public void setAnswer1(String answer1) {
		this.answer1 = answer1;
	}
	public String getAnswer2() {
		return answer2;
	}
	public void setAnswer2(String answer) {
		this.answer2 = answer;
	}
	public String getAnswer3() {
		return answer3;
	}
	public void setAnswer3(String answer3) {
		this.answer3 = answer3;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getLinkToQuestions(String lang){
        return Utils.getPropertyServerUrl()+"/test/testerquestions?qt="+getQuestionsToken()
                +(getTesterToken()==null?"":"&tt="+getTesterToken())
                +"&tl="+getTextLengthAbbreviation()+"&lang="+lang;
	}
	@Override
	public String toString() {
		return "Questions [questionsToken=" + questionsToken + ", testerToken="
				+ testerToken + ", questions=" + Arrays.toString(questions)
				+ ", answerOptions=" + DisplayUtils.displayMap(answerOptions) + ", answer1=" + answer1
				+ ", answer2=" + answer2 + ", answer3=" + answer3 + "]";
	}
	
}
