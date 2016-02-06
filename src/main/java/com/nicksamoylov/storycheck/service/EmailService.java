package com.nicksamoylov.storycheck.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.nicksamoylov.storycheck.domain.News;
import com.nicksamoylov.storycheck.domain.SampleData;
import com.nicksamoylov.storycheck.domain.SampleEmail;
import com.nicksamoylov.storycheck.domain.test.Questions;
import com.nicksamoylov.storycheck.domain.test.EmailText;
import com.nicksamoylov.storycheck.domain.test.Test;
import com.nicksamoylov.storycheck.domain.test.Testgroup;
import com.nicksamoylov.storycheck.domain.users.Tester;
import com.nicksamoylov.storycheck.domain.users.User;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.Utils;

@Service("emailService")
public class EmailService {
	private static final Logging log = Logging.getLog("EmailService");
    
	public void sendEmailRegistrationDeleted(final User user, final Locale locale) {
		log.debug("sendEmailRegistrationDeleted("+user+")");
		EmailText emailText = findEmailText(EMAILTEXT_REG_DELETED, Utils.isRussian(locale));
		String text2 = emailText.getText2().replaceAll(PLACEHOLDER_ADMIN_EMAIL, Utils.getPropertyAdminEmail());
		emailText.setText2(text2);
        sendEmailToUser(user.getUsername(), emailText, locale);
        sendTextToAdminWithLogAttached(user, "Gone "+user.display(), user.toString());
    }

    public void sendEmailUsernameChanged(final String oldEmail, final String newEmail, final String tempPassword, final Locale locale) {
		log.debug("sendEmailUsernameChanged("+oldEmail+"=>"+newEmail+")");
		EmailText emailText = findEmailText(EMAILTEXT_EMAIL_CHANGED, Utils.isRussian(locale));
		String text1 = emailText.getText1().replaceAll(PLACEHOLDER_USER_OLD_EMAIL, oldEmail).replaceAll(PLACEHOLDER_USER_EMAIL, newEmail);
		String text2 = emailText.getText2().replaceAll(PLACEHOLDER_ADMIN_EMAIL, Utils.getPropertyAdminEmail());
		emailText.setText1(text1);
		emailText.setText2(text2);
        sendEmailToUser(oldEmail, emailText, locale);

        emailText = findEmailText(EMAILTEXT_EMAIL_CHANGE_LOGIN, Utils.isRussian(locale));
		text1 = emailText.getText1().replaceAll(PLACEHOLDER_USER_OLD_EMAIL, oldEmail).replaceAll(PLACEHOLDER_USER_EMAIL, newEmail);
        emailText.setEmailChangeLink(tempPassword);
		emailText.setText1(text1);
		emailText.setText2(null);
        sendEmailToUser(newEmail, emailText, locale);
    }

    public void updateEmailsCount(final User user, final int emailsCount){
    	if(user.isEmailsCountSameDate()){
        	user.setEmailsCountToday(user.getEmailsCountToday()+emailsCount);
    	}
    	else {
        	user.setEmailsCountToday(emailsCount);
    	}
    	user.setEmailsCountDate(new Date());
    	user.setEmailsCountTotal(user.getEmailsCountTotal()+emailsCount);
		log.debug("updateEmailsCount(add count="+emailsCount+","+user+")");
		log.debug("updateEmailsCount(): sql="+User.SQL_UPDATE_EMAIL_COUNT_BY_ID);
		log.debug("updateEmailsCount(): map="+user.allValuesMap());
		jdbcTemplate.update(User.SQL_UPDATE_EMAIL_COUNT_BY_ID, user.allValuesMap());
    }
	
