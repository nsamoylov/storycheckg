package com.nicksamoylov.storycheck.service;

import java.io.File;
import java.security.Principal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicksamoylov.storycheck.domain.test.Test;
import com.nicksamoylov.storycheck.domain.test.Testgroup;
import com.nicksamoylov.storycheck.domain.users.Blog;
import com.nicksamoylov.storycheck.domain.users.BlogComment;
import com.nicksamoylov.storycheck.domain.users.BlogCommentEdit;
import com.nicksamoylov.storycheck.domain.users.BlogEdit;
import com.nicksamoylov.storycheck.domain.users.Contact;
import com.nicksamoylov.storycheck.domain.users.Request;
import com.nicksamoylov.storycheck.domain.users.Role;
import com.nicksamoylov.storycheck.domain.users.Tester;
import com.nicksamoylov.storycheck.domain.users.User;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.FileUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.StringUtils;
import com.nicksamoylov.storycheck.utils.Utils;

@Service("userService")
@Transactional
public class UserService {
	private static final Logging log = Logging.getLog("UserService");

	public BlogComment findBlogCommentById(int id){
		MapSqlParameterSource map = new MapSqlParameterSource();
		map.addValue(BlogComment.DB_ID, id);
	    try{
	    	BlogComment comment = jdbcTemplate.queryForObject(BlogComment.SQL_SELECT_BY_ID, map, new BlogCommentMapper());
			map = new MapSqlParameterSource();
			map.addValue(BlogCommentEdit.DB_COMMENT_ID, id);
	    	List<BlogCommentEdit> edits = jdbcTemplate.query(BlogCommentEdit.SQL_SELECT_BY_COMMENT_ID, map, new BlogCommentEditMapper());
	    	comment.setEdits(edits);
	    	log.debug("findBlogCommentById("+id+"): "+comment);
	        return comment;
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error("findBlogCommentById("+id+"): result(ex)=null");
	    	return null;
	    }
	}
	public List<BlogComment> findBlogComments(User user, int blogId){
		MapSqlParameterSource map = new MapSqlParameterSource();
		map.addValue(BlogComment.DB_BLOG_ID, blogId);
		List<BlogComment> l = jdbcTemplate.query(BlogComment.SQL_SELECT_BY_BLOG_ID, map, new BlogCommentMapper());
		for(BlogComment comment:l){
		    comment.setIsEditable(user);
			map = new MapSqlParameterSource();
			map.addValue(BlogCommentEdit.DB_COMMENT_ID, comment.getId());
	    	List<BlogCommentEdit> edits = jdbcTemplate.query(BlogCommentEdit.SQL_SELECT_BY_COMMENT_ID, map, new BlogCommentEditMapper());
	    	comment.setEdits(edits);
		}
    	log.debug("findBlogComments("+blogId+"): comments.size="+l.size());
		return l;
	}
	public void saveBlogComment(BlogComment comment, User user, boolean isRussian, EmailService emailService){
    	log.error("saveBlogComment(): "+comment);
    	comment.setUser(user);
    	log.error("saveBlogComment(): "+comment);
    	String subject;
    	String text = comment.getTextEdit();
    	if(comment.getId() > 0){
    		BlogCommentEdit bce = new BlogCommentEdit(comment);
    		List<BlogCommentEdit> l = jdbcTemplate.query(BlogCommentEdit.SQL_SELECT_BY_TEXT, bce.allValuesMap(), new BlogCommentEditMapper());
    		if(l.size() == 0){
        		jdbcTemplate.update(BlogCommentEdit.SQL_INSERT, bce.allValuesMap());

        		MapSqlParameterSource map = new MapSqlParameterSource();
        		map.addValue(BlogComment.DB_ID, comment.getId());
        		map.addValue(BlogComment.DB_MODIFIED_DATE, new Date());
        		jdbcTemplate.update(BlogComment.SQL_UPDATE_MODIFIED_DATE_BY_ID, map);
        		subject = "Blog comment "+comment.getId()+" update: " + comment.getTitle();
    		}
    		else {
        		subject = "Blog comment "+comment.getId()+" duplicate update: " + comment.getTitle();
    		}    		
    	}
    	else {
    		comment.setText(text);
    		List<BlogComment> l = jdbcTemplate.query(BlogComment.SQL_SELECT_BY_TEXT, comment.allValuesMap(), new BlogCommentMapper());
    		if(l.size() == 0){
        		jdbcTemplate.update(BlogComment.SQL_INSERT, comment.allValuesMap());
        		subject = "Blog comment new: " + comment.getTitle();
    		}
    		else {
    			subject = "Blog comment duplicate: " + comment.getTitle();
    		}
    	}
		emailService.sendBlogUpdateToAdminWithLogAttached(user, subject, text);
	}
	public void saveBlog(Blog blog, User user, boolean isRussian, EmailService emailService){
    	log.error("saveBlog(): "+blog);
    	blog.setUser(user);
    	blog.setIsRussian(isRussian);
    	log.error("saveBlog(): "+blog);
    	String subject;
    	String text = blog.getTextEdit();
    	if(blog.getId() > 0){
    		BlogEdit be = new BlogEdit(blog);
    		List<BlogEdit> l = jdbcTemplate.query(BlogEdit.SQL_SELECT_BY_TEXT, be.allValuesMap(), new BlogEditMapper());
    		if(l.size() == 0){
        		jdbcTemplate.update(BlogEdit.SQL_INSERT, be.allValuesMap());

        		MapSqlParameterSource map = new MapSqlParameterSource();
        		map.addValue(Blog.DB_ID, blog.getId());
        		map.addValue(Blog.DB_MODIFIED_DATE, new Date());
        		jdbcTemplate.update(Blog.SQL_UPDATE_MODIFIED_DATE_BY_ID, map);
        		subject = "Blog "+blog.getId()+" update: " + blog.getTitle();
    		}
    		else {
        		subject = "Blog "+blog.getId()+" duplicate update: " + blog.getTitle();
    		}
    	}
    	else {
    		blog.setText(text);
    		List<Blog> l = jdbcTemplate.query(Blog.SQL_SELECT_BY_TEXT, blog.allValuesMap(), new BlogMapper());
    		if(l.size() == 0){
        		jdbcTemplate.update(Blog.SQL_INSERT, blog.allValuesMap());
        		subject = "Blog new: " + blog.getTitle();
    		}
    		else {
        		subject = "Blog duplicate: " + blog.getTitle();
    		}
    	}
		emailService.sendBlogUpdateToAdminWithLogAttached(user, subject, text);
	}
	public Blog findBlogById(int id){
		MapSqlParameterSource map = new MapSqlParameterSource();
		map.addValue(Blog.DB_ID, id);
	    try{
	    	Blog blog = jdbcTemplate.queryForObject(Blog.SQL_SELECT_BY_ID, map, new BlogMapper());

	    	map = new MapSqlParameterSource();
			map.addValue(BlogComment.DB_BLOG_ID, id);
	    	int c = jdbcTemplate.queryForInt(BlogComment.SQL_COUNT_BY_BLOG_ID, map);
	    	blog.setCommentsPublished(c);
	    	
	    	if(blog.getModifiedDate() != null){
				map.addValue(BlogComment.DB_CREATED_DATE, blog.getModifiedDate());
		    	c = jdbcTemplate.queryForInt(BlogComment.SQL_COUNT_BY_BLOG_ID_SINCE_DATE, map);
		    	blog.setCommentsModified(c);
	    	}
	    	
			map = new MapSqlParameterSource();
			map.addValue(BlogEdit.DB_BLOG_ID, id);
	    	List<BlogEdit> edits = jdbcTemplate.query(BlogEdit.SQL_SELECT_BY_BLOG_ID, map, new BlogEditMapper());
	    	blog.setEdits(edits);
	    	log.debug("findBlogById("+id+"): "+blog);
	        return blog;
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error("findBlogById("+id+"): result(ex)=null");
	    	return null;
	    }
	}
	public List<Blog> findBlogs(int userId, String key, boolean isRussian){
		MapSqlParameterSource map = new MapSqlParameterSource();
		map.addValue(Blog.DB_LANG, Utils.lang(isRussian));
		Set<Integer> ids = new HashSet<Integer>();
		ids.addAll(jdbcTemplate.queryForList(Blog.SQL_SELECT_ID_BY_KEY(key), map, Integer.class));
		ids.addAll(jdbcTemplate.queryForList(BlogEdit.SQL_SELECT_BLOG_ID_BY_KEY(key), map, Integer.class));
		
		Set<Integer> idsBlogs = new HashSet<Integer>();
		idsBlogs.addAll(ids);
		
		Set<Integer> idsComments = new HashSet<Integer>();
		List<BlogComment> l = jdbcTemplate.query(BlogComment.SQL_SELECT_BY_KEY(key), map, new BlogCommentMapper());
		for(BlogComment bc:l){
			ids.add(bc.getBlogId());
			idsComments.add(bc.getId());
			updateFoundCounts(userId, bc);
		}
		List<Integer> ids1 = jdbcTemplate.queryForList(BlogCommentEdit.SQL_SELECT_COMMENT_ID_BY_KEY(key), map, Integer.class);
		if(ids1.size() > 0){
			ids.addAll(jdbcTemplate.queryForList(BlogComment.SQL_SELECT_BLOG_IDS_BY_COMMENT_IDS(DisplayUtils.commaSeparatedSorted(ids1)), map, Integer.class));
			for(int id:ids1){
				if(!idsComments.contains(id)){
					BlogComment bc = findBlogCommentById(id);
					updateFoundCounts(userId, bc);
				}
			}
		}
		List<Integer> idsSorted = new ArrayList<Integer>(ids);
		Collections.sort(idsSorted, Collections.reverseOrder());
		List<Blog> blogs = new ArrayList<Blog>();
		for(int id:ids){
			Blog blog = findBlogById(id);
			updateFoundCounts(userId, blog);
			blogs.add(blog);
		}
    	log.debug("findBlogs(ru="+isRussian+", key="+key+"): blogs.size="+blogs.size());
		return blogs;
	}
	public List<Blog> findBlogs(int userId, boolean isRussian){
		MapSqlParameterSource map = new MapSqlParameterSource();
		map.addValue(Blog.DB_LANG, Utils.lang(isRussian));
		List<Blog> l = jdbcTemplate.query(Blog.SQL_SELECT_BY_LANG, map, new BlogMapper());
		for(Blog blog:l){
			updateFoundCounts(userId, blog);
		}
    	log.debug("findBlogs(ru="+isRussian+"): blogs.size="+l.size());
		return l;
	}
	private void updateFoundCounts(int userId, Blog blog){
		if(userId != blog.getUserId()){
			blog.setFoundPublished(blog.getFoundPublished()+1);
			blog.setFoundModified(blog.getFoundModified()+1);
			jdbcTemplate.update(Blog.SQL_UPDATE_COUNT_FOUND_BY_ID, blog.allValuesMap());
		}
		MapSqlParameterSource map = new MapSqlParameterSource();
		map.addValue(BlogComment.DB_BLOG_ID, blog.getId());
	    int c = jdbcTemplate.queryForInt(BlogComment.SQL_COUNT_BY_BLOG_ID, map);
	    blog.setCommentsPublished(c);
	}
	private void updateFoundCounts(int userId, BlogComment comment){
		if(userId == comment.getUserId()){
			return;
		}
		comment.setFoundPublished(comment.getFoundPublished()+1);
		comment.setFoundModified(comment.getFoundModified()+1);
		jdbcTemplate.update(BlogComment.SQL_UPDATE_COUNT_FOUND_BY_ID, comment.allValuesMap());
	}
	public void updateViewedCount(int userId, Blog blog){
		if(userId == blog.getUserId()){
			return;
		}
		blog.setViewedPublished(blog.getViewedPublished()+1);
		blog.setViewedModified(blog.getViewedModified()+1);
		jdbcTemplate.update(Blog.SQL_UPDATE_COUNT_VIEWED_BY_ID, blog.allValuesMap());
	}
	public void updateViewedCount(int userId, List<BlogComment> comments){
		for(BlogComment comment:comments){
			if(userId != comment.getUserId()){
				comment.setViewedPublished(comment.getViewedPublished()+1);
				comment.setViewedModified(comment.getViewedModified()+1);
				jdbcTemplate.update(BlogComment.SQL_UPDATE_COUNT_VIEWED_BY_ID, comment.allValuesMap());
			}
		}
	}
	public Contact findContactByToken(String token){
    	Contact contact = new Contact();
    	contact.setContactToken(token);
	    try{
	    	contact = jdbcTemplate.queryForObject(Contact.SQL_SELECT_BY_TOKEN, contact.allValuesMap(), new ContactMapper());
	    	log.debug("findContactByToken("+token+"): "+contact);
	        return contact;
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error("findContactByToken("+token+"): result(ex)=null");
	    	return null;
	    }
	}
	public void saveContactAndSendEmail(String emailFrom, String emailTo, Request request, Locale locale, EmailService emailService){
		String contactToken = UUID.randomUUID().toString();
		String subject = request.getTitle().startsWith("Re:")?request.getTitle():"Re:"+request.getTitle();
		Contact contact = new Contact(0,request.getId(),request.getRoleName(),emailTo,emailFrom,subject,request.getEmailText(),contactToken);
		jdbcTemplate.update(Contact.SQL_INSERT, contact.allValuesMap());
		emailService.sendEmailRequest(request.getEmail(), subject, request.getEmailText(), contactToken, locale);
	}
	// Writers look for editor/proofreader/translator requests 
	// But ignore their own requests
	public List<Request> findRequestsByRole(User user, String r2, boolean isRussian){
	    //Writers pull all requests_editors, except same userId 
	    int roleId2 = -1;
	    if(user.getId() > 90){
		    if(user.hasRole(r2)){
				Role role2 = new Role(r2, user);
			    roleId2 = jdbcTemplate.queryForInt(role2.sqlSelectIdByUserId(), role2.allValuesMap());
		    }
	    }
    	Request req = new Request(r2);
    	req.setLang(Utils.lang(isRussian));
    	StringBuffer sql = new StringBuffer(req.sqlSelectFromWhere());
    	if(roleId2 > 90){
    		sql.append(req.sqlRoleIdNot()+roleId2).append(" and ");  
    	}
    	sql.append(Request.SQL_LANG_EQUALS).append("'").append(Utils.lang(isRussian)).append("'").append(" order by ").append(Request.DB_ID);
		log.debug("findRequestsByRole("+r2+"): sql="+sql);
	    List<Request> reqs = jdbcTemplate.query(sql.toString(), new MapSqlParameterSource(), new FindRequestsMapper(req.getRoleName()));
	    for(Request rq:reqs){
	    	setRoleAndContactsCount(rq, r2);
	    	rq.setFoundModified(rq.getFoundModified()+1);
	    	rq.setFoundPublished(rq.getFoundPublished()+1);
	    	jdbcTemplate.update(rq.sqlUpdateCountersById(), rq.allValuesMap());
	    }
		log.debug("findRequestsByRole("+r2+"): requests.size="+reqs.size());
	    return reqs;
	}
	// Editor/Proofreader/Translator(e/p/t) looks for writer requests that have keyword edit/proofread/translat
	// But ignore their own requests
	public List<Request> findWriterRequestsByKeywords(User user, String[] kws, boolean isRussian){
	    //Editor (editor_id in requests_editors and userId) looks in requests_writers (except same userId) for keyword "edit"
		String r = "w";
	    int writerId = -1;
	    if(user.getId() > 90){
		    if(user.hasRole(r)){
				Role roleWriter = new Role(r, user);
			    writerId = jdbcTemplate.queryForInt(roleWriter.sqlSelectIdByUserId(), roleWriter.allValuesMap());
		    }
	    }
    	Request req = new Request(r);
    	StringBuffer sql = new StringBuffer(req.sqlSelectFromWhere());
    	if(writerId > 90){
    		sql.append(req.sqlRoleIdNot()+writerId).append(" and ");  
    	}
    	sql.append("(");
    	boolean isFirst = true;
    	for(String kw:kws){
    		if(isFirst){
    			isFirst = false;
    		}
    		else{
    			sql.append(" or ");
    		}
    		sql.append(req.sqlTitleAndRequestLike(kw));
    	}
    	sql.append(")");
    	sql.append(" order by id");
		log.debug("findWriterRequestsByKeywords("+DisplayUtils.commaSeparated(kws)+"): sql="+sql);
	    List<Request> reqs = jdbcTemplate.query(sql.toString(), new MapSqlParameterSource(), new FindRequestsMapper(req.getRoleName()));
	    for(Request rq:reqs){
	    	setRoleAndContactsCount(rq, r);
	    	rq.setFoundModified(rq.getFoundModified()+1);
	    	rq.setFoundPublished(rq.getFoundPublished()+1);
	    	jdbcTemplate.update(rq.sqlUpdateCountersById(), rq.allValuesMap());
	    }
		log.debug("findWriterRequestsByKeywords("+DisplayUtils.commaSeparated(kws)+"): requests.size="+reqs.size());
	    return reqs;
	}
	public void deleteRequest(Request request) {
		jdbcTemplate.update(request.sqlDeleteById(), request.allValuesMap());
	}
	public Request findRequestById(String roleShortOrLong, int id, boolean isRussian) {
    	Request req = findRequestByIdAnyUser(roleShortOrLong, id, isRussian);
    	if(req != null){
	    	setRoleAndContactsCount(req, roleShortOrLong);
    	}
        return req;
	}	
	public Request findRequestByIdAnyUser(String roleShortOrLong, int id, boolean isRussian) {
    	Request req = new Request(roleShortOrLong);
    	req.setId(id);
	    try{
	    	req = jdbcTemplate.queryForObject(req.sqlSelectById(), req.allValuesMap(), new RequestMapper(req.getRoleName(), req.DB_ROLE_ID));
	    	log.debug("findRequestById("+id+"): "+req);
	        return req;
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error("findRequestByIdAnyUser("+id+"): result(ex)=null");
	    	return null;
	    }
	}	
	public void updateRequestViewCounters(Request req) {
    	req.setViewedModified(req.getViewedModified()+1);
    	req.setViewedPublished(req.getViewedPublished()+1);
    	jdbcTemplate.update(req.sqlUpdateCountersById(), req.allValuesMap());
	}	
	public void saveRequest(String roleShortOrLong, User user, Request request) {
		Role role = findRole(roleShortOrLong, user);
    	request.setRole(role);
		if(request.getId() < 1){
			jdbcTemplate.update(request.sqlInsert(), request.allValuesMap());
		}
		else {
			jdbcTemplate.update(request.sqlUpdateById(), request.allValuesMap());
			Request req = findRequestById(roleShortOrLong, request.getId(), false);
	    	req.setFoundModified(0);
	    	req.setViewedModified(0);
	    	jdbcTemplate.update(req.sqlUpdateCountersById(), req.allValuesMap());
		}
	}	
	public List<Request> findTesterRequestsByItesterIdAndLanguageOrderById(int itesterId, boolean isRussian){
	    Request r = new Request("i");
	    r.setRoleId(itesterId);
	    r.setLang(Utils.lang(isRussian));
	    List<Request> reqs = jdbcTemplate.query(r.sqlSelectByRoleIdAndLanguageOrderById(), r.allValuesMap(), new RequestMapper(r.getRoleName(), r.DB_ROLE_ID));
		log.debug("findTesterRequestsByItesterIdAndLanguageOrderById("+itesterId+","+Utils.lang(isRussian)+"): requests.size="+reqs.size());
	    return reqs;
	}
	public List<Request> findRequestsByUserIdOrderById(String roleShortOrLong, User user, boolean isRussian){
		Role role = findRole(roleShortOrLong, user);
	    Request r = new Request(role);
	    List<Request> reqs = jdbcTemplate.query(r.sqlSelectByRoleIdOrderById(), r.allValuesMap(), new RequestMapper(r.getRoleName(), r.DB_ROLE_ID));
	    for(Request req:reqs){
	    	setRoleAndContactsCount(req, role);
	    }
		log.debug("findRequestsByUserIdOrderById(userId="+user.getId()+", r="+roleShortOrLong+"): requests.size="+reqs.size());
	    return reqs;
	}
	private void setRoleAndContactsCount(Request req, Role role){
    	req.setRole(role);
    	Contact contact = new Contact();
    	contact.setRequestId(req.getId());
    	contact.setRoleName(req.getRoleName());
    	contact.setEmailTo(role.getEmail());
	    int contactsPublished = jdbcTemplate.queryForInt(Contact.SQL_COUNT_BY_REQUEST_ID, contact.allValuesMap());
	    req.setContactsPublished(contactsPublished);
    	if(req.getModifiedDate() != null){
    		contact.setCreatedDate(req.getModifiedDate());
	    	int contactsModified = jdbcTemplate.queryForInt(Contact.SQL_COUNT_BY_REQUEST_ID, contact.allValuesMap());
	    	req.setContactsModified(contactsModified);
    	}
	}
	private void setRoleAndContactsCount(Request req, String roleShort){
		Role role = findRole(roleShort, req.getRoleId());
		setRoleAndContactsCount(req, role);
	}
	public Role findRole(String roleShortOrLong, int roleId){
		Role role = new Role(roleShortOrLong);
		role.setId(roleId);
	    List<Role> roles = jdbcTemplate.query(role.sqlSelectById(), role.allValuesMap(), new RoleMapper());
		if(roles.size() > 0){
			role = roles.get(0);
			User user = findUserById(role.getUserId());
		    role.setUser(user);
		    role.setRoleName(roleShortOrLong);
		}
		log.debug("findRole("+roleShortOrLong+",roleId="+roleId+"): "+role);
	    return role;
	}
	private Role findRole(String roleShortOrLong, User user){
		Role role = new Role(roleShortOrLong, user);
	    int roleId = jdbcTemplate.queryForInt(role.sqlSelectIdByUserId(), role.allValuesMap());
	    role.setId(roleId);
		log.debug("findRole("+roleShortOrLong+",userName="+user.getUsername()+"): "+role);
	    return role;
	}
	
