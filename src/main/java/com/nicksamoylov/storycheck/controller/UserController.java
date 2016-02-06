package com.nicksamoylov.storycheck.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nicksamoylov.storycheck.domain.News;
import com.nicksamoylov.storycheck.domain.SampleData;
import com.nicksamoylov.storycheck.domain.users.Blog;
import com.nicksamoylov.storycheck.domain.users.BlogComment;
import com.nicksamoylov.storycheck.domain.users.Contact;
import com.nicksamoylov.storycheck.domain.users.Request;
import com.nicksamoylov.storycheck.domain.users.Role;
import com.nicksamoylov.storycheck.domain.users.User;
import com.nicksamoylov.storycheck.service.EmailService;
import com.nicksamoylov.storycheck.service.MiscService;
import com.nicksamoylov.storycheck.service.UserService;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.StringUtils;
import com.nicksamoylov.storycheck.utils.Utils;

@Controller
@RequestMapping({"/user"})
public class UserController extends Base{
	private static final Logging log = Logging.getLog("UserController");

    @RequestMapping("/blogcommentedit")
    public String blogcommentedit(HttpServletRequest httpRequest, final Locale locale, final Model model, int id, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("blogcommentedit", isRussian, "id="+id);
    	log.debug(m);
    	boolean isWriter = false;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(user.getId() > 90){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			isWriter = user.isWriter();
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
		BlogComment comment = userService.findBlogCommentById(id);
		if(comment == null){
			return blogs(httpRequest, locale, model, ra);
		}
    	String dest = "user/blogcommentedit"; 
		model.addAttribute("comment", comment);
		model.addAttribute("iswriter", isWriter);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id);
		return dest;
    }
    @RequestMapping("/blogcommentsave")
    public String blogcommentsave(HttpServletRequest httpRequest, @Valid BlogComment comment, final BindingResult bindingResult,  
    		int blogId, final Locale locale, final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("blogcommentsave", isRussian, "id="+comment.getId());
    	log.debug(m+": "+comment);
    	boolean isWriter = false;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(user.getId() > 90){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			isWriter = user.isWriter();
			boolean isError = false;
			String text = comment.getId() > 0?comment.getTextEdit():comment.getText();
			if(text == null || text.trim().length() < BlogComment.DB_TEXT_MIN_LENGTH){
				String msg = getMessage("Length.blogComment.short", locale);
	    		model.addAttribute("error", msg);
				isError = true;
			}
			else if(text.trim().length() > BlogComment.DB_TEXT_MAX_LENGTH){
				String msg = getMessage("Length.blogComment.long", locale);
	    		model.addAttribute("error", msg);
				isError = true;
	    	}
	        if(isError || bindingResult.hasErrors()) {
	        	if(comment.getId() > 0){
		        	BlogComment commentEdit = userService.findBlogCommentById(comment.getId());
		        	commentEdit.setText(text);
		        	String dest = "user/blogcommentedit"; 
		    		model.addAttribute("comment", commentEdit);
		    		model.addAttribute("iswriter", isWriter);
		        	model.addAttribute("resubmitUri", "/"+dest+"?id="+comment.getId());
		    		return dest;
	        	}
	        	else {
		    		Blog blog = userService.findBlogById(blogId);
		    		if(blog == null){
		    			return blogs(httpRequest, locale, model, ra);
		    		}
		    		blog.setIsEditable(user);
		    		List<BlogComment> comments = userService.findBlogComments(user, blog.getId());
		        	String dest = "user/blogview"; 
		    		model.addAttribute("blog", blog);
		    		model.addAttribute("comments", comments);
		    		model.addAttribute("comment", comment);
		    		model.addAttribute("iswriter", isWriter);
		        	model.addAttribute("resubmitUri", "/"+dest+"?id="+blogId);
		    		return dest;
	        	}
	    	}
	        else {
	        	comment.setText(text);
	        	userService.saveBlogComment(comment, user, isRussian, emailService);
	        }
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
		Blog blog = userService.findBlogById(blogId);
		if(blog == null){
			return blogs(httpRequest, locale, model, ra);
		}
		blog.setIsEditable(user);
		List<BlogComment> comments = userService.findBlogComments(user, blog.getId());
    	String dest = "user/blogview"; 
		model.addAttribute("blog", blog);
		model.addAttribute("comments", comments);
		model.addAttribute("comment", new BlogComment());
		model.addAttribute("iswriter", isWriter);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+blogId);
		return dest;
    }
    @RequestMapping("/blogview")
    public String blogview(HttpServletRequest httpRequest, final Locale locale, final Model model, 
    		int id, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("blogview", isRussian, "id="+id);
    	log.debug(m);
    	boolean isWriter = false;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(user.getId() > 90){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			isWriter = user.isWriter();
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
		Blog blog = userService.findBlogById(id);
		if(blog == null){
			return blogs(httpRequest, locale, model, ra);
		}
		userService.updateViewedCount(user.getId(), blog);
		blog.setIsEditable(user);
		List<BlogComment> comments = userService.findBlogComments(user, blog.getId());
		userService.updateViewedCount(user.getId(), comments);
    	String dest = "user/blogview"; 
		model.addAttribute("blog", blog);
		model.addAttribute("comments", comments);
		model.addAttribute("comment", new BlogComment());
		model.addAttribute("iswriter", isWriter);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id);
		return dest;
    }
    @RequestMapping("/blogsave")
    public String blogsave(HttpServletRequest httpRequest, @Valid Blog blog, final BindingResult bindingResult, 
    		final Locale locale, final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("blogsave", isRussian, "id="+blog.getId());
    	log.debug(m+": "+blog);
    	boolean isWriter = false;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(user.getId() > 90){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			isWriter = user.isWriter();
			boolean isError = false;
			if(blog.isTextEmpty() || blog.getTextEdit().length() < Blog.DB_TEXT_MIN_LENGTH){
				String msg = getMessage("Length.blog.short", locale);
	    		model.addAttribute("error", msg);
				isError = true;
			}
			else if(blog.getTextEdit().length() > Blog.DB_TEXT_MAX_LENGTH){
				String msg = getMessage("Length.blog.long", locale);
	    		model.addAttribute("error", msg);
				isError = true;
	    	}
	        if(isError || bindingResult.hasErrors()) {
	        	String dest = "user/blogedit"; 
	        	Blog blogEdit = blog.getId() < 1? blog: userService.findBlogById(blog.getId());
	        	blogEdit.setTextEdit(blog.getTextEdit());
	    		model.addAttribute("blog", blogEdit);
	    		model.addAttribute("iswriter", isWriter);
	        	model.addAttribute("resubmitUri", blog.getId()>0?("/"+dest+"?id="+blog.getId()):("/user/blogadd"));
	    		return dest;
	    	}
	        else {
	        	userService.saveBlog(blog, user, isRussian, emailService);
	        }
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
		return blogs(httpRequest, locale, model, ra);
    }
    @RequestMapping("/blogedit")
    public String blogedit(HttpServletRequest httpRequest, final Locale locale, final Model model, int id, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("blogedit", isRussian, "id="+id);
    	log.debug(m);
    	boolean isWriter = false;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(user.getId() > 90){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			isWriter = user.isWriter();
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
		Blog blog = userService.findBlogById(id);
		if(blog == null){
			return blogs(httpRequest, locale, model, ra);
		}
    	String dest = "user/blogedit"; 
		model.addAttribute("blog", blog);
		model.addAttribute("iswriter", isWriter);
    	model.addAttribute("resubmitUri", "/user/blogadd");
		return dest;
    }
    @RequestMapping("/blogadd")
    public String blogadd(HttpServletRequest httpRequest, final Locale locale, final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("blogadd", isRussian, null);
    	log.debug(m);
    	boolean isWriter = false;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(user.getId() > 90){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			isWriter = user.isWriter();
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
    	String dest = "user/blogedit"; 
		model.addAttribute("blog", new Blog());
		model.addAttribute("iswriter", isWriter);
    	model.addAttribute("resubmitUri", "/user/blogadd");
		return dest;
    }
    @RequestMapping("/blogsearch")
    public String blogsearch(HttpServletRequest httpRequest, Locale locale, Model model, Blog blog, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("blogsearch", isRussian, "key="+blog.getSearch());
    	log.debug(m);
    	boolean isWriter = false;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(user.getId() > 90){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			isWriter = user.isWriter();
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
    	List<Blog> blogs = this.userService.findBlogs(user.getId(), blog.getSearch(), isRussian);
    	String dest = "user/blogs"; 
		model.addAttribute("blogs", blogs);
		model.addAttribute("blog", new Blog());
		model.addAttribute("noblogs", blogs.size()>0?false:getMessage("blog.not.found", new Object[]{blog.getSearch()}, locale));
		model.addAttribute("iswriter", isWriter);
    	model.addAttribute("resubmitUri", "/"+dest);
		return dest;
    }
    @RequestMapping("/blogs")
    public String blogs(HttpServletRequest httpRequest, Locale locale, Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("blogs", isRussian, null);
    	log.debug(m);
    	boolean isWriter = false;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(user.getId() > 90){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			isWriter = user.isWriter();
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
    	List<Blog> blogs = this.userService.findBlogs(user.getId(), isRussian);
    	String dest = "user/blogs"; 
		model.addAttribute("blogs", blogs);
		model.addAttribute("blog", new Blog());
		model.addAttribute("noblogs", blogs.size()>0?false:getMessage("blog.not.published", locale));
		model.addAttribute("iswriter", isWriter);
    	model.addAttribute("resubmitUri", "/"+dest);
		return dest;
    }
	@RequestMapping("/requestanswersend")
    public String requestemairequestanswersendlsend(HttpServletRequest httpRequest, final Locale locale, final Model model, 
    		Request request, int uid, int uid2, String r, String p, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("requestanswersend", isRussian, r, "reqId="+request.getId()+",uid="+uid+",uid2="+uid2);
    	log.debug(m);    	
		User user = userService.findUserById(uid);
		User user2 = userService.findUserById(uid2);
		if(!Role.roleExists(r) || user.getId() < 90 || user2.getId() < 90){
			String msg = "Role.roleExists(r)="+Role.roleExists(r)+", user="+uid+" ("+user.display()+") or user2="+uid2+" ("+user2.display()+") not found";
    		userService.recordVisitor(user.getUsername(), m, msg, httpRequest);
    		emailService.sendProblemToAdminWithLogAttached(user, msg);
			userService.recordVisit(m, httpRequest);
    		return showMessage(model, MSG_THANK_YOU, locale, false);
		}
		userService.recordVisit(m, user.getUsername());
		Request req = this.userService.findRequestById(r, request.getId(), isRussian);
		String emailText = request.getEmailText();
		req.setEmailText(emailText);
		if(emailText == null || emailText.trim().length() < Contact.DB_TEXT_MIN_LENGTH){
			String msg = getMessage("Length.request.email.short", locale);
    		model.addAttribute("error", msg);
	    	String dest = "user/requestanswer";
			model.addAttribute("role", r);
			model.addAttribute("userid", uid);
			model.addAttribute("userid2", uid2);
			model.addAttribute("request", req);
			model.addAttribute("iswriter", false);
	    	model.addAttribute("resubmitUri", "/user/requestanswer?p="+p);
	        return dest;
		}				
		userService.saveContactAndSendEmail(user.getUsername(), user2.getUsername(), req, locale, emailService);
		return showMessage(model, MSG_EMAIL_SENT_REQUEST, locale, false);
    }
    @RequestMapping("/requestanswer")
    public String requestanswer(HttpServletRequest httpRequest, final Model model, final Locale locale, final String p) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("requestanswer", isRussian, "p="+p);
    	log.debug(m);  
    	//email_from A, email_to B, p
    	//email_from B, email_to C, p1
    	//email_from B, email_to A, p2
    	//email_from D, email_to C, p3
    	//The thread is identified by A+B order by date
    	//p identifies the caller by email_t o
    	//find role_name, request_id, email_from, email_to, subject, text by p
    	Contact contact = userService.findContactByToken(p);
    	if(contact == null){
    		userService.recordVisitor(m, httpRequest);
    		emailService.sendTextToAdminWithLogAttached(new User(), "Contact not found", m);
    		return showMessage(model, MSG_CONTACT_NOT_FOUND, locale, false);
    	}
		User user = userService.findUserByUsername(contact.getEmailTo());
		userService.recordVisitor(user.getUsername(), m, user.display(), httpRequest);
		User user2 = userService.findUserByUsername(contact.getEmailFrom());
		if(user.getId() < 90 || user2.getId() < 90){
			String msg = "User="+contact.getEmailTo()+" ("+user.display()+") or user2="+contact.getEmailFrom()+" ("+user2.display()+") not found";
			userService.recordVisitor(user.getUsername(), m, msg, httpRequest);
    		emailService.sendProblemToAdminWithLogAttached(user, msg);
    		return showMessage(model, MSG_CONTACT_NOT_FOUND, locale, false);
		}
		Request request = userService.findRequestById(contact.getRoleName(), contact.getRequestId(), isRussian);
    	request.setTitle(contact.getSubject());
    	request.setRequestText(contact.getText());
    	String dest = "user/requestanswer";
		model.addAttribute("p", p);
		model.addAttribute("role", request.getRole().getShort());
		model.addAttribute("userid", user.getId());
		model.addAttribute("userid2", user2.getId());
		model.addAttribute("request", request);
		model.addAttribute("iswriter", false);
    	model.addAttribute("resubmitUri", "/user/requestanswer?p="+p);
        return dest;
    }
	@RequestMapping("/requestemailsend")
    public String requestemailsend(HttpServletRequest httpRequest, final Locale locale, final Model model, 
    		Request request, int uid, String r, String r2, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("requestemailsend", isRussian, r, r2, "uid="+uid);
    	log.debug(m);    	
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		User user2 = userService.findUserById(uid);
		if(!Role.roleExists(r) || !Role.roleExists(r2) || user.getId() < 90 || user2.getId() < 90){
			userService.recordVisit(m, httpRequest);
			return requestsfind(httpRequest, locale, model, r, r2, ra);
		}
		userService.recordVisit(m, user.getUsername());
		user2 = userService.findUserByUsername(user2.getUsername());
		if(!user.hasRole(r) || !user2.hasRole(r2)){
			userService.recordVisitor(user.getUsername(), m, msgUsersRoles(null,r, r2, user, user2), httpRequest);
    		emailService.sendProblemToAdminWithLogAttached(user, msgUsersRoles(m,r, r2, user, user2));
			return requests(httpRequest, locale, model, r, ra); 
		}
		Request req = userService.findRequestById(r2, request.getId(), isRussian);
		//req has to have user (email), to whom the message is sent
		String emailText = request.getEmailText();
		req.setEmailText(emailText);
		if(emailText == null || emailText.trim().length() < 10){
			String msg = getMessage("Length.request.email.short", locale);
    		model.addAttribute("error", msg);
        	String dest = "user/requestview"; 
    		model.addAttribute("role", r);
    		model.addAttribute("role2", r2);
    		model.addAttribute("userid", uid);
    		model.addAttribute("request", req);
    		model.addAttribute("iswriter", user.isWriter());
        	model.addAttribute("resubmitUri", false);
    		return dest;
		}				
		userService.saveContactAndSendEmail(user.getUsername(), user2.getUsername(), req, locale, emailService);
		return showMessage(model, MSG_EMAIL_SENT_REQUEST, locale, false);
    }
    private String msgUsersRoles(String m, String r, String r2, User user, User user2){
    	return (m==null?"":m+": ") + "User "+user.getId()+" is authorized, user "+user2.getId()
        				   + " exists, roles exist, but user.hasRole(r)="
			               + user.hasRole(r)+" and user2.hasRole(r2)="+user2.hasRole(r2)
			               +",user="+user.display()+",user2="+user.display();
    }
    @RequestMapping("/requestview")
    public String requestview(HttpServletRequest httpRequest, final Locale locale, final Model model, 
    		int id, String r, String r2, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("requestview", isRussian, r, r2, "id="+id);
    	log.debug(m);
    	int userId = -1;
    	boolean isWriter = false;
		Request request;
		if(!Role.roleExists(r) || !Role.roleExists(r2)){
			userService.recordVisit(m, httpRequest);
			request = SampleData.request(isRussian, r);
		}
		else {
			User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
			if(user.getId() > 90){
				if(user.hasRole(r)){
					userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
					request = this.userService.findRequestById(r2, id, isRussian);
					this.userService.updateRequestViewCounters(request);
					userId = request.getUser().getId();
					isWriter = user.isWriter();
				}
				else {
					userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
	    			userService.recordVisitor(user.getUsername(), m, msgUserRole(null, r, user), httpRequest);
	        		emailService.sendProblemToAdminWithLogAttached(user, msgUserRole(m, r, user));
					return requests(httpRequest, locale, model, r, ra); 
				}
			}
			else {
				userService.recordVisit(m, httpRequest);
				request = this.userService.findRequestByIdAnyUser(r2, id, isRussian);
				this.userService.updateRequestViewCounters(request);
			}
		}
    	String dest = "user/requestview"; 
		model.addAttribute("role", r);
		model.addAttribute("role2", r2);
		model.addAttribute("userid", userId);
		model.addAttribute("request", request);
		model.addAttribute("iswriter", isWriter);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id+"&r="+r+"&r2="+r2);
		return dest;
    }
    @RequestMapping("/requestsfind")
    public String requestsfind(HttpServletRequest httpRequest, final Locale locale, final Model model, 
    		String r, String r2, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("requestsfind", isRussian, r, r2);
    	log.debug(m);
    	boolean isWriter = false;
		List<Request> requests;
		if(!Role.roleExists(r) || !Role.roleExists(r2)){
			userService.recordVisit(m, httpRequest);
			requests = new ArrayList<Request>(); 
		}
		else {
			User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
			if(user.getId() > 90){
				if(user.hasRole(r)){
					userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
					isWriter = user.isWriter();
				}
				else {
					userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
	    			userService.recordVisitor(user.getUsername(), m, msgUserRole(null, r, user), httpRequest);
	        		emailService.sendProblemToAdminWithLogAttached(user, msgUserRole(m, r, user));
	        		String dest = "index";
	        		logout(httpRequest, SecurityContextHolder.getContext());
	        		model.addAttribute("resubmitUri", "/"+dest);
			        return dest;
				}
			}
			else {
				userService.recordVisit(m, httpRequest);
			}
			if("w".equals(r)){
				requests = userService.findRequestsByRole(user, r2, isRussian);
			}
			else {
				String[] localizedKeywords = localizeKeywords(r, locale);
				requests = userService.findWriterRequestsByKeywords(user, localizedKeywords, isRussian);
			}
		}
    	String dest = "user/requestsfind"; 
		model.addAttribute("role", r);
		model.addAttribute("asrole", Role.getAsRole(r));
		model.addAttribute("requests", requests);
		model.addAttribute("iswriter", isWriter);
    	model.addAttribute("resubmitUri", "/"+dest+"?r="+r+"&r2="+r2);
		return dest;
    }
    @RequestMapping("/request")
    public String request(HttpServletRequest httpRequest, final Locale locale, final Model model, int id, String r, 
    		RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("request", isRussian, r+",id="+id);
    	log.debug(m);    	
		Request request;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(Role.roleExists(r) && user.getId() > 0){
			if(user.hasRole(r)){
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
				request = this.userService.findRequestById(r, id, isRussian);
			}
			else {
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
    			userService.recordVisitor(user.getUsername(), m, msgUserRole(null, r, user), httpRequest);
        		emailService.sendProblemToAdminWithLogAttached(user, msgUserRole(m, r, user));
				return requests(httpRequest, locale, model, r, ra); 
			}
		}
		else {
			userService.recordVisit(m, httpRequest);
			request = SampleData.request(isRussian, r);
		}
    	String dest = "user/request"; 
		model.addAttribute("role", r);
		model.addAttribute("request", request);
		model.addAttribute("iswriter", user.isWriter());
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id+"&r="+r);
		return dest;
    }
    @RequestMapping("/requestedit")
    public String requestedit(HttpServletRequest httpRequest, @RequestParam String buttonValue, final Locale locale, 
    		final Model model, int id, String r, RedirectAttributes ra) {
    	if("requestdel".equals(buttonValue)){
    		return requestdel(httpRequest, locale, model, id, r, "", ra);
    	}
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("requestedit", isRussian, r+",id="+id);
    	log.debug(m);    	
		Request request;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(Role.roleExists(r) && user.getId() > 0){
			if(user.hasRole(r)){
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
				request = this.userService.findRequestById(r, id, isRussian);
			}
			else {
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
    			userService.recordVisitor(user.getUsername(), m, msgUserRole(null, r, user), httpRequest);
        		emailService.sendProblemToAdminWithLogAttached(user, msgUserRole(m, r, user));
				return requests(httpRequest, locale, model, r, ra); 
			}
		}
		else {
			userService.recordVisit(m, httpRequest);
			request = SampleData.request(isRussian, r);
		}
    	String dest = "user/requestedit"; 
		model.addAttribute("role", r);
		model.addAttribute("request", request);
		if(Role.getRoleKeywords(r) != null){
			model.addAttribute("keywordswarning", getKeywordsWarningMessage(r, locale));
		}
		model.addAttribute("iswriter", user.isWriter());
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id+"&r="+r+"&buttonValue="+buttonValue);
    	//model.addAttribute("resubmitUri", request.getId()>0?("/"+dest+"?id="+request.getId()+"&r="+r):("/user/requestadd?r="+r));
		return dest;
    }
    @RequestMapping("/requestdel")
    public String requestdel(HttpServletRequest httpRequest, final Locale locale, final Model model, 
    		int id, String r, String confirm, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("requestdel", isRussian, r+",id="+id+",confirm="+confirm);
    	log.debug(m);    	
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(Role.roleExists(r) && user.getId() > 0){
			if(user.hasRole(r)){
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
				Request request = this.userService.findRequestById(r, id, isRussian);
		        if(StringUtils.isEmpty(confirm)){
		        	model.addAttribute("namedelete", request.getTitle());
		    		model.addAttribute("deleteUri","/user/requestdel?id="+id+"&r="+r);
		        	return request(httpRequest, locale, model, id, r, ra);
		        }
		        else {
		    		userService.deleteRequest(request);
		        }
			}
			else {
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
    			userService.recordVisitor(user.getUsername(), m, msgUserRole(null, r, user), httpRequest);
        		emailService.sendProblemToAdminWithLogAttached(user, msgUserRole(m, r, user));
				return requests(httpRequest, locale, model, r, ra); 
			}
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
    	return requests(httpRequest, locale, model, r, ra);
    }
    private static final String divText = "<"+Request.DB_REQUEST_MAX_LENGTH+">";
    @RequestMapping("/requestsave")
    public String requestsave(HttpServletRequest httpRequest, @Valid final Request request, final BindingResult bindingResult, 
    		final Locale locale, final Model model, String r, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("requestsave", isRussian, r, "id="+request.getId());
    	log.debug(m+":"+request);    	
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(Role.roleExists(r) && user.getId() > 0){
			if(user.hasRole(r)){
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
				boolean isError = false;
				if(request.isRequestEmpty() || request.getRequestText().length() < Request.DB_REQUEST_MIN_LENGTH){
					String msg = getMessage("Length.request.short", locale);
		    		model.addAttribute("error", msg);
					isError = true;
				}
				else if(request.getRequestText().length() > Request.DB_REQUEST_MAX_LENGTH){
					String msg = getMessage("Length.request.long", locale);
		    		model.addAttribute("error", msg);
		    		String text = request.getRequestText();
					String str1 = StringUtils.substring(text, Request.DB_REQUEST_MAX_LENGTH);
					String str2 = text.substring(Request.DB_REQUEST_MAX_LENGTH);
					request.setRequestText(str1+divText+str2);
					isError = true;
		    	}
		        if(isError || bindingResult.hasErrors()) {
		        	String dest = "user/requestedit"; 
		    		model.addAttribute("role", r);
		    		model.addAttribute("request", request);
		    		model.addAttribute("iswriter", user.isWriter());
		        	model.addAttribute("resubmitUri", request.getId()>0?("/"+dest+"?id="+request.getId()+"&r="+r):("/user/requestadd?r="+r));
		    		return dest;
		    	}
		        else {
					String searchText = (request.getTitle() + " " + request.getRequestText()).toUpperCase();
					if(isKeywordFound(searchText, r, locale)){
						request.setLang(Utils.lang(isRussian));
			        	userService.saveRequest(r, user, request);
					}
					else {
			    		String dest = "user/requestedit"; 
			    		model.addAttribute("error", getKeywordsMissingMessage(r, locale));
			    		model.addAttribute("role", r);
			    		model.addAttribute("request", request);
			    		model.addAttribute("iswriter", user.isWriter());
			        	model.addAttribute("resubmitUri", request.getId()>0?("/"+dest+"?id="+request.getId()+"&r="+r):("/user/requestadd?r="+r));
			    		return dest;
					}
		        }
			}
			else {
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
    			userService.recordVisitor(user.getUsername(), m, msgUserRole(null, r, user), httpRequest);
        		emailService.sendProblemToAdminWithLogAttached(user, msgUserRole(m, r, user));
			}
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
		return requests(httpRequest, locale, model, r, ra);
    }
    @RequestMapping("/requestadd")
    public String requestadd(HttpServletRequest httpRequest, final Locale locale, final Model model, String r, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("requestadd", isRussian, r, null);
    	log.debug(m);    	
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(Role.roleExists(r) && user.getId() > 0){
			if(user.hasRole(r)){
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
				Role role = new Role(r, user);
		    	String dest = "user/requestedit"; 
				model.addAttribute("role", r);
				model.addAttribute("request", new Request(role));
				if("w".equals(r)){
					model.addAttribute("keywordswarning", getKeywordsWarningMessage(r, locale));
				}
				model.addAttribute("iswriter", user.isWriter());
		    	model.addAttribute("resubmitUri", "/user/requestadd?r="+r);
				return dest;
			}
			else {
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
    			userService.recordVisitor(user.getUsername(), m, msgUserRole(null, r, user), httpRequest);
        		emailService.sendProblemToAdminWithLogAttached(user, msgUserRole(m, r, user));
			}
		}
		else {
			userService.recordVisit(m, httpRequest);
		}
		return requests(httpRequest, locale, model, r, ra); 
    }
    @RequestMapping("/requests")
    public String requests(HttpServletRequest httpRequest, final Locale locale, final Model model, String r, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("requests", isRussian, r, null);
    	log.debug(m);    	
		List<Request> requests;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(Role.roleExists(r) && user.getId() > 0){
			if(user.hasRole(r)){
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
				requests = this.userService.findRequestsByUserIdOrderById(r, user, isRussian);
			}
			else {
				userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
    			userService.recordVisitor(user.getUsername(), m, msgUserRole(null, r, user), httpRequest);
        		emailService.sendProblemToAdminWithLogAttached(user, msgUserRole(m, r, user));
				requests = new ArrayList<Request>(); 
			}
		}
		else {
			userService.recordVisit(m, httpRequest);
			requests = SampleData.requests(isRussian, r);
		}
    	String dest = "user/requests"; 
		model.addAttribute("role", r);
		model.addAttribute("asrole", Role.getAsRole(r));
		model.addAttribute("requests", requests);
		model.addAttribute("iswriter", user.isWriter());
    	model.addAttribute("resubmitUri", "/"+dest+"?r="+r);
		return dest;
    }
    private String msgUserRole(String m, String r, User user){
    	return (m==null?"":m+": ") + "User is authorized, role "+r+" exists, but does not belong to "+user.display();
    }
	@RequestMapping({"/confirm"})
    public String confirm(HttpServletRequest request, final Locale locale, final Model model, final String p) {
    	log.debug("confirm("+p+")");
		if(p != null){
			User user = userService.findUserByPasswordInTemp(p);
	    	log.debug("confirm("+p+"): user="+user);
			if(user != null){
				if(user.getModifiedDate() == null){
	    			if(userService.isRegistrationExpired(user)){
	            		String dest =  showMessage(model, MSG_REG_AGAIN, locale, false);
	            		logout(request, SecurityContextHolder.getContext());
	            		return dest;
	    			}
	    			else {
	    				emailService.sendTextToAdminWithLogAttached(user, "New "+user.display(), user.toString());
	    				String dest = "login";
	            		model.addAttribute("resubmitUri", "/"+dest);
	    				return dest;
	    			}
				}
				else {
	    			if(userService.isEmailChangeExpired(user)){
	            		String dest =  showMessage(model, MSG_EMAIL_CHANGE_AGAIN, locale, false);
	            		logout(request, SecurityContextHolder.getContext());
	            		model.addAttribute("resubmitUri", "/"+dest);
	            		return dest;
	    			}
	    			else {
	            		String dest =  showMessage(model, MSG_EMAIL_CHANGED, locale, false);
	            		logout(request, SecurityContextHolder.getContext());
	            		model.addAttribute("resubmitUri", "/"+dest);
	            		return dest;
	    			}
				}
			}
		}
    	String dest = "index";
		model.addAttribute("resubmitUri", "/"+dest);
        return dest;
	}

	@RequestMapping({"/useredit"})
    public String useredit(HttpServletRequest request, final Locale locale, final Model model) {
		boolean isRussian = Utils.isRussian(locale);
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
    	String m = m("useredit", isRussian, "id="+user.getId());
    	log.debug(m);
		if(user.getId() > 0){
			userService.recordVisitor(SecurityContextHolder.getContext().getAuthentication().getName(), m, user.display(), request);
		}
		else {
			userService.recordVisitor(m, request);
		}
		String dest = "user/useredit";
    	model.addAttribute("dontshowlogin", true);
		model.addAttribute("user", user);
    	model.addAttribute("resubmitUri", "/"+dest);
        return dest;
    }
    
    @RequestMapping({"/useradd"})
    public String useradd(HttpServletRequest request, final Locale locale, final Model model) {
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("useradd", isRussian, "");
    	log.debug(m+"()");
		//String userId = cookie;//getUserIdAndSetCookie(request, response);
		userService.recordVisitor(m, request);
    	String dest = "user/useredit";
    	model.addAttribute("dontshowlogin", true);
    	model.addAttribute("user", new User(locale));
    	model.addAttribute("resubmitUri", "/user/useradd");
        return dest;
    }

    @RequestMapping("/usersave")
    public String usersave(HttpServletRequest request, @Valid final User user, final BindingResult bindingResult, 
    		@RequestParam String buttonValue, final Locale locale, final Model model) {
    	if("userdelete".equals(buttonValue)){
    		model.addAttribute("id", user.getId());
    		model.addAttribute("confirmdelete", true);
    		return showMessage(model, MSG_REG_DELETE_WARN, locale, "/user/useredit");
    	}
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("usersave", isRussian, "id="+user.getId());
    	log.debug(m); 
		userService.recordVisitor(user.getUsername(), m, user.toString(), request);
    	User oldUser = null;
    	boolean usernameChanged = false;
    	if(user.getId() > 0){
        	oldUser = userService.findUserById(user.getId());
        	if(oldUser == null){
        		String msg = m+": user id="+user.getId()+", but user does not exist";
        		emailService.sendProblemToAdminWithLogAttached(user, msg);
    			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
    			String dest = "index";
        		model.addAttribute("resubmitUri", "/"+dest);
        		logout(request, SecurityContextHolder.getContext());
        		return dest;
        	}
    		usernameChanged = !oldUser.getUsername().equals(user.getUsername());
    	}
    	else {
    		if(StringUtils.isEmpty(user.getPassword())){
    			bindingResult.rejectValue("password", "NotEmpty.user.password", "Password is required");
    		}
    		else if(user.getPassword().length() < 7){
    			bindingResult.rejectValue("password", "Length.user.password", "Password is expected to be 7 characters long at least");
    		}
    		if(StringUtils.isEmpty(user.getPasswordAgain())){
    			bindingResult.rejectValue("passwordAgain", "NotEmpty.user.passwordAgain", "Password is required");
    		}
    		else if(user.getPasswordAgain().length() < 7){
    			bindingResult.rejectValue("passwordAgain", "Length.user.passwordAgain", "Password is expected to be 7 characters long at least");
    		}
    		if(!bindingResult.hasErrors() && !user.getPassword().equals(user.getPasswordAgain())) {
    			bindingResult.rejectValue("password", "user.passwords.nomatch", "Password and Password again do not match");
    		}
    	}
		if(userService.usernameExists(user.getId(), user.getUsername())){
			bindingResult.rejectValue("username", "exists.user.email", "This email exists already");
		}
		if(user.getRoles() == null || user.getRoles().size() == 0){
			bindingResult.rejectValue("roles", "NotEmpty.user.roles", "Roles selection is required");
		}
        if(bindingResult.hasErrors()) {
        	String dest = "user/useredit";
    		model.addAttribute("user", user);
        	model.addAttribute("resubmitUri", "/"+dest);
            return dest;
    	}
    	if(user.getId() > 0){
    		user.setPassword(oldUser.getPassword());
        	if(usernameChanged){
        		String newEmail = user.getUsername();
        		String tempPassword = UUID.randomUUID().toString();
        		Set<String> delRoles = userService.saveUserTemp(user, tempPassword, oldUser.getUsername());
        		if(delRoles.size() > 0){
            		User newUser = userService.findUserByUsername(newEmail);
            		userService.deleteUserRoles(newUser, delRoles, emailService);
        		}
            	emailService.sendEmailUsernameChanged(oldUser.getUsername(), newEmail, tempPassword, locale);
    			String dest = showMessage(model, MSG_EMAIL_CHANGED_SENT, new Object[]{newEmail}, locale, false);
        		logout(request, SecurityContextHolder.getContext());
        		userService.recordVisitor(user.getUsername(), m, "Email changed from "+oldUser.getUsername()+" to "+newEmail, request);
    	        return dest;
        	}
        	Set<String> delRoles = userService.saveUser(user);
        	if(delRoles.size() == 0){
            	String dest = "user/options";
        		model.addAttribute("user", user);
        		model.addAttribute("moreoptions", true);
        		model.addAttribute("resubmitUri", "/moreoptions");
        		return dest;
        	}
        	else {
        		List<String> l = new ArrayList<String>();
        		for(String role:delRoles){
        			l.add(getMessage(role,locale));
        		}
        		String roles = DisplayUtils.commaSeparatedSorted(l);
        		model.addAttribute("roles", DisplayUtils.commaSeparatedSorted(delRoles));
        		model.addAttribute("confirmdeleteroles", true);
        		return showMessage(model, MSG_ROLES_DELETE_WARN, new Object[]{roles}, locale, "/user/useredit");
        	}
    	}
    	else {
        	user.setEnabled(true);
    		String tempPassword = UUID.randomUUID().toString();
        	userService.saveUserTemp(user, tempPassword);
        	emailService.sendEmailRegistration(user, tempPassword, locale);
    		return showMessage(model, MSG_EMAIL_REG_SENT, new Object[]{user.getUsername()}, locale, false);
    	}
    }
    
    @RequestMapping({"/userdeleteroles"})
    public String userdeleteroles(@RequestParam String confirm, final Locale locale, final Model model, 
    		String roles, HttpServletRequest request) {
		if("dontdelete".equals(confirm)){
			return useredit(request, locale, model);
		}
		boolean isRussian = Utils.isRussian(locale);
    	Set<String> delRoles = DisplayUtils.parseCommaSeparatedStrings(roles);
    	String m = m("userdeleteroles", isRussian, "roles="+delRoles);
		log.debug(m);
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUser(authentication);
    	if(isUserAuthorized(authentication)){
    		userService.recordVisitor(authentication.getName(), m, user.toString(), request);
    		userService.deleteUserRoles(user, delRoles, emailService);
    		user = userService.findUserByUsername(user.getUsername());
        	String dest = "user/options";
    		model.addAttribute("user", user);
    		model.addAttribute("moreoptions", true);
    		model.addAttribute("resubmitUri", "/moreoptions");
    		return dest;
    	}
    	else {
    		String msg = m+": unauthorized user attempted to delete user roles "+DisplayUtils.commaSeparated(delRoles);
    		emailService.sendProblemToAdminWithLogAttached(user, msg);
			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
	    	logout(request, SecurityContextHolder.getContext());
			return "index";
    	}
    }

    @RequestMapping({"/userdelete"})
    public String userdelete(@RequestParam String confirm, final Locale locale, final Model model, HttpServletRequest request) {
		if("dontdelete".equals(confirm)){
			return useredit(request, locale, model);
		}
		boolean isRussian = Utils.isRussian(locale);
    	String m = m("userdelete", isRussian, null);
		log.debug(m);
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUser(authentication);
    	if(isUserAuthorized(authentication)){
    		userService.recordVisitor(authentication.getName(), m, user.toString(), request);
    		userService.deleteUser(user, emailService);
    		emailService.sendEmailRegistrationDeleted(user, locale);
    	}
    	else {
    		String msg = m+": unauthorized user attempted to delete user profile "+user;
    		emailService.sendProblemToAdminWithLogAttached(user, msg);
			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
    	}
    	logout(request, SecurityContextHolder.getContext());
		return showMessage(model, MSG_GOODBYE, locale, false);
    }

	@Autowired
	private UserService userService;

	@Autowired
	private MiscService miscService;

    @Autowired 
    private EmailService emailService;

	public UserController() {
		super();
	}

    @ModelAttribute("news")
    public String news(final Locale locale) {
    	return this.miscService.findNews(News.AREA_HOME, Utils.isRussian(locale));
    }    

}