	public void sendEmailRegistration(final User user, final String tempPassword, final Locale locale) {
		log.debug("sendEmailRegistration("+user+")");
		EmailText emailText = findEmailText(EMAILTEXT_EMAIL_REG, Utils.isRussian(locale));
		String text1 = emailText.getText1().replaceAll(PLACEHOLDER_USER_FIRST_NAME, user.getFirstname()).replaceAll(PLACEHOLDER_USER_LAST_NAME, user.getLastname());
        emailText.setRegistrationLink(tempPassword);
		emailText.setText1(text1);
		emailText.setText2(null);
        sendEmailToUser(user.getUsername(), emailText, locale);
    }
	public void sendEmailRequest(String emailTo, String subject, String text, String contactToken, Locale locale) {
		log.debug("sendEmailRequest(to"+emailTo+")");
		EmailText emailText = new EmailText();
		emailText.setSubject(subject);
		emailText.setText1(text);
		emailText.setEmailRequestContactLink(contactToken);
        sendEmailToUser(emailTo, emailText, locale);
    }
    private void sendEmailToUser(final String emailTo, final EmailText emailText, final Locale locale) {
		log.debug("sendEmailToUser("+emailTo+")");
        try{
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            message.setSubject(emailText.getSubject());
            message.setFrom(Utils.getPropertyAdminEmailFrom());
            message.setTo(emailTo);
            String htmlContent = generateEmailContent(emailText, null, locale);
            message.setText(htmlContent, true /* isHtml */);
            this.mailSender.send(mimeMessage);
        }
        catch (Exception ex){
        	log.error("sendEmailToUser():", ex);
        }
    }
    public void sendEmailSample(final String email, final Locale locale){
		boolean isRussian = Utils.isRussian(locale);
		SampleEmail se = findSampleEmail(isRussian);
		User user = new User(0, email);
		user.setFirstname(se.getFirstname());
		user.setLastname(se.getLastname());
		Test test = new Test();
		test.setTestTextLength(Test.TEXT_LENGTH_SHORT);
		test.setEmailText(findPrepareEmailText1(user, test.getTestTextLength(), isRussian));
		EmailText emailText = findPrepareEmailTextForTesters(user, test, null, isRussian);
		emailText.setText(se.getText());
		String subject = emailText.getSubject();
		Questions questions = new Questions(Test.TEXT_LENGTH_SHORT, messageSource, locale);
		emailText.setQuestions(questions);
		emailText.setSubject(subject+" "+getMessage(MSG_EMAIL_SAMPLE, locale));
    	sendTextShort(email, emailText, locale);
        emailText.setSubject(subject+" "+MSG_EMAIL_ADMIN_COPY);
        sendTextShort(Utils.getPropertyAdminEmail(), emailText, locale);
    }
    private static Random random = new Random();
    private SampleEmail findSampleEmail(boolean isRussian){
	    MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue(SampleEmail.DB_LANG, Utils.lang(isRussian));
		List<Integer> l = jdbcTemplate.queryForList(SampleEmail.SQL_SELECT_IDS_BY_LANG, params, Integer.class);
		int rn =  random.nextInt(l.size());
	    params.addValue(SampleEmail.DB_ID, l.get(rn));
	    return jdbcTemplate.query(SampleEmail.SQL_SELECT_BY_ID, params, new SampleEmailMapper()).get(0);
    }
    public String generateEmailContentWoText(final User user, final Test test, final Locale locale) {
		log.debug("generateEmailContentWoText("+user+")");
		String result;
        String text = getMessage(test.getTestTextLength()+MSG_TEXT_LENGTH_PLACEHOLDER_WAS, locale);
		EmailText emailText = findPrepareEmailTextForTesters(user, test, text, Utils.isRussian(locale));
		String subject = emailText.getSubject();
		emailText.setSubject(subject+getMessage(MSG_EMAIL_SAMPLE, locale));
		Questions questions;
        switch (test.getTestTextLength()) {
		case Test.TEXT_LENGTH_SHORT:
			questions = new Questions(Test.TEXT_LENGTH_SHORT, messageSource, locale);
			emailText.setQuestions(questions);
			result = generateEmailContent(emailText, null, locale);
			break;
		case Test.TEXT_LENGTH_LONG:
			questions = new Questions(Test.TEXT_LENGTH_LONG, messageSource, locale);
			emailText.setQuestions(questions);
			result = generateEmailContent(emailText, null, locale);
			break;
		case Test.TEXT_LENGTH_IMAGE:
			questions = new Questions(Test.TEXT_LENGTH_IMAGE, messageSource, locale);
			emailText.setQuestions(questions);
			result = generateEmailContent(emailText, null, locale);
			break;
		default:
			throw new RuntimeException("generateEmailContentWoText(): Unexpected test.getTestTextLength()="+test.getTestTextLength());
		}
        return result;
    }
    private String generateEmailContent(final EmailText emailText, String fileName, final Locale locale) {
        final Context ctx = new Context(locale);
        ctx.setVariable("text1", emailText.getText1());
        ctx.setVariable("text", emailText.getText());
        ctx.setVariable("text2", emailText.getText2());
        ctx.setVariable("registrationLink", emailText.getRegistrationLink());
        ctx.setVariable("emailChangeLink", emailText.getEmailChangeLink());
        ctx.setVariable("emailRequestContactLink", emailText.getEmailRequestContactLink());
        ctx.setVariable("questions", emailText.getQuestions());
        ctx.setVariable("text3", emailText.getText3());
        ctx.setVariable("text4", emailText.getText4());        
        ctx.setVariable("testers", emailText.getTesters());        
        ctx.setVariable("testgroupname", emailText.getTestgroupName());
        if(fileName != null){
        	ctx.setVariable("imageName", fileName); // so that we can reference it from HTML        
        }
        return this.templateEngine.process("email-text.html", ctx);
    }

