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
import com.nicksamoylov.storycheck.utils.StringUtils;

public class BlogComment {
	private int id;
	private int blogId;
	private int userId;
	private String userName;
	private String userEmail;
	@NotEmpty
    @Length(min=2)
	private String title;
	private String text;
	private String textEdit;
	private Date createdDate;
	private int foundPublished;
	private int viewedPublished;
	private Date modifiedDate;
	private int foundModified;
	private int viewedModified;
	private boolean editable;
	private List<BlogCommentEdit> edits = new ArrayList<BlogCommentEdit>();

	private static final String DB_TABLE = "blogs_comments";
	private static final String DB_ID_GEN_NAME = "blogs_comments_id";
	public static final String DB_ID = "id";
	public static final String DB_BLOG_ID = "blog_id";
	public static final String DB_USER_ID = "user_id";
	public static final String DB_USER_NAME = "name";
	public static final String DB_USER_EMAIL = "email";
	public static final String DB_TITLE = "title";
	public static final String DB_TEXT = "text";
	public static final String DB_CREATED_DATE="created_date";
	public static final String DB_FOUND_PUBLISHED="found_published";
	public static final String DB_VIEWED_PUBLISHED="viewed_published";
	public static final String DB_MODIFIED_DATE="modified_date";
	public static final String DB_FOUND_MODIFIED="found_modified";
	public static final String DB_VIEWED_MODIFIED="viewed_modified";
	public static final int DB_TEXT_MIN_LENGTH = 2;
	public static final int DB_TEXT_MAX_LENGTH = 1100;
	public static String IDS = "";

	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_USER_NAME, new Integer(256));
		DB_COLUMN_SIZES.put(DB_USER_EMAIL, new Integer(256));
		DB_COLUMN_SIZES.put(DB_TITLE, new Integer(100));
		DB_COLUMN_SIZES.put(DB_TEXT, new Integer(1100));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_BLOG_ID);
		DB_COLUMNS_INSERT.add(DB_USER_ID);
		DB_COLUMNS_INSERT.add(DB_USER_NAME);
		DB_COLUMNS_INSERT.add(DB_USER_EMAIL);
		DB_COLUMNS_INSERT.add(DB_TITLE);
		DB_COLUMNS_INSERT.add(DB_TEXT);
	}
	private static final Set<String> DB_COLUMNS_UPDATE = new HashSet<String>();
	static {
		DB_COLUMNS_UPDATE.add(DB_ID);
		DB_COLUMNS_UPDATE.add(DB_TEXT);
		DB_COLUMNS_UPDATE.add(DB_MODIFIED_DATE);
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
	public static String SQL_SELECT_BY_ID = "select * from "+DB_TABLE
			+" where "+DB_ID+"=:"+DB_ID;
	public static String SQL_SELECT_BY_BLOG_ID = "select * from "+DB_TABLE
			+" where "+DB_BLOG_ID+"=:"+DB_BLOG_ID;
	public static String SQL_SELECT_BY_TEXT = "select * from "+DB_TABLE
			+" where "+DB_TEXT+"=:"+DB_TEXT;
	public static String SQL_COUNT_BY_BLOG_ID = "select count(*) from "+DB_TABLE
			+" where "+DB_BLOG_ID+"=:"+DB_BLOG_ID;
	public static String SQL_COUNT_BY_BLOG_ID_SINCE_DATE = "select count(*) from "+DB_TABLE
			+" where "+DB_BLOG_ID+"=:"+DB_BLOG_ID
			+" and "+DB_CREATED_DATE+" > :"+DB_CREATED_DATE;
	public static String SQL_UPDATE_MODIFIED_DATE_BY_ID = "update "+DB_TABLE
			+" set "+DB_MODIFIED_DATE+"=:"+DB_MODIFIED_DATE
			+", "+DB_FOUND_MODIFIED+"=0, "+DB_VIEWED_MODIFIED+"=0"
			+" where "+DB_ID+"=:"+DB_ID;
	public static String SQL_UPDATE_COUNT_FOUND_BY_ID = "update "+DB_TABLE
			+" set "+DB_FOUND_PUBLISHED+"=:"+DB_FOUND_PUBLISHED
			+", "+DB_FOUND_MODIFIED+"=:"+DB_FOUND_MODIFIED
			+" where "+DB_ID+"=:"+DB_ID;
	public static String SQL_UPDATE_COUNT_VIEWED_BY_ID = "update "+DB_TABLE
			+" set "+DB_VIEWED_PUBLISHED+"=:"+DB_VIEWED_PUBLISHED
			+", "+DB_VIEWED_MODIFIED+"=:"+DB_VIEWED_MODIFIED
			+" where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_BY_KEY(String key){ 
		return "select * from " + DB_TABLE
			+ " where lower("+DB_USER_NAME+") like lower('%"+key+"%') or lower("+DB_TITLE
			+") like lower('%"+key+"%') or lower("+DB_TEXT+") like lower('%"+key+"%')";
	}
	public static final String SQL_SELECT_BLOG_IDS_BY_COMMENT_IDS(String ids){ 
		return "select "+DB_BLOG_ID+" from " + DB_TABLE 
				+ " where "+DB_ID+" in("+ids+")";
	}
	
	public BlogComment(){		
	}
	public BlogComment(int blogId, int userId, String userName, String userEmail, String title, String text) {
		super();
		this.blogId = blogId;
		this.userId = userId;
		this.userName = fitDb(userName, DB_USER_NAME);
		this.userEmail = fitDb(userEmail, DB_USER_EMAIL);
		this.title = fitDb(title, DB_TITLE);
		this.text = fitDb(text, DB_TEXT);
	}
	public BlogComment(int id, int blogId, int userId, String userName,
			String userEmail, String title, String text, Date createdDate,
			int foundPublished, int viewedPublished, Date modifiedDate,
			int foundModified, int viewedModified) {
		super();
		this.id = id;
		this.blogId = blogId;
		this.userId = userId;
		this.userName = userName;
		this.userEmail = userEmail;
		this.title = title;
		this.text = text;
		this.createdDate = createdDate;
		this.foundPublished = foundPublished;
		this.viewedPublished = viewedPublished;
		this.modifiedDate = modifiedDate;
		this.foundModified = foundModified;
		this.viewedModified = viewedModified;
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_BLOG_ID, blogId);
		values.put(DB_USER_ID, userId);
		values.put(DB_USER_NAME, userName);
		values.put(DB_USER_EMAIL, userEmail);
		values.put(DB_TITLE, title);
		values.put(DB_TEXT, text);
		values.put(DB_CREATED_DATE, createdDate);
		values.put(DB_FOUND_PUBLISHED, foundPublished);
		values.put(DB_VIEWED_PUBLISHED, viewedPublished);
		values.put(DB_MODIFIED_DATE, new Date());
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
	public int getBlogId() {
		return blogId;
	}
	public void setBlogId(int blogId) {
		this.blogId = blogId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public void setUser(User user){
		this.userId = user.getId();
		this.userEmail = fitDb(user.getUsername(), DB_USER_EMAIL);
		this.userName = fitDb(user.getFirstname()+" "+user.getLastname(), DB_USER_NAME);
	}
	public boolean isEditable() {
		return editable;
	}
	public void setIsEditable(User user) {
		this.editable = this.userId==user.getId();
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = fitDb(userName, DB_USER_NAME);
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = fitDb(userEmail, DB_USER_EMAIL);
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
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = fitDb(text, DB_TEXT);
	}
	public String getTextEdit() {
		return textEdit;
	}
	public void setTextEdit(String text) {
		this.textEdit = fitDb(text, DB_TEXT);
	}
	public boolean isTextEmpty(){
		return this.textEdit == null || this.textEdit.trim().length() == 0;
	}
	public String getCreatedDateString() {
		return DateUtils.formatDate(createdDate);
	}
	public int getFoundPublished() {
		return foundPublished;
	}
	public void setFoundPublished(int foundPublished) {
		this.foundPublished = foundPublished;
	}
	public int getViewedPublished() {
		return viewedPublished;
	}
	public void setViewedPublished(int viewedPublished) {
		this.viewedPublished = viewedPublished;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public int getFoundModified() {
		return foundModified;
	}
	public void setFoundModified(int foundModified) {
		this.foundModified = foundModified;
	}
	public int getViewedModified() {
		return viewedModified;
	}
	public void setViewedModified(int viewedModified) {
		this.viewedModified = viewedModified;
	}
	public List<BlogCommentEdit> getEdits() {
		return edits;
	}
	public void setEdits(List<BlogCommentEdit> edits) {
		this.edits = edits;
	}
	@Override
	public String toString() {
		return "BlogComment [id=" + id + ", blogId=" + blogId + ", userId="
				+ userId + ", userName=" + userName + ", userEmail="
				+ userEmail + ", title=" + title + ", text.length=" + (text==null?0:text.length())
				+ ", createdDate=" + createdDate + ", foundPublished="
				+ foundPublished + ", viewedPublished=" + viewedPublished
				+ ", modifiedDate=" + modifiedDate + ", foundModified="
				+ foundModified + ", viewedModified=" + viewedModified + "]";
	}

}
