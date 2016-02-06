package com.nicksamoylov.storycheck.domain.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class Testgroup {
	private int id;
	private int writerId;
	@NotEmpty
    @Length(min=2, max=64)
	private String name;
	private Set<Integer> testersId = new HashSet<Integer>(); 
	private String text = "";
	private String size = "0";

	public static final String DB_TABLE = "testgroups";
	public static final String DB_ID_GEN_NAME = "testgroups_id";
	public static final String DB_ID = "id";
	public static final String DB_WRITER_ID = "writer_id";
	public static final String DB_NAME = "name";
	public static final String DB_MODIFIED_DATE="modified_date";
	public static final String DB_TEXT = Test.DB_TEST_MEMBERS_TEXT;
	public static final int DB_TEXT_MAX_LENGTH = 1100;
	
	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_NAME, new Integer(64));
	}

	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_WRITER_ID);
		DB_COLUMNS_INSERT.add(DB_NAME);
	}
	private static final Set<String> DB_COLUMNS_UPDATE = new HashSet<String>();
	static {
		DB_COLUMNS_UPDATE.add(DB_ID);
		DB_COLUMNS_UPDATE.add(DB_NAME);
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
	public static final String SQL_COUNT_BY_WRITER_ID = "select count (*) from " + DB_TABLE 
		    + " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID;
	public static final String SQL_UPDATE_BY_ID = "update " + DB_TABLE +" set " + SET_DB_COLUMNS_UPDATE 
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_ALL_BY_WRITER_ID_ORDER_BY_NAME= "select * from " + DB_TABLE 
		    + " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID +" order by " +DB_NAME;
	public static final String SQL_SELECT_BY_WRITER_ID_AND_ID = "select * from " + DB_TABLE 
			+ " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID +" and "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_BY_WRITER_ID_AND_NAME = "select * from " + DB_TABLE 
			+ " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID +" and "+DB_NAME+"=:"+DB_NAME;
	public static final String SQL_DELETE_BY_ID = "delete from " + DB_TABLE + " where "+DB_ID+"=:"+DB_ID;

	public static final String DB_TABLE_TESTGROUP_MEMBERS = "testgroup_members";
	public static final String DB_TESTGROUP_MEMBERS_TESTER_ID = "tester_id";
	public static final String DB_TESTGROUP_MEMBERS_TESTGROUP_ID = "testgroup_id";

	public static final String SQL_SELECT_BY_TEST_ID_ORDER_BY_NAME= "select * from " + DB_TABLE 
		    + " where "+DB_ID+" in("+Test.SQL_SELECT_TESTGROUP_IDS_BY_TEST_ID +") order by " +DB_NAME;
	private static final String SQL_SELECT_TESTGROUP_IDS_BY_TESTER_ID = "select "+DB_TESTGROUP_MEMBERS_TESTGROUP_ID+" from " + DB_TABLE_TESTGROUP_MEMBERS 
		    + " where "+DB_TESTGROUP_MEMBERS_TESTER_ID+"=:"+DB_TESTGROUP_MEMBERS_TESTER_ID;
	public static final String SQL_SELECT_BY_TESTER_ID_ORDER_BY_NAME= "select * from " + DB_TABLE 
		    + " where "+DB_ID+" in("+SQL_SELECT_TESTGROUP_IDS_BY_TESTER_ID +") order by " +DB_NAME;

	public static final String SQL_INSERT_MEMBER = "insert into "+ DB_TABLE_TESTGROUP_MEMBERS
			+ " ("+DB_TESTGROUP_MEMBERS_TESTGROUP_ID+","+DB_TESTGROUP_MEMBERS_TESTER_ID+")"
			+ " values(:"+DB_TESTGROUP_MEMBERS_TESTGROUP_ID+", :"+DB_TESTGROUP_MEMBERS_TESTER_ID+")";
	public static final String SQL_SELECT_TESTER_IDS_BY_TESTGROUP_ID = "select "+DB_TESTGROUP_MEMBERS_TESTER_ID+" from " + DB_TABLE_TESTGROUP_MEMBERS 
		    + " where "+DB_TESTGROUP_MEMBERS_TESTGROUP_ID+"=:"+DB_TESTGROUP_MEMBERS_TESTGROUP_ID;
	public static final String SQL_DELETE_ALL_MEMBERSHIP = "delete from " + DB_TABLE_TESTGROUP_MEMBERS 
			+ " where "+DB_TESTGROUP_MEMBERS_TESTGROUP_ID+"=:"+DB_TESTGROUP_MEMBERS_TESTGROUP_ID;
	public static final String SQL_DELETE_ANY_MEMBERSHIP = "delete from " + DB_TABLE_TESTGROUP_MEMBERS 
			+ " where "+DB_TESTGROUP_MEMBERS_TESTER_ID+"=:"+DB_TESTGROUP_MEMBERS_TESTER_ID;
	public static final String sqlDeleteGroupMembersByIds(Collection ids){
		return "delete from " + DB_TABLE_TESTGROUP_MEMBERS + " where "+DB_TESTGROUP_MEMBERS_TESTGROUP_ID+"=:"+DB_TESTGROUP_MEMBERS_TESTGROUP_ID
				+" and "+DB_TESTGROUP_MEMBERS_TESTER_ID	+ " in("+DisplayUtils.commaSeparatedSorted(ids)+")";
	}
	
	public Testgroup() {
		super();
	}
	public Testgroup(int id, int writerId, String name) {
		super();
		this.id = id;
		this.writerId = writerId;
		this.name = fitDb(name, DB_NAME);
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_WRITER_ID, writerId);
		values.put(DB_NAME, name);
		values.put(DB_MODIFIED_DATE, new Date());
		return values;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getWriterId() {
		return writerId;
	}
	public void setWriterId(int writerId) {
		this.writerId = writerId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = fitDb(name, DB_NAME);
	}
    public Set<Integer> getTestersId() {
		return testersId;
	}
	public void setTestersId(Set<Integer> testersId) {
		this.testersId = testersId;
	}
	public void addTesterId(Integer testerId) {
		this.testersId.add(testerId);
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = (text==null?"":text.trim());
	}
	public boolean isTextEmpty(){
		return this.text == null || this.text.length() == 0;
	}
	@Override
	public String toString() {
		return "Testgroup [id=" + id + ", writerId=" + writerId + ", name="
				+ name + ", text.length()=" + (text==null?null:text.length()) + "]";
	}

	
}