    public void sendEmailSample(final User user, final Test test, final Locale locale) {
		log.debug("sendEmailSample("+user+")");
		if(user.getId() > 0){
			String sendTo = user.getUsername();
	        String text = getMessage(test.getTestTextLength()+MSG_TEXT_LENGTH_PLACEHOLDER, locale);
			EmailText emailText = findPrepareEmailTextForTesters(user, test, text, Utils.isRussian(locale));
			String subject = emailText.getSubject();
			emailText.setSubject(subject+getMessage(MSG_EMAIL_SAMPLE, locale));
			Questions questions;
	        switch (test.getTestTextLength()) {
			case Test.TEXT_LENGTH_SHORT:
				questions = new Questions(Test.TEXT_LENGTH_SHORT, messageSource, locale);
				emailText.setQuestions(questions);
				sendTextShort(sendTo, emailText, locale);
		        emailText.setSubject(subject+MSG_EMAIL_ADMIN_COPY);
		        sendTextShort(Utils.getPropertyAdminEmail(), emailText, locale);
				break;
			case Test.TEXT_LENGTH_LONG:
				questions = new Questions(Test.TEXT_LENGTH_LONG, messageSource, locale);
				emailText.setQuestions(questions);
				sendTextLong(sendTo, emailText, null, null, locale);
		        emailText.setSubject(subject+MSG_EMAIL_ADMIN_COPY);
		        sendTextLong(Utils.getPropertyAdminEmail(), emailText, null, null, locale);
				break;
			case Test.TEXT_LENGTH_IMAGE:
				questions = new Questions(Test.TEXT_LENGTH_IMAGE, messageSource, locale);
				emailText.setQuestions(questions);
				sendImage(sendTo, emailText, null, null, locale);
		        emailText.setSubject(subject+MSG_EMAIL_ADMIN_COPY);
		        sendImage(Utils.getPropertyAdminEmail(), emailText, null, null, locale);
				break;
			default:
				throw new RuntimeException("sendEmailSample(): Unexpected test.getTestTextLength()="+test.getTestTextLength());
			}
		}
    }

