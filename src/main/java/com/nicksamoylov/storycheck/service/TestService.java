package com.nicksamoylov.storycheck.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.nicksamoylov.storycheck.domain.SampleData;
import com.nicksamoylov.storycheck.domain.test.AnswerOption;
import com.nicksamoylov.storycheck.domain.test.Questions;
import com.nicksamoylov.storycheck.domain.test.Test;
import com.nicksamoylov.storycheck.domain.test.Testgroup;
import com.nicksamoylov.storycheck.domain.test.results.AnswerResult;
import com.nicksamoylov.storycheck.domain.test.results.QuestionResult;
import com.nicksamoylov.storycheck.domain.test.results.TestResult;
import com.nicksamoylov.storycheck.domain.test.results.TesterResult;
import com.nicksamoylov.storycheck.domain.test.results.TestgroupResult;
import com.nicksamoylov.storycheck.domain.test.results.UserResult;
import com.nicksamoylov.storycheck.domain.users.Itester;
import com.nicksamoylov.storycheck.domain.users.Request;
import com.nicksamoylov.storycheck.domain.users.Role;
import com.nicksamoylov.storycheck.domain.users.Tester;
import com.nicksamoylov.storycheck.domain.users.User;
import com.nicksamoylov.storycheck.service.UserService.UserMapper;
import com.nicksamoylov.storycheck.utils.FileUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.Utils;

@Service("testService")
@Transactional
public class TestService {
	private static final Logging log = Logging.getLog("TestService");
	public static final String SUCCESS = "Success";
	
