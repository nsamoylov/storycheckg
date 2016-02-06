package com.nicksamoylov.storycheck.domain.users;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nicksamoylov.storycheck.utils.DateUtils;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class BlogCommentEdit {
	private int id;
	private int commentId;
	private String text;
	private Date createdDate;

	private static final String DB_TABLE = "blogs_comments_edits";
	private static final String DB_ID_GEN_NAME = "blogs_comments_edits_id";
	public static final String DB_ID = "id";
	public static final String DB_COMMENT_ID = "comment_id";
	public static final String DB_TEXT = "text";
	public static final String DB_CREATED_DATE="created_date";

	public static final int DB_TEXT_MIN_LENGTH = 2;
	public static final int DB_TEXT_MAX_LENGTH = 1100;

	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_TEXT, new Integer(1100));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_COMMENT_ID);
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
	public static String SQL_SELECT_BY_COMMENT_ID = "select * from "+DB_TABLE
			+" where "+DB_COMMENT_ID+"=:"+DB_COMMENT_ID + " order by "+DB_ID;
	public static String SQL_SELECT_BY_TEXT = "select * from "+DB_TABLE
			+" where "+DB_TEXT+"=:"+DB_TEXT;
	public static final String SQL_SELECT_COMMENT_ID_BY_KEY(String key){ 
		return "select "+DB_COMMENT_ID+" from " + DB_TABLE 
				+ " where lower("+DB_TEXT+") like lower('%"+key+"%')";
	}

	public BlogCommentEdit(BlogComment comment) {
		this.commentId = comment.getId();
		this.text = comment.getTextEdit();		
	}
	public BlogCommentEdit(int id, int commentId, String text, Date createdDate) {
		super();
		this.id = id;
		this.commentId = commentId;
		this.text = text;
		this.createdDate = createdDate;
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_COMMENT_ID, commentId);
		values.put(DB_TEXT, text);
		values.put(DB_CREATED_DATE, createdDate);
		return values;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCommentId() {
		return commentId;
	}
	public void setCommentId(int commentId) {
		this.commentId = commentId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = fitDb(text, DB_TEXT);
	}
	public String getCreatedDateString() {
		return DateUtils.formatDate(createdDate);
	}
	@Override
	public String toString() {
		return "BlogCommentEdit [id=" + id + ", commentId=" + commentId
				+ ", text.length=" + (text==null?"0":text.length()) + ", createdDate=" + createdDate + "]";
	}

}