    public void sendEmailToTesters(final User user, final Set<Tester> testers, final List<String> testersDisplay, 
    		final String questionsToken, final Map<String, String> testerTokens, final Test test, final Testgroup tg, final Locale locale) {
		log.debug("sendEmailTesters(testId="+test.getId()+", tgId="+tg.getId()+")");
		if(user.getId() > 0){
			Questions questions = new Questions(test.getTestTextLength(), messageSource, locale); 
			questions.setQuestionsToken(questionsToken);
			Questions questionsForNonTester = new Questions(test.getTestTextLength(), messageSource, locale); 
			questionsForNonTester.setQuestionsToken(questionsToken);
			EmailText emailText = findPrepareEmailTextForTesters(user, test, null, Utils.isRussian(locale));
			String subject = emailText.getSubject();
	        switch (test.getTestTextLength()) {
			case Test.TEXT_LENGTH_SHORT:
		        emailText.setText(tg.getText());
				for(Tester t:testers){
					questions.setTesterToken(testerTokens.get(t.getEmail()));
					emailText.setQuestions(questions);
			        sendTextShort(t.getEmail(), emailText, locale);
				}
				emailText.setTesters(testersDisplay);
				emailText.setTestgroupName(tg.getName());
				emailText.setQuestions(questionsForNonTester);
				emailText.setSubject(subject+" "+getMessage(MSG_EMAIL_AUTHOR_COPY, locale));
		        sendTextShort(user.getUsername(), emailText, locale);
		        emailText.setSubject(subject+" "+MSG_EMAIL_ADMIN_COPY);
		        sendTextShort(Utils.getPropertyAdminEmail(), emailText, locale);
		        break;
			case Test.TEXT_LENGTH_LONG:
				String fileNameText = tg.getText();
				File fileText = Test.getFile(test.getWriterId(), test.getId(), tg.getId());
				for(Tester t:testers){
					questions.setTesterToken(testerTokens.get(t.getEmail()));
					emailText.setQuestions(questions);
					sendTextLong(t.getEmail(), emailText, fileNameText, fileText, locale);
				}
				emailText.setTesters(testersDisplay);
				emailText.setTestgroupName(tg.getName());
				emailText.setSubject(subject+" "+getMessage(MSG_EMAIL_AUTHOR_COPY, locale));
				emailText.setQuestions(questionsForNonTester);
				sendTextLong(user.getUsername(), emailText, fileNameText, fileText, locale);
		        emailText.setSubject(subject+" "+MSG_EMAIL_ADMIN_COPY);
				sendTextLong(Utils.getPropertyAdminEmail(), emailText, fileNameText, fileText, locale);
				break;
			case Test.TEXT_LENGTH_IMAGE:
				String fileNameImage = tg.getText();
				File fileImage = Test.getFile(test.getWriterId(), test.getId(), tg.getId());
				for(Tester t:testers){
					questions.setTesterToken(testerTokens.get(t.getEmail()));
					emailText.setQuestions(questions);
					sendImage(t.getEmail(), emailText, fileNameImage, fileImage, locale);
				}
				emailText.setTesters(testersDisplay);
				emailText.setTestgroupName(tg.getName());
				emailText.setSubject(subject+" "+getMessage(MSG_EMAIL_AUTHOR_COPY, locale));
				emailText.setQuestions(questionsForNonTester);
				sendImage(user.getUsername(), emailText, fileNameImage, fileImage, locale);
		        emailText.setSubject(subject+" "+MSG_EMAIL_ADMIN_COPY);
				sendImage(Utils.getPropertyAdminEmail(), emailText, fileNameImage, fileImage, locale);
				break;
			default:
				throw new RuntimeException("sendEmailTesters(): Unexpected test.getTestTextLength()="+test.getTestTextLength());
			}
		}
    }

    public void sendTextShort(final String recipientEmail, final EmailText emailText, final Locale locale) {
		log.debug("sendTextShort("+recipientEmail+")");
        try{
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            message.setSubject(emailText.getSubject());
            message.setFrom(Utils.getPropertyAdminEmailFrom());
            message.setTo(recipientEmail);
            String htmlContent = generateEmailContent(emailText, null, locale);
            message.setText(htmlContent, true /* isHtml */);
            this.mailSender.send(mimeMessage);
        }
        catch (Exception ex){
        	log.error("sendTextShort():", ex);
        }
    }


    private void sendTextLong(final String recipientEmail, final EmailText emailText,  
    		                   final String fileName, final File file, final Locale locale) {
		log.debug("sendTextLong("+recipientEmail+")");
        try{
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
            message.setSubject(emailText.getSubject());
            message.setFrom(Utils.getPropertyAdminEmailFrom());
            message.setTo(recipientEmail);
            String htmlContent = generateEmailContent(emailText, null, locale);
            message.setText(htmlContent, true /* isHtml */);
            if(fileName != null){
                message.addAttachment(fileName, file);
            }
            this.mailSender.send(mimeMessage);        
        }
        catch (Exception ex){
        	log.error("sendTextLong():", ex);
        }
    }

