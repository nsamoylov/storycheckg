package com.nicksamoylov.storycheck.domain.users;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class Contact {
	private static final Logging log = Logging.getLog("Contact");
	private int id;
	private int requestId;
	private String roleName;
	private String emailTo;
	private String emailFrom;
	private String subject;
	private String text;
	private String contactToken;
	private Date createdDate;

	private static String DB_TABLE = "Requests_contacts";
	private static String DB_ID_GEN_NAME = "Requests_contacts_id";
	public static final String DB_ID = "id";
	public static final String DB_REQUEST_ID = "request_id";
	public static final String DB_ROLE_NAME = "role_name";
	public static final String DB_EMAIL_TO = "email_to";
	public static final String DB_EMAIL_FROM = "email_from";
	public static final String DB_SUBJECT = "subject";
	public static final String DB_TEXT = "text";
	public static final String DB_CREATED_DATE = "ctreated_date";
	public static final String DB_CONTACT_TOKEN = "contact_token";
	public static final int DB_TEXT_MIN_LENGTH = 10;
	public static final int DB_TEXT_MAX_LENGTH = Request.DB_REQUEST_MAX_LENGTH;

	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_ROLE_NAME, new Integer(40));
		DB_COLUMN_SIZES.put(DB_EMAIL_TO, new Integer(256));
		DB_COLUMN_SIZES.put(DB_EMAIL_FROM, new Integer(256));
		DB_COLUMN_SIZES.put(DB_SUBJECT, new Integer(100));
		DB_COLUMN_SIZES.put(DB_TEXT, new Integer(DB_TEXT_MAX_LENGTH));
		DB_COLUMN_SIZES.put(DB_CONTACT_TOKEN, new Integer(64));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_REQUEST_ID);
		DB_COLUMNS_INSERT.add(DB_ROLE_NAME);
		DB_COLUMNS_INSERT.add(DB_EMAIL_TO);
		DB_COLUMNS_INSERT.add(DB_EMAIL_FROM);
		DB_COLUMNS_INSERT.add(DB_SUBJECT);
		DB_COLUMNS_INSERT.add(DB_TEXT);
		DB_COLUMNS_INSERT.add(DB_CONTACT_TOKEN);
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
	public static String SQL_SELECT_BY_TOKEN = "select * from "+DB_TABLE
			+" where "+DB_CONTACT_TOKEN+"=:"+DB_CONTACT_TOKEN;
	public static String SQL_COUNT_BY_REQUEST_ID = "select count(distinct "+DB_EMAIL_FROM+") from "+DB_TABLE
			+" where "+DB_REQUEST_ID+"=:"+DB_REQUEST_ID+" and "+DB_ROLE_NAME+"=:"+DB_ROLE_NAME
			+" and "+DB_EMAIL_TO+"=:"+DB_EMAIL_TO+" and "+DB_EMAIL_FROM+" != :"+DB_EMAIL_TO;
	public static String SQL_COUNT_BY_REQUEST_ID_SINCE_DATE = "select count(distinct "+DB_EMAIL_FROM+") from "+DB_TABLE
			+" where "+DB_REQUEST_ID+"=:"+DB_REQUEST_ID+" and "+DB_ROLE_NAME+"=:"+DB_ROLE_NAME
			+" and "+DB_EMAIL_TO+"=:"+DB_EMAIL_TO+" and "+DB_EMAIL_FROM+" != :"+DB_EMAIL_TO
			+" and "+DB_CREATED_DATE+" > :"+DB_CREATED_DATE;
	public static String SQL_DELETE_BY_USER_AND_ROLE_NAMES = "delete from "+DB_TABLE
			+" where "+DB_ROLE_NAME+"=:"+DB_ROLE_NAME+" and ("+DB_EMAIL_TO+"=:"+DB_EMAIL_TO+" or "+DB_EMAIL_FROM+"=:"+DB_EMAIL_FROM+")";

	public Contact() {
	}
	public Contact(final int id, final int requestId, String roleName, String emailTo, String emailFrom,
			String subject, String text, String contactToken) {
		super();
		this.id = id;
		this.requestId = requestId;
		this.roleName = fitDb(roleName, DB_ROLE_NAME);
		this.emailTo = fitDb(emailTo, DB_EMAIL_TO);
		this.emailFrom = fitDb(emailFrom, DB_EMAIL_FROM);
		this.subject = fitDb(subject, DB_SUBJECT);
		this.text = fitDb(text, DB_TEXT);
		this.contactToken = fitDb(contactToken, DB_CONTACT_TOKEN);
	}

	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_REQUEST_ID, requestId);
		values.put(DB_ROLE_NAME, roleName);
		values.put(DB_EMAIL_TO, emailTo);
		values.put(DB_EMAIL_FROM, emailFrom);
		values.put(DB_SUBJECT, subject);
		values.put(DB_TEXT, text);
		values.put(DB_CONTACT_TOKEN, contactToken);
		values.put(DB_CREATED_DATE, createdDate);
		return values;
	}
	public final int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRequestId() {
		return requestId;
	}
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getEmailTo() {
		return emailTo;
	}
	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}
	public String getEmailFrom() {
		return emailFrom;
	}
	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
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
	public String getContactToken() {
		return contactToken;
	}
	public void setContactToken(String contactToken) {
		this.contactToken = contactToken;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	@Override
	public String toString() {
		return "Contact [id=" + id + ", requestId=" + requestId + ", roleName="
				+ roleName + ", emailTo=" + emailTo + ", emailFrom="
				+ emailFrom + ", subject=" + subject + ", text.length=" + text.length()
				+ ", contactToken=" + contactToken + "]";
	}

}