	public String findPasswordInTemp(final User user) {
		String password = null;
		List<String> l = jdbcTemplate.queryForList(User.SQL_SELECT_PASSWORD_FROM_TEMP_BY_ID, user.allValuesMap(), String.class);
		if(l.size() > 0){
			password = l.get(0);
		}
		log.debug("findUserByPasswordInTemp("+user.getId()+"): "+password);
        return password;
	}	
	public User findUserByPasswordInTemp(final String tempPassword) {
		int id = 0;
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(User.DB_PASSWORD, tempPassword);
		List<Integer> l = jdbcTemplate.queryForList(User.SQL_SELECT_ID_BY_PASSWORD_FROM_TEMP, map, Integer.class);
		if(l.size() > 0){
			id = l.get(0);
		}
		User user = null;
		if(id > 0){
			user = findUserById(id);
			user.setRoles(findUserRoles(user.getId()));
		}
		log.debug("findUserByPasswordInTemp("+tempPassword+"): "+user);
        return user;
	}	
	public boolean isEmailChangeExpired(final User user) {
		boolean isExpired = false;
		List<Timestamp> ld = jdbcTemplate.queryForList(User.SQL_SELECT_DATE_FROM_TEMP, user.allValuesMap(), Timestamp.class);
		Date now = new Date();
		int diffInMinutes = (int)( (now.getTime() - ld.get(0).getTime())/(1000 * 60));
	    MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(User.DB_ID, user.getId());
		if(diffInMinutes < 11){
			isExpired = false;
		}
		else {
			List<String> l = jdbcTemplate.queryForList(User.SQL_SELECT_USERNAME_FROM_TEMP, user.allValuesMap(), String.class);
			if(l.size() > 0){
				String newEmailAddress = l.get(0);
		    	map.addValue(User.DB_USERNAME, newEmailAddress);
		    	map.addValue(User.DB_MODIFIED_DATE, new Date());
				jdbcTemplate.update(User.SQL_UPDATE_USERNAME_BY_ID, map);
				TestService ts = new TestService(this.jdbcTemplate);
				int itesterId = ts.findRoleId(user, Role.ROLE_TESTER);;
				map = new MapSqlParameterSource();
		    	map.addValue(Tester.DB_EMAIL, newEmailAddress);
		    	map.addValue(Tester.DB_ITESTER_ID, itesterId);
				jdbcTemplate.update(Tester.SQL_UPDATE_EMAIL_BY_ITESTER_ID, map);
			}
			isExpired = true;
		}
		jdbcTemplate.update(User.SQL_DELETE_USER_TEMP_BY_ID, map);
		log.debug("isEmailChangeExpired("+user+"): "+isExpired);
		return isExpired;
	}
	public boolean wasEmailChanged(User user){
		String tempEmail = null;
		List<String> l = jdbcTemplate.queryForList(User.SQL_SELECT_USERNAME_FROM_TEMP, user.allValuesMap(), String.class);
		if(l.size() > 0){
			tempEmail = l.get(0);
		}
		boolean result = tempEmail == null || !user.getUsername().equals(tempEmail);
		log.debug("wasEmailChanged(): "+result);
		return result;
	}
	public boolean isConfirmationRequired(User user){
		List<Timestamp> ld = jdbcTemplate.queryForList(User.SQL_SELECT_DATE_FROM_TEMP, user.allValuesMap(), Timestamp.class);
		log.debug("isConfirmationRequired("+user.getUsername()+"): "+(ld.size() > 0));
		return ld.size() > 0;
	}
	public boolean isRegistrationExpired(final User user) {
		boolean isExpired = false;
		List<Timestamp> ld = jdbcTemplate.queryForList(User.SQL_SELECT_DATE_FROM_TEMP, user.allValuesMap(), Timestamp.class);
		Date now = new Date();
		int diffInMinutes = (int)( (now.getTime() - ld.get(0).getTime())/(1000 * 60));
		if(diffInMinutes < 11){
			final MapSqlParameterSource map = new MapSqlParameterSource();
	    	map.addValue(User.DB_ID, user.getId());
	    	map.addValue(User.DB_MODIFIED_DATE, new Date());
			jdbcTemplate.update(User.SQL_UPDATE_MODIFIED_DATE_BY_ID, map);
			jdbcTemplate.update(User.SQL_DELETE_USER_TEMP_BY_ID, map);
			isExpired = false;
		}
		else {
			deleteUser(user, null);
			isExpired = true;
		}
		log.debug("isRegistrationExpired("+user.getUsername()+"): "+isExpired);
		return isExpired;
	}
	
