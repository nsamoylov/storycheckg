package com.nicksamoylov.storycheck.domain.users;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.nicksamoylov.storycheck.domain.test.Testgroup;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class Itester {
	private int id;
	private int userId;
	private String status;
	private Date createdDate;
	private Date modifiedDate;

	private static final String DB_TABLE = "Users_itesters";
	private static final String DB_ID_GEN_NAME = DB_TABLE+"_id";
	public static final String DB_ID = "id";
	public static final String DB_USER_ID = "user_id";
	public static final String DB_STATUS = "status";
	public static final String DB_CREATED_DATE="created_date";
	public static final String DB_MODIFIED_DATE="modified_date";

	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_STATUS, new Integer(64));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_USER_ID);
		DB_COLUMNS_INSERT.add(DB_STATUS);
	}
	private static final Set<String> DB_COLUMNS_UPDATE = new HashSet<String>();
	static {
		DB_COLUMNS_UPDATE.add(DB_ID);
		DB_COLUMNS_UPDATE.add(DB_MODIFIED_DATE);
		DB_COLUMNS_UPDATE.addAll(DB_COLUMNS_INSERT);
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
	private static final String SET_DB_COLUMNS_UPDATE;
	static {
		List<String> SET = new ArrayList<String>();
		for(String n: DB_COLUMNS_UPDATE){
			SET.add(n+"=:"+n);
		}
		SET_DB_COLUMNS_UPDATE = DisplayUtils.commaSeparated(SET);
	}

	public static final String SQL_INSERT = "insert into "	+ DB_TABLE
			+ " ("+DB_ID+","+DB_COLUMNS_INSERT_SORTED+")"
			+ " values(nextval(\'"+DB_ID_GEN_NAME+"\'), "+DB_COLUMNS_INSERT_WITH_PREF_SORTED+")";
	public static final String SQL_SELECT_ALL_NEW_ITESTERS(List<Integer> ids){
		return "select * from Users_itesters "+(ids.size()==0?"":" where ID not in ("+DisplayUtils.commaSeparatedSorted(ids)+")") 
			+" order by ID";
	}
	public static final String SQL_SELECT_BY_ID = "select * from " + DB_TABLE 
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_UPDATE_BY_ID = "update " + DB_TABLE +" set " + SET_DB_COLUMNS_UPDATE 
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_DELETE_BY_ID = "delete from " + DB_TABLE + " where "+DB_ID+"=:"+DB_ID;
	
	public Itester(int id, int userId, String status, Date createdDate,
			Date modifiedDate) {
		super();
		this.id = id;
		this.userId = userId;
		this.status = status;
		this.createdDate = createdDate;
		this.modifiedDate = modifiedDate;
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_USER_ID, userId);
		values.put(DB_STATUS, status);
		values.put(DB_MODIFIED_DATE, new Date());
		return values;
	}
	public final int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	@Override
	public String toString() {
		return "Itester [id=" + id + ", userId=" + userId + ", status="
				+ status + ", createdDate=" + createdDate + ", modifiedDate="
				+ modifiedDate + "]";
	}
	
}
