package com.nicksamoylov.storycheck.controller;

import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.nicksamoylov.storycheck.domain.News;
import com.nicksamoylov.storycheck.domain.users.User;
import com.nicksamoylov.storycheck.service.EmailService;
import com.nicksamoylov.storycheck.service.MiscService;
import com.nicksamoylov.storycheck.service.UserService;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.Utils;

@Controller
public class MiscController extends Base implements HandlerExceptionResolver{
	private static final Logging log = Logging.getLog("MiscController");
	private static final String errorDest = "redirect:/index"; //"error"

    @RequestMapping({"/keywords"})
    @ResponseBody
    public String keywords(final HttpServletRequest request, final Model model, final Locale locale) {
    	return "I am fine";
    }
    @RequestMapping({"/","/index"})
        public String index(final HttpServletRequest request, final Model model, final Locale locale) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("index", isRussian, null);
    	log.debug(m);    	
    	String dest = "index";
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
			userService.recordVisitor(user.getUsername(), m, user.display(), request);
    		if(user.getId() > 90){
				if(user.getModifiedDate() == null){
	    			if(userService.isConfirmationRequired(user)){//there is a user temp record
	            		dest =  showMessage(model, MSG_EMAIL_REG_SEE_EMAIL, new Object[]{user.getUsername()}, locale, false);
	            		logout(request, SecurityContextHolder.getContext());
	    		        return dest;
	    			}
	    			else {
	            		String msg = "User is authorized, userId="+user.getId()+", but modified date is null with no user temp record";
	        			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
	            		emailService.sendProblemToAdminWithLogAttached(user, msg);
	    		        return moreoptions(request, model, locale);
	    			}
				}
				else {
	    			if(userService.isConfirmationRequired(user)){//there is a user temp record
	    				if(userService.wasEmailChanged(user)){
		            		dest =  showMessage(model, MSG_EMAIL_CHANGE_SEE_EMAIL, new Object[]{user.getUsername()}, locale, false);
		            		logout(request, SecurityContextHolder.getContext());
		            		model.addAttribute("resubmitUri", "/"+dest);
		    		        return dest;
	    				}
	    				else {
		            		String msg = "User is authorized and modified date not null and there is user temp record, but email did not change";
		            		emailService.sendProblemToAdminWithLogAttached(user, msg);
		        			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
		            		emailService.sendProblemToAdminWithLogAttached(user, msg);
		    		        return moreoptions(request, model, locale);
	    				}
	    			}
	    			else{
	    		        return moreoptions(request, model, locale);
	    			}
				}
			}
    		else {
        		String msg = "User is authorized, but userId="+user.getId();
    			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
        		emailService.sendProblemToAdminWithLogAttached(user, msg);
        		logout(request, SecurityContextHolder.getContext());
        		model.addAttribute("resubmitUri", "/"+dest);
		        return dest;
    		}
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    		}
    		else {
        		userService.recordVisitor(m, request);
    		}
    	}
		model.addAttribute("resubmitUri", "/"+dest);
        return dest;
    }
    
    @RequestMapping({"/moreoptions"})
    public String moreoptions(HttpServletRequest request,final Model model, final Locale locale) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("moreoptions", isRussian, null);
    	log.debug(m);    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUser(authentication);
		if(user.getId()>90){
			userService.recordVisit(m, user.getUsername());
		}
		else {
			userService.recordVisit(m, request);
		}
    	String dest = "user/options";
		model.addAttribute("user", user);
		model.addAttribute("moreoptions", true);
		model.addAttribute("resubmitUri", "/moreoptions");
        return dest;
    }
    
	@RequestMapping({"/login"})
    public String login(HttpServletRequest request, Model model, Locale locale) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("login", isRussian, null);
    	log.debug(m);    	
		userService.recordVisitor(m, request);
    	String dest = "login";
    	model.addAttribute("resubmitUri", "/"+dest);
        return dest;
    }
    
    @RequestMapping("/login-error")
    public String loginError(HttpServletRequest request,final Model model, Locale locale) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("loginError", isRussian, null);
    	log.debug(m);    	
		userService.recordVisitor(m, request);
    	String dest = "login";
        model.addAttribute("loginError", true);
    	model.addAttribute("resubmitUri", "/"+dest);
        return dest;
    }
	