	public final void deleteUser(final User user, EmailService emailService){
		deleteUserRoles(user, user.getRoles(), emailService);

		final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(User.DB_ID, user.getId());
		jdbcTemplate.update(User.SQL_DELETE_USER_TEMP_BY_ID, map);

    	map.addValue(User.DB_USERNAME, user.getUsername());
		jdbcTemplate.update(User.SQL_DELETE_BY_USERNAME, map);
	}
	
	public final void deleteUserRoles(final User user, final Set<String> delRoles, EmailService emailService){
		if(delRoles.size() > 0){
			for(String role:delRoles){
				if(Role.ROLE_WRITER.equals(role)){
					TestService ts = new TestService(this.jdbcTemplate);
					int writerId = ts.findRoleId(user, Role.ROLE_WRITER);
					if(writerId > 0){
						List<Test> tests = ts.findTestsByWriterIdOrderByName(false, writerId);
						List<Testgroup> tgs = ts.findTestgroupsByWriterIdOrderByName(false, writerId);
						for(Tester tester:ts.findTestersByWriterIdOrderByFirstname(false, writerId)){
							ts.deleteTester(tester);
						}
						for(Testgroup tg:tgs){
							ts.deleteTestgroup(false, tg);
						}
						for(Test test:tests){
							ts.deleteTest(false, test);
						}
						File dir = new File(Test.getTextfileDirName(writerId));
						if(dir.exists()){
							boolean success = FileUtils.deleteFileTree(dir);
							if(!success && emailService != null){
								emailService.sendProblemToAdminWithLogAttached(user, "Failed to delete "+dir);
							}
						}
					}
				}
				else if(Role.ROLE_TESTER.equals(role)){
					TestService ts = new TestService(this.jdbcTemplate);
					int itesterId = ts.findRoleId(user, Role.ROLE_TESTER);;
					final MapSqlParameterSource map = new MapSqlParameterSource();
			    	map.addValue(Tester.DB_ITESTER_ID, itesterId);
					jdbcTemplate.update(Tester.SQL_DELETE_BY_ITESTER_ID, map);
				}
		    	Contact contact = new Contact();
		    	contact.setRoleName(role);
		    	contact.setEmailTo(user.getUsername());
		    	contact.setEmailFrom(user.getUsername());
				jdbcTemplate.update(Contact.SQL_DELETE_BY_USER_AND_ROLE_NAMES, contact.allValuesMap());

				Role r = findRole(role, user);
		    	Request req = new Request(r);
				jdbcTemplate.update(req.sqlDeleteByRoleId(), req.allValuesMap());
				jdbcTemplate.update(r.sqlDeleteByUserId(), r.allValuesMap());
			}
			final MapSqlParameterSource map = new MapSqlParameterSource();
		    map.addValue(Role.DB_ROLES_USER_ID, user.getId());
			jdbcTemplate.update(Role.sqlDeleteRoles(delRoles), map);
		}
	}
	
