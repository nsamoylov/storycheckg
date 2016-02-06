package com.nicksamoylov.storycheck.controller;

import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.ui.Model;

import com.nicksamoylov.storycheck.domain.users.Role;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.Utils;

public class Base {
	protected String m(String method, boolean isRussian, String p){
    	return method+"("+Utils.lang(isRussian)+(p==null || p.length()==0?"":","+p)+")";
    }
	protected String m(String method, boolean isRussian, String r, String r2){
    	return method+"("+Utils.lang(isRussian)+","+r+(r2==null || r2.length()==0?"":","+r2)+")";
    }
	protected String m(String method, boolean isRussian, String r, String r2, String p){
    	return method+"("+Utils.lang(isRussian)+","+r+(r2==null || r2.length()==0?"":","+r2)+(p==null || p.length()==0?"":","+p)+")";
    }
	protected static final String MSG_GOODBYE             = "msg.goodbye";
	protected static final String MSG_THANK_YOU           = "msg.thankyou";
	protected static final String MSG_ANSWER_COMPLETED    = "msg.answer.completed.already";
	protected static final String MSG_EMAIL_REG_SENT      = "msg.email.registration.sent";
	protected static final String MSG_EMAIL_REG_SEE_EMAIL = "msg.email.registration.see.email";
	protected static final String MSG_REG_AGAIN           = "msg.register.again";
	protected static final String MSG_REG_DELETE_WARN     = "msg.registration.delete.warn";
	protected static final String MSG_ROLES_DELETE_WARN   = "msg.roles.delete.warn";
	protected static final String MSG_EMAIL_SENT_LIMIT    = "msg.email.sent.limit";
	protected static final String MSG_EMAIL_CHANGED_SENT  = "msg.email.changed.sent";
	protected static final String MSG_EMAIL_CHANGE_AGAIN  = "msg.email.change.again";
	protected static final String MSG_EMAIL_CHANGED       = "msg.email.changed";
	protected static final String VALUE_EMAIL_TESTERS_MAP = "emailtestersmap";
	protected static final String MSG_EMAIL_SENT_REQUEST  = "msg.email.sent.request";
	protected static final String MSG_EMAIL_CHANGE_SEE_EMAIL = "msg.email.change.see.email";
	protected static final String MSG_CONTACT_NOT_FOUND   = "msg.contact.notfound";
	
	protected String showMessage(final Model model, final String key, final Locale locale, final Object resubmitUriValue) {
        return showMessage(model, null, getMessage(key, locale), locale, resubmitUriValue);
    }
    protected String showMessage(final Model model, final String key, final Object[] params, final Locale locale, final Object resubmitUriValue) {
        return showMessage(model, null, getMessage(key, params, locale), locale, resubmitUriValue);
    }
    protected String showMessage(final Model model, final String key, final Object value, final Locale locale, final Object resubmitUriValue) {
		String dest =  "message";
		if(key == null){
			model.addAttribute("message", value);
		}
		else {
			model.addAttribute(key, value);
		}
    	model.addAttribute("resubmitUri", resubmitUriValue);
    	model.addAttribute("returnhome", getMessage("returnhome", locale));
        return dest;
    }

    protected String getUserIdAndSetCookie(HttpServletRequest request, HttpServletResponse response){
       	String cookieName = "Storycheck.UserId";
       	String userId = null;
       	if(request.getCookies() != null){
       		for(Cookie c:request.getCookies()){
       			if(cookieName.equals(c.getName())){
       				userId = c.getValue();
       				break;
       			}
       		}
       	}
       	log.debug("*******userId="+userId);
       	if(userId == null){
       		userId = UUID.randomUUID().toString();
       		Cookie cookie = new Cookie(cookieName, userId);
       		cookie.setMaxAge(7*24*60*60);
       		cookie.setDomain(".Storycheck.com");
       		response.addCookie(cookie);
       	}
       	return userId;
    }
    protected boolean isKeywordFound(String searchText, String r, Locale locale){
    	if(!"w".equals(r)){
    		return true;
    	}
    	boolean isFound = false;
    	String[] keywords = Role.getRoleKeywords(r);
    	if(keywords == null){
    		isFound = true;
    	}
    	else {
    		for(String kw:keywords){
	        	String kwl = getMessage(kw, locale).toUpperCase();
				if(searchText.contains(kwl)){
					isFound = true;
					break;
				}		
    		}
    	}
    	return isFound;
    }
    protected String getKeywordsMissingMessage(String r, Locale locale){
		String kws = constructLocalizedKeywordsList(r, locale);
		return getMessage("keyword.request.missing", new Object[]{kws}, locale);
    }
    protected String getKeywordsWarningMessage(String r, Locale locale){
		String kws = constructLocalizedKeywordsList(r, locale);
		return getMessage("keyword.request.help", new Object[]{kws}, locale);
    }
    protected String[] localizeKeywords(String r, Locale locale){
    	String[] keywords = Role.getRoleKeywords(r);
    	String[] kwLocal = new String[keywords.length];
		for(int i = 0; i<keywords.length;i++){
			String kw = keywords[i];
			kwLocal[i] = getMessage(kw, locale);
		}
		return kwLocal;
    }
    private String constructLocalizedKeywordsList(String r, Locale locale){
    	String[] kwLocal = localizeKeywords(r, locale);
    	StringBuffer kws = new StringBuffer();
		if(kwLocal.length > 0){
			for(int c = 0; c < kwLocal.length; c++){
				if(c==0){
					kws.append("\"").append(kwLocal[c]).append("\"");
				}
				else if(c < kwLocal.length-1){
					kws.append(", ").append("\"").append(kwLocal[c]).append("\"");
				}
				else {
					String or = getMessage("keyword.request.or", locale);
					kws.append(" ").append(or).append(" ").append("\"").append(kwLocal[c]).append("\"");
				}
			}
		}
		return kws.toString();
    }

    
	private static final Logging log = Logging.getLog("BaseController");
	public Base() {
		super();
	}
    @Autowired
    private MessageSource messageSource;
    protected final String getMessage(final String key, final Locale locale){
    	return getMessage(key, new Object[0], locale);
    }
    protected final String getMessage(final String key, final Object[] params, final Locale locale){
    	return messageSource.getMessage(key, params, locale);
    }
	
    protected final boolean isUserAuthorized(final Authentication authentication){
    	return authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }

    protected void logout(HttpServletRequest request, SecurityContext context){
    	context.setAuthentication(null);  
    	request.getSession().invalidate();
    }
    
}
