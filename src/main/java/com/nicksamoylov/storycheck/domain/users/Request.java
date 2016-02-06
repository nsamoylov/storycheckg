package com.nicksamoylov.storycheck.domain.users;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.nicksamoylov.storycheck.utils.DateUtils;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class Request {
	private static final Logging log = Logging.getLog("Request");
	private int id;
	private int roleId;
	@NotEmpty
    @Length(min=10)
	private String title;
	private String request;
	private String lang;
	private Date createdDate;
	private int foundPublished;
	private int viewedPublished;
	private int contactsPublished;
	private Date modifiedDate;
	private int foundModified;
	private int viewedModified;
	private int contactsModified = 0;
	private Role role = new Role();
	private User user = new User();
	private String emailText;

	private String DB_TABLE = "";
	private String DB_ID_GEN_NAME = "";
	public String DB_ROLE_ID = "";
	public static final String DB_ID = "id";
	public static final String DB_TITLE = "title";
	public static final String DB_REQUEST = "request";
	public static final String DB_LANG = "lang";
	public static final String DB_CREATED_DATE="created_date";
	public static final String DB_MODIFIED_DATE="modified_date";
	public static final String DB_FOUND_PUBLISHED="found_published";
	public static final String DB_VIEWED_PUBLISHED="viewed_published";
	public static final String DB_FOUND_MODIFIED="found_modified";
	public static final String DB_VIEWED_MODIFIED="viewed_modified";
	public static final int DB_REQUEST_MIN_LENGTH = 40;
	public static final int DB_REQUEST_MAX_LENGTH = 1100;

	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_TITLE, new Integer(100));
		DB_COLUMN_SIZES.put(DB_REQUEST, new Integer(1100));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v, DB_COLUMN_SIZES.get(n));
	}
	private Set<String> DB_COLUMNS_INSERT;
	private Set<String> DB_COLUMNS_UPDATE;
	private Set<String> DB_COUNTERS_UPDATE;
	private String DB_COLUMNS_INSERT_WITH_PREF_SORTED;
	private List<String> DB_COLUMNS_INSERT_WITH_PREF;
	private String DB_COLUMNS_INSERT_SORTED;
	private String SET_DB_COLUMNS_UPDATE;
	private String SET_DB_COUNTERS_UPDATE;
	private String SQL_INSERT;
	private String SQL_SELECT_BY_ID;
	private String SQL_SELECT_BY_ROLE_ID_ORDER_BY_ID;
	private String SQL_SELECT_BY_ROLE_ID_AND_LANGUAGE_ORDER_BY_ID;
	private String SQL_UPDATE_BY_ID;
	private String SQL_UPDATE_COUNTERS_BY_ID;
	private String SQL_SELECT_FROM_WHERE;
	private String SQL_ROLE_ID_NOT;
	private String SQL_DELETE_BY_ROLE_ID;
	private String SQL_DELETE_BY_ID;
	public static String SQL_LANG_EQUALS = " "+DB_LANG+"=";
	
	public String sqlInsert(){
		return String.format(SQL_INSERT, DB_TABLE, DB_ID_GEN_NAME);
	}
	public String sqlSelectById(){
		return String.format(SQL_SELECT_BY_ID, DB_TABLE);
	}
	public String sqlSelectByRoleIdOrderById(){
		return String.format(SQL_SELECT_BY_ROLE_ID_ORDER_BY_ID, new Object[]{DB_TABLE, DB_ROLE_ID, DB_ROLE_ID});
	}
	public String sqlSelectByRoleIdAndLanguageOrderById(){
		return String.format(SQL_SELECT_BY_ROLE_ID_AND_LANGUAGE_ORDER_BY_ID, new Object[]{DB_TABLE, DB_ROLE_ID, DB_ROLE_ID});
	}
	public String sqlUpdateById(){
		return String.format(SQL_UPDATE_BY_ID, DB_TABLE);
	}
	public String sqlUpdateCountersById(){
		return String.format(SQL_UPDATE_COUNTERS_BY_ID, DB_TABLE);
	}
	public String sqlSelectFromWhere(){
		return String.format(SQL_SELECT_FROM_WHERE, new Object[]{DB_TABLE});
	}
	public String sqlRoleIdNot(){
		return String.format(SQL_ROLE_ID_NOT, new Object[]{DB_ROLE_ID});
	}
	public String sqlTitleAndRequestLike(String keyword){
		return " lower(title) like lower('%"+keyword+"%') or lower(request) like lower('%"+keyword+"%') ";
	}
	
	public String sqlDeleteByRoleId(){
		return String.format(SQL_DELETE_BY_ROLE_ID, new Object[]{DB_TABLE, DB_ROLE_ID, DB_ROLE_ID});
	}
	public String sqlDeleteById(){
		return String.format(SQL_DELETE_BY_ID, DB_TABLE);
	}
	public Request() {
	}
	public Request(String roleShort) {
		super();
		setRoleName(roleShort);
	}
	public Request(Role role) {
		super();
		setRole(role);
	}
	public Request(int id, String title, String request) {
		super();
		this.id = id;
		this.title = fitDb(title, DB_TITLE);
		this.request = fitDb(request, DB_REQUEST);
	}
	public Request(int id, String title, String request, Date createdDate, int foundPublished, int viewedPublished, 
			Date modifiedDate, int foundModified, int viewedModified, int roleId) {
		super();
		this.id = id;
		this.roleId = roleId;
		this.title = fitDb(title, DB_TITLE);
		this.request = fitDb(request, DB_REQUEST);
		this.createdDate = createdDate;
		this.foundPublished = foundPublished;
		this.viewedPublished = viewedPublished;
		this.modifiedDate = modifiedDate;
		this.foundModified = foundModified;
		this.viewedModified = viewedModified;
	}
    private void setTableAndSequencer(final String roleName){
		switch (roleName) {
		case Role.ROLE_WRITER:
			DB_TABLE = "Requests_writers";
			DB_ROLE_ID = "writer_id";
			break;
		case Role.ROLE_TESTER:
			DB_TABLE = "Requests_itesters";
			DB_ROLE_ID = "itester_id";
			break;
		case Role.ROLE_EDITOR:
			DB_TABLE = "Requests_editors";
			DB_ROLE_ID = "editor_id";
			break;
		case Role.ROLE_TRANSLATOR:
			DB_TABLE = "Requests_translators";
			DB_ROLE_ID = "translator_id";
			break;
		case Role.ROLE_PROOFREADER:
			DB_TABLE = "Requests_proofreaders";
			DB_ROLE_ID = "proofreader_id";
			break;
		default:
			throw new RuntimeException("Add role "+roleName+" to Request().");
		}
		DB_ID_GEN_NAME = DB_TABLE+"_id";
		
		DB_COLUMNS_INSERT = new HashSet<String>();
		DB_COLUMNS_INSERT.add(DB_ROLE_ID);
		DB_COLUMNS_INSERT.add(DB_TITLE);
		DB_COLUMNS_INSERT.add(DB_REQUEST);
		DB_COLUMNS_INSERT.add(DB_LANG);
		DB_COLUMNS_INSERT_SORTED = DisplayUtils.commaSeparatedSorted(DB_COLUMNS_INSERT);;		
		DB_COLUMNS_INSERT_WITH_PREF = new ArrayList<String>();
		for(String n: DB_COLUMNS_INSERT){
			DB_COLUMNS_INSERT_WITH_PREF.add(":"+n);
		}
		DB_COLUMNS_INSERT_WITH_PREF_SORTED = DisplayUtils.commaSeparatedSorted(DB_COLUMNS_INSERT_WITH_PREF);

		DB_COLUMNS_UPDATE = new HashSet<String>();
		DB_COLUMNS_UPDATE.add(DB_ID);
		DB_COLUMNS_UPDATE.add(DB_MODIFIED_DATE);
		DB_COLUMNS_UPDATE.addAll(DB_COLUMNS_INSERT);		
        List<String> SET = new ArrayList<String>();
		for(String n: DB_COLUMNS_UPDATE){
			SET.add(n+"=:"+n);
		}
		SET_DB_COLUMNS_UPDATE = DisplayUtils.commaSeparated(SET);

		DB_COUNTERS_UPDATE = new HashSet<String>();
		DB_COUNTERS_UPDATE.add(DB_VIEWED_MODIFIED);
		DB_COUNTERS_UPDATE.add(DB_FOUND_MODIFIED);
		DB_COUNTERS_UPDATE.add(DB_VIEWED_PUBLISHED);
		DB_COUNTERS_UPDATE.add(DB_FOUND_PUBLISHED);
        SET = new ArrayList<String>();
		for(String n: DB_COUNTERS_UPDATE){
			SET.add(n+"=:"+n);
		}
		SET_DB_COUNTERS_UPDATE = DisplayUtils.commaSeparated(SET);

		SQL_INSERT = "insert into %s" 
				+ " ("+DB_ID+", "+DB_COLUMNS_INSERT_SORTED+")"
				+ " values(nextval(\'%s\'), "+DB_COLUMNS_INSERT_WITH_PREF_SORTED+")";
		SQL_SELECT_BY_ID = "select * from %s"  
				+ " where "+DB_ID+"=:"+DB_ID;
		SQL_SELECT_BY_ROLE_ID_ORDER_BY_ID = "select * from %s"  
			    + " where %s=:%s order by "+DB_ID;
		SQL_SELECT_BY_ROLE_ID_AND_LANGUAGE_ORDER_BY_ID = "select * from %s"  
			    + " where %s=:%s and "+DB_LANG+"=:"+DB_LANG+" order by "+DB_ID;
		SQL_UPDATE_BY_ID = "update %s set " + SET_DB_COLUMNS_UPDATE 
				+ " where "+DB_ID+"=:"+DB_ID;
		SQL_UPDATE_COUNTERS_BY_ID = "update %s set " + SET_DB_COUNTERS_UPDATE 
				+ " where "+DB_ID+"=:"+DB_ID;
		
		SQL_SELECT_FROM_WHERE = "select * from %s where ";
		SQL_ROLE_ID_NOT = " %s != ";
		
		SQL_DELETE_BY_ROLE_ID = "delete from %s"  
			    + " where %s=:%s";
		SQL_DELETE_BY_ID = "delete from %s where "+DB_ID+"=:"+DB_ID;
    }
	public Map<String, Object> allValuesMap() {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_ROLE_ID, roleId);
		values.put(DB_TITLE, title);
		values.put(DB_REQUEST, request);
		values.put(DB_LANG, lang);
		values.put(DB_MODIFIED_DATE, new Date());
		values.put(DB_FOUND_PUBLISHED, foundPublished);
		values.put(DB_VIEWED_PUBLISHED, viewedPublished);
		values.put(DB_FOUND_MODIFIED, foundModified);
		values.put(DB_VIEWED_MODIFIED, viewedModified);
		return values;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.roleId = role.getId();
		this.role = role;
		this.user = role.getUser();
		setTableAndSequencer(role.getRoleName());
	}
	public String getRoleName() {
		return role.getRoleName();
	}
	public void setRoleName(String roleName) {
		this.role = new Role(roleName);
		setTableAndSequencer(Role.getRoleName(roleName));
	}
	public User getUser() {
		return user;
	}
	public String getEmail() {
		return user.getUsername();
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = fitDb(title, DB_TITLE);
	}
	public boolean isTitleEmpty(){
		return this.title == null || this.title.length() == 0;
	}
	public String getRequestText() {
		return request;
	}
	public void setRequestText(String request) {
		this.request = fitDb(request, DB_REQUEST);
	}
	public String getRequestShort() {
		return StringUtils.substringWithDots(request, 197);
	}
	public boolean isRequestEmpty(){
		return this.request == null || this.request.length() == 0;
	}
	public String getCreatedDateString() {
		return DateUtils.formatDate(createdDate);
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public int getFoundPublished() {
		return foundPublished;
	}
	public void setFoundPublished(int foundPublished) {
		this.foundPublished = foundPublished;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public String getModifiedDateString() {
		return DateUtils.formatDate(modifiedDate);
	}
	public int getFoundModified() {
		return foundModified;
	}
	public void setFoundModified(int foundModified) {
		this.foundModified = foundModified;
	}
	public int getViewedPublished() {
		return viewedPublished;
	}
	public void setViewedPublished(int viewedPublished) {
		this.viewedPublished = viewedPublished;
	}
	public int getViewedModified() {
		return viewedModified;
	}
	public void setViewedModified(int viewedModified) {
		this.viewedModified = viewedModified;
	}
	public int getContactsPublished() {
		return contactsPublished;
	}
	public void setContactsPublished(int contactsPublished) {
		this.contactsPublished = contactsPublished;
	}
	public int getContactsModified() {
		return contactsModified;
	}
	public void setContactsModified(int contactsModified) {
		this.contactsModified = contactsModified;
	}
	public String getEmailText() {
		return emailText;
	}
	public void setEmailText(String emailText) {
		this.emailText = emailText;
	}
	public boolean isEmailTextEmpty(){
		return this.emailText == null || this.emailText.length() == 0;
	}
	@Override
	public String toString() {
		return "Request [id=" + id + ", roleId=" + roleId + ", title=" + title
				+ ", request.length=" + (request==null?"0":request.length()) + ", createdDate=" + createdDate
				+ ", foundPublished=" + foundPublished + ", modifiedDate="
				+ modifiedDate + ", foundModified=" + foundModified
				+ ", contactsPublished=" + contactsPublished + ", contactsModified=" + contactsModified 
				+ ", role=" + (role==null?"null":role.getRoleName()) + ", user=" + (user==null?"null":user.getUsername()) + "]";
	}

}