/*    @ExceptionHandler(Throwable.class)  
    public String exception(HttpServletRequest request,Exception ex, Locale locale, Principal principal) {  
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		userService.recordVisitor( "exception", authentication.getName(), request);
    	}
    	else {
    		userService.recordVisitor( "exception", null, request);
    	}
    	log.error("exception("+ex.getMessage()+")");
    	sendProblemReport(principal, locale, ex);
        return errorDest;    
    }
*/    
    @ExceptionHandler(Throwable.class)  
    public String exception(HttpServletRequest request, Exception ex, Locale locale, Principal principal) {  
		boolean isRussian = Utils.isRussian(locale);
		String m = m("exception", isRussian, null);
    	String exStr = Logging.stackTraceToString(ex);
    	log.error(m+": "+exStr);
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
    		userService.recordVisitor(authentication.getName(), m, user.display()+":"+exStr, request);
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display()+":"+exStr, request);
    		}
    		else {
        		userService.recordVisitor(m+":"+exStr, request);
    		}
    	}
    	sendProblemReport(request, principal, locale, ex);
        return errorDest;    
    }
    
    @RequestMapping("/error")
    public String error(HttpServletRequest request,Locale locale, Principal principal) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("error", isRussian, null);
    	log.debug(m);    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
    		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    		}
    		else {
        		userService.recordVisitor(m, request);
    		}
    	}
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        if(throwable != null){
        	log.error(m+": send problem report");
        	sendProblemReport(request, principal, locale, throwable);
        	if(throwable.getCause() != null){
            	log.error(m+": send problem report cause");
            	sendProblemReport(request, principal, locale, throwable.getCause());
        	}
        }
        return errorDest;    
    }

    @RequestMapping("/errorException")
    public String errorException(Throwable throwable, HttpServletRequest request,Locale locale, Principal principal) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("errorException", isRussian, null);
    	String exStr = Logging.stackTraceToString(throwable);
    	log.error(m+": "+exStr);
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
    		userService.recordVisitor(authentication.getName(), m, user.display()+":"+exStr, request);
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display()+":"+exStr, request);
    		}
    		else {
        		userService.recordVisitor(m+":"+exStr, request);
    		}
    	}
        if(throwable != null){
        	log.error(m+": send problem report");
        	sendProblemReport(request, principal, locale, throwable);
        	if(throwable.getCause() != null){
            	log.error(m+": send problem report cause");
            	sendProblemReport(request, principal, locale, throwable.getCause());
        	}
        }
        return errorDest;    
    }
    
    @RequestMapping("/error400")
    public String error400(HttpServletRequest request,Locale locale, Principal principal) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("error400", isRussian, null);
    	log.debug(m);    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
    		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    		}
    		else {
        		userService.recordVisitor(m, request);
    		}
    	}
     	logRequestParameters("400", request, locale, principal);
        return errorDest;    
    }

    @RequestMapping("/error401")
    public String error401(HttpServletRequest request,Locale locale, Principal principal) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("error401", isRussian, null);
    	log.debug(m);    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
    		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    		}
    		else {
        		userService.recordVisitor("error401", request);
    		}
    	}
     	logRequestParameters("401", request, locale, principal);
        return errorDest;    
    }

    @RequestMapping("/error403")
    public String error403(HttpServletRequest request,Locale locale, Principal principal) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("error403", isRussian, null);
    	log.debug(m);    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
    		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    		}
    		else {
        		userService.recordVisitor(m, request);
    		}
    	}
     	logRequestParameters("403", request, locale, principal);
        return errorDest;    
    }

    @RequestMapping("/error404")
    public String error404(HttpServletRequest request, Locale locale, Principal principal) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("error404", isRussian, "uri="+(String)request.getAttribute("javax.servlet.forward.request_uri"));
    	log.debug(m);    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
    		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    		}
    		else {
        		userService.recordVisitor(m, request);
    		}
    	}
     	logRequestParameters("404", request, locale, principal);
        return errorDest;    
    }

    @RequestMapping("/error500")
    public String error500(HttpServletRequest request,Locale locale, Principal principal) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("error500", isRussian, null);
    	log.debug(m);    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
    		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    		}
    		else {
        		userService.recordVisitor(m, request);
    		}
    	}
     	logRequestParameters("500", request, locale, principal);
        return errorDest;    
    }

    @RequestMapping("/error503")
    public String displayErrorCode503(HttpServletRequest request, Locale locale, Principal principal) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("error503", isRussian, null);
    	log.debug(m);    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
    		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display(), request);
    		}
    		else {
        		userService.recordVisitor(m, request);
    		}
    	}
     	logRequestParameters("503", request, locale, principal);
        return errorDest;    
    }

    @RequestMapping("/robots.txt")
    public String knownRequests(HttpServletRequest request) {
    	return "index";
    }
    
    public ModelAndView resolveException(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception exception){
    	String m = "resolveException()";
    	String exStr = Logging.stackTraceToString(exception);
    	log.error(m+": "+exStr);
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication)){
    		User user = userService.findUser(authentication);
    		userService.recordVisitor(authentication.getName(), m, user.display()+":"+exStr, request);
    	}
    	else {
    		User user = userService.findUser(authentication);
    		if(user.getId()>90){
        		userService.recordVisitor(authentication.getName(), m, user.display()+":"+exStr, request);
    		}
    		else {
        		userService.recordVisitor(m+":"+exStr, request);
    		}
    	}
    	log.error("resolveException()");
    	if(exception != null){
    		Locale defaultLocale = Locale.getDefault();
        	log.error("resolveException(): send problem report with default locale="+defaultLocale);
        	sendProblemReport(request, null, defaultLocale, exception);
        	if(exception.getCause() != null){
            	log.error("resolveException(): send problem report cause with default locale="+defaultLocale);
            	sendProblemReport(request, null, defaultLocale, exception.getCause());
        	}
    	}
        return new ModelAndView(errorDest);
    }
    
    private void logRequestParameters(String code, HttpServletRequest request, Locale locale, Principal principal){
    	log.error("logRequestParameters("+code+"): uri="+(String)request.getAttribute("javax.servlet.forward.request_uri"));
    	Enumeration<String> pars = request.getParameterNames(); 
    	log.error("logRequestParameters("+code+"): ---------------Start request parameters-------------------");
    	while(pars.hasMoreElements()){
    		String name = pars.nextElement();
        	log.error("logRequestParameters("+code+"): "+name+"="+DisplayUtils.commaSeparatedSorted(request.getParameterValues(name)));
    	}
    	log.error("logRequestParameters("+code+"): ---------------End request parameters-------------------");
		sendProblemReport(request, principal, locale, null);
    }
    
    private void sendProblemReport(HttpServletRequest request, Principal principal, Locale locale, Throwable ex) {
		if(userService.isOneOfTheKnownVisitors(request)){
			return;
		}
    	log.error("sendProblemReport("+principal+")", ex);
		User user = findUser(principal);
    	emailService.sendLogToAdmin(user, ex);
    }

    private User findUser(final Principal principal){
		User user;
    	if(principal == null){
    		user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
    	}
    	else {
    		user = userService.findUser(principal);
    	}
    	return user;
    }

	@Autowired
	private UserService userService;

	@Autowired
	private MiscService miscService;

    @Autowired 
    private EmailService emailService;

	public MiscController() {
		super();
	}

    @ModelAttribute("news")
    public String news(Locale locale) {
    	return this.miscService.findNews(News.AREA_HOME, Utils.isRussian(locale));
    }    
    
 
    
}
