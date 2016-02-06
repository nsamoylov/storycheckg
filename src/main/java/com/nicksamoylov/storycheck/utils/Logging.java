package com.nicksamoylov.storycheck.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Logging {
	private static final String STACK_TRACE_PREFIX = "Original stack trace: ";
	public static final String USER_UNKNOWN = "unknown";
	public static String USER_CURRENT = "unknown";
    private Log log;

    protected Logging(Log log){
        this.log = log;
    }

    public static Logging getLog(String name){
        return new Logging(LogFactory.getLog(name));
    }

    public static Logging getLog(Class clazz){
        return new Logging(LogFactory.getLog(clazz));
    }

    public boolean isDebugEnabled(){
        return log.isDebugEnabled();
    }

    public void debug(String msg){
        log.debug(getUserPrefix()+msg);
    }

    public void debug(String msg, Throwable ex){
        log.debug(getUserPrefix()+msg+"\n"+stackTraceToString(ex));
    }

    public void warn(Exception ex){
        log.warn(getUserPrefix()+"\n"+stackTraceToString(ex));
    }

    public void warn(String msg){
        log.warn(getUserPrefix()+msg);
    }

    public void warn(Throwable ex){
        log.warn(getUserPrefix()+"\n"+stackTraceToString(ex));
    }

    public void warn(String msg, Exception ex){
        log.warn(getUserPrefix()+msg+"\n"+stackTraceToString(ex));
    }

    public void warn(String msg, Throwable ex){
        log.warn(getUserPrefix()+msg+"\n"+stackTraceToString(ex));
    }

    public void info(String msg){
        log.info(getUserPrefix()+msg);
    }

    public void error(String msg){
        log.error(getUserPrefix()+msg);
    }

    public void error(Exception ex){
        log.error(getUserPrefix()+"\n"+stackTraceToString(ex));
    }

    public void error(String msg, Exception ex){
        log.error(getUserPrefix()+msg+"\n"+stackTraceToString(ex));
    }

    public void error(Throwable ex){
        log.error(getUserPrefix()+"\n"+stackTraceToString(ex));
    }

    public void error(String msg, Throwable ex){
        log.error(getUserPrefix()+msg+"\n"+stackTraceToString(ex));
    }

    public void fatal(String msg, Exception ex){
        log.fatal(getUserPrefix()+msg+"\n"+stackTraceToString(ex));
    }

    public void fatal(String msg){
        log.fatal(getUserPrefix()+msg);
    }

    public String appendErrors(List<String> errors){
    	StringBuffer sb = new StringBuffer();
    	for(String error: errors){
    		sb.append(error);
    	}
    	return sb.toString();
    }

    public void logErrors(List<String> errors){
    	StringBuffer sb = new StringBuffer();
    	sb.append("logErrors:");
    	if(errors.size() == 0){
    		sb.append("-");
        	log.debug(sb.toString());
    	}
    	else {
        	for(String error: errors){
        		sb.append("\n").append(error);
        	}
        	log.warn(sb.toString());
    	}
    }
    public static String stackTraceToString(Throwable ex){
        String returnString = null;
        if (ex != null){
            String message = ex.getMessage();
            if (message != null && message.indexOf(STACK_TRACE_PREFIX) > -1){
                returnString = message;
            }
            else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                StringBuffer sb = sw.getBuffer();
                returnString = STACK_TRACE_PREFIX + sb.toString();
            }
        }
        return returnString;
    }
    public static String stackTraceToStringWoLineEnds(Throwable ex){
        String returnString = "See log attached to the problem report.";
        if (ex != null){
            String message = ex.getMessage();
            if (message != null && message.indexOf(STACK_TRACE_PREFIX) > -1){
                returnString = message;
            }
            else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                StringBuffer sb = sw.getBuffer();
                String[] split = sb.toString().split(System.getProperty("line.separator"), 0);
                for(String str:split){
                	returnString = returnString + " " + str.trim();
                }
                returnString = STACK_TRACE_PREFIX + returnString;
            }
        }
        return returnString;
    }
    public static final String getOriginalMessage1(Throwable ex){
        String returnString = null;
        if (ex != null){
            String message = ex.getMessage();
            if (message != null){
                if (message.indexOf(STACK_TRACE_PREFIX) > -1){
                    final String AT = "at ";
                    final String EXCEPTION = "Exception: ";
                    final int indexAT = message.indexOf(AT);
                    final int indexEXCEPTION = message.indexOf(EXCEPTION);
                    if (indexAT > -1 && indexEXCEPTION > -1 && indexAT > indexEXCEPTION){
                        returnString = message.substring(indexEXCEPTION + EXCEPTION.length(), indexAT);
                    }
                    else{
                        returnString = message.substring(message.indexOf(STACK_TRACE_PREFIX) + STACK_TRACE_PREFIX.length());
                    }
                }
            }
        }
        return returnString;
    }

    public String getUserPrefix(){
        return "<"+USER_CURRENT+"> ";
    }


}