    private void sendImage(final String recipientEmail, final EmailText emailText,   
    		                final String fileName, final File file, final Locale locale) {
		log.debug("sendImage("+recipientEmail+")");
        try{
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
            message.setSubject(emailText.getSubject());
            message.setFrom(Utils.getPropertyAdminEmailFrom());
            message.setTo(recipientEmail);
            String htmlContent = generateEmailContent(emailText, fileName, locale);
            message.setText(htmlContent, true /* isHtml */);
            if(fileName != null){
                message.addInline(fileName, file);
            }
            this.mailSender.send(mimeMessage);
        }
        catch (Exception ex){
        	log.error("sendImage():", ex);
        }
    }    
    public void sendLogToAdmin(final User user, final Throwable ex) {
    	log.error(user.toString(), ex==null?new RuntimeException("No Exception object"):ex);
    	sendProblemToAdminWithLogAttached(user, Logging.stackTraceToString(ex));
    }    
    public void sendProblemToAdminWithLogAttached(final User user, final String text) {
        sendTextToAdminWithLogAttached(user, "Problem report "+user.display(), text);
    }    
    public void sendBlogUpdateToAdminWithLogAttached(final User user, final String subject, final String text) {
		if("nsamoylov@hotmail.com".equals(user.getUsername()) || "nsamoylov@gmail.com".equals(user.getUsername())){
			log.debug("sendBlogUpdateToAdminWithLogAttached("+subject+")");
			return;
		}
		sendTextToAdminWithLogAttached(user, subject, text);
    }    
    public void sendTextToAdminWithLogAttached(final User user, final String subject, final String text) {
		log.debug("sendTextToAdminWithLogAttached("+subject+")");
		if(Utils.isAdminEmailSendOff()){
			log.error(text);
			return;
		}
        final Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("user", user.display());
        ctx.setVariable("text", text);
/*      
        String imageLogoName = messageSource.getMessage("logo", new Object[0], locale);
        ctx.setVariable("logo", imageLogoName); // so that we can reference it from HTML
        String imageNickName = "Nick50.jpg";
        ctx.setVariable("nick", imageNickName); // so that we can reference it from HTML
*/        
        try{
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
            message.setSubject(Utils.getEnvironmentPrefix()+subject);
            message.setFrom(Utils.getPropertyAdminEmailFrom());
            message.setTo(Utils.getPropertyAdminEmail());
/*            
            addInlineImage(message, imageLogoName);
            addInlineImage(message, imageNickName);
*/
            final String htmlContent = this.templateEngine.process("email-log.html", ctx);
            message.setText(htmlContent, true /* isHtml */);

        	RollingFileAppender rfa = (RollingFileAppender)Logger.getRootLogger().getAppender("fileAppender");
        	File file = new File(rfa.getFile());
            message.addAttachment("storycheck.log", file);
            this.mailSender.send(mimeMessage);
        }
        catch (Exception ex){
        	log.error("sendLog():", ex);
        }
    }

    private void addInlineImage(MimeMessageHelper message, final String imageName){
        File image = Test.getApplicationImageFile(imageName);
        Path path = Paths.get(image.getPath());
        try{
            byte[] imagebytes = Files.readAllBytes(path);
            final InputStreamSource imageSource = new ByteArrayResource(imagebytes);
            message.addInline(image.getName(), imageSource, "image/gif");
        }
        catch (Exception ex){
        	log.error("addInlineImage():", ex);
        }
    }

    public EmailText findPrepareEmailTextForTesters(final User user, final Test test, final String text, final boolean isRussian){
		log.debug("findEmailText(testId="+test.getId()+", isRussian="+isRussian+")");
		EmailText et = findEmailText(test.getTestTextLength(), isRussian);
        String subject = et.getSubject().replaceAll(PLACEHOLDER_USER_FIRST_NAME, user.getFirstname()).replaceAll(PLACEHOLDER_USER_LAST_NAME, user.getLastname());
        et.setSubject(subject);
		et.setText1(test.getEmailText());
		et.setText(text);
		String text2 = et.getText2()==null?null:et.getText2().replaceAll(PLACEHOLDER_USER_EMAIL, user.getUsername());
		et.setText2(text2);
		return et;
	}
    