	public void cloneItesterToTester(int itesterId, int writerId){
		if(writerId > 90){
		    MapSqlParameterSource map = new MapSqlParameterSource();
	    	map.addValue(Itester.DB_ID, itesterId);
			Itester itester = jdbcTemplate.queryForObject(Itester.SQL_SELECT_BY_ID, map, new ItesterMapper());
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(User.DB_ID, itester.getUserId());
			User user = jdbcTemplate.queryForObject(User.SQL_SELECT_USER_BY_ID, params, new UserMapper(false));
			Tester tester = new Tester(user, writerId, itesterId, new ArrayList<Request>());
			jdbcTemplate.update(Tester.SQL_INSERT, tester.allValuesMap());
		}
	}
	public int countItestersByWriter(int userId, int writerId, boolean isRussian){
		int c = 0;
	    MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(Tester.DB_WRITER_ID, writerId);
		List<Integer> ids = jdbcTemplate.queryForList(Tester.SQL_SELECT_ITESTER_IDS_BY_WRITER_ID, map, Integer.class);
		for(int id:ids){
			map = new MapSqlParameterSource();
	    	map.addValue(Itester.DB_ID, id);
			Itester it = jdbcTemplate.queryForObject(Itester.SQL_SELECT_BY_ID, map, new ItesterMapper());
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(User.DB_ID, it.getUserId());
			User user = jdbcTemplate.queryForObject(User.SQL_SELECT_USER_BY_ID, params, new UserMapper(false));
			if(user.getId() != userId && user.speaksLanguage(isRussian)){
				c++;
			}
		}
		log.debug("countItestersByWriter(userId="+userId+", writerId="+writerId+", lang="+Utils.lang(isRussian)+"): count="+c);
		return c;
	}
	public List<Tester> findNewItesters(int userId, int writerId, boolean isRussian, UserService userService){
		List<Tester> testers = new ArrayList<Tester>();
	    MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(Tester.DB_WRITER_ID, writerId);
		List<Integer> ids = jdbcTemplate.queryForList(Tester.SQL_SELECT_ITESTER_IDS_BY_WRITER_ID, map, Integer.class);
		List<Itester> itesters = jdbcTemplate.query(Itester.SQL_SELECT_ALL_NEW_ITESTERS(ids), map, new ItesterMapper());
		Map<Integer, Tester> testerMap = new HashMap<Integer, Tester>();
		for(Itester it:itesters){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(User.DB_ID, it.getUserId());
			User user = jdbcTemplate.queryForObject(User.SQL_SELECT_USER_BY_ID, params, new UserMapper(false));
			if(user.getId() != userId && user.speaksLanguage(isRussian)){
				List<Request> requests = userService.findRequestsByUserIdOrderById("i", user, isRussian);
				Tester tester = new Tester(user, writerId, it.getId(), requests);
				testerMap.put(it.getId(), tester);
			}
		}
		if(testerMap.size() > 0){
			List<Integer> keys = new ArrayList<Integer>(testerMap.keySet());
			Collections.sort(keys);
			for(int key:keys){
				testers.add(testerMap.get(key));
			}
		}
		log.debug("findNewItesters(userId="+userId+", writerId="+writerId+", lang="+Utils.lang(isRussian)+"): itesters.size="+testers.size());
		return testers;
	}
	public String sendFileToResponse(File file, String originalFileName, HttpServletResponse response){
		String result = SUCCESS;
		if(file != null && file.exists()){
			if(originalFileName == null){
				originalFileName = file.getName();
			}
			response.setContentType(new MimetypesFileTypeMap().getContentType(file));
			response.setContentLength((int)file.length());
			InputStream is = null;
			try{
				response.setHeader("content-disposition", "attachment; filename=" + URLEncoder.encode(originalFileName, "UTF-8"));
				is = new FileInputStream(file);
				FileCopyUtils.copy(is, response.getOutputStream());
			}
			catch(Throwable ex){
	    		log.error("sendFileToResponse():", ex);
	    		result = "Failed to stream file "+originalFileName+" to response, see log.";
			}
			finally{
				FileUtils.closeNoExceptions(is);
			}
		}
		else {
			result = "File "+file+" not found.";
		}
		return result;
	}
	public TestgroupResult findTestgroupResultById(final int tgId, boolean isRussian){
		TestgroupResult tr;
	    MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(TestgroupResult.DB_ID, tgId);
	    try{
			tr = jdbcTemplate.queryForObject(TestgroupResult.SQL_SELECT_BY_ID, map, new TestgroupResultMapper());
			List<String> tgIds = new ArrayList<String>();
			tgIds.add(Integer.toString(tgId));
			
			map = new MapSqlParameterSource();
			map.addValue(QuestionResult.DB_TEST_ID, tr.getTestId());
			map.addValue(QuestionResult.DB_QUESTION_ORDER, 1);
			QuestionResult qr = jdbcTemplate.queryForObject(QuestionResult.SQL_SELECT_BY_TEST_ID_AND_ORDER, map, new QuestionResultMapper());
			List<String> testerIds = jdbcTemplate.queryForList(TesterResult.sqlSelectTesterIdsByGroupIds(tgIds), map, String.class);

			map = new MapSqlParameterSource();
			int answersCount = jdbcTemplate.queryForInt(AnswerResult.sqlCountAnswersByTesterIdsAndQuestionId(testerIds, qr.getId()), map);

			tr.setNumberAnswered(Integer.toString(answersCount));
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error(ex);
	    	tr = SampleData.testgroupResult(1, tgId, isRussian);
	    }
        return tr;
	}
	public List<String> findTestgroupResultComments(int tgId){
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(TesterResult.DB_TESTGROUP_ID, tgId);
		return jdbcTemplate.queryForList(TesterResult.SQL_SELECT_COMMENTS_BY_GROUP_ID, map, String.class);
	}
	public List<String> findTesterResultIdsByGroupId(int tgId){
		List<String> tgIds = new ArrayList<String>();
		tgIds.add(Integer.toString(tgId));
	    final MapSqlParameterSource map = new MapSqlParameterSource();
		return jdbcTemplate.queryForList(TesterResult.sqlSelectTesterIdsByGroupIds(tgIds), map, String.class);
	}
	public List<TesterResult> findTesterResultsByGroupId(int tgId){
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(TesterResult.DB_TESTGROUP_ID, tgId);
		return jdbcTemplate.query(TesterResult.SQL_SELECT_BY_TESTGROUP_ID, map, new TesterResultMapper());
	}
	public List<TestgroupResult> findTestgroupResultsByTestIdOrdeByName(int testId){
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(TestgroupResult.DB_TEST_ID, testId);
    	return jdbcTemplate.query(TestgroupResult.SQL_SELECT_BY_TEST_ID_ORDER_BY_NAME, map, new TestgroupResultMapper());
	}
	public List<TestgroupResult> findTestgroupResultsByTestIdOrdeByName(TestResult test, Questions questions, Locale locale){
		List<TestgroupResult> testgroupstest = findTestgroupResultsByTestIdOrdeByName(test.getId());
	    final MapSqlParameterSource map = new MapSqlParameterSource();
		for(int i = 1; i<=questions.getQuestions().length; i++ ){
			QuestionResult qr = findQuestionResultByTestIdAndOrder(test, i, locale);
			int na = questions.getAnswerOptionsNumber(i);
			for(TestgroupResult tr:testgroupstest){
				List<String> testerIds = findTesterResultIdsByGroupId(tr.getId()); 
				String[] a = new String[na];
				for(int j = 1; j<=na; j++ ){
					String answerKey = questions.getAnswerKey(i, j);
				    int count = jdbcTemplate.queryForInt(AnswerResult.sqlCountAnswersByTesterIdsQuestionIdAndAnswerKey(testerIds, qr.getId(), answerKey), map);
					a[j-1] = String.valueOf(count);
				}
				tr.setQuestionAnswers(i, a);
				if(i == 1){
				    int count = jdbcTemplate.queryForInt(AnswerResult.sqlCountAnswersByTesterIdsAndQuestionId(testerIds, qr.getId()), map);
				    tr.setNumberAnswered(String.valueOf(count));
				}
			}
		}
		return testgroupstest;
	}
	public List<TestResult> findTestResultsByUserIdOrderByNameDate(int userId){
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(TestResult.DB_USER_ID, userId);
		return jdbcTemplate.query(TestResult.SQL_SELECT_BY_USER_ID_ORDER_NAME_DATE, map, new TestResultMapper());
	}
	public TestResult findTestResultById(int userId, int id, boolean isRussian){
		TestResult result = null;
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(TestResult.DB_ID, id);
    	map.addValue(TestResult.DB_USER_ID, userId);
	    try{
			result = jdbcTemplate.queryForObject(TestResult.SQL_SELECT_BY_ID, map, new TestResultMapper());
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error("findTestResultById(): userid="+userId+", id="+id, ex);
	    	result = SampleData.testResult(id, isRussian);
	    }
        return result;
	}
    public String saveAnswer(final Questions questions, long timeSpentSec, boolean isRussian){
		log.debug("saveAnswer("+questions+")");
		TesterResult testerResult = findTesterResultByTesterToken(questions.getTesterToken(), isRussian);
		if(testerResult.getId() < 90){
			return SUCCESS;
		}
		TestResult testResult = findTestResultByQuestionsToken(questions.getQuestionsToken(), isRussian);
		if(testResult.getId() < 90){
			return SUCCESS;
		}
		List<QuestionResult> qrs = findQuestionResultsByTestIdOrdered(testResult.getId());
		int questionId = 0;
		for(QuestionResult qr:qrs){
			String answerKey;
			switch (qr.getQuestionOrder()) {
			case 1:
				answerKey = questions.getAnswer1();
				questionId = qr.getId();
				break;
			case 2:
				answerKey = questions.getAnswer2();
				break;
			case 3:
				answerKey = questions.getAnswer3();
				break;
			default:
				throw new RuntimeException("Add more answers in TestService.java");
			}
			AnswerResult ar = new AnswerResult(0, testerResult.getId(), qr.getId(), answerKey);
			try{
				jdbcTemplate.update(AnswerResult.SQL_INSERT, ar.allValuesMap());
			}
			catch(Throwable ex){
				//Sometimes it happens that answer result was inserted, but tester.answeredDate was not updated because of 
				//java.lang.IllegalArgumentException: Can't change resolved type for param: 2 from 20 to 23
				List<AnswerResult> storedArs =	jdbcTemplate.query(AnswerResult.SQL_SELECT_BY_TESTER_ID_AND_QUESTION_ID, ar.allValuesMap(), new AnswerResultMapper());
				if(storedArs.size() > 0 && testerResult.getAnsweredDate() == null){
			    	testerResult.setComments(questions.getComments());
			    	testerResult.setTimeSpentSec(timeSpentSec);
					testerResult.setAnsweredDate(new Date());
					try{
						jdbcTemplate.update(TesterResult.SQL_UPDATE_BY_ID, testerResult.allValuesMap());
						testResult = findTestResultByQuestionsToken(questions.getQuestionsToken(), isRussian);
					}
					catch(Throwable x){
						log.error("Failed to update answer date on "+testerResult, ex);
						//did all I could
					}
				}
				String msg = "Failed to insert tester="+testerResult.getId()+" answer="+answerKey+" for question="+qr.getId()+", qt="+questions.getQuestionsToken()+", tt="+questions.getTesterToken();
				log.error(msg, ex);
				return msg;
			}
		}
		if(testerResult.getAnsweredDate() == null){
	    	testerResult.setComments(questions.getComments());
	    	testerResult.setTimeSpentSec(timeSpentSec);
	    	testerResult.setAnsweredDate(new Date());
			try{
				jdbcTemplate.update(TesterResult.SQL_UPDATE_BY_ID, testerResult.allValuesMap());
			}
			catch(Throwable ex){
				String msg = "Failed to update tester result="+testerResult.getId()+": comments='"+testerResult.getComments()+"', timeSpent="+testerResult.getTimeSpentSec()+", answered date="+testerResult.getAnsweredDate();
				log.error(msg, ex);
				return msg;
			}
		}
		//To set percentCompleted and completedDate for test:
		//- select tgIds by testId => all tgIds
		//- select testerIds by all tgIds => total testers
		//- select answerIds by testersId in (total testers) => total answered
	    MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(TestgroupResult.DB_TEST_ID, testResult.getId());
		List<String> tgIds = jdbcTemplate.queryForList(TestgroupResult.SQL_SELECT_IDS_BY_TEST_ID, map, String.class);
		
		map = new MapSqlParameterSource();
		List<String> testerIds = jdbcTemplate.queryForList(TesterResult.sqlSelectTesterIdsByGroupIds(tgIds), map, String.class);

		map = new MapSqlParameterSource();
		int answersCount = jdbcTemplate.queryForInt(AnswerResult.sqlCountAnswersByTesterIdsAndQuestionId(testerIds, questionId), map);

		testResult.setNumberAnswered(Integer.toString(answersCount));
		if(testResult.getNumberTesters().equals(testResult.getNumberAnswered())){
			testResult.setCompletedDate(new Date());
		}
		try{
			jdbcTemplate.update(TestResult.SQL_UPDATE_BY_ID, testResult.allValuesMap());
		}
		catch(Throwable ex){
			String msg = "Failed to update test result "+testResult.getId()+": number answered='"+testResult.getNumberAnswered()+"', date="+testResult.getCompletedDateString();
			log.error(msg, ex);
			return msg;
		}
		return SUCCESS;
    }
    public Questions getQuestionsIfNoAnswer(final String qt, final String tt, final String tl, final Locale locale){
    	Questions questions = null;
    	if(tt==null){
			questions = findQuestionsByQuestionsToken(qt, null, tl,locale); 
    	}
    	else {
    		TesterResult tr = findTesterResultByTesterToken(tt, Utils.isRussian(locale));
    		if(tr.getId() > 90 && tr.getAnsweredDate() == null){
    			questions = findQuestionsByQuestionsToken(qt, tt, tl, locale); 
    		}
    	}
		log.debug("getQuestionsIfNoAnswer("+qt+","+tt+","+tl+"): questions="+questions);
    	return questions;
    }
    public Questions findQuestionsByQuestionsToken(String questionsToken, String testerToken, String textLength, Locale locale){
    	Questions questions;
		TestResult testResult = findTestResultByQuestionsToken(questionsToken, Utils.isRussian(locale));
		if(testResult.getId() < 90){
			String tl = textLength==null?"tts":textLength;
    		questions = new Questions(tl, messageSource, locale); 
		}
		else {
			List<QuestionResult> qrs = findQuestionResultsByTestIdOrdered(testResult.getId());
			for(QuestionResult qr: qrs){
				qr.setAnswerOptions(findAnswerOptionsByQuestionId(qr.getId())); 
			}
			questions = new Questions(questionsToken, testerToken, qrs, locale); 
		}
		return questions;
    }
    public void saveEmailContent(String emailContent, final int writerId, final Test test, final String questionsToken,	
    		final Testgroup tg, boolean isRussian){
    	TestResult testResult = findTestResultByQuestionsToken(questionsToken, isRussian);
    	if(testResult.getId() > 90 && testResult.getEmailText()==null){
    		testResult.setEmailText(test.getEmailText());
    		jdbcTemplate.update(TestResult.SQL_UPDATE_EMAILTEXT_BY_ID, testResult.allValuesMap());
    	}
    }
    public Map<String, String> prepareResultsArea(EmailService emailService, final User user, final Test test, final String questionsToken, 
    		final Testgroup tg, final Set<Tester> testers, final int emailsCount, final Locale locale){
		log.debug("prepareResultsArea("+questionsToken+")");
		boolean isRussian = Utils.isRussian(locale);
    	//insert/update Results_Users
    	//insert Resuls_Tests with questionsToken and ref to Results_Users
    	//insert Resuls_Questions with ref to Results_Tests
    	//insert Results_Answer_options with ref to Results_Questions
    	//insert Results_Testgroups with a ref to Results_Tests
    	//insert Results_Testers with testerToken and ref to Results_Testgroups
    	UserResult userResult = findUserResultByUserId(user.getId());
    	if(userResult.getId() < 1){
        	userResult = new UserResult(user);
			jdbcTemplate.update(UserResult.SQL_INSERT, userResult.allValuesMap());
    	}
    	else {
    		userResult.setUserData(user);
			jdbcTemplate.update(UserResult.SQL_UPDATE_BY_ID, userResult.allValuesMap());
    	}
    	
		Questions questions = new Questions(test.getTestTextLength(), messageSource, locale);
    	TestResult testResult = findTestResultByQuestionsToken(questionsToken, isRussian);
    	if(testResult.getId() < 90){
        	testResult = new TestResult(test, user.getId(), questionsToken, emailsCount);
    		jdbcTemplate.update(TestResult.SQL_INSERT, testResult.allValuesMap());
    		testResult = jdbcTemplate.queryForObject(TestResult.SQL_SELECT_BY_QUESTIONS_TOKEN, testResult.allValuesMap(), new TestResultMapper());

    		int testId = testResult.getId();
    		String language = User.getLanguage(isRussian);
    		int i = 1;
    		for(String q:questions.getQuestionKeys()){
    			String questionRu = getMessage(q, Utils.getLocale(true));
    			String questionEn = getMessage(q, Utils.getLocale(false));
    			QuestionResult questionResult = new QuestionResult(0, testId, i, questionEn, questionRu, language);
    			jdbcTemplate.update(QuestionResult.SQL_INSERT, questionResult.allValuesMap());
    			questionResult = findQuestionResultByTestIdAndOrder(testResult, questionResult.getQuestionOrder(), locale);
    			if(questionResult.getId() > 90){
        			for(int j=1; j<=questions.getAnswerOptionsNumber(i); j++){
        				String aKey = questions.getAnswerKey(i,j);
        				String answerRu = getMessage(aKey, Utils.getLocale(true));
        				String answerEn = getMessage(aKey, Utils.getLocale(false));
        				AnswerOption ao = new AnswerOption(0, questionResult.getId(), j, aKey, answerEn, answerRu);
        				jdbcTemplate.update(AnswerOption.SQL_INSERT, ao.allValuesMap());
        			}
    			}
    			i++;
    		}
    	}
		
    	TestgroupResult testgroupResult = new TestgroupResult(testResult.getId(), tg, testers.size());
		jdbcTemplate.update(TestgroupResult.SQL_INSERT, testgroupResult.allValuesMap());
		testgroupResult = findTestgroupResultByTestIdAndName(testResult.getId(), tg.getName(), isRussian);
		if(!testResult.isTextShort()){
			String result = FileUtils.copy(Test.getFile(tg.getWriterId(), test.getId(), tg.getId()), TestResult.getFile(user.getId(), testgroupResult.getTestId(), testgroupResult.getId()), true);
			if(!SUCCESS.equals(result)){
				emailService.sendProblemToAdminWithLogAttached(user, result);
			}
		}
		
    	Map<String, String> testerTokens = new HashMap<String, String>();
    	for(Tester t:testers){
       		String testerToken = UUID.randomUUID().toString();
            testerTokens.put(t.getEmail(), testerToken);
            if(testgroupResult.getId() > 90){
            	TesterResult testerResult = new TesterResult(t, testgroupResult.getId(), testerToken, t.isIndependent());
        		jdbcTemplate.update(TesterResult.SQL_INSERT, testerResult.allValuesMap());
            }
    	}    	
    	return testerTokens;
    }
    
