package com.nicksamoylov.storycheck.controller;

import java.io.File;
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

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nicksamoylov.storycheck.domain.News;
import com.nicksamoylov.storycheck.domain.SampleData;
import com.nicksamoylov.storycheck.domain.test.Questions;
import com.nicksamoylov.storycheck.domain.test.Test;
import com.nicksamoylov.storycheck.domain.test.Testgroup;
import com.nicksamoylov.storycheck.domain.test.results.TestResult;
import com.nicksamoylov.storycheck.domain.test.results.TesterResult;
import com.nicksamoylov.storycheck.domain.test.results.TestgroupResult;
import com.nicksamoylov.storycheck.domain.users.Itester;
import com.nicksamoylov.storycheck.domain.users.Request;
import com.nicksamoylov.storycheck.domain.users.Role;
import com.nicksamoylov.storycheck.domain.users.Tester;
import com.nicksamoylov.storycheck.domain.users.User;
import com.nicksamoylov.storycheck.service.EmailService;
import com.nicksamoylov.storycheck.service.MiscService;
import com.nicksamoylov.storycheck.service.TestService;
import com.nicksamoylov.storycheck.service.UserService;
import com.nicksamoylov.storycheck.utils.DateUtils;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.StringUtils;
import com.nicksamoylov.storycheck.utils.Utils;

@Controller
@RequestMapping("/test")
public class TestController extends Base{
	private static Logging log = Logging.getLog("TestController");
	public static Map<String, Long> timeSpentMap = new HashMap<String, Long>();

    @RequestMapping("/testgroupresult")
    public String testgroupresult(HttpServletRequest request, final Locale locale, final Model model, 
    		final int tgId, final String qt, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroupresult", isRussian, "id="+tgId+",qt="+qt);
    	log.debug(m);    	
		TestResult test;
		List<String> comments; 
		Questions questions; 
		TestgroupResult testgroup;
		List<TesterResult> testerstestgroup;
		if(isUserAuthorized(SecurityContextHolder.getContext().getAuthentication())){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
			test = testService.findTestResultByQuestionsToken(qt, isRussian);
			if(test.getId() < 90){
				String msg = "Test results for questionToken "+qt+" is not found";
    			emailService.sendProblemToAdminWithLogAttached(user, msg);
    			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
    			test = SampleData.testResult(1, isRussian);
    			comments = SampleData.comments(tgId, isRussian);
    			questions = new Questions(test.getTestTextLength(), messageSource, locale);
    			testgroup = SampleData.testgroupResult(1, tgId, isRussian);
    			testerstestgroup = SampleData.testerResults(tgId, isRussian);
			}
			else if(test.getUserId() != user.getId()){
				String msg = "User id "+user.getId()+" does not match the id="+test.getUserId()+" based on qt="+qt;
    			emailService.sendProblemToAdminWithLogAttached(user, msg);
    			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
    			test = SampleData.testResult(1, isRussian);
    			comments = SampleData.comments(tgId, isRussian);
    			questions = new Questions(test.getTestTextLength(), messageSource, locale);
    			testgroup = SampleData.testgroupResult(1, tgId, isRussian);
    			testerstestgroup = SampleData.testerResults(tgId, isRussian);
			}
			else {
    			comments = testService.findTestgroupResultComments(tgId);
				questions = testService.findQuestionsByQuestionsToken(qt, null, test.getTestTextLength(), locale);
				testgroup = testService.findTestgroupResultById(tgId, isRussian);
    			testerstestgroup = testService.findTesterResultsByGroupId(tgId);
			}
		}
		else {
    		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
    		if(user.getId()>90){
        		userService.recordVisit(m, user.getUsername());
    			test = testService.findTestResultByQuestionsToken(qt, isRussian);
    			comments = testService.findTestgroupResultComments(tgId);
				questions = testService.findQuestionsByQuestionsToken(qt, null, test.getTestTextLength(), locale);
				testgroup = testService.findTestgroupResultById(tgId, isRussian);
    			testerstestgroup = testService.findTesterResultsByGroupId(tgId);
    		}
    		else {
        		userService.recordVisit(m, request);
    			test = SampleData.testResult(1, isRussian);
    			comments = SampleData.comments(tgId, isRussian);
    			questions = new Questions(test.getTestTextLength(), messageSource, locale);
    			testgroup = SampleData.testgroupResult(1, tgId, isRussian);
    			testerstestgroup = SampleData.testerResults(tgId, isRussian);
    		}
		}
    	String dest = "test/testgroupresult";
		model.addAttribute("test", test);
		model.addAttribute("comments", comments);
		model.addAttribute("questions", questions);
		model.addAttribute("testgroup", testgroup);
		model.addAttribute("testerstestgroup", testerstestgroup);
		model.addAttribute("resubmitUri", "/"+dest+"?tgId="+tgId+"&qt="+qt);
		return dest;
    }
    
    @RequestMapping("/testgroupresulttext")
    public String testgroupresulttext(HttpServletRequest request, final Locale locale, final Model model, final int tgId, 
    		final String qt, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroupresulttext", isRussian, "id="+tgId+",qt="+qt);
    	log.debug(m);    	
		TestResult test;
		TestgroupResult testgroup;
		List<String> comments; 
		List<TesterResult> testerstestgroup;
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(isUserAuthorized(SecurityContextHolder.getContext().getAuthentication()) || user.getId()>90){
			userService.recordVisit(m, user.getUsername());
			test = testService.findTestResultByQuestionsToken(qt, isRussian);
			if(test.getId() < 90){
				String msg = "Test results for questionToken "+qt+" is not found";
    			emailService.sendProblemToAdminWithLogAttached(user, msg);
    			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
    			test = SampleData.testResult(1, isRussian);
    			comments = SampleData.comments(tgId, isRussian);
    			testgroup = SampleData.testgroupResult(1, tgId, isRussian);
    			testerstestgroup = SampleData.testerResults(tgId, isRussian);
			}
			else if(test.getUserId() != user.getId()){
				String msg = "User id "+user.getId()+" does not match the id="+test.getUserId()+" based on qt="+qt;
    			emailService.sendProblemToAdminWithLogAttached(user, msg);
    			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
    			test = SampleData.testResult(1, isRussian);
    			comments = SampleData.comments(tgId, isRussian);
    			testgroup = SampleData.testgroupResult(1, tgId, isRussian);
    			testerstestgroup = SampleData.testerResults(tgId, isRussian);
			}
			else {
    			comments = testService.findTestgroupResultComments(tgId);
				testgroup = testService.findTestgroupResultById(tgId, isRussian);
    			testerstestgroup = testService.findTesterResultsByGroupId(tgId);
			}
		}
		else {
			userService.recordVisit(m, request);
			test = SampleData.testResult(1, isRussian);
			comments = SampleData.comments(tgId, isRussian);
			testgroup = SampleData.testgroupResult(1, tgId, isRussian);
			testerstestgroup = SampleData.testerResults(tgId, isRussian);
		}
    	String dest = "test/testgroupresulttext";
		model.addAttribute("test", test);
		model.addAttribute("comments", comments);
		model.addAttribute("testgroup", testgroup);
		model.addAttribute("testerstestgroup", testerstestgroup);
		model.addAttribute("resubmitUri", "/"+dest+"?tgId="+tgId+"&qt="+qt);
		return dest;
    }
    