	public void saveUserTemp(final User user, final String tempPassword) {
		log.debug("saveUserTemp("+user+")");
		String encodedPassword = passwordEncoder.encodePassword(user.getPassword(), null);
        user.setPassword(encodedPassword);        
        saveUser(user);
        User savedUser = findUserByUsername(user.getUsername());
		Map<String, Object> params = new HashMap<String,Object>();
		params.put(User.DB_ID, savedUser.getId());
		params.put(User.DB_USERNAME, user.getUsername());
		params.put(User.DB_PASSWORD, tempPassword);
		jdbcTemplate.update(User.SQL_INSERT_TEMP, params);
	}
	
	public Set<String> saveUserTemp(final User user, final String tempPassword, final String oldUsername) {
		log.debug("saveUserTemp("+user+")");		
		Set<String> delRoles = saveUser(user);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put(User.DB_ID, user.getId());
		params.put(User.DB_USERNAME, oldUsername);
		params.put(User.DB_PASSWORD, tempPassword);
		jdbcTemplate.update(User.SQL_INSERT_TEMP, params);
		return delRoles;
	}
	
	public Set<String> saveUser(final User user) {
		log.debug("saveUser("+user+")");
		Set<String> delRoles = new HashSet<String>();
		if(user.getId() < 1){
			jdbcTemplate.update(User.SQL_INSERT, user.allValuesMap());
			User u = findUserByUsername(user.getUsername());
			for(String role:user.getRoles()){
				Map<String, Object> params = new HashMap<String,Object>();
				params.put(Role.DB_ROLES_ROLE, role);
				params.put(Role.DB_ROLES_USER_ID, u.getId());
				jdbcTemplate.update(Role.SQL_INSERT_ROLE, params);
				Role r = new Role(role, u);
				jdbcTemplate.update(r.sqlInsert(), r.allValuesMap());
			}
		}
		else {
			final User u = findUserById(user.getId());
			if(u != null){
				u.setUsername(user.getUsername());
				u.setPassword(user.getPassword());
				u.setFirstname(user.getFirstname());
				u.setLastname(user.getLastname());
				u.setLanguage(user.getLanguage());
				jdbcTemplate.update(User.SQL_UPDATE_BY_ID, u.allValuesMap());
			    
				delRoles = findRemovedRoles(user);
				//delete them after confirmation

				Set<String> addRoles = new HashSet<String>(user.getRoles());
				Set<String> oldRoles = findUserRoles(u.getId());
				addRoles.removeAll(oldRoles);
			    for(String role:addRoles){
					Map<String, Object> params = new HashMap<String,Object>();
					params.put(Role.DB_ROLES_ROLE, role);
					params.put(Role.DB_ROLES_USER_ID, u.getId());
					jdbcTemplate.update(Role.SQL_INSERT_ROLE, params);
					Role r = new Role(role, u);
					jdbcTemplate.update(r.sqlInsert(), r.allValuesMap());
			    }
			}
		}		
		return delRoles;
	}
	public Set<String> findRemovedRoles(final User user) {
		Set<String> delRoles = new HashSet<String>();
		final User u = findUserById(user.getId());
		if(u != null){
			Set<String> oldRoles = findUserRoles(u.getId());
			delRoles = new HashSet<String>(oldRoles);
			delRoles.removeAll(user.getRoles());
		}
		return delRoles;
	}
	public boolean usernameExists(final int userId, final String username){
		boolean exists = false;
		if(StringUtils.isEmpty(username)){
			log.debug("usernameExists("+username+"): username is empty");
		}
		else {
		    try{
				User u = null;
			    final MapSqlParameterSource params = new MapSqlParameterSource();
			    params.addValue(User.DB_USERNAME, username);
				u = jdbcTemplate.queryForObject(User.SQL_SELECT_USER_BY_USERNAME, params, new UserMapper());
				if(userId < 1){
					exists = u != null;
				}
				else {
					exists = u != null && u.getId() != userId;
				}
		    }
		    catch(IncorrectResultSizeDataAccessException ex){
		    	exists = false;
		    }
		}
		log.debug("usernameExists("+username+"): "+exists);
		return exists;
	}	

