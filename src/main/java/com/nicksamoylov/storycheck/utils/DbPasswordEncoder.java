package com.nicksamoylov.storycheck.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.authentication.encoding.PasswordEncoder;

public class DbPasswordEncoder extends JdbcDaoSupport{	
	private static final Logging log = Logging.getLog("DbPasswordEncoder");
	private PasswordEncoder passwordEncoder;
	
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void secureDatabase() {
		getJdbcTemplate().query("select id, password from users", new RowCallbackHandler(){
		@Override
		public void processRow(ResultSet rs) throws SQLException {
			String user_id = rs.getString(1);
			String password = rs.getString(2);
			String encodedPassword = passwordEncoder.encodePassword(password, null);
			getJdbcTemplate().update("update users set password = '"+encodedPassword+"' where id = '"+user_id+"'");
				log.debug("Updating password for user id="+user_id+" to "+encodedPassword);
			}
		});
	}

}