    public String findPrepareEmailText1(final User user, final String key, final boolean isRussian){
		log.debug("findEmailText("+key+", isRussian="+isRussian+")");
		EmailText et = findEmailText(key, isRussian);
		return et.getText1().replaceAll(PLACEHOLDER_USER_FIRST_NAME, user.getFirstname()).replaceAll(PLACEHOLDER_USER_LAST_NAME, user.getLastname());
	}
    
    private EmailText findEmailText(String key, boolean isRussian){
		String lang =  Utils.lang(isRussian);
	    MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue(EmailText.DB_KEY, key);
	    params.addValue(EmailText.DB_LANG, lang);
		return jdbcTemplate.queryForObject(EmailText.SQL_SELECT_BY_KEY_AND_LANG, params, new EmailTextMapper());
    }

	private static final class EmailTextMapper implements RowMapper<EmailText> {
        public EmailText mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        	final EmailText emailText = new EmailText(rs.getString(EmailText.DB_KEY), rs.getString(EmailText.DB_LANG), 
        			rs.getString(EmailText.DB_SUBJECT), rs.getString(EmailText.DB_TEXT1), rs.getString(EmailText.DB_TEXT2), 
        			rs.getString(EmailText.DB_TEXT3), rs.getString(EmailText.DB_TEXT4)); 
            return emailText;
        }
	}
	private static final class SampleEmailMapper implements RowMapper<SampleEmail> {
	    public SampleEmail mapRow(final ResultSet rs, final int rowNum) throws SQLException {
	    	final SampleEmail email = new SampleEmail(rs.getInt(SampleEmail.DB_ID), rs.getString(SampleEmail.DB_FIRSTNAME), 
	    			rs.getString(SampleEmail.DB_LASTNAME), rs.getString(SampleEmail.DB_TEXT), rs.getString(SampleEmail.DB_LANG)); 
	        return email;
	    }
	}
	
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Resource(name="dataSource")
	public void setDataSource(DataSource dataSource) {
	    this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

    @Autowired 
    private JavaMailSender mailSender;
    
    @Autowired 
    private TemplateEngine templateEngine;

    @Autowired
    private MessageSource messageSource;
    public final String getMessage(final String key, final Locale locale){
    	return getMessage(key, new Object[0], locale);
    }
    public final String getMessage(final String key, final Object[] params, final Locale locale){
    	return messageSource.getMessage(key, params, locale);
    }
    
	private static final String EMAILTEXT_EMAIL_REG          = "email.reg";
	private static final String EMAILTEXT_EMAIL_PASSWORD     = "email.password";
	private static final String EMAILTEXT_EMAIL_CHANGED      = "email.changed";
	private static final String EMAILTEXT_EMAIL_CHANGE_LOGIN = "email.change.login";
	private static final String EMAILTEXT_REG_DELETED        = "reg.deleted";
	
	private static final String PLACEHOLDER_USER_FIRST_NAME = "<user.first.name>";
	private static final String PLACEHOLDER_USER_LAST_NAME  = "<user.last.name>";
	private static final String PLACEHOLDER_USER_EMAIL      = "<user.email>";
	private static final String PLACEHOLDER_PASSWORD        = "<password>";
	private static final String PLACEHOLDER_USER_OLD_EMAIL  = "<user.email.old>";
	private static final String PLACEHOLDER_ADMIN_EMAIL     = "<admin.email>";
	
    private static final String MSG_EMAIL_SAMPLE            = "email.sample";
	private static final String MSG_EMAIL_ADMIN_COPY        = "(admin copy)";
	private static final String MSG_EMAIL_AUTHOR_COPY       = "email.author.copy";
	private static final String MSG_TEXT_LENGTH_PLACEHOLDER = ".placeholder";
	private static final String MSG_TEXT_LENGTH_PLACEHOLDER_WAS = ".was.placeholder";

}