	public User findUserById(final int id){ 
	    final MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue(User.DB_ID, id);
	    try{
			User user = jdbcTemplate.queryForObject(User.SQL_SELECT_USER_BY_ID, params, new UserMapper(true));
			log.debug("findUserById("+id+"): "+user);
			return user;
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
			log.error("findUserById("+id+"): user(ex)=null");
	    	return null;
	    }
	}
		
	public User findUser(final Principal principal){
		User user = principal == null? findUserByUsername(null):findUserByUsername(principal.getName());
		log.debug("findUser(principal="+principal+"): "+user);
		return user;
	}
	
	public User findUser(final Authentication authentication){
		User user = null;
		if(authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()){
			user = findUserByUsername(authentication.getName());
		}
		else {
			user = findUserByUsername(null);
		}
		log.debug("findUser(authentication="+authentication+"): "+user);
		return user;
	}
	
	public User findUserByUsername(final String username) {
		User user;
		if(username == null){
			user = new User(-1, null);
		}
		else {
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(User.DB_USERNAME, username);
			List<User> l = jdbcTemplate.query(User.SQL_SELECT_USER_BY_USERNAME, params, new UserMapper());
			if(l.size()==0){
				user = new User(-2, username);
			}
			else {
				user = l.get(0);
				user.setRoles(findUserRoles(user.getId()));
			}
		}
		log.debug("findUserByUsername("+username+"): user="+user);
		return user;
	}
	
