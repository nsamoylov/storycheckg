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
import org.hibernate.validator.constraints.NotEmpty;

import com.nicksamoylov.storycheck.domain.test.Testgroup;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.StringUtils;

public class Tester {
	private int id;
	private int writerId;
	@NotEmpty
	@Email
	private String email;
	private String firstname;
	private String lastname;
	private String street;
	private String city;
	private String state;
	private String zipcode;
	private String country;
	private String language;
	private int itesterId = 0;
	private List<Request> requests = new ArrayList<Request>();

	private static final String DB_TABLE = "testers";
	private static final String DB_ID_GEN_NAME = "testers_id";
	public static final String DB_ID = "id";
	public static final String DB_WRITER_ID = "writer_id";
	public static final String DB_EMAIL = "email";
	public static final String DB_FIRSTNAME = "firstname";
	public static final String DB_LASTNAME = "lastname";
	public static final String DB_STREET = "street";
	public static final String DB_CITY = "city";
	public static final String DB_STATE = "state";
	public static final String DB_ZIPCODE = "zipcode";
	public static final String DB_COUNTRY = "country";
	public static final String DB_LANG = "lang";
	public static final String DB_MODIFIED_DATE="modified_date";
	public static final String DB_ITESTER_ID = "itester_id";

	private static final Map<String, Integer> DB_COLUMN_SIZES = new HashMap<String, Integer>();
	static {
		DB_COLUMN_SIZES.put(DB_EMAIL, new Integer(256));
		DB_COLUMN_SIZES.put(DB_FIRSTNAME, new Integer(128));
		DB_COLUMN_SIZES.put(DB_LASTNAME, new Integer(128));
		DB_COLUMN_SIZES.put(DB_STREET, new Integer(256));
		DB_COLUMN_SIZES.put(DB_CITY, new Integer(128));
		DB_COLUMN_SIZES.put(DB_STATE, new Integer(64));
		DB_COLUMN_SIZES.put(DB_ZIPCODE, new Integer(10));
		DB_COLUMN_SIZES.put(DB_COUNTRY, new Integer(64));
		DB_COLUMN_SIZES.put(DB_LANG, new Integer(16));
	}
	private static final String fitDb(String v, String n){
		return StringUtils.substring(v==null?"":v.trim(), DB_COLUMN_SIZES.get(n));
	}
	private static final Set<String> DB_COLUMNS_INSERT = new HashSet<String>();
	static {
		DB_COLUMNS_INSERT.add(DB_WRITER_ID);
		DB_COLUMNS_INSERT.add(DB_EMAIL);
		DB_COLUMNS_INSERT.add(DB_FIRSTNAME);
		DB_COLUMNS_INSERT.add(DB_LASTNAME);
		DB_COLUMNS_INSERT.add(DB_STREET);
		DB_COLUMNS_INSERT.add(DB_CITY);
		DB_COLUMNS_INSERT.add(DB_STATE);
		DB_COLUMNS_INSERT.add(DB_ZIPCODE);
		DB_COLUMNS_INSERT.add(DB_COUNTRY);
		DB_COLUMNS_INSERT.add(DB_LANG);
		DB_COLUMNS_INSERT.add(DB_ITESTER_ID);
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
	public static final String SQL_COUNT_BY_WRITER_ID = "select count (*) from " + DB_TABLE 
		    + " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID;
	public static final String SQL_SELECT_ITESTER_IDS_BY_WRITER_ID = "select "+DB_ITESTER_ID+" from " + DB_TABLE 
		    + " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID
		    + " and "+DB_ITESTER_ID+" > 0";
	public static final String SQL_UPDATE_BY_ID = "update " + DB_TABLE +" set " + SET_DB_COLUMNS_UPDATE 
			+ " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_ALL_BY_WRITER_ID_ORDER_BY_FIRSTNAME = "select * from " + DB_TABLE 
			+ " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID +" order by " +DB_FIRSTNAME;
/*	public static final String SQL_SELECT_ALL_BY_WRITER_ID_AND_LANG_ORDER_BY_FIRSTNAME = "select * from " + DB_TABLE 
			+ " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID 
			+" and ("+DB_LANG+"=:"+DB_LANG+" or "+DB_LANG+"='"+User.getLanguageBoth()+"')" 
			+" order by " +DB_FIRSTNAME;
*/	
	public static final String SQL_SELECT_BY_WRITER_ID_AND_ID = "select * from " + DB_TABLE 
			+ " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID +" and "+DB_ID+"=:"+DB_ID;
	public static final String SQL_SELECT_BY_WRITER_ID_AND_EMAIL = "select * from " + DB_TABLE 
			+ " where "+DB_WRITER_ID+"=:"+DB_WRITER_ID +" and "+DB_EMAIL+"=:"+DB_EMAIL;
	public static final String SQL_UPDATE_EMAIL_BY_ITESTER_ID = "update " + DB_TABLE 
			+ " set "+DB_EMAIL+"=:"+DB_EMAIL
			+" where "+DB_ITESTER_ID+"=:"+DB_ITESTER_ID;
	public static final String SQL_DELETE_BY_ID = "delete from " + DB_TABLE + " where "+DB_ID+"=:"+DB_ID;
	public static final String SQL_DELETE_BY_ITESTER_ID = "delete from " + DB_TABLE + " where "+DB_ITESTER_ID+"=:"+DB_ITESTER_ID;

	public static final String SQL_SELECT_BY_TESTGROUP_ID_ORDER_BY_FIRSTNAME= "select * from " + DB_TABLE 
		    + " where "+DB_ID+" in("+Testgroup.SQL_SELECT_TESTER_IDS_BY_TESTGROUP_ID +") order by " +DB_FIRSTNAME;
	
	public Tester() {
		super();
	};
	public Tester(User user, int writerId, int itesterId, List<Request> requests) {
		super();
		this.itesterId = itesterId;
		this.writerId = writerId;
		this.email = user.getUsername();
		this.firstname = user.getFirstname();
		this.lastname = user.getLastname();
		this.language = user.getLanguage();
		this.requests = requests;
	};
	public Tester(String[] tester) {
		super();
		this.id = Integer.parseInt(tester[0]);
		this.writerId = Integer.parseInt(tester[1]);
		this.email = fitDb(tester[2], DB_EMAIL);
		this.firstname = fitDb(tester[3], DB_FIRSTNAME);
		this.lastname = fitDb(tester[4], DB_LASTNAME);
		this.street = fitDb(tester[5], DB_STREET);
		this.city = fitDb(tester[6], DB_CITY);
		this.state = fitDb(tester[7], DB_STATE);
		this.zipcode = fitDb(tester[8], DB_ZIPCODE);
		this.country = fitDb(tester[9], DB_COUNTRY);
		this.language = tester[10];
	};
	public Tester(final int id, final int writerId, String email, String firstname, String lastname,
			String street, String city, String state, String zipcode, String country, String language, int itesterId) {
		super();
		this.id = id;
		this.writerId = writerId;
		this.email = fitDb(email, DB_EMAIL);
		this.firstname = fitDb(firstname, DB_FIRSTNAME);
		this.lastname = fitDb(lastname, DB_LASTNAME);
		this.street = fitDb(street, DB_STREET);
		this.city = fitDb(city, DB_CITY);
		this.state = fitDb(state, DB_STATE);
		this.zipcode = fitDb(zipcode, DB_ZIPCODE);
		this.country = fitDb(country, DB_COUNTRY);
		this.language = language;
		this.itesterId = itesterId;
	}
	public final Map<String, Object> allValuesMap() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(DB_ID, id);
		values.put(DB_WRITER_ID, writerId);
		values.put(DB_EMAIL, email);
		values.put(DB_FIRSTNAME, firstname);
		values.put(DB_LASTNAME, lastname);
		values.put(DB_STREET, street);
		values.put(DB_CITY, city);
		values.put(DB_STATE, state);
		values.put(DB_ZIPCODE, zipcode);
		values.put(DB_COUNTRY, country);
		values.put(DB_LANG, language);
		values.put(DB_ITESTER_ID, itesterId);
		values.put(DB_MODIFIED_DATE, new Date());
		return values;
	}
	public final int getId() {
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
	public final String getEmail() {
		return isIndependent()?"independent":email;
	}
	public final void setEmail(String email) {
		this.email = fitDb(email, DB_EMAIL);
	}
	public final String getFirstname() {
		return firstname;
	}
	public final String getFirstnameRead() {
		return firstname==null?"-":firstname;
	}
	public final void setFirstname(String firstname) {
		this.firstname = fitDb(firstname, DB_FIRSTNAME);
	}
	public final String getLastname() {
		return lastname;
	}
	public final String getLastnameRead() {
		return lastname==null?"-":lastname;
	}
	public final void setLastname(String lastname) {
		this.lastname = fitDb(lastname, DB_LASTNAME);
	}
	public final String getStreet() {
		return street;
	}
	public final void setStreet(String street) {
		this.street = fitDb(street, DB_STREET);
	}
	public final String getCity() {
		return city;
	}
	public final void setCity(String city) {
		this.city = fitDb(city, DB_CITY);
	}
	public final String getState() {
		return state;
	}
	public final void setState(String state) {
		this.state = fitDb(state, DB_STATE);
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = fitDb(zipcode, DB_ZIPCODE);
	}
	public final String getCountry() {
		return country;
	}
	public final void setCountry(String country) {
		this.country = fitDb(country, DB_COUNTRY);
	}
    public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public static List<String> getLanguageOptions(){
		return User.getLanguageOptions();
	}
	public boolean isIndependent() {
		return itesterId > 0;
	}
	public int getItesterId() {
		return itesterId;
	}
	public void setItesterId(int itesterId) {
		this.itesterId = itesterId;
	}
	public List<Request> getRequests() {
		return requests;
	}
	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}
	@Override
	public String toString() {
		return "Tester [id=" + id + ", writerId=" + writerId + ", email="
				+ email + ", firstname=" + firstname + ", lastname=" + lastname
				+ ", lang=" + language + ", itesterId=" + itesterId + "]";
	}

	
}
