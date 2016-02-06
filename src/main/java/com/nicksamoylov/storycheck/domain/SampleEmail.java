package com.nicksamoylov.storycheck.domain;

public class SampleEmail {
	private int id;
	private String firstname;
	private String lastname;
	private String text = "";
	private String language;

	private static final String DB_TABLE = "sample_emails";
	public static final String DB_ID = "id";
	public static final String DB_FIRSTNAME = "firstname";
	public static final String DB_LASTNAME = "lastname";
	public static final String DB_TEXT = "text";
	public static final String DB_LANG = "lang";

	public static final String SQL_SELECT_IDS_BY_LANG = "select "+DB_ID+" from " + DB_TABLE 
			+ " where "+DB_LANG+"=:"+DB_LANG;
	public static final String SQL_SELECT_BY_ID = "select * from " + DB_TABLE + " where "+DB_ID+"=:"+DB_ID;

	public SampleEmail() {
	}
	public SampleEmail(int id, String firstname, String lastname, String text,
			String language) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.text = text;
		this.language = language;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	@Override
	public String toString() {
		return "SampleEmail [id=" + id + ", firstname=" + firstname
				+ ", lastname=" + lastname + ", text.length=" + text.length() + ", language="
				+ language + "]";
	}

}