	private Set<String> findUserRoles(final int userId){
	    final MapSqlParameterSource params2 = new MapSqlParameterSource();
	    params2.addValue(Role.DB_ROLES_USER_ID, userId);
		Set<String> roles = new HashSet<String>();
		roles.addAll(jdbcTemplate.queryForList(Role.SQL_SELECT_ROLES_BY_USER_ID, params2, String.class));
		return roles;
	}

	public static final class UserMapper implements RowMapper<User> {
		private boolean includePassword = false;
		public UserMapper(){
			includePassword = false;
		}
		public UserMapper(boolean includePassword){
			this.includePassword = includePassword;
		}
        public User mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final User user = new User(rs.getInt(User.DB_ID), rs.getString(User.DB_USERNAME), 
        			rs.getString(User.DB_FIRSTNAME), rs.getString(User.DB_LASTNAME), rs.getString(User.DB_STREET), 
        			rs.getString(User.DB_CITY), rs.getString(User.DB_STATE), rs.getString(User.DB_ZIPCODE),  
        			rs.getString(User.DB_COUNTRY), rs.getString(User.DB_LANG), rs.getInt(User.DB_EMAILS_COUNT_TODAY), 
        			rs.getTimestamp(User.DB_EMAILS_COUNT_DATE), rs.getInt(User.DB_EMAILS_COUNT_TOTAL), 
        			rs.getTimestamp(User.DB_MODIFIED_DATE), rs.getBoolean(User.DB_ENABLED));
        	if(includePassword){
            	user.setPassword(rs.getString(User.DB_PASSWORD));
        	}
            return user;
        }
	}
	
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	@Resource(name="dataSource")
	public void setDataSource(DataSource dataSource) {
	    this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

    @Autowired
    private ShaPasswordEncoder passwordEncoder;
    
	public UserService() {
		super();
	}

	private static final String SQL_INSERT_VISIT = "insert into visits (user_id, method) values(:user_id,:method)";
	private static final String SQL_INSERT_VISITOR = "insert into visitors (user_id, method, visitor) values(:user_id,:method,:visitor)";
	private static final String SQL_SELECT_LATEST_VISITOR_USERID_METHOD = "select user_id, method from visitors order by created_date desc limit 1";
	private static final String UNKNOWN = "unknown";
	public void recordVisitor(String method, HttpServletRequest request){
		recordVisitor(null, method, null, request);
	}
	public void recordVisitor(String userId1, String method, String visitor, HttpServletRequest request){
		if(isOneOfTheKnownVisitors(request) || (userId1 !=null && userId1.contains("nsamoylov"))){
			return;
		}
		String userId = (userId1==null || "".equals(userId1.trim()))?UNKNOWN:userId1;
		if(request != null){
			String ip = getRemoteIPAddress(request);
			if(UNKNOWN.equals(userId)){
				userId = UNKNOWN+":"+ip;
			}
		}
		if(!userId.equals(Logging.USER_CURRENT)){
		    Logging.USER_CURRENT = userId;
			String headers = UNKNOWN;
			if(request != null){
			    headers = logRequestHeaders(request);
			}
			if(headers.length() > 1){
				String user_id = StringUtils.substring(userId, 256);
				String methodValue = StringUtils.substring(method, 256);				
				final MapSqlParameterSource map = new MapSqlParameterSource();
				List<List<String>> l = jdbcTemplate.query(SQL_SELECT_LATEST_VISITOR_USERID_METHOD, map, new TwoValuesMapper());				
				log.debug("userId="+userId+", user_id="+user_id+", method="+methodValue+", l="+l);
				log.debug("l.get(0)="+l.get(0));
				
				if(l.size()>0 && user_id.equals(l.get(0).get(0)) && methodValue.equals(l.get(0).get(1))){
					log.debug("same visitor again: userId="+user_id+", method="+methodValue);
				}
				else{
				    map.addValue("user_id", user_id);
				    map.addValue("method", methodValue);
				    if(!UNKNOWN.equals(headers)){
					    headers = "Headers: " + headers;
				    }
				    map.addValue("visitor", StringUtils.substring(visitor==null?headers:visitor+", "+headers, 1100));
					jdbcTemplate.update(SQL_INSERT_VISITOR, map);
				}
			}
		}
	}
	public void recordVisit(String method, HttpServletRequest request){
		recordVisit(request, method, null);
	}
	public void recordVisit(String method, String userId){
		recordVisit(null, method, userId);
	}
	public void recordVisit(HttpServletRequest request, String method, String userId1){
		if(isOneOfTheKnownVisitors(request) || (userId1 !=null &&userId1.contains("nsamoylov"))){
			return;
		}
		String userId = (userId1==null || "".equals(userId1.trim()))?UNKNOWN:userId1;
		if(request != null){
			String ip = getRemoteIPAddress(request);
			if(UNKNOWN.equals(userId)){
				userId = UNKNOWN+":"+ip;
			}
		}
		final MapSqlParameterSource map = new MapSqlParameterSource();
	    map.addValue("user_id", StringUtils.substring(userId, 256));
	    map.addValue("method", StringUtils.substring(method, 256));
		jdbcTemplate.update(SQL_INSERT_VISIT, map);
		if(!userId.equals(Logging.USER_CURRENT)){
		    Logging.USER_CURRENT = userId;
		}
	}
	private static List<String> knownUserAgents = null;
	private static final String SQL_SELECT_ALL_AGENTS = "select * from known_agents";
	public boolean isOneOfTheKnownVisitors(HttpServletRequest request){
		if(request != null){
			if(request.getHeader("user-agent") != null){
				if(knownUserAgents == null){
					knownUserAgents = jdbcTemplate.queryForList(SQL_SELECT_ALL_AGENTS, new MapSqlParameterSource(), String.class);
				}
				String header = request.getHeader("user-agent");
				for(String agent: knownUserAgents){
					if(header.contains(agent)){
						return true;
					}
				}
			}
		}
		return false;
	}
	private static final class RoleMapper implements RowMapper<Role> {
        public Role mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final Role role = new Role(rs.getInt(Role.DB_ID), rs.getInt(Role.DB_USER_ID),
        			rs.getString(Role.DB_STATUS));
            return role;
        }
	}
	private static final class BlogEditMapper implements RowMapper<BlogEdit> {
        public BlogEdit mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final BlogEdit edit = new BlogEdit(rs.getInt(BlogEdit.DB_ID), rs.getInt(BlogEdit.DB_BLOG_ID),
                    rs.getString(BlogEdit.DB_TEXT), rs.getTimestamp(Blog.DB_CREATED_DATE));
            return edit;
        }
	}
	private static final class BlogCommentEditMapper implements RowMapper<BlogCommentEdit> {
        public BlogCommentEdit mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final BlogCommentEdit edit = new BlogCommentEdit(rs.getInt(BlogCommentEdit.DB_ID), 
        			rs.getInt(BlogCommentEdit.DB_COMMENT_ID), 
        			rs.getString(BlogCommentEdit.DB_TEXT), rs.getTimestamp(BlogCommentEdit.DB_CREATED_DATE));
            return edit;
        }
	}
	private static final class BlogMapper implements RowMapper<Blog> {
        public Blog mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final Blog blog = new Blog(rs.getInt(Blog.DB_ID), rs.getInt(Blog.DB_USER_ID),
        			rs.getString(Blog.DB_USER_NAME), rs.getString(Blog.DB_USER_EMAIL), 
        			rs.getString(Blog.DB_TITLE), rs.getString(Blog.DB_TEXT), rs.getTimestamp(Blog.DB_CREATED_DATE), 
        			rs.getInt(Blog.DB_FOUND_PUBLISHED), rs.getInt(Blog.DB_VIEWED_PUBLISHED), rs.getTimestamp(Blog.DB_MODIFIED_DATE),
        			rs.getInt(Blog.DB_FOUND_MODIFIED), rs.getInt(Blog.DB_VIEWED_MODIFIED));
            return blog;
        }
	}
	private static final class BlogCommentMapper implements RowMapper<BlogComment> {
        public BlogComment mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final BlogComment comment = new BlogComment(rs.getInt(BlogComment.DB_ID), 
        			rs.getInt(BlogComment.DB_BLOG_ID), rs.getInt(BlogComment.DB_USER_ID),
        			rs.getString(BlogComment.DB_USER_NAME), rs.getString(BlogComment.DB_USER_EMAIL), 
        			rs.getString(BlogComment.DB_TITLE), rs.getString(BlogComment.DB_TEXT), 
        			rs.getTimestamp(BlogComment.DB_CREATED_DATE), rs.getInt(BlogComment.DB_FOUND_PUBLISHED), 
        			rs.getInt(BlogComment.DB_VIEWED_PUBLISHED), rs.getTimestamp(BlogComment.DB_MODIFIED_DATE),
        			rs.getInt(BlogComment.DB_FOUND_MODIFIED), rs.getInt(BlogComment.DB_VIEWED_MODIFIED));
            return comment;
        }
	}
	private static final class ContactMapper implements RowMapper<Contact> {
        public Contact mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final Contact contact = new Contact(rs.getInt(Contact.DB_ID), rs.getInt(Contact.DB_REQUEST_ID),
        			rs.getString(Contact.DB_ROLE_NAME), rs.getString(Contact.DB_EMAIL_TO), 
        			rs.getString(Contact.DB_EMAIL_FROM), rs.getString(Contact.DB_SUBJECT),
        			rs.getString(Contact.DB_TEXT), rs.getString(Contact.DB_CONTACT_TOKEN));
            return contact;
        }
	}
	private static final class FindRequestsMapper implements RowMapper<Request> {
		private String roleName;
		public FindRequestsMapper(String roleName){
			this.roleName = roleName;
		}
        public Request mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final Request request = new Request(rs.getInt(Request.DB_ID), rs.getString(Request.DB_TITLE), 
        			                            rs.getString(Request.DB_REQUEST));
        	request.setRoleName(roleName);
            return request;
        }
	}
	private static final class RequestMapper implements RowMapper<Request> {
		private String roleName;
		private String roleIdColumnName;
		public RequestMapper(String roleName, String roleIdColumnName){
			this.roleName = roleName;
			this.roleIdColumnName = roleIdColumnName;
		}
        public Request mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final Request request = new Request(rs.getInt(Request.DB_ID), rs.getString(Request.DB_TITLE), 
        			rs.getString(Request.DB_REQUEST), rs.getTimestamp(Request.DB_CREATED_DATE), rs.getInt(Request.DB_FOUND_PUBLISHED),
        			rs.getInt(Request.DB_VIEWED_PUBLISHED), rs.getTimestamp(Request.DB_MODIFIED_DATE), rs.getInt(Request.DB_FOUND_MODIFIED),  
        			rs.getInt(Request.DB_VIEWED_MODIFIED), rs.getInt(roleIdColumnName));
        	request.setRoleName(roleName);
            return request;
        }
	}
	private static final class TwoValuesMapper implements RowMapper<List<String>> {
        public List<String> mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	List<String> l = new ArrayList<String>();
        	l.add(rs.getString(1));
        	l.add(rs.getString(2));
            return l;
        }
	}
	private String logRequestHeaders(HttpServletRequest request){
		List<String> list = Collections.list(request.getHeaderNames());
		StringBuffer sb = new StringBuffer();
		if(list.size() >0 ){
			if(request != null){
				sb.append("[");
		    	log.debug("---------------Start headers-------------------");
		    	for(String name: list){
		    		String header = name+"="+request.getHeader(name);
		    		sb.append(header).append(",");
		    		log.debug(header);
		    	}
		    	log.debug("---------------End headers-------------------");
				sb.append("]");
			}
		}
		return sb.toString();
	}
    private String getRemoteIPAddress(HttpServletRequest request) {
    	String ip;
    	if (request.getHeader("X_FORWARDED_FOR") != null){
    		ip = request.getHeader("X_FORWARDED_FOR");
    	}
    	else if (request.getHeader("HTTP_X_FORWARDED_FOR") != null) {
    		ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    	}
    	else if (request.getHeader("X-FORWARDED-FOR") != null) {
    		ip = request.getHeader("X-FORWARDED-FOR");
    	}
    	else if (request.getHeader("HTTP-X-FORWARDED-FOR") != null) {
    		ip = request.getHeader("HTTP-X-FORWARDED-FOR");
    	}
    	else if (request.getHeader("REMOTE_ADDR") != null) {
    		ip = request.getHeader("REMOTE_ADDR");
    	}
    	else if (request.getHeader("HTTP_X_FORWARDED_FOR") != null) {
    		ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    	}
    	else if (request.getHeader("HTTP_X_FORWARDED_FOR") != null) {
    		ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    	}
    	else{
    		ip = request.getRemoteAddr();
    	}
        return ip;
    }
    
}