    @RequestMapping("/testgroupresultfileview")
    public String testgroupresultfileview(HttpServletRequest request, HttpServletResponse response, final Locale locale, final Model model, 
    		final int tgId, final String qt, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroupresultfileview", isRussian, "id="+tgId+",qt="+qt);
    	log.debug(m);    	
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
		if(isUserAuthorized(SecurityContextHolder.getContext().getAuthentication()) || user.getId()>90){
			userService.recordVisit(m, user.getUsername());
			TestResult test = testService.findTestResultByQuestionsToken(qt, isRussian);
			File file = TestResult.getFile(user.getId(), test.getId(), tgId);
            TestgroupResult tgr = testService.findTestgroupResultById(tgId, isRussian);
            String originalFileName = tgr.getId() > 90?tgr.getText():"testfile";
			String result = testService.sendFileToResponse(file, originalFileName, response);
    		if(!TestService.SUCCESS.equals(result)){
    			emailService.sendProblemToAdminWithLogAttached(user, result);
    			userService.recordVisitor(user.getUsername(), m, result+":"+user.display(), request);
    		}
		}
		return null;
    }
    
    @RequestMapping("/testgroupfileview")
    public String testgroupfileview(HttpServletRequest request, HttpServletResponse response, final Locale locale, final Model model, 
    		final int tgId, final int testId, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroupfileview", isRussian, "id="+tgId+",testId="+testId);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		if(writerId > 0){
			File file = Test.getFile(writerId, testId, tgId);
            String originalFileName = testService.findTestText(isRussian, writerId, testId, tgId);
			String result = testService.sendFileToResponse(file, originalFileName, response);
    		if(!TestService.SUCCESS.equals(result)){
    			User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
    			emailService.sendProblemToAdminWithLogAttached(user, result);
    			userService.recordVisitor(user.getUsername(), m, result+":"+user.display(), request);
    		}
		}
		return null;
    }
    
    @RequestMapping("/testresultcomments")
    public String testresultcomments(HttpServletRequest request, final Locale locale, final Model model, 
    		final int tgId, final String qt, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testresultcomments", isRussian, "tgId="+tgId+",qt="+qt);
    	log.debug(m);    	
		TestResult test;
		List<String> comments; 
		TestgroupResult testgroup;
		if(isUserAuthorized(SecurityContextHolder.getContext().getAuthentication())){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
			test = testService.findTestResultByQuestionsToken(qt, isRussian);
			if(test.getId() < 90){
				String msg = "Test results for questionToken "+qt+" is not found";
    			emailService.sendProblemToAdminWithLogAttached(user, msg);
    			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
    			test = SampleData.testResult(1, isRussian);
    			comments = SampleData.comments(tgId, isRussian);
    			testgroup = SampleData.testgroupResult(1, tgId, isRussian);
			}
			else if(test.getUserId() != user.getId()){
				String msg = "User id "+user.getId()+" does not match the id="+test.getUserId()+" based on qt="+qt;
    			emailService.sendProblemToAdminWithLogAttached(user, msg);
    			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
    			test = SampleData.testResult(1, isRussian);
    			comments = SampleData.comments(tgId, isRussian);
    			testgroup = SampleData.testgroupResult(1, tgId, isRussian);
			}
			else {
    			comments = testService.findTestgroupResultComments(tgId);
				testgroup = testService.findTestgroupResultById(tgId, isRussian);
			}
		}
		else {
			userService.recordVisit(m, request);
			test = SampleData.testResult(1, isRussian);
			comments = SampleData.comments(tgId, isRussian);
			testgroup = SampleData.testgroupResult(1, tgId, isRussian);
		}
    	String dest = "test/testresultcomments";
		model.addAttribute("test", test);
		model.addAttribute("comments", comments);
		model.addAttribute("testgroup", testgroup);
		model.addAttribute("resubmitUri", "/"+dest+"?tgId="+tgId+"&qt="+qt);
		return dest;
    }
    
    @RequestMapping("/testresult")
    public String testresult(HttpServletRequest request, final Locale locale, final Model model, 
    		final String qt, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testresult", isRussian, "qt="+qt);
    	log.debug(m);    	
		TestResult test;
		Questions questions; 
		List<TestgroupResult> testgroupstest;
		if(isUserAuthorized(SecurityContextHolder.getContext().getAuthentication())){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
			test = testService.findTestResultByQuestionsToken(qt, isRussian);
			if(test.getId() < 90){
				String msg = "Test results for questionToken "+qt+" is not found";
    			emailService.sendProblemToAdminWithLogAttached(user, msg);
    			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
    			test = SampleData.testResult(1, isRussian);
    			testgroupstest = SampleData.testgroupsTestResults(1, isRussian);
    			questions = new Questions(test.getTestTextLength(), messageSource, locale);
			}
			else if(test.getUserId() != user.getId()){
				String msg = "User id "+user.getId()+" does not match the id="+test.getUserId()+" based on qt="+qt;
    			emailService.sendProblemToAdminWithLogAttached(user, msg);
    			userService.recordVisitor(user.getUsername(), m, msg+":"+user.display(), request);
    			test = SampleData.testResult(1, isRussian);
    			testgroupstest = SampleData.testgroupsTestResults(1, isRussian);
    			questions = new Questions(test.getTestTextLength(), messageSource, locale);
			}
			else {
				questions = testService.findQuestionsByQuestionsToken(qt, null, test.getTestTextLength(), locale);
				testgroupstest = testService.findTestgroupResultsByTestIdOrdeByName(test, questions, locale);
			}
		}
		else {
			userService.recordVisit(m, request);
			test = SampleData.testResult(1, isRussian);
			testgroupstest = SampleData.testgroupsTestResults(1, isRussian);
			questions = new Questions(test.getTestTextLength(), messageSource, locale);
		}
    	String dest = "test/testresult";
		model.addAttribute("test", test);
		model.addAttribute("questions", questions);
		model.addAttribute("testgroupstest", testgroupstest);
		model.addAttribute("resubmitUri", "/"+dest+"?qt="+qt);
		return dest;
    }

    @RequestMapping("/testresults")
    public String testresults(HttpServletRequest request, final Locale locale, final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testresults", isRussian, "");
    	log.debug(m);    	
		List<TestResult> tests;
		Map<Integer, List<TestgroupResult>> testgroupsMap;
		if(isUserAuthorized(SecurityContextHolder.getContext().getAuthentication())){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
			tests = testService.findTestResultsByUserIdOrderByNameDate(user.getId());
			testgroupsMap = new HashMap<Integer, List<TestgroupResult>>();
			for(TestResult t: tests){
				List<TestgroupResult> testgroups = testService.findTestgroupResultsByTestIdOrdeByName(t.getId()); 
				testgroupsMap.put(t.getId(), testgroups);
			}
		}
		else {
			userService.recordVisit(m, request);
			tests = SampleData.testResults(isRussian);
			testgroupsMap = SampleData.testgroupResultsMap(isRussian);
		}
    	String dest = "test/testresults";
		model.addAttribute("tests", tests);
		model.addAttribute("testgroupsmap", testgroupsMap);
		model.addAttribute("resubmitUri", "/"+dest);
		return dest;
    }
    
    @RequestMapping("/testeranswer")
    public String testeranswer(HttpServletRequest request, final Questions questions, final Model model, final Locale locale) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testeranswer", isRussian, "");
    	log.debug(m);    	
    	if(questions.getTesterToken() != null){
    		long timeSpentSec = -1; 
    		if(timeSpentMap.keySet().contains(questions.getTesterToken())){
    			timeSpentSec = System.currentTimeMillis()/1000 - timeSpentMap.get(questions.getTesterToken()); 
    			timeSpentMap.remove(questions.getTesterToken());
    		}
    		String result = testService.saveAnswer(questions, timeSpentSec, isRussian);
    		if(!TestService.SUCCESS.equals(result)){
    			User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
    			emailService.sendProblemToAdminWithLogAttached(user, result);
    			userService.recordVisitor(user.getUsername(), m, result+":"+user.display(), request);
    		}
    	}
		String dest =  "message";
		model.addAttribute("becometester", true);
    	model.addAttribute("resubmitUri", false);
    	model.addAttribute("returnhome", getMessage("returnhome", locale));
        return dest;
    }

    @RequestMapping("/testerquestions")
    public String testerquestions(final Model model, final Locale locale, final String qt, final String tt, final String tl) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testerquestions", isRussian, "qt="+qt+",tt="+tt+",tl="+tl);
    	log.debug(m);    	
    	Questions questions = testService.getQuestionsIfNoAnswer(qt, tt, tl, locale);
		if(questions == null){
	        return showMessage(model, MSG_ANSWER_COMPLETED, locale, false);
		}
		else {
			if(tt != null && !timeSpentMap.keySet().contains(tt)){
				timeSpentMap.put(tt, System.currentTimeMillis()/1000);
			}
	    	String dest = "test/testerquestions";
	    	model.addAttribute("questions", questions);
	    	model.addAttribute("resubmitUri", "/"+dest+"?qt="+qt+(tt==null?"":"&tt="+tt)+"&tl="+tl);
	        return dest;
		}
    }

	@RequestMapping("/sampleemail")
    public String sampleemail(HttpServletRequest request, final Locale locale, final Model model) {
		boolean isRussian = Utils.isRussian(locale);
		String email = request.getParameter("email");
		String m = m("sampleemail", isRussian, "email="+email);
    	log.debug(m);    	
    	if(email == null || "".equals(email.trim())){
    		model.addAttribute("error", "email.sample.required");
    	}
    	else {
    		userService.recordVisitor(email, m, "Got email sample", request);
    		emailService.sendEmailSample(email, locale);
    	}
		String dest = "index";
		model.addAttribute("resubmitUri", "/"+dest);
    	return dest;
    }
	
	@RequestMapping("/samplepages")
    public String samplepages(HttpServletRequest request, final Locale locale, final Model model, RedirectAttributes ra) {
		String userId;// = cookie;//getUserIdAndSetCookie(request, response);
		boolean isRussian = Utils.isRussian(locale);
		String m = m("samplepages", isRussian, "");
    	log.debug(m);    	
		userService.recordVisitor(m, request);
		int writerId = -1;
		List<Test> tests = this.testService.findTestsByWriterIdOrderByName(isRussian, writerId);
		Map<Integer, List<Testgroup>> testgroupsMap = new HashMap<Integer, List<Testgroup>>();
		for(Test t: tests){
			List<Testgroup> testgroups = this.testService.findTestgroupsByTestIdOrderByName(isRussian, writerId, t.getId()); 
			testgroupsMap.put(t.getId(), testgroups);
		}
		
    	String dest = "test/tests";
		model.addAttribute("tests", tests);
		model.addAttribute("testgroupsmap", testgroupsMap);
		model.addAttribute("resubmitUri", "/"+dest);
		return "redirect:/"+dest;
    }
    
    @RequestMapping("/tests")
    public String tests(HttpServletRequest request, final Locale locale, final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("tests", isRussian, "");
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		List<Test> tests = this.testService.findTestsByWriterIdOrderByName(isRussian, writerId);
		Map<Integer, List<Testgroup>> testgroupsMap = new HashMap<Integer, List<Testgroup>>();
		for(Test t: tests){
			List<Testgroup> testgroups = this.testService.findTestgroupsByTestIdOrderByName(isRussian, writerId, t.getId()); 
			testgroupsMap.put(t.getId(), testgroups);
		}
    	String dest = "test/tests"; 
		model.addAttribute("tests", tests);
		model.addAttribute("testgroupsmap", testgroupsMap);
		return addGuide(dest, model, isRussian, writerId);
    }
    private String addGuide(String dest, Model model, boolean isRussian, int writerId){
    	boolean createtest = testService.countTests(isRussian, writerId) == 0;
    	boolean createtester = testService.countTesters(isRussian, writerId) == 0;
    	boolean createtestgroup = testService.countTestgroups(isRussian, writerId) == 0;
		model.addAttribute("isnew", createtest || createtester || createtestgroup);
		model.addAttribute("createtest", createtest);
		model.addAttribute("createtester", createtester);
		model.addAttribute("createtestgroup", createtestgroup);
		model.addAttribute("resubmitUri", "/"+dest);
		return dest;
    }
    
    @RequestMapping("/test")
    public String test(HttpServletRequest request, final Locale locale, final Model model, final int id, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("test", isRussian, "id="+id);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Test test = testService.findTestById(isRussian, writerId, id);		
		List<Testgroup> testgroupstest = testService.findTestgroupsByTestIdOrderByName(isRussian, writerId, id);
		Map<Integer, List<Test>> testsMap = testService.tgTestsMap(isRussian, writerId, testgroupstest);
		String dest = "test/test";
		model.addAttribute("test", test);
		model.addAttribute("testsmap", testsMap);
		model.addAttribute("testgroupstest", testgroupstest);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id);
        return dest;
    }
    
    @RequestMapping("/testadd")
    public String testadd(HttpServletRequest request, final Locale locale, final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testadd", isRussian, null);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		List<Testgroup> testgroupsall = testService.findTestgroupsByWriterIdOrderByName(isRussian, writerId);
		Map<Integer, List<Test>> testsMap = testService.tgTestsMap(isRussian, writerId, testgroupsall);
    	String dest = "test/testedit";
    	model.addAttribute("test", new Test());
    	model.addAttribute("testsmap", testsMap);
		model.addAttribute("testgroupsall", testgroupsall);
    	model.addAttribute("resubmitUri", "/test/testadd");
        return dest;
    }
    
    @RequestMapping("/testedit")
    public String testedit(HttpServletRequest request, @RequestParam String buttonValue, final Locale locale, 
    		final Model model, final int id, final String r, RedirectAttributes ra) {
    	if("testrun".equals(buttonValue)){
    		return testrun(request, "", locale, model, id, r, ra);
    	}
    	if("testdel".equals(buttonValue)){
    		return testdel(request, locale, model, id, "", ra);
    	}
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testedit", isRussian, "id="+id);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Test test = testService.findTestById(isRussian, writerId, id);
		List<Testgroup> testgroupstest = testService.findTestgroupsByTestIdOrderByName(isRussian, writerId, id);    	
		for(Testgroup tg: testgroupstest){
			test.addTestgroupId(tg.getId());
		}
		List<Testgroup> testgroupsall = testService.findTestgroupsByWriterIdOrderByName(isRussian, writerId);
		Map<Integer, List<Test>> testsMap = testService.tgTestsMap(isRussian, writerId, testgroupsall);
		String dest = "test/testedit";
		model.addAttribute("r", r);
		model.addAttribute("test", test);
		model.addAttribute("testsmap", testsMap);
		model.addAttribute("testgroupsall", testgroupsall);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id+"&r="+r+"&buttonValue="+buttonValue);
        return dest;
    }
    
	@RequestMapping("/testrun")
	public String testrun(HttpServletRequest request, @RequestParam String confirm, final Locale locale, final Model model, 
			final int id, final String r, RedirectAttributes ra) {
		if("dontrun".equals(confirm)){
			return display(request, null, r, locale, model, id, 0, 0, ra, "tests");
		}
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testrun", isRussian, "id="+id+",confirm="+confirm);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Test test = testService.findTestById(isRussian, writerId, id);
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(isUserAuthorized(authentication) && test.isReady()){
    		User user = userService.findUser(authentication);
			List<Testgroup> testgroupstest = testService.findTestgroupsByTestIdOrderByName(isRussian, writerId, id);
			int emailsCount = 0;
			Map<String, Set<Tester>> mapTesters = new HashMap<String, Set<Tester>>();
			Map<String, List<String>> mapTestersDisplay = new HashMap<String, List<String>>();
			for(Testgroup tg: testgroupstest){
				Set<String> emails = new HashSet<String>();
				Set<Tester> testers = new HashSet<Tester>();
				List<String> testersDisplay = new ArrayList<String>();
				List<Integer> ids = testService.findTesterIdsByTestgroupIdOrderByFirstname(isRussian, writerId, tg.getId());
				for(int testerId:ids){
					Tester t = testService.findTesterById(isRussian, writerId, testerId);
					if(emails.add(t.getEmail())){
						emailsCount++;
						testers.add(t);
						testersDisplay.add(t.getFirstname()+" "+t.getLastname()+" ("+t.getEmail()+")");
					}
				}
				mapTesters.put(tg.getName(), testers);
				Collections.sort(testersDisplay);
				mapTestersDisplay.put(tg.getName(), testersDisplay);
			}
			model.addAttribute("r", r);
			model.addAttribute("id", id);
	        if("testrun".equals(confirm)){
				emailService.updateEmailsCount(user, emailsCount);
	        	String questionsToken = UUID.randomUUID().toString();
				for(Testgroup tg: testgroupstest){
					Map<String, String> testerTokens = testService.prepareResultsArea(emailService, user, test, questionsToken, tg, mapTesters.get(tg.getName()), emailsCount, locale);
					emailService.sendEmailToTesters(user, mapTesters.get(tg.getName()), mapTestersDisplay.get(tg.getName()), questionsToken, testerTokens, test, tg,  locale);
					String emailContent = emailService.generateEmailContentWoText(user, test, locale);
					testService.saveEmailContent(emailContent, writerId, test, questionsToken, tg, isRussian);
				}
	        	return display(request, null, r, locale, model, test.getId(), 0, 0, ra, "tests");
	        }
	        else {
				String resubmitUri = "/test/testrun?id="+id+"&r="+r+"&confirm=";
	        	int limit = Utils.getPropertyEmailLimitPerDay();
	        	if((emailsCount+user.getEmailsCountToday())>limit && (user.isEmailsCountSameDate() || emailsCount > limit)){
		        	//This test requires to send {0} emails. Meanwhile, you have sent today {1} emails already. 
		        	//With the number of the current test emails, the total count for today will be {2}, 
		        	//which exceeds daily limit of {3} emails. Please, either wait for {4} hours {5} minutes 
		        	//before launching this test again or decrease number of the emails sent by the test.
	        		Date now = new Date();
	        		Date midnight = DateUtils.nextMidnight();
	        		int minutes = (int)( (midnight.getTime() - now.getTime())/(1000 * 60));
	        		int hours = (int)(minutes/60);
	        		Object[] params = new Object[]{emailsCount, user.getEmailsCountToday(), emailsCount+user.getEmailsCountToday(),
	        				limit, hours, (minutes - hours*60)};
            		return showMessage(model, MSG_EMAIL_SENT_LIMIT, params, locale, resubmitUri);

	        	}
	        	else {
	        		model.addAttribute("r", r);
	        		model.addAttribute("id", test.getId());
	        		model.addAttribute("email", user.getUsername());
            		return showMessage(model, VALUE_EMAIL_TESTERS_MAP, mapTestersDisplay, locale, resubmitUri);
	        	}
	        }
    	}
		else {
        	return display(request, null, r, locale, model, test.getId(), 0, 0, ra, "tests");
		}
	}

    @RequestMapping("/testsave")
    public String testsave(HttpServletRequest request, @Valid final Test test, final BindingResult bindingResult, final Locale locale, 
    		final Model model, final String r, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testsave", isRussian, "id="+test.getId());
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), test.getId()>0?test.getWriterId():-1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			String oldTestTextLength = null;
			Test tst;
			if(test.getId() > 90){
				tst = testService.findTestById(isRussian, writerId, test.getId());
				oldTestTextLength = tst.getTestTextLength();
				tst.setName(test.getName());
				tst.setTestTextLength(test.getTestTextLength());
				tst.setTestgroupsId(test.getTestgroupsId());
			}
			else {
				tst = test;
			}
			tst.setWriterId(writerId);
			if(testService.testNameExists(writerId, tst.getId(), tst.getName(), isRussian)){
				bindingResult.rejectValue("name", "exists.test.name", "This test name exists already");
			}
			if(bindingResult.hasErrors()) {
	    		List<Testgroup> testgroupsall = this.testService.findTestgroupsByWriterIdOrderByName(Utils.isRussian(locale), writerId); 
	    		Map<Integer, List<Test>> testsMap = testService.tgTestsMap(isRussian, writerId, testgroupsall);
	        	String dest = "test/testedit";
	        	model.addAttribute("test", tst);
	        	model.addAttribute("testsmap", testsMap);
	        	model.addAttribute("testgroupsall", testgroupsall);
	        	model.addAttribute("resubmitUri", test.getId()>0?("/"+dest+"?id="+test.getId()+"&r="+r+"&buttonValue="):("/test/testadd?r="+r));
	            return dest;
	    	}
	        else {
	        	if(tst.getId() > 90){
	        		String newTextLength = tst.getTestTextLength();
	        		if(!oldTestTextLength.equals(newTextLength)){
	        			List<Testgroup> testgroupstest = testService.findTestgroupsByTestIdOrderByName(isRussian, writerId, test.getId());
	        			Map<Integer, String> delTextGroups = new HashMap<Integer, String>();
	        			for(Testgroup tg: testgroupstest){
	        				if(!tg.isTextEmpty()){
	        					delTextGroups.put(tg.getId(), tg.getName());
	        				}
	        			}
	        			if(delTextGroups.keySet().size() > 0){
	                		tst.setTestTextLength(oldTestTextLength);
	                    	testService.saveTest(isRussian, tst);
	                    	testService.addUpdateTestMembership(isRussian, tst);
	                		tst.setStatus(testService.findTestStatus(isRussian, writerId, tst));
	                		tst.setTestTextLength(newTextLength);
	                		List<Testgroup> testgroupsall = this.testService.findTestgroupsByWriterIdOrderByName(Utils.isRussian(locale), writerId); 
	                		Map<Integer, List<Test>> testsMap = testService.tgTestsMap(isRussian, writerId, testgroupsall);
	                		String dest = "test/testedit";
	                		model.addAttribute("test", tst);
	                    	model.addAttribute("testsmap", testsMap);
	                    	model.addAttribute("testgroupsall", testgroupsall);
	                    	model.addAttribute(Test.TEXT_LENGTH_IMAGE.equals(oldTestTextLength)?"deleteimages":"deletetexts", DisplayUtils.commaSeparatedSorted(delTextGroups.values()));
	                    	model.addAttribute("resubmitUri", "/"+dest+"?id="+test.getId()+"&r="+r+"&buttonValue=");
	                        return dest;
	        			}
	        		}
	            	testService.saveTest(isRussian, tst);
	        	}
	        	else {
	    			if(test.isImage()){
	        			tst.setStatus(test.getTestgroupsId().size()>0?Test.STATUS_PARTIAL_IMAGE:Test.STATUS_NO_GROUPS_NO_IMAGE);
	    			}
	    			else {
	        			tst.setStatus(test.getTestgroupsId().size()>0?Test.STATUS_PARTIAL_TEXT:Test.STATUS_NO_GROUPS_NO_TEXT);
	    			}
	            	int id = testService.saveTest(isRussian, tst);
	            	tst.setId(id);
	        	}
            	testService.addUpdateTestMembership(isRussian, tst);
	        }
		}
		else {
			userService.recordVisit(m, request);
		}
		return display(request, null, r, locale, model, test.getId(), 0, 0, null, "test");
    }

    @RequestMapping("/testdel")
    public String testdel(HttpServletRequest request, final Locale locale, final Model model, final int id, final String confirm, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testdel", isRussian, "id="+id+", confirm="+confirm);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			Test test = testService.findTestById(isRussian, writerId, id);
	        if(StringUtils.isEmpty(confirm)){
	        	model.addAttribute("namedelete", test.getName());
	        	model.addAttribute("deleteUri", "/test/testdel?id="+id);
	        	return test(request, locale, model, id, ra);
	        }
	        else {
	    		testService.deleteTest(isRussian, test);
	        }
		}
		else {
			userService.recordVisit(m, request);
		}
		return "redirect:/"+tests(request, locale, model, ra);
    }

    @RequestMapping("/testemailedit")
    public String testemailedit(HttpServletRequest request, final Locale locale, final Model model, 
    		final int testId, final String r, RedirectAttributes ra){
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testemailedit", isRussian, "id="+testId);
    	log.debug(m+": r="+r);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Test test = testService.findTestById(isRussian, writerId, testId); 
		List<Testgroup> testgroupstest = testService.findTestgroupsByTestIdOrderByName(isRussian, writerId, testId);
		List<Testgroup> testgroupsall = testService.findTestgroupsByWriterIdOrderByName(isRussian, writerId);		
		Map<Integer, List<Test>> testsMap = testService.tgTestsMap(isRussian, writerId, testgroupsall);
		if(test.isEmailTextEmpty()){
        	User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
			test.setEmailText(emailService.findPrepareEmailText1(user, test.getTestTextLength(), isRussian));
		}
		String dest = "test/testemailedit";
		model.addAttribute("r", r);
		model.addAttribute("test", test);
		model.addAttribute("testsmap", testsMap);
		model.addAttribute("testgroupstest", testgroupstest);
    	model.addAttribute("resubmitUri", "/"+dest+"?testId="+testId+"&r="+r);
        return dest;
    }

    private static final String divEmail = "<"+Test.DB_EMAIL_MAX_LENGTH+">";
    @RequestMapping("/testemailsave")
    public String testemailsave(HttpServletRequest request, @Valid final Test test, final BindingResult bindingResult, @RequestParam String saveButton, 
    		final Locale locale, final Model model, final String r, RedirectAttributes ra) throws MessagingException{
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testemailsave", isRussian, "id="+test.getId());
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), test.getWriterId());
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			Test tst = test.getWriterId()>0?testService.findTestById(isRussian, writerId, test.getId()):test;
			String emailText = test.getEmailText()==null?"":test.getEmailText().replaceAll(divEmail,"");
			tst.setEmailText(emailText);
			boolean isError = false;
			if(emailText.trim().length() < Test.DB_EMAIL_MIN_LENGTH){
	    		model.addAttribute("error", "Length.test.email.text.short");
				isError = true;
			}
			else if(emailText.length() > Test.DB_EMAIL_MAX_LENGTH){
	    		model.addAttribute("error", "Length.test.email.text.long");
				String str1 = StringUtils.substring(emailText, Test.DB_EMAIL_MAX_LENGTH);
				String str2 = emailText.substring(Test.DB_EMAIL_MAX_LENGTH);
				tst.setEmailText(str1+divEmail+str2);
				isError = true;
	    	}        
	        if(isError){
	    		List<Testgroup> testgroupstest = testService.findTestgroupsByTestIdOrderByName(isRussian, writerId, test.getId());
	    		List<Testgroup> testgroupsall = testService.findTestgroupsByWriterIdOrderByName(isRussian, writerId);		
	    		Map<Integer, List<Test>> testsMap = testService.tgTestsMap(isRussian, writerId, testgroupsall);
	    		String dest = "test/testemailedit";
	    		model.addAttribute("r", r);
	    		model.addAttribute("test", tst);
	    		model.addAttribute("testsmap", testsMap);
	    		model.addAttribute("testgroupstest", testgroupstest);
	        	model.addAttribute("resubmitUri", "/"+dest+"?testId="+test.getId()+"&r="+r);
	            return dest;
	        }
	        else {        	
	        	testService.saveTest(isRussian, tst);
	        	if("sendemailsample".equals(saveButton)){
	            	User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
	            	emailService.sendEmailSample(user, tst, locale);
	        	}
	        }
		}
		else {
			userService.recordVisit(m, request);
		}
    	return display(request, null, r, locale, model, test.getId(), 0, 0, ra, "test");
    }

    @RequestMapping("/testgroups")
    public String testgroups(HttpServletRequest request, final Locale locale, final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroups", isRussian, null);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		List<Testgroup> testgroupsall = this.testService.findTestgroupsByWriterIdOrderByName(isRussian, writerId);		
		Map<Integer, List<Test>> testsMap = testService.tgTestsMap(isRussian, writerId, testgroupsall);
    	String dest = "test/testgroups";
		model.addAttribute("testsmap", testsMap);
		model.addAttribute("testgroupsall", testgroupsall);
		model.addAttribute("resubmitUri", "/"+dest);
		return addGuide(dest, model, isRussian, writerId);
    }
    
    @RequestMapping("/testgroup")
    public String testgroup(HttpServletRequest request, final Locale locale, final Model model, final int id, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroup", isRussian, "id="+id);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Testgroup tg = testService.findTestgroupById(isRussian, writerId, id);
		List<Test> teststestgroup = testService.findTestsByTestgroupIdOrderByName(isRussian, writerId, id); 
		List<Tester> testerstestgroup = testService.findTestersByTestgroupIdOrderByFirstname(isRussian, writerId, id);
		Map<Integer, List<Test>> testerTestsMap = testService.testerTestsMap(isRussian, writerId, testerstestgroup);
		Map<Integer, List<Testgroup>> testerTestgroupsMap = testService.testerTestgroupsMap(isRussian, writerId, testerstestgroup);
		String dest = "test/testgroup";
		model.addAttribute("testgroup", tg);
		model.addAttribute("teststestgroup", teststestgroup);
		model.addAttribute("testerstestgroup", testerstestgroup);
		model.addAttribute("testertestsmap", testerTestsMap);
		model.addAttribute("testertestgroupsmap", testerTestgroupsMap);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id);
        return dest;
    }

    @RequestMapping("/testgroupedit")
    public String testgroupedit(HttpServletRequest request, @RequestParam String buttonValue, final Locale locale, 
    		final Model model, final int id, RedirectAttributes ra) {
    	if("testgroupdel".equals(buttonValue)){
    		return testgroupdel(request, locale, model, id, "", ra);
    	}
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroupedit", isRussian, "id="+id);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Testgroup tg = testService.findTestgroupById(isRussian, writerId, id);
		for(Tester tester: testService.findTestersByTestgroupIdOrderByFirstname(isRussian, writerId, id)){
			tg.addTesterId(tester.getId());
		}
		List<Tester> testersall = testService.findTestersByWriterIdOrderByFirstname(isRussian, writerId);
		List<Test> teststestgroup = testService.findTestsByTestgroupIdOrderByName(isRussian, writerId, id); 
		Map<Integer, List<Test>> testerTestsMap = testService.testerTestsMap(isRussian, writerId, testersall);
		Map<Integer, List<Testgroup>> testerTestgroupsMap = testService.testerTestgroupsMap(isRussian, writerId, testersall);
		String dest = "test/testgroupedit";
		model.addAttribute("testgroup", tg);
		model.addAttribute("testersall", testersall);
		model.addAttribute("teststestgroup", teststestgroup);
		model.addAttribute("testertestsmap", testerTestsMap);
		model.addAttribute("testertestgroupsmap", testerTestgroupsMap);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id+"&buttonValue="+buttonValue);
        return dest;
    }

    @RequestMapping("/testgroupadd")
    public String testgroupadd(HttpServletRequest request, final Locale locale, final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroupadd", isRussian, null);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		List<Tester> testersall = testService.findTestersByWriterIdOrderByFirstname(isRussian, writerId);
		Map<Integer, List<Test>> testerTestsMap = testService.testerTestsMap(isRussian, writerId, testersall);
		Map<Integer, List<Testgroup>> testerTestgroupsMap = testService.testerTestgroupsMap(isRussian, writerId, testersall);
    	String dest = "test/testgroupedit";
    	model.addAttribute("testgroup", new Testgroup());
		model.addAttribute("testersall", testersall);
		model.addAttribute("testertestsmap", testerTestsMap);
		model.addAttribute("testertestgroupsmap", testerTestgroupsMap);
    	model.addAttribute("resubmitUri", "/test/testgroupadd");
        return dest;
    }
    
    @RequestMapping("/testgroupsave")
    public String testgroupsave(HttpServletRequest request, @Valid final Testgroup tg, final BindingResult bindingResult, final Locale locale, 
    		final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroupsave", isRussian, "id="+tg.getId());
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), tg.getId()>0?tg.getWriterId():-1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			tg.setWriterId(writerId);
			if(testService.testgroupNameExists(writerId, tg.getId(), tg.getName())){
				bindingResult.rejectValue("name", "exists.testgroup.name", "This testgroup name exists already");
			}
			if(tg.getTestersId() == null || tg.getTestersId().size() == 0){
				bindingResult.rejectValue("testersId", "empty.testgroup.testersId", "At least one tester is required");
			}
	        if(bindingResult.hasErrors()) {
	    		List<Tester> testersall = testService.findTestersByWriterIdOrderByFirstname(Utils.isRussian(locale), writerId);
	    		Map<Integer, List<Test>> testerTestsMap = testService.testerTestsMap(isRussian, writerId, testersall);
	    		Map<Integer, List<Testgroup>> testerTestgroupsMap = testService.testerTestgroupsMap(isRussian, writerId, testersall);
	        	String dest = "test/testgroupedit";
	        	model.addAttribute("testgroup", tg);
	    		model.addAttribute("testersall", testersall);
	    		model.addAttribute("testertestsmap", testerTestsMap);
	    		model.addAttribute("testertestgroupsmap", testerTestgroupsMap);
	        	model.addAttribute("resubmitUri", tg.getId()>0?("/"+dest+"?id="+tg.getId()):("/test/testgroupadd"));
	            return dest;
	    	}
	        else {
	        	testService.saveTestgroupWithMembership(tg, isRussian);
	        }
		}
		else {
			userService.recordVisit(m, request);
		}
    	return "redirect:/"+testgroups(request, locale, model, null);
    }

    @RequestMapping("/testgrouptext")
    public String testgrouptext(HttpServletRequest request, final Locale locale, final Model model, final int testId, final int id, RedirectAttributes ra){
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgrouptext", isRussian, "id="+id+",testId="+testId);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Testgroup tg = testService.findTestgroupById(isRussian, writerId, id);
		tg.setText(testService.findTestText(isRussian, writerId, testId, id));
		Test test = testService.findTestById(isRussian, writerId, testId); 
		List<Tester> testerstestgroup = testService.findTestersByTestgroupIdOrderByFirstname(isRussian, writerId, id);
		Map<Integer, List<Test>> testerTestsMap = testService.testerTestsMap(isRussian, writerId, testerstestgroup);
		Map<Integer, List<Testgroup>> testerTestgroupsMap = testService.testerTestgroupsMap(isRussian, writerId, testerstestgroup);
		String dest = "test/testgrouptext";
		model.addAttribute("test", test);
		model.addAttribute("testgroup", tg);
		model.addAttribute("testerstestgroup", testerstestgroup);
		model.addAttribute("testertestsmap", testerTestsMap);
		model.addAttribute("testertestgroupsmap", testerTestgroupsMap);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id+"&testId="+testId);
        return dest;
    }

    @RequestMapping("/testgrouptextedit")
    public String testgrouptextedit(HttpServletRequest request, final Locale locale, final Model model, final int testId, 
    		final int id, final String r, RedirectAttributes ra){
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgrouptextedit", isRussian, "id="+id+",testId="+testId);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Testgroup tg = testService.findTestgroupById(isRussian, writerId, id);
		tg.setText(testService.findTestText(isRussian, writerId, testId, id));
		Test test = testService.findTestById(isRussian, writerId, testId); 
		List<Tester> testerstestgroup = testService.findTestersByTestgroupIdOrderByFirstname(isRussian, writerId, id);
		Map<Integer, List<Test>> testerTestsMap = testService.testerTestsMap(isRussian, writerId, testerstestgroup);
		Map<Integer, List<Testgroup>> testerTestgroupsMap = testService.testerTestgroupsMap(isRussian, writerId, testerstestgroup);
		String dest = "test/testgrouptextedit";
		model.addAttribute("r", r);
		model.addAttribute("test", test);
		model.addAttribute("testgroup", tg);
		model.addAttribute("testerstestgroup", testerstestgroup);
		model.addAttribute("testertestsmap", testerTestsMap);
		model.addAttribute("testertestgroupsmap", testerTestgroupsMap);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id+"&testId="+testId+"&r="+r);
        return dest;
    }

    private static final String divText = "<"+Testgroup.DB_TEXT_MAX_LENGTH+">";
    @RequestMapping("/testgrouptextsave")
    public String testgrouptextsave(HttpServletRequest request, final Testgroup testgroup, final BindingResult bindingResult, 
    		final Locale locale, final Model model, final int testId, final String r, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgrouptextsave", isRussian, "id="+testgroup.getId()+",testId="+testId);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), testgroup.getWriterId());
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			Testgroup tg = testService.findTestgroupById(isRussian, writerId, testgroup.getId());
			String text = testgroup.getText()==null?"":testgroup.getText().trim().replaceAll(divText, "");
			tg.setText(text);
			boolean isError = false;
			if(text.length()==0){
	    		model.addAttribute("error", "test.text.required");
				isError = true;
			}
			else if(text.length() > Testgroup.DB_TEXT_MAX_LENGTH){
	    		model.addAttribute("error", "test.text.too.long");
				String str1 = StringUtils.substring(text, Testgroup.DB_TEXT_MAX_LENGTH);
				String str2 = text.substring(Testgroup.DB_TEXT_MAX_LENGTH);
				tg.setText(str1+divText+str2);
				isError = true;
	    	}
	        if(isError){
	    		Test test = testService.findTestById(isRussian, writerId, testId); 
	    		List<Tester> testerstestgroup = testService.findTestersByTestgroupIdOrderByFirstname(isRussian, writerId, testgroup.getId());
	    		Map<Integer, List<Test>> testerTestsMap = testService.testerTestsMap(isRussian, writerId, testerstestgroup);
	    		Map<Integer, List<Testgroup>> testerTestgroupsMap = testService.testerTestgroupsMap(isRussian, writerId, testerstestgroup);
	    		String dest = "test/testgrouptextedit";
	    		model.addAttribute("r", r);
	    		model.addAttribute("test", test);
	    		model.addAttribute("testgroup", tg);
	    		model.addAttribute("testerstestgroup", testerstestgroup);
	    		model.addAttribute("testertestsmap", testerTestsMap);
	    		model.addAttribute("testertestgroupsmap", testerTestgroupsMap);
	        	model.addAttribute("resubmitUri", "/"+dest+"?id="+testgroup.getId()+"&testId="+testId+"&r="+r);
	            return dest;
	        }
	        else {        	
	        	testService.saveTestgroupText(tg, testId);
	        }
		}
		else {
			userService.recordVisit(m, request);
		}
    	return display(request, null, r, locale, model, testId, testgroup.getId(), 0, ra, "testgrouptext");
    }

    @RequestMapping("/testgrouptextdel")
    public String testgrouptextdel(HttpServletRequest request, final Locale locale, final Model model, final int testId, 
    		final String testTextLength, final String confirm, final String r, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgrouptextdel", isRussian, "testId="+testId+",confirm="+confirm);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		if(!StringUtils.isEmpty(confirm) && writerId > 0){
    		Test test = testService.findTestById(isRussian, writerId, testId);
			for(Testgroup tg: testService.findTestgroupsByTestIdOrderByName(isRussian, writerId, testId)){
				test.addTestgroupId(tg.getId());
			}
		    testService.deleteTestgroupTexts(writerId, testId, test.getTestTextLength(), test.getTestgroupsId());
		    test.setTestTextLength(testTextLength);
		    testService.saveTest(isRussian, test);
		}
    	return display(request, null, r, locale, model, testId, 0, 0, ra, "test");
    }

    @RequestMapping("/testgroupfilesave")
    public String testgroupfilesave(HttpServletRequest request, @RequestParam("file") MultipartFile file, Testgroup testgroup, 
    		Model model, Locale locale, int testId, final String r, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroupfilesave", isRussian, "id="+testgroup.getId()+",testId="+testId);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), testgroup.getWriterId());
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			Testgroup tg = testService.findTestgroupById(isRussian, writerId, testgroup.getId());
			boolean isError = false;
			if(file.isEmpty()){
	    		model.addAttribute("error", "test.file.required");
				isError = true;
			}
			else if(file.getSize() > 204800){//200K
	    		model.addAttribute("error", "test.file.too.long");
				isError = true;
	    	}
	        if(isError){
	    		Test test = testService.findTestById(isRussian, writerId, testId); 
	    		List<Tester> testerstestgroup = testService.findTestersByTestgroupIdOrderByFirstname(isRussian, writerId, testgroup.getId());
	    		Map<Integer, List<Test>> testerTestsMap = testService.testerTestsMap(isRussian, writerId, testerstestgroup);
	    		Map<Integer, List<Testgroup>> testerTestgroupsMap = testService.testerTestgroupsMap(isRussian, writerId, testerstestgroup);
	    		String dest = "test/testgrouptextedit";
	    		model.addAttribute("test", test);
	    		model.addAttribute("testgroup", tg);
	    		model.addAttribute("testerstestgroup", testerstestgroup);
	    		model.addAttribute("testertestsmap", testerTestsMap);
	    		model.addAttribute("testertestgroupsmap", testerTestgroupsMap);
	        	model.addAttribute("resubmitUri", "/"+dest+"?id="+testgroup.getId()+"&testId="+testId+"&r="+r);
	            return dest;
	        }
	        else {
	        	testService.saveTestgroupFile(tg, testId, file);
	        }
		}
		else {
			userService.recordVisit(m, request);
		}
    	return display(request, null, r, locale, model, testId, testgroup.getId(), 0, ra, "testgrouptext");
    }
    
    @RequestMapping("/testgroupfiledel")
    public String testgroupfiledel(HttpServletRequest request, final Locale locale, final Model model, final int testId, final int tgId, final String r, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroupfiledel", isRussian, "id="+tgId+",testId="+testId);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
	    testService.deleteTestgroupText(writerId, testId, tgId, Test.TEXT_LENGTH_LONG);
		return display(request, null, r, locale, model, testId, tgId, 0, ra, "testgrouptext");
    }
    
    @RequestMapping("/testgroupdel")
    public String testgroupdel(HttpServletRequest request, final Locale locale, final Model model, final int id, String confirm, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testgroupdel", isRussian, "id="+id+",confirm="+confirm);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			Testgroup tg = testService.findTestgroupById(isRussian, writerId, id);
	        if(StringUtils.isEmpty(confirm)){
	        	model.addAttribute("namedelete", tg.getName());
	        	model.addAttribute("deleteUri", "/test/testgroupdel?id="+id);
	        	final List<Test> tests = testService.findTestsByTestgroupIdOrderByName(isRussian, writerId, id);
	        	if(tests.size() > 0){
	            	model.addAttribute("warn1property", "tests.affected");
	            	List<String> l = new ArrayList<String>();
	            	for(Test t: tests){
	            		l.add(t.getName());
	            	}
	            	model.addAttribute("warn1text", DisplayUtils.commaSeparated(l));
	        	}
	        	return testgroup(request, locale, model, id, ra);
	        }
	        else {
	    		testService.deleteTestgroup(isRussian, tg);
	        }
		}
		else {
			userService.recordVisit(m, request);
		}
		return "redirect:/"+testgroups(request, locale, model, ra);
    }

    @RequestMapping("/testers")
    public String testers(HttpServletRequest request, final Locale locale, final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testers", isRussian, null);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		List<Tester> testersall = testService.findTestersByWriterIdOrderByFirstname(isRussian, writerId);
		Map<Integer, List<Test>> testerTestsMap = testService.testerTestsMap(isRussian, writerId, testersall);
		Map<Integer, List<Testgroup>> testerTestgroupsMap = testService.testerTestgroupsMap(isRussian, writerId, testersall);
    	String dest = "test/testers";
		model.addAttribute("testersall", testersall);
		model.addAttribute("testertestsmap", testerTestsMap);
		model.addAttribute("testertestgroupsmap", testerTestgroupsMap);
		model.addAttribute("resubmitUri", "/"+dest);
		return addGuide(dest, model, isRussian, writerId);
    }

    @RequestMapping("/tester")
    public String tester(HttpServletRequest request, final Locale locale, final Model model, final int id, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("tester", isRussian, "id="+id);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Tester tester = testService.findTesterById(isRussian, writerId, id);		
		List<Testgroup> testgroupstester = testService.findTestgroupsByTesterIdOrderByName(isRussian, writerId, id);
		List<Test> teststester = testService.findTesterTestsByTestgroupsOrderByName(isRussian, writerId, testgroupstester);
    	String dest = "test/tester";
		model.addAttribute("tester", tester);
		model.addAttribute("teststester", teststester);
		model.addAttribute("testgroupstester", testgroupstester);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id);
        return dest;
    }

    @RequestMapping("/testeredit")
    public String testeredit(HttpServletRequest request, @RequestParam String buttonValue, final Locale locale, 
    		final Model model, final int id, RedirectAttributes ra) {
    	if("testerdel".equals(buttonValue)){
    		return testerdel(request, locale, model, id, "", ra);
    	}
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testeredit", isRussian, "id="+id);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Tester tester = testService.findTesterById(isRussian, writerId, id);
		List<Testgroup> testgroupstester = testService.findTestgroupsByTesterIdOrderByName(isRussian, writerId, id);
		List<Test> teststester = testService.findTesterTestsByTestgroupsOrderByName(isRussian, writerId, testgroupstester);
		String dest = "test/testeredit";
		model.addAttribute("tester", tester);
		model.addAttribute("teststester", teststester);
		model.addAttribute("testgroupstester", testgroupstester);
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id+"&buttonValue="+buttonValue);
        return dest;
    }
    
    @RequestMapping("/itesteradd")
    public String itesteradd(HttpServletRequest request, Locale locale, Testgroup testgroup, Model model, int id, RedirectAttributes ra) {
		Set<Integer> ids = new HashSet<Integer>();
		ids.add(id);
		testgroup.setTestersId(ids);
		String m = m("***itesteradd", false, "id="+id+",ids="+DisplayUtils.commaSeparatedSorted(testgroup.getTestersId()));
    	log.debug(m);    	
        return itestersadd(request, testgroup, locale, model, ra);
    }

    @RequestMapping("/itestersadd")
    public String itestersadd(HttpServletRequest request, Testgroup testgroup, Locale locale, Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("itestersadd", isRussian, "ids="+DisplayUtils.commaSeparatedSorted(testgroup.getTestersId()));
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0 && testgroup.getWriterId() == writerId){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			for(int id:testgroup.getTestersId()){
				testService.cloneItesterToTester(id, writerId);
			}
		}
		else {
			userService.recordVisit(m, request);
		}
        return testers(request, locale, model, ra);
    }

    @RequestMapping("/itesterrequest")
    public String itesterrequest(HttpServletRequest request, final Locale locale, final Model model, int id, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("itesterrequest", isRussian, "id="+id);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Request req = userService.findRequestById("i", id, isRussian);
		Testgroup tg = new Testgroup();
		tg.setWriterId(writerId);
    	String dest = "test/itesterrequest";
    	model.addAttribute("request", req);
		model.addAttribute("testgroup", tg);
		model.addAttribute("itesterId", req.getRoleId());
    	model.addAttribute("resubmitUri", "/"+dest+"?id="+id);
        return dest;
    }

    @RequestMapping("/itesters")
    public String itesters(HttpServletRequest request, final Locale locale, final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("itesters", isRussian, null);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
		Testgroup tg = new Testgroup();
		tg.setWriterId(writerId);
		User user = userService.findUser(SecurityContextHolder.getContext().getAuthentication());
    	List<Tester> itesters = testService.findNewItesters(user==null?0:user.getId(), writerId, isRussian, userService);
    	Map<Integer, List<Request>> testerrequestsmap = new HashMap<Integer, List<Request>>();
    	for(Tester tester:itesters){
    		tester.setId(tester.getItesterId());
    		List<Request> reqs = userService.findTesterRequestsByItesterIdAndLanguageOrderById(tester.getItesterId(), isRussian);
    		testerrequestsmap.put(tester.getId(), reqs);
    	}
    	String notestersMsg = null;
    	if(itesters.size() == 0){
    		int c = testService.countItestersByWriter(user.getId(), writerId, isRussian);
    		notestersMsg = getMessage(c==0?"itesters.no":"itesters.no.more", locale);
    	}
    	String dest = "test/itesters";
		model.addAttribute("testgroup", tg);
    	model.addAttribute("testersall", itesters);
    	model.addAttribute("notestersmsg", notestersMsg);
    	model.addAttribute("testerrequestsmap", testerrequestsmap);
    	model.addAttribute("resubmitUri", "/"+dest);
        return dest;
    }

    @RequestMapping("/testeradd")
    public String testeradd(HttpServletRequest request, @RequestParam String buttonValue, Locale locale, Model model, RedirectAttributes ra) {
    	if("addindependent".equals(buttonValue)){
    		return itesters(request, locale, model, ra);
    	}
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testeradd", isRussian, null);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
		}
		else {
			userService.recordVisit(m, request);
		}
    	String dest = "test/testeredit";
    	model.addAttribute("tester", new Tester());
    	model.addAttribute("resubmitUri", "/test/testeradd");
        return dest;
    }

    @RequestMapping("/testersave")
    public String testersave(HttpServletRequest request, @Valid final Tester tester, final BindingResult bindingResult, final Locale locale, 
    		final Model model, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testersave", isRussian, "id="+tester.getId());
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), tester.getId()>0?tester.getWriterId():-1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			tester.setWriterId(writerId);
			if(testService.testerEmailExists(writerId, tester.getId(), tester.getEmail())){
				bindingResult.rejectValue("email", "exists.tester.email", "This email exists already");
			}
	        if(bindingResult.hasErrors()) {
	    		List<Testgroup> testgroupstester = testService.findTestgroupsByTesterIdOrderByName(isRussian, writerId, tester.getId());
	    		List<Test> teststester = testService.findTesterTestsByTestgroupsOrderByName(isRussian, writerId, testgroupstester);
	        	String dest = "test/testeredit";
	    		model.addAttribute("tester", tester);
	    		model.addAttribute("teststester", teststester);
	    		model.addAttribute("testgroupstester", testgroupstester);
	        	model.addAttribute("resubmitUri", tester.getId()>0?("/"+dest+"?id="+tester.getId()):("/test/testeradd"));
	            return dest;
	    	}
	        else {
	        	testService.saveTester(tester);
	        }
		}
		else {
			userService.recordVisit(m, request);
		}
    	return "redirect:/"+testers(request, locale, model, null);
    }

    @RequestMapping("/testerdel")
    public String testerdel(HttpServletRequest request, final Locale locale, final Model model, final int id, String confirm, RedirectAttributes ra) {
		boolean isRussian = Utils.isRussian(locale);
		String m = m("testerdel", isRussian, "id="+id+",confirm="+confirm);
    	log.debug(m);    	
		int writerId = findValidWriterId(SecurityContextHolder.getContext().getAuthentication(), -1);
		if(writerId > 0){
			userService.recordVisit(m, SecurityContextHolder.getContext().getAuthentication().getName());
			Tester tester = testService.findTesterById(isRussian, writerId, id);
	        if(StringUtils.isEmpty(confirm)){
	        	model.addAttribute("namedelete", tester.getFirstname()+" "+tester.getLastname());
	        	model.addAttribute("deleteUri", "/test/testerdel?id="+id);
	        	final List<Testgroup> testgroups = testService.findTestgroupsByTesterIdOrderByName(isRussian, writerId, id);
	        	if(testgroups.size() > 0){
	            	model.addAttribute("warn1property", "testgroups.affected");
	            	List<String> l = new ArrayList<String>();
	            	for(Testgroup t: testgroups){
	            		l.add(t.getName());
	            	}
	            	model.addAttribute("warn1text", DisplayUtils.commaSeparated(l));
	            	Set<String> tests = new HashSet<String>();
	            	for(Testgroup t:testgroups){
	                	for(Test test: testService.findTestsByTestgroupIdOrderByName(isRussian, writerId, t.getId())){
	                    	tests.add(test.getName());
	                	}
	            	}
	            	if(tests.size() > 0){
	                	model.addAttribute("warn2property", "testgroups.tests.affected");
	                	model.addAttribute("warn2text", DisplayUtils.commaSeparatedSorted(tests));
	            	}
	        	}
	        	return tester(request, locale, model, id, ra);
	        }
	        else {
	    		testService.deleteTester(tester);
	        }
		}
		else {
			userService.recordVisit(m, request);
		}
    	return "redirect:/"+testers(request, locale, model, ra);
    }

    private int findValidWriterId(final Authentication authentication, final int writerId){
    	int returnId = -1;
		User user = userService.findUser(authentication);
    	if(isUserAuthorized(authentication) || user.getId()>90){
    		returnId = testService.findRoleId(user, Role.ROLE_WRITER);
    	}
    	log.debug("findValidWriterId(): writerId="+returnId);
    	return returnId;
    }

   private String display(HttpServletRequest request, String redirect, String r, final Locale locale, final Model model, final int testId, final int tgId, 
		   final int testerId, RedirectAttributes ra, final String defaultR){
   	   log.debug("display("+redirect+","+r+","+testId+","+tgId+","+testerId+","+defaultR+")");
	   if(r == null) r = "";
	   if(redirect == null) redirect = "";
	   switch (r) {
		case "tests":
			return redirect+tests(request, locale, model, ra);
		case "testers":
			return redirect+testers(request, locale, model, ra);
		case "testgroups":
			return redirect+testgroups(request, locale, model, ra);
		case "test":
			return redirect+test(request, locale, model, testId, ra);
		case "tester":
			return redirect+tester(request, locale, model, testerId, ra);
		case "testgroup":
			return redirect+testgroup(request, locale, model, tgId, ra);
		case "testgrouptext":
	    	return redirect+testgrouptext(request, locale, model, testId, tgId, ra);
		default:
			if(defaultR != null && "".equals(defaultR)){
				display(request, "".equals(redirect)?null:redirect, "".equals(defaultR)?null:defaultR, locale, model, testId, tgId, testerId, ra, null);
			}
			return tests(request, locale, model, ra);
	   }
   }
    

   @Autowired
	private TestService testService;

	@Autowired
	private MiscService miscService;

   @Autowired 
   private EmailService emailService;

	@Autowired
	private UserService userService;

    @Autowired
    private MessageSource messageSource;
  
    public TestController() {
       super();
    }
   
    @ModelAttribute("news")
    public String news(final Locale locale) {
    	return this.miscService.findNews(News.AREA_TEST, Utils.isRussian(locale));
    }    

}