	private List<QuestionResult> findQuestionResultsByTestIdOrdered(final int testId){
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(QuestionResult.DB_TEST_ID, testId);
        return jdbcTemplate.query(QuestionResult.SQL_SELECT_BY_TEST_ID_ORDERED, map, new QuestionResultMapper());
	}
	private List<AnswerOption> findAnswerOptionsByQuestionId(final int questionId){
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(AnswerOption.DB_QUESTION_ID, questionId);
        return jdbcTemplate.query(AnswerOption.SQL_SELECT_BY_QUESTION_ID_ORDERED, map, new AnswerOptionMapper());
	}
	public QuestionResult findQuestionResultByTestIdAndOrder(final TestResult testResult, final int qOrder, Locale locale){
		QuestionResult result;
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(QuestionResult.DB_TEST_ID, testResult.getId());
    	map.addValue(QuestionResult.DB_QUESTION_ORDER, qOrder);
	    try{
			result = jdbcTemplate.queryForObject(QuestionResult.SQL_SELECT_BY_TEST_ID_AND_ORDER, map, new QuestionResultMapper());
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error("findQuestionResultByTestIdAndOrder(): qOrder="+qOrder+", "+testResult, ex);
	    	result = questionResult(testResult, qOrder, locale);
	    }
        return result;
	}
	private TestgroupResult findTestgroupResultByTestIdAndName(final int testId, final String tgName, boolean isRussian){
		TestgroupResult result;
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(TestgroupResult.DB_TEST_ID, testId);
    	map.addValue(TestgroupResult.DB_NAME, tgName);
	    try{
			result = jdbcTemplate.queryForObject(TestgroupResult.SQL_SELECT_BY_TEST_ID_AND_NAME, map, new TestgroupResultMapper());
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error("findTestgroupResultByTestIdAndName(): testId="+testId+", tgName="+tgName, ex);
	    	result = SampleData.testgroupResult(testId, 1, isRussian);
	    }
        return result;
	}
	public TesterResult findTesterResultByTesterToken(final String testerToken, boolean isRussian){
		TesterResult result;
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(TesterResult.DB_TESTER_TOKEN, testerToken);
	    try{
			result = jdbcTemplate.queryForObject(TesterResult.SQL_SELECT_BY_TESTER_TOKEN, map, new TesterResultMapper());
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error("findTesterResultByTesterToken(): testerToken="+testerToken, ex);
	    	result = SampleData.testerResult(isRussian); 
	    }
        return result;
	}
	public TestResult findTestResultByQuestionsToken(final String questionsToken, boolean isRussian){
		TestResult result;
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(TestResult.DB_QUESTIONS_TOKEN, questionsToken);
	    try{
			result  = jdbcTemplate.queryForObject(TestResult.SQL_SELECT_BY_QUESTIONS_TOKEN, map, new TestResultMapper());
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error("findTestResultByQuestionsToken(): questionsToken="+questionsToken, ex);
	    	result = SampleData.testResult(1, isRussian);
	    }
        return result;
	}
	private UserResult findUserResultByUserId(final int userId){
		UserResult result;
	    final MapSqlParameterSource map = new MapSqlParameterSource();
    	map.addValue(UserResult.DB_ID, userId);
	    try{
			result  = jdbcTemplate.queryForObject(UserResult.SQL_SELECT_BY_USER_ID, map, new UserResultMapper());
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
	    	log.error("findUserResultByUserId():userId="+userId, ex);
	    	UserResult user = new UserResult();
	    	user.setId(-1);
	    	return user;
	    }
        return result;
	}	
	public int findRoleId(final User user, String role) {
		Role r = new Role(role, user);
	    int writerId = jdbcTemplate.queryForInt(r.sqlSelectIdByUserId(), r.allValuesMap());
		log.debug("findRoleId(role="+role+", "+user.display()+"): id="+writerId);
		return writerId;
	}	
	public int countTests(final boolean isRussian, final int writerId) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Test.DB_WRITER_ID, writerId);
		    int c = jdbcTemplate.queryForInt(Test.SQL_COUNT_BY_WRITER_ID, params);
			log.debug("countTests("+writerId+"): "+c);
			return c;
		}
		else {
			return 1;
		}
	}
	public int countTesters(final boolean isRussian, final int writerId) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Tester.DB_WRITER_ID, writerId);
		    int c = jdbcTemplate.queryForInt(Tester.SQL_COUNT_BY_WRITER_ID, params);
			log.debug("countTesters("+writerId+"): "+c);
			return c;
		}
		else {
			return 1;
		}
	}
	public int countTestgroups(final boolean isRussian, final int writerId) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Testgroup.DB_WRITER_ID, writerId);
		    int c = jdbcTemplate.queryForInt(Testgroup.SQL_COUNT_BY_WRITER_ID, params);
			log.debug("countTestgroups("+writerId+"): "+c);
			return c;
		}
		else {
			return 1;
		}
	}
	public List<Test> findTestsByWriterIdOrderByName(final boolean isRussian, final int writerId) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Test.DB_WRITER_ID, writerId);
		    List<Test> tests = jdbcTemplate.query(Test.SQL_SELECT_ALL_BY_WRITER_ID_ORDER_BY_NAME, params, new TestMapper());
		    List<Integer> ids = new ArrayList<Integer>();
			for(Test test: tests){
				test.setStatus(findTestStatus(isRussian, writerId, test));
				ids.add(test.getId());
			}
			log.debug("findTestsByWriterIdOrderByName("+writerId+"): ids="+ids);
			return tests;
		}
		else {
			return SampleData.tests(isRussian);
		}
	}
	
	public Test findTestById(final boolean isRussian, final int writerId, final int id) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Test.DB_ID, id);
		    params.addValue(Test.DB_WRITER_ID, writerId);
		    try{
				Test test = jdbcTemplate.queryForObject(Test.SQL_SELECT_BY_WRITER_ID_AND_ID, params, new TestMapper());
				test.setStatus(findTestStatus(isRussian, writerId, test));
				log.debug("findTestById("+id+"): test="+test);
				return test;
		    }
		    catch(IncorrectResultSizeDataAccessException ex){
				log.debug("findTestById("+id+"): test(ex)=null");
				return SampleData.test(id, isRussian);
		    }
		}
		else {
			return SampleData.test(id, isRussian);
		}
	}
	private Test findTestByIdWoStatus(final int writerId, final int id, boolean isRussian){ 
	    final MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue(Test.DB_ID, id);
	    params.addValue(Test.DB_WRITER_ID, writerId);
	    try{
			Test test = jdbcTemplate.queryForObject(Test.SQL_SELECT_BY_WRITER_ID_AND_ID, params, new TestMapper());
			log.debug("findTestByIdWoStatus("+id+"): test="+test);
			return test;
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
			log.debug("findTestByIdWoStatus("+id+"): test(ex)=null");
	    	return SampleData.test(1, isRussian);
	    }
	}
	
	
    public String findTestStatus(final boolean isRussian, final int writerId, final Test test){
    	String status = test.isImage()?Test.STATUS_NO_GROUPS_NO_IMAGE:Test.STATUS_NO_GROUPS_NO_TEXT;
		boolean allTextsSet = true;
		Set<Integer> tgIds = findTestgroupIdsByTestId(isRussian, writerId, test.getId());
		if(tgIds.size() > 0){
			for(int tgId:tgIds){
				String text = findTestText(isRussian, writerId, test.getId(), tgId);
				if(text == null || text.trim().length() == 0){
					allTextsSet = false;
				}
			}
			if(allTextsSet){				
				if(test.getEmailText() == null || test.getEmailText().trim().length()==0){
					status = Test.STATUS_NO_EMAIL;
				}
				else {
					status = Test.STATUS_READY;
				}
			}
			else {
				status = test.isImage()?Test.STATUS_PARTIAL_IMAGE:Test.STATUS_PARTIAL_TEXT;
			}
		}
		if(!status.equals(test.getStatus())){
		    final MapSqlParameterSource map = new MapSqlParameterSource();
	    	map.addValue(Test.DB_ID, test.getId());
	    	map.addValue(Test.DB_STATUS, status);
	    	map.addValue(Test.DB_MODIFIED_DATE, new Date());
	    	test.setStatus(status);
			jdbcTemplate.update(Test.SQL_UPDATE_STATUS_BY_ID, map);
			log.debug("findTestStatus("+test.getStatus()+"): new status="+status);
		}
		else {
			log.debug("findTestStatus("+test.getStatus()+"): no change status="+status);
		}
		return status;
    }
    
	public int saveTest(final boolean isRussian, final Test test) {
		int testId = 1;
		if(test.getWriterId() > 0){
			if(test.getId() < 1){
				jdbcTemplate.update(Test.SQL_INSERT, test.allValuesMap());
				final Test t = findTestByName(test.getWriterId(), test.getName(), isRussian);
				testId = t.getId();
			}
			else {
				Test t = findTestByIdWoStatus(test.getWriterId(), test.getId(), isRussian);
				if(t.getId() > 90){
					test.setStatus(findTestStatus(isRussian, test.getWriterId(), test));
					jdbcTemplate.update(Test.SQL_UPDATE_BY_ID, test.allValuesMap());
					t = findTestByName(test.getWriterId(), test.getName(), isRussian);
					testId = t.getId();
				}
			}
		}
		log.debug("saveTest("+test+"): testId="+testId);
		return testId;
	}
	
	private Set<Integer> findTestgroupIdsByTestId(boolean isRussian, int writerId, int testId){
		Set<Integer> ids = new HashSet<Integer>();
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    if(testId > 0){
			    params.addValue(Test.DB_TEST_MEMBERS_TEST_ID, testId);
				ids.addAll(jdbcTemplate.queryForList(Test.SQL_SELECT_TESTGROUP_IDS_BY_TEST_ID, params, Integer.class));
		    }
		    else {
		    	for(Testgroup tg:SampleData.testgroups(isRussian)){
		    		ids.add(tg.getId());
		    	}
		    }
		}
		else {
	    	for(Testgroup tg:SampleData.testgroups(isRussian)){
	    		ids.add(tg.getId());
	    	}
		}
		log.debug("findTestgroupIdsByTestId("+testId+"): ids="+ids);
	    return ids;
	}

	public void deleteTest(final boolean isRussian, final Test test) {
		log.debug("deleteTest("+test+")");
		if(test.getWriterId() > 0){
			final Test t = findTestByIdWoStatus(test.getWriterId(), test.getId(), isRussian);
			if(t != null && t.getId() > 90){
				if(!t.isTextShort()){
					for(int tgId: findTestgroupIdsByTestId(isRussian, test.getWriterId(), test.getId())){
						deleteTestgroupFile(test.getWriterId(), test.getId(), tgId);
					}
				}
			    final MapSqlParameterSource map = new MapSqlParameterSource();
		    	map.addValue(Test.DB_TEST_MEMBERS_TEST_ID, test.getId());
				jdbcTemplate.update(Test.SQL_DELETE_ALL_MEMBERS, map);
				jdbcTemplate.update(Test.SQL_DELETE_BY_ID, test.allValuesMap());
			}
		}
	}
	
	public List<Test> findTestsByTestgroupIdOrderByName(final boolean isRussian, final int writerId, final int testgroupId) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Test.DB_TEST_MEMBERS_TESTGROUP_ID, testgroupId);
		    final List<Test> tests = jdbcTemplate.query(Test.SQL_SELECT_BY_TESTGROUP_IDS_ORDER_BY_NAME, params, new TestMapper());
		    List<Integer> ids = new ArrayList<Integer>();
			for(final Test t: tests){
				if(t.getWriterId() != writerId){
					return SampleData.tests(isRussian);
				}
				ids.add(t.getId());
			}
			log.debug("findTestsByTestgroupIdOrderByName("+testgroupId+"): ids="+ids);
			return tests;
		}
		else {
			return SampleData.tests(isRussian);
		}
	}
	
	public String findTestText(final boolean isRussian, final int writerId, final int testId, final int testgroupId) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Test.DB_TEST_MEMBERS_TEST_ID, testId);
		    params.addValue(Test.DB_TEST_MEMBERS_TESTGROUP_ID, testgroupId);
		    List<String> l = jdbcTemplate.queryForList(Test.SQL_SELECT_TEXT_BY_TEST_ID_AND_TESTGROUP_ID, params, String.class);
		    String text = ""; 
		    if(l.size() > 0){
		    	text = l.get(0);
		    }
			log.debug("findTestText(testId="+testId+",tgId="+testgroupId+"): text.length()="+(text==null?"null":text.length()));
			return text;
		}
		else {
			return SampleData.testText(testId, testgroupId, isRussian);
		}
	}
	
	public void saveTestgroupText(final Testgroup tg, final int testId) {
		log.debug("saveTestgroupText(tgId="+tg.getId()+",testId="+testId+")");
		if(tg.getWriterId() > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Test.DB_TEST_MEMBERS_TEST_ID, testId);
		    params.addValue(Test.DB_TEST_MEMBERS_TESTGROUP_ID, tg.getId());
		    params.addValue(Test.DB_TEST_MEMBERS_TEXT, tg.getText());
		    jdbcTemplate.update(Test.SQL_UPDATE_TEXT_BY_TEST_ID_AND_TESTGROUP_ID, params);
		}
	}
	
	public void saveTestgroupFile(final Testgroup tg, final int testId, final MultipartFile file) {
		log.debug("saveTestgroupFile(testId="+testId+", file="+file.getOriginalFilename()+")");
    	tg.setText(file.getOriginalFilename());
    	saveTestgroupText(tg, testId);
    	if(tg.getWriterId() > 0){
    		FileUtils.saveFile(file, Test.getTextfileDirName(tg.getWriterId()), Test.getTextfileName(testId, tg.getId()));
    	}
	}
	
	public void deleteTestgroupTexts(final int writerId, final int testId, final String textLength, final Set<Integer> tgIds){
		log.debug("deleteTestgroupTexts(testId="+testId+")");
		for(int tgId:tgIds){
			deleteTestgroupText(writerId, testId, tgId, textLength);
		}
	}
	
	public void deleteTestgroupText(final int writerId, final int testId, final int tgId, final String textLength){
		log.debug("deleteTestgroupTexts(testId="+testId+", tgId="+tgId+")");
		if(writerId >0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Test.DB_TEST_MEMBERS_TEST_ID, testId);
		    params.addValue(Test.DB_TEST_MEMBERS_TESTGROUP_ID, tgId);
		    params.addValue(Test.DB_TEST_MEMBERS_TEXT, null);
		    jdbcTemplate.update(Test.SQL_UPDATE_TEXT_BY_TEST_ID_AND_TESTGROUP_ID, params);
		    if(Test.TEXT_LENGTH_LONG.equals(textLength)){
		    	deleteTestgroupFile(writerId, testId, tgId);
		    }
		}
	}
	
	public void deleteTestgroupFile(final int writerId, final int testId, final int tgId){
		if(writerId >0){
	    	File testFile = Test.getFile(writerId, testId, tgId);
	    	if(testFile.exists()){
	    		testFile.delete();
				log.debug("deleteTestgroupFile(testId="+testId+"): file deleted: "+testFile.getName());
	    	}
	    	else {
				log.debug("deleteTestgroupFile(testId="+testId+"): file not found: "+testFile.getName());
	    	}
		}
	}
	
	public List<Testgroup> findTestgroupsByWriterIdOrderByName(final boolean isRussian, final int writerId) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Testgroup.DB_WRITER_ID, writerId);
			List<Testgroup> list = jdbcTemplate.query(Testgroup.SQL_SELECT_ALL_BY_WRITER_ID_ORDER_BY_NAME, params, new TestgroupMapper());
			for(Testgroup tg:list){
				int size = findTesterIdsByTestgroupIdOrderByFirstname(isRussian, writerId, tg.getId()).size();
	            tg.setSize(Integer.toString(size));
			}
			log.debug("findTestgroupsByWriterIdOrderByName("+writerId+"): list.size="+list.size());
			return list;
		}
		else {
			return SampleData.testgroups(isRussian);
		}
	}
	
	public List<Testgroup> findTestgroupsByTestIdOrderByName(final boolean isRussian, final int writerId, final int id) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Test.DB_TEST_MEMBERS_TEST_ID, id);
		    final List<Testgroup> testgroups = jdbcTemplate.query(Testgroup.SQL_SELECT_BY_TEST_ID_ORDER_BY_NAME, params, new TestgroupMapper());
		    List<Integer> ids = new ArrayList<Integer>();
			for(final Testgroup tg: testgroups){
				if(tg.getWriterId() != writerId){
					return SampleData.testgroups(isRussian);
				}
				tg.setText(findTestText(isRussian, writerId, id, tg.getId()));
				int size = findTesterIdsByTestgroupIdOrderByFirstname(isRussian, writerId, tg.getId()).size();
	            tg.setSize(Integer.toString(size));
	            ids.add(tg.getId());
			}
			log.debug("findTestgroupsByTestIdOrderByName("+id+"): ids="+ids);
			return testgroups;
		}
		else {
			return SampleData.testgroups(id, isRussian);
		}
	}
	
	public List<Testgroup> findTestgroupsByTesterIdOrderByName(final boolean isRussian, final int writerId, final int id) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Testgroup.DB_TESTGROUP_MEMBERS_TESTER_ID, id);
		    final List<Testgroup> testgroups = jdbcTemplate.query(Testgroup.SQL_SELECT_BY_TESTER_ID_ORDER_BY_NAME, params, new TestgroupMapper());
			log.debug("findTestgroupsByTesterIdOrderByName("+id+"): testgroups.size="+testgroups.size());
			return testgroups;
		}
		else {
			return SampleData.testgroups(isRussian);
		}
	}
	
	public Testgroup findTestgroupById(final boolean isRussian, final int writerId, final int id) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Testgroup.DB_ID, id);
		    params.addValue(Testgroup.DB_WRITER_ID, writerId);
		    Testgroup tg = null;
		    try{
		    	tg = jdbcTemplate.queryForObject(Testgroup.SQL_SELECT_BY_WRITER_ID_AND_ID, params, new TestgroupMapper());
	    		log.debug("findTestgroupById("+id+"): tg="+tg);
	    		return tg;
		    }
		    catch(IncorrectResultSizeDataAccessException ex){
	    		log.debug("findTestgroupById("+id+"): tg(ex)="+tg);
				return SampleData.testgroup(id, isRussian);
		    }
		}
		else {
			return SampleData.testgroup(id, isRussian);
		}
	}
	public Map<Integer, List<Test>> testerTestsMap(boolean isRussian, final int writerId, final List<Tester> testers){
		if(writerId > 0){
			Map<Integer, List<Test>> testsMap = new HashMap<Integer, List<Test>>();
			for(Tester t: testers){
				List<Testgroup> tgs = findTestgroupsByTesterIdOrderByName(isRussian, writerId, t.getId());
				List<Test> tests = findTesterTestsByTestgroupsOrderByName(isRussian, writerId, tgs); 
				testsMap.put(t.getId(), tests);
			}
			return testsMap;
		}
		else {
			return SampleData.testerTestsMap(isRussian);
		}
	}
	
	public Map<Integer, List<Testgroup>> testerTestgroupsMap(boolean isRussian, final int writerId, final List<Tester> testers){
		if(writerId > 0){
			Map<Integer, List<Testgroup>> testsMap = new HashMap<Integer, List<Testgroup>>();
			for(Tester t: testers){
				List<Testgroup> tgs = findTestgroupsByTesterIdOrderByName(isRussian, writerId, t.getId());
				testsMap.put(t.getId(), tgs);
			}
			return testsMap;
		}
		else {
			return SampleData.testerTestgroupsMap(isRussian);
		}
	}
	
	public Map<Integer, List<Test>> tgTestsMap(boolean isRussian, final int writerId, final List<Testgroup> testgroups){
		if(writerId > 0){
			Map<Integer, List<Test>> testsMap = new HashMap<Integer, List<Test>>();
			for(Testgroup tg: testgroups){
				List<Test> tests = findTestsByTestgroupIdOrderByName(isRussian, writerId, tg.getId()); 
				testsMap.put(tg.getId(), tests);
			}
			return testsMap;
		}
		else {
			return SampleData.tgTestsMap(isRussian);
		}
	}
	
	public List<Test> findTesterTestsByTestgroupsOrderByName(final boolean isRussian, final int writerId, final List<Testgroup> testgroups){
		if(writerId > 0){
			Map<String, Test> testsMap = new HashMap<String, Test>();
			for(Testgroup tg: testgroups){
				for(Test t: findTestsByTestgroupIdOrderByName(isRussian, writerId, tg.getId())){
					testsMap.put(t.getName(), t);
				}
			}
			List<Test> teststester = new ArrayList<Test>();
			List<String> l = new ArrayList<String>(testsMap.keySet());
	    	Collections.sort(l);
			for(String s:l){
				teststester.add(testsMap.get(s));
			}
			log.debug("findTesterTestsByTestgroupsOrderByName(testgroups.size()="+testgroups.size()+"): teststester.size="+teststester.size());
			return teststester;
		}
		return SampleData.tests(isRussian);
	}
	
	public void saveTestgroupWithMembership(final Testgroup tg, final boolean isRussian) {
		log.debug("saveTestgroupWithMembership ("+tg+")");
		if(tg.getWriterId() > 0){
			if(tg.getId() == 0){
				jdbcTemplate.update(Testgroup.SQL_INSERT, tg.allValuesMap());
				final Testgroup t = findTestgroupByName(tg.getWriterId(), tg.getName());
				tg.setId(t.getId());
				addUpdateTestgroupMembers(tg);
			}
			else {
				final Testgroup t = findTestgroupById(isRussian, tg.getWriterId(), tg.getId());
				if(t.getId() > 90){
					jdbcTemplate.update(Testgroup.SQL_UPDATE_BY_ID, tg.allValuesMap());
					addUpdateTestgroupMembers(tg);
				}
			}			
		}
	}
	
	private void addUpdateTestgroupMembers(Testgroup tg){
		log.debug("addUpdateTestgroupMembers("+tg+")");
		if(tg.getTestersId() == null){
			tg.setTestersId(new HashSet<Integer>());
		}
	    final MapSqlParameterSource params = new MapSqlParameterSource();
	    if(tg.getId() > 0){
		    params.addValue(Testgroup.DB_TESTGROUP_MEMBERS_TESTGROUP_ID, tg.getId());
			Set<Integer> oldIds = new HashSet<Integer>(jdbcTemplate.queryForList(Testgroup.SQL_SELECT_TESTER_IDS_BY_TESTGROUP_ID, params, Integer.class));
			if(oldIds.size() > 0){
				Set<Integer> delIds = new HashSet<Integer>(oldIds);
				delIds.removeAll(tg.getTestersId());
				if(delIds.size() > 0){
					jdbcTemplate.update(Testgroup.sqlDeleteGroupMembersByIds(delIds), params);
				}
			}
			if(tg.getTestersId().size() > 0){
				tg.getTestersId().removeAll(oldIds);
			}
	    }
	    if(tg.getTestersId().size() > 0){
		    MapSqlParameterSource[] maps = new MapSqlParameterSource[tg.getTestersId().size()];
		    int i = 0;
		    for(int testerId:tg.getTestersId()){
			    final MapSqlParameterSource map = new MapSqlParameterSource();
		    	map.addValue(Testgroup.DB_TESTGROUP_MEMBERS_TESTGROUP_ID, tg.getId());
		    	map.addValue(Testgroup.DB_TESTGROUP_MEMBERS_TESTER_ID, testerId);
		    	maps[i++] = map;
		    }
			jdbcTemplate.batchUpdate(Testgroup.SQL_INSERT_MEMBER, maps);
	    }
	}
	
	public void deleteTestgroup(final boolean isRussian, final Testgroup tg) {
		log.debug("deleteTestgroup("+tg+")");
		if(tg.getWriterId() > 0){
			final Testgroup t = findTestgroupById(isRussian, tg.getWriterId(), tg.getId());
			if(t != null && t.getId() > 90){
				for(Test test: findTestsByTestgroupIdOrderByName(isRussian, t.getWriterId(), t.getId())){
					if(test.isTextLong()){
						deleteTestgroupFile(tg.getWriterId(), test.getId(), tg.getId());
					}
				}				
			    final MapSqlParameterSource map = new MapSqlParameterSource();
		    	map.addValue(Testgroup.DB_TESTGROUP_MEMBERS_TESTGROUP_ID, tg.getId());
				jdbcTemplate.update(Testgroup.SQL_DELETE_ALL_MEMBERSHIP, map);
				
				final MapSqlParameterSource map2 = new MapSqlParameterSource();
		    	map2.addValue(Test.DB_TEST_MEMBERS_TESTGROUP_ID, tg.getId());
				jdbcTemplate.update(Test.SQL_DELETE_ALL_MEMBERSHIP, map2);				

                jdbcTemplate.update(Testgroup.SQL_DELETE_BY_ID, tg.allValuesMap());
		    }
		}
	}
	
	public boolean testNameExists(final int writerId, final int id, final String name, boolean isRussian){
		boolean result= false;
		if(!StringUtils.isEmpty(name)){
			Test t = findTestByName(writerId, name, isRussian);
			if(id < 1){
				result = t.getId() > 90;
			}
			else {
				result = t.getId() < 90 && t.getId() != id;
			}
		}
		log.debug("testNameExists("+name+"): "+result);
		return result;
	}
	
	public boolean testgroupNameExists(final int writerId, final int id, final String name){
		boolean result= false;
		if(!StringUtils.isEmpty(name)){
			Testgroup t = findTestgroupByName(writerId, name);
			if(id < 1){
				result = t != null;
			}
			else {
				result = t != null && t.getId() != id;
			}
		}
		log.debug("testgroupNameExists("+name+"): "+result);
		return result;
	}
	
	public boolean testerEmailExists(final int writerId, final int id, final String email){
		boolean result= false;
		if(StringUtils.isEmpty(email)){
			Tester t = findTesterByEmail(writerId, email);
			if(id < 1){
				result = t != null;
			}
			else {
				result = t != null && t.getId() != id;
			}
		}
		log.debug("testerEmailExists("+email+"): "+result);
		return result;
	}
	
	private Test findTestByName(final int writerId, final String name, boolean isRussian){ 
	    final MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue(Test.DB_WRITER_ID, writerId);
	    params.addValue(Test.DB_NAME, name);
	    try{
			Test test = jdbcTemplate.queryForObject(Test.SQL_SELECT_BY_WRITER_ID_AND_NAME, params, new TestMapper());
			log.debug("findTestByName("+name+"): "+test);
			return test;
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
			log.debug("findTestByName("+name+"): test(ex)=null");
	    	return SampleData.test(1, isRussian);
	    }
	}
		
	private Testgroup findTestgroupByName(final int writerId, final String name){ 
	    final MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue(Testgroup.DB_WRITER_ID, writerId);
	    params.addValue(Testgroup.DB_NAME, name);
	    try{
			Testgroup tg = jdbcTemplate.queryForObject(Testgroup.SQL_SELECT_BY_WRITER_ID_AND_NAME, params, new TestgroupMapper());
			log.debug("findTestgroupByName("+name+"): "+tg);
	    	return tg;
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
			log.debug("findTestgroupByName("+name+"): tg(ex)=null");
	    	return null;
	    }
	}
		
	private Tester findTesterByEmail(final int writerId, final String email){ 
	    final MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue(Tester.DB_WRITER_ID, writerId);
	    params.addValue(Tester.DB_EMAIL, email);
	    try{
			Tester tester = jdbcTemplate.queryForObject(Tester.SQL_SELECT_BY_WRITER_ID_AND_EMAIL, params, new TesterMapper());
			log.debug("findTesterByEmail("+email+"): "+tester);
	    	return tester;
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
			log.debug("findTesterByEmail("+email+"): tester(ex)=null");
	    	return null;
	    }
	}
		
	public List<Tester> findTestersByWriterIdOrderByFirstname(final boolean isRussian, final int writerId) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Tester.DB_WRITER_ID, writerId);
		    List<Tester> l = jdbcTemplate.query(Tester.SQL_SELECT_ALL_BY_WRITER_ID_ORDER_BY_FIRSTNAME, params, new TesterMapper());
			log.debug("findTestersByWriterIdOrderByFirstname("+writerId+"): list.size="+l.size());
			return l;
		}
		else {
			return SampleData.testers(isRussian);
		}
	}
	
