package com.nicksamoylov.storycheck.domain.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class Role {
	private static final Logging log = Logging.getLog("Role");
	public static final String ROLE_ADMIN="role.admin"; 
	public static final String ROLE_WRITER="role.writer";
	public static final String ROLE_TESTER="role.tester";
	public static final String ROLE_PROOFREADER="role.proofreader";
	public static final String ROLE_EDITOR="role.editor";
	public static final String ROLE_TRANSLATOR="role.translator";
	private static final List<String> ROLE_OPTIONS = new ArrayList<String>();
	static {
		ROLE_OPTIONS.add(ROLE_WRITER);
		ROLE_OPTIONS.add(ROLE_TESTER);
		ROLE_OPTIONS.add(ROLE_PROOFREADER);
		ROLE_OPTIONS.add(ROLE_EDITOR);
		ROLE_OPTIONS.add(ROLE_TRANSLATOR);
	}
	private static final Map<String, String> ROLE_SHORTS = new HashMap<String, String>();
	static {
		ROLE_SHORTS.put(ROLE_WRITER, "w");
		ROLE_SHORTS.put(ROLE_TESTER, "i");
		ROLE_SHORTS.put(ROLE_PROOFREADER, "p");
		ROLE_SHORTS.put(ROLE_EDITOR, "e");
		ROLE_SHORTS.put(ROLE_TRANSLATOR, "t");
	}
	private static final Map<String, String> ROLE_LONGS = new HashMap<String, String>();
	static {
		ROLE_LONGS.put("w", ROLE_WRITER);
		ROLE_LONGS.put("i", ROLE_TESTER);
		ROLE_LONGS.put("p", ROLE_PROOFREADER);
		ROLE_LONGS.put("e", ROLE_EDITOR);
		ROLE_LONGS.put("t", ROLE_TRANSLATOR);
	}
	private static final Map<String, String> ROLE_ASROLE = new HashMap<String, String>();
	static {
		ROLE_ASROLE.put("w", "requests.aswriter");
		ROLE_ASROLE.put("i", "requests.astester");
		ROLE_ASROLE.put("p", "requests.asproofreader");
		ROLE_ASROLE.put("e", "requests.aseditor");
		ROLE_ASROLE.put("t", "requests.astranslator");
	}
	private static final Map<String, String[]> ROLE_KEYWORDS = new HashMap<String, String[]>();
	static {
		ROLE_KEYWORDS.put("w", new String[]{"keyword.request.proofread", "keyword.request.edit","keyword.request.translate"});
		ROLE_KEYWORDS.put("p", new String[]{"keyword.request.proofread"});
		ROLE_KEYWORDS.put("e", new String[]{"keyword.request.edit"});
		ROLE_KEYWORDS.put("t", new String[]{"keyword.request.translate"});
	}
	public static final String DB_TABLE_ROLES = "user_roles";
	public static final String DB_ROLES_USER_ID = "user_id";
	public static final String DB_ROLES_ROLE = "role";
	public static final String SQL_INSERT_ROLE = "insert into " + DB_TABLE_ROLES
			+ " ("+DB_ROLES_USER_ID+","+DB_ROLES_ROLE+")"
			+ " values(:"+DB_ROLES_USER_ID+",:"+DB_ROLES_ROLE+")";
	public static final String SQL_SELECT_ROLES_BY_USER_ID = "select role from " + DB_TABLE_ROLES 
		    + " where "+DB_ROLES_USER_ID+"=:"+DB_ROLES_USER_ID;
	public static final String sqlDeleteRoles(Collection roles){
		return "delete from " + DB_TABLE_ROLES + " where "+DB_ROLES_USER_ID+"=:"+DB_ROLES_USER_ID
				+" and "+DB_ROLES_ROLE	+ " in("+DisplayUtils.commaSeparatedSql(roles)+")";
	}

	private int id;
	private int userId;
	private String roleName;
	private String status;
	private User user = new User();

	private String DB_TABLE = "";
	private String DB_ID_GEN_NAME = "";
	public static final String DB_ID = "id";
	public static final String DB_USER_ID = "user_id";
	public static final String DB_STATUS = "status";
	public static final String DB_MODIFIED_DATE="modified_date";

	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_STATUS, new Integer(20));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v, DB_COLUMN_SIZES.get(n));
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

	private static String SQL_INSERT = "insert into %s" 
			+ " ("+DB_ID+","+DB_COLUMNS_INSERT_SORTED+")"
			+ " values(nextval(\'%s\'), "+DB_COLUMNS_INSERT_WITH_PREF_SORTED+")";
	private static String SQL_SELECT_ID_BY_USER_ID = "select "+DB_ID+" from %s"  
		    + " where "+DB_USER_ID+"=:"+DB_USER_ID;
	private static String SQL_SELECT_BY_USER_ID = "select * from %s"  
		    + " where "+DB_USER_ID+"=:"+DB_USER_ID;
	private static String SQL_SELECT_BY_ID = "select * from %s" 
			+ " where "+DB_ID+"=:"+DB_ID;
	private static String SQL_UPDATE_BY_ID = "update %s set " + SET_DB_COLUMNS_UPDATE 
			+ " where "+DB_ID+"=:"+DB_ID;
	private static String SQL_DELETE_BY_USER_ID = "delete from %s where "+DB_USER_ID+"=:"+DB_USER_ID;
	
	public String sqlInsert(){
		return String.format(SQL_INSERT, DB_TABLE, DB_ID_GEN_NAME);
	}
	public String sqlSelectIdByUserId(){
		return String.format(SQL_SELECT_ID_BY_USER_ID, DB_TABLE);
	}
	public String sqlSelectByUserId(){
		return String.format(SQL_SELECT_BY_USER_ID, DB_TABLE);
	}
	public String sqlSelectById(){
		return String.format(SQL_SELECT_BY_ID, DB_TABLE);
	}
	public String sqlUpdateById(){
		return String.format(SQL_UPDATE_BY_ID, DB_TABLE);
	}
	public String sqlDeleteByUserId(){
		return String.format(SQL_DELETE_BY_USER_ID, DB_TABLE);
	}

	public Role() {
	};

	public Role(int id, int userId, String status) {
		super();
		this.id = id;
		this.userId = userId;
		this.status = status;
	}
	public Role(final String roleShortOrLong) {
		super();
		setRoleName(roleShortOrLong);
	}
	public Role(final String roleShortOrLong, User user) {
		super();
		setRoleName(roleShortOrLong);
		this.user = user;
		this.userId = user.getId();
	}
    private void setTableAndSequencer(){
		switch (roleName) {
		case ROLE_WRITER:
			DB_TABLE = "Users_writers";
			break;
		case ROLE_EDITOR:
			DB_TABLE = "Users_editors";
			break;
		case ROLE_TESTER:
			DB_TABLE = "Users_itesters";
			break;
		case ROLE_TRANSLATOR:
			DB_TABLE = "Users_translators";
			break;
		case ROLE_PROOFREADER:
			DB_TABLE = "Users_proofreaders";
			break;
		default:
			throw new RuntimeException("Add role "+roleName+" to Role().");
		}
		DB_ID_GEN_NAME = DB_TABLE+"_id";
    }

	public Map<String, Object> allValuesMap() {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_USER_ID, userId);
		values.put(DB_STATUS, status);
		values.put(DB_MODIFIED_DATE, new Date());
		return values;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String shortOrLong) {
		this.roleName = getRoleName(shortOrLong);
		setTableAndSequencer();
	}
	public static String getRoleName(String roleShort) {
		if(ROLE_LONGS.keySet().contains(roleShort)){
			return ROLE_LONGS.get(roleShort);
		}
		return roleShort;
	}
	public static String getAsRole(String roleShort) {
		return ROLE_ASROLE.get(roleShort);
	}
	public static boolean roleExists(String role) {
		return ROLE_LONGS.keySet().contains(role) || ROLE_OPTIONS.contains(role);
	}
	public static String[] getRoleKeywords(String roleShort) {
		return ROLE_KEYWORDS.get(roleShort);
	}
	public String getShort() {
		return ROLE_SHORTS.get(this.roleName);
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getEmail() {
		return user.getUsername();
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = fitDb(status, DB_STATUS);
	}
	public static List<String> getRoleOptions(){
		return ROLE_OPTIONS;
	}
	@Override
	public String toString() {
		return "Role [table="+DB_TABLE+", id=" + id + ", user_id=" + userId  + ", userName=" + (user==null?"null":user.getUsername()) + ", status=" + status + "]";
	}
	

}
