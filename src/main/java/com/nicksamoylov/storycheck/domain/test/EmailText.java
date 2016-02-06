package com.nicksamoylov.storycheck.domain.test;

import java.util.List;

import com.nicksamoylov.storycheck.utils.Utils;

public class EmailText {
    private String key;
    private String lang;
    private String subject;
    private String text;
    private String text1;
    private String text2;
    private String text3;
    private String text4;
    private Questions questions;
    private String testgroupName;
    private List<String> testers;
    private String emailChangeLink;
    private String registrationLink;
    private String emailRequestContactLink;
	public static final String DB_TABLE = "emailtexts";
	public static final String DB_KEY = "key";
	public static final String DB_LANG = "lang";
	public static final String DB_SUBJECT = "subject";
	public static final String DB_TEXT1 = "text1";
	public static final String DB_TEXT2 = "text2";
	public static final String DB_TEXT3 = "text3";
	public static final String DB_TEXT4 = "text4";

	public static final String SQL_SELECT_BY_KEY_AND_LANG = "select * from " + DB_TABLE 
			+ " where "+DB_KEY+"=:"+DB_KEY + " and "+DB_LANG+"=:"+DB_LANG;

	public EmailText() {
		super();
	}
	public EmailText(String key, String lang, String subject, String text1, String text2, String text3, String text4) {
		super();
		this.key = key;
		this.lang = lang;
		this.subject = subject;
		this.text1 = text1;
		this.text2 = text2;
		this.text3 = text3;
		this.text4 = text4;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getText1() {
		return text1;
	}
	public void setText1(String text1) {
		this.text1 = text1;
	}
	public String getText2() {
		return text2;
	}
	public void setText2(String text2) {
		this.text2 = text2;
	}
	public String getText3() {
		return text3;
	}
	public void setText3(String text3) {
		this.text3 = text3;
	}
	public String getText4() {
		return text4;
	}
	public void setText4(String text4) {
		this.text4 = text4;
	}
	public List<String> getTesters() {
		return testers;
	}
	public void setTesters(List<String> testers) {
		this.testers = testers;
	}
	public String getTestgroupName() {
		return testgroupName;
	}
	public void setTestgroupName(String testgroupName) {
		this.testgroupName = testgroupName;
	}
	public String getRegistrationLink() {
		return registrationLink;
	}
	public void setRegistrationLink(String tempPassword) {
		this.registrationLink = Utils.getPropertyServerUrl()+"/user/confirm?p="+tempPassword;
	}
	public String getEmailChangeLink() {
		return emailChangeLink;
	}
	public void setEmailChangeLink(String tempPassword) {
		this.emailChangeLink = Utils.getPropertyServerUrl()+"/user/confirm?p="+tempPassword;
	}
	public String getEmailRequestContactLink() {
		return emailRequestContactLink;
	}
	public void setEmailRequestContactLink(String tempPassword) {
		this.emailRequestContactLink = Utils.getPropertyServerUrl()+"/user/requestanswer?p="+tempPassword;
	}
	public Questions getQuestions() {
		return questions;
	}
	public void setQuestions(Questions questions) {
		this.questions = questions;
	}
	@Override
	public String toString() {
		return "EmailText [key=" + key + ", lang=" + lang + ", subject=" + subject + ", text1=" + text1
				+ ", text2=" + text2 + ", text3=" + text3 + ", text4=" + text4
				+ "]";
	}

}