/*	public List<Tester> findTestersByWriterIdAndLangOrderByFirstname(final boolean isRussian, final int writerId) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Tester.DB_WRITER_ID, writerId);
		    params.addValue(Tester.DB_LANG, User.getLanguage(isRussian));
		    List<Tester> l = jdbcTemplate.query(Tester.SQL_SELECT_ALL_BY_WRITER_ID_AND_LANG_ORDER_BY_FIRSTNAME, params, new TesterMapper());
			log.debug("findTestersByWriterIdAndLangOrderByFirstname("+writerId+"): list.size="+l.size());
			return l;
		}
		else {
			return SampleData.testers(isRussian);
		}
	}
*/	
	public Tester findTesterById(final boolean isRussian, final int writerId, final int id) {
		if(writerId > 0){
			final Tester t = findTesterById(writerId, id);
			if(t == null){
				return SampleData.tester(id, isRussian);
			}
			else {
				log.debug("findTesterById("+id+"): "+t);
				return t;
			}
		}
		else {
			return SampleData.tester(id, isRussian);
		}
	}
	
	private Tester findTesterById(final int writerId, final int id){ 
	    final MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue(Tester.DB_ID, id);
	    params.addValue(Tester.DB_WRITER_ID, writerId);
	    try{
			Tester tester = jdbcTemplate.queryForObject(Tester.SQL_SELECT_BY_WRITER_ID_AND_ID, params, new TesterMapper());
			log.debug("findTesterById("+id+"): "+tester);
	    	return tester;
	    }
	    catch(IncorrectResultSizeDataAccessException ex){
			log.debug("findTesterById("+id+"): tester(ex)=null");
	    	return null;
	    }
	}
	
	public List<Tester> findTestersByTestgroupIdOrderByFirstname(final boolean isRussian, final int writerId, final int groupId) {
		if(writerId > 0){
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Testgroup.DB_TESTGROUP_MEMBERS_TESTGROUP_ID, groupId);
		    final List<Tester> testers = jdbcTemplate.query(Tester.SQL_SELECT_BY_TESTGROUP_ID_ORDER_BY_FIRSTNAME, params, new TesterMapper());
			log.debug("findTestersByTestgroupIdOrderByFirstname("+groupId+"): testers.size="+testers.size());
		    return testers;
		}
		else {
			return SampleData.testers(groupId, isRussian);
		}
	}
	
	public List<Integer> findTesterIdsByTestgroupIdOrderByFirstname(final boolean isRussian, final int writerId, final int groupId) {
		List<Integer> ids = new ArrayList<Integer>();
		for(Tester t: findTestersByTestgroupIdOrderByFirstname(isRussian, writerId, groupId)){
			ids.add(t.getId());
		}
		log.debug("findTesterIdsByTestgroupIdOrderByFirstname("+groupId+"): ids="+ids);
		return ids;
	}
	
	public int saveTester(final Tester tester) {
		log.debug("saveTester ("+tester+")");
		int id = 0;
		if(tester.getWriterId() > 0){
			if(tester.getId() < 1){
				jdbcTemplate.update(Tester.SQL_INSERT, tester.allValuesMap());
			}
			else {
				final Tester t = findTesterById(tester.getWriterId(), tester.getId());
				if(t != null){
					jdbcTemplate.update(Tester.SQL_UPDATE_BY_ID, tester.allValuesMap());
					id = t.getId();
				}
			}			
		}
		return id;
	}
	
	public void deleteTester(final Tester tester) {
		log.debug("deleteTester("+tester+")");
		if(tester.getWriterId() > 0){
			final Tester t = findTesterById(tester.getWriterId(), tester.getId());
			if(t != null && t.getId() > 90){
			    final MapSqlParameterSource map = new MapSqlParameterSource();
		    	map.addValue(Testgroup.DB_TESTGROUP_MEMBERS_TESTER_ID, tester.getId());
				jdbcTemplate.update(Testgroup.SQL_DELETE_ANY_MEMBERSHIP, map);
				jdbcTemplate.update(Tester.SQL_DELETE_BY_ID, tester.allValuesMap());
			}
		}
	}
	public void addUpdateTestMembership(final boolean isRussian, Test test){
		log.debug("addUpdateTestMembership("+test+")");
		if(test.getWriterId() < 1) return;
		if(test.getTestgroupsId() == null){
			test.setTestgroupsId(new HashSet<Integer>());
		}
	    Set<Integer> oldIds = findTestgroupIdsByTestId(isRussian, test.getWriterId(), test.getId());
		if(oldIds.size() > 0){
			Set<Integer> delIds = new HashSet<Integer>(oldIds);
			delIds.removeAll(test.getTestgroupsId());
			deleteTestMemebership(test.getWriterId(), test.getId(), delIds);
		}
		Set<Integer> addIds = new HashSet<Integer>(test.getTestgroupsId());
		if(test.getTestgroupsId().size() > 0){
			addIds.removeAll(oldIds);
		}
	    addTestMembership(test.getId(), addIds);
	}
	private void addTestMembership(final int testId, final Set<Integer> tgIds){
		log.debug("addTestMembership(tgIds="+tgIds+")");
	    if(tgIds.size() > 0){
		    MapSqlParameterSource[] maps = new MapSqlParameterSource[tgIds.size()];
		    int i = 0;
		    for(int id:tgIds){
			    final MapSqlParameterSource map = new MapSqlParameterSource();
		    	map.addValue(Test.DB_TEST_MEMBERS_TEST_ID, testId);
		    	map.addValue(Test.DB_TEST_MEMBERS_TESTGROUP_ID, id);
		    	maps[i++] = map;
		    }
			jdbcTemplate.batchUpdate(Test.SQL_INSERT_MEMBER, maps);
	    }
	}
	private void deleteTestMemebership(final int writerId, final int testId, final Set<Integer> tgIds){
		log.debug("deleteTestMemebership(tgIds="+tgIds+")");
		if(tgIds.size() > 0){
			for(int tgId:tgIds){
				deleteTestgroupFile(writerId, testId, tgId);
			}
		    final MapSqlParameterSource params = new MapSqlParameterSource();
		    params.addValue(Test.DB_TEST_MEMBERS_TEST_ID, testId);
			jdbcTemplate.update(Test.sqlDeleteGroupMembersByIds(tgIds), params);
		}
	}
	private static final class TestMapper implements RowMapper<Test> {
        public Test mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final Test test = new Test(rs.getInt(Test.DB_ID), rs.getInt(Test.DB_WRITER_ID), 
        			rs.getString(Test.DB_NAME), rs.getString(Test.DB_STATUS), rs.getString(Test.DB_TEXT_LENGTH),
        			rs.getString(Test.DB_EMAIL_TEXT)); 
            return test;
        }
	}
	private static final class TestgroupMapper implements RowMapper<Testgroup> {
        public Testgroup mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final Testgroup group = new Testgroup(rs.getInt(Testgroup.DB_ID), rs.getInt(Testgroup.DB_WRITER_ID), 
        			rs.getString(Testgroup.DB_NAME)); 
            return group;
        }
	}
	private static final class ItesterMapper implements RowMapper<Itester> {
        public Itester mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final Itester tester = new Itester(rs.getInt(Itester.DB_ID), rs.getInt(Itester.DB_USER_ID), 
        			rs.getString(Itester.DB_STATUS), rs.getTimestamp(Itester.DB_CREATED_DATE), 
        			rs.getTimestamp(Itester.DB_MODIFIED_DATE)); 
            return tester;
        }
	}
	private static final class TesterMapper implements RowMapper<Tester> {
        public Tester mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final Tester tester = new Tester(rs.getInt(Tester.DB_ID), rs.getInt(Tester.DB_WRITER_ID), rs.getString(Tester.DB_EMAIL), 
        			rs.getString(Tester.DB_FIRSTNAME), rs.getString(Tester.DB_LASTNAME), rs.getString(Tester.DB_STREET), 
        			rs.getString(Tester.DB_CITY), rs.getString(Tester.DB_STATE), rs.getString(Tester.DB_ZIPCODE),  
        			rs.getString(Tester.DB_COUNTRY), rs.getString(Tester.DB_LANG), rs.getInt(Tester.DB_ITESTER_ID)); 
            return tester;
        }
	}
	private static final class UserResultMapper implements RowMapper<UserResult> {
        public UserResult mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final UserResult user = new UserResult(rs.getInt(UserResult.DB_ID), rs.getString(UserResult.DB_USERNAME), 
        			rs.getString(UserResult.DB_FIRSTNAME), rs.getString(UserResult.DB_LASTNAME), rs.getString(UserResult.DB_LANG), 
        			rs.getInt(UserResult.DB_EMAILS_COUNT_TOTAL));
            return user;
        }
	}
	private static final class TestResultMapper implements RowMapper<TestResult> {
        public TestResult mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final TestResult test = new TestResult(rs.getInt(TestResult.DB_ID), rs.getInt(TestResult.DB_USER_ID), 
        			rs.getString(TestResult.DB_NAME), rs.getString(TestResult.DB_TEXT_LENGTH), rs.getString(TestResult.DB_EMAIL_TEXT), 
        			rs.getString(TestResult.DB_QUESTIONS_TOKEN), rs.getTimestamp(TestResult.DB_CREATED_DATE),
        			rs.getTimestamp(TestResult.DB_COMPLETED_DATE), rs.getString(TestResult.DB_NUMBER_TESTERS), 
        			rs.getString(TestResult.DB_NUMBER_ANSWERED));
            return test;
        }
	}
	private static final class TestgroupResultMapper implements RowMapper<TestgroupResult> {
        public TestgroupResult mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final TestgroupResult testgroup = new TestgroupResult(rs.getInt(TestgroupResult.DB_ID), 
        			rs.getInt(TestgroupResult.DB_TEST_ID), rs.getString(TestgroupResult.DB_NAME), 
        			rs.getInt(TestgroupResult.DB_SIZE), rs.getString(TestgroupResult.DB_TEXT));
            return testgroup;
        }
	}
	private static final class TesterResultMapper implements RowMapper<TesterResult> {
        public TesterResult mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final TesterResult tester = new TesterResult(rs.getInt(TesterResult.DB_ID), rs.getInt(TesterResult.DB_TESTGROUP_ID), 
        			rs.getString(TesterResult.DB_TESTER_TOKEN), rs.getString(TesterResult.DB_EMAIL),
        		    rs.getString(TesterResult.DB_FIRSTNAME), rs.getString(TesterResult.DB_LASTNAME),
        		    rs.getString(TesterResult.DB_LANG), rs.getBoolean(TesterResult.DB_INDEPENDENT), 
        		    rs.getString(TesterResult.DB_COMMENTS), rs.getInt(TesterResult.DB_TIME_SPENT_SEC),
        		    rs.getTimestamp(TesterResult.DB_ANSWERED_DATE));
            return tester;
        }
	}
	private static final class QuestionResultMapper implements RowMapper<QuestionResult> {
        public QuestionResult mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final QuestionResult question = new QuestionResult(rs.getInt(QuestionResult.DB_ID), rs.getInt(QuestionResult.DB_TEST_ID), 
        			rs.getInt(QuestionResult.DB_QUESTION_ORDER), rs.getString(QuestionResult.DB_QUESTION_EN), 
        			rs.getString(QuestionResult.DB_QUESTION_RU), rs.getString(QuestionResult.DB_LANGUAGE));
            return question;
        }
	}
	private static final class AnswerOptionMapper implements RowMapper<AnswerOption> {
        public AnswerOption mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final AnswerOption answer = new AnswerOption(rs.getInt(AnswerOption.DB_ID), rs.getInt(AnswerOption.DB_QUESTION_ID), 
        			rs.getInt(AnswerOption.DB_ANSWER_ORDER), rs.getString(AnswerOption.DB_ANSWER_KEY), 
        			rs.getString(AnswerOption.DB_ANSWER_EN), rs.getString(AnswerOption.DB_ANSWER_RU));
            return answer;
        }
	}
	private static final class AnswerResultMapper implements RowMapper<AnswerResult> {
        public AnswerResult mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final AnswerResult answer = new AnswerResult(rs.getInt(AnswerResult.DB_ID), 
        			rs.getInt(AnswerResult.DB_TESTER_ID), rs.getInt(AnswerResult.DB_QUESTION_ID),  
        			rs.getString(AnswerResult.DB_ANSWER_KEY));
            return answer;
        }
	}
	private QuestionResult questionResult(final TestResult testResult, final int qOrder, Locale locale){
		Questions qs = new Questions(testResult.getTestTextLength(), messageSource, locale);
		String questionRu = getMessage(qs.getQuestionKeys()[qOrder-1], Utils.getLocale(true));
		String questionEn = getMessage(qs.getQuestionKeys()[qOrder-1], Utils.getLocale(false));
		String language = User.getLanguage(Utils.isRussian(locale));
		return new QuestionResult(1, testResult.getId(), qOrder, questionEn, questionRu, language);		
	}

	private NamedParameterJdbcTemplate jdbcTemplate;
	
	@Resource(name="dataSource")
	public void setDataSource(final DataSource dataSource) {
	    this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}
	
	public TestService(final NamedParameterJdbcTemplate jdbcTemplate) {
		super();
	    this.jdbcTemplate = jdbcTemplate;
	}

	public TestService(){
		super();
	}
    @Autowired
    private MessageSource messageSource;
    public final String getMessage(final String key, final Locale locale){
    	return getMessage(key, new Object[0], locale);
    }
    public final String getMessage(final String key, final Object[] params, final Locale locale){
    	return messageSource.getMessage(key, params, locale);
    }
}
