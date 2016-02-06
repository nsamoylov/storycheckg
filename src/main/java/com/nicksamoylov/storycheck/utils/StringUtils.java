package com.nicksamoylov.storycheck.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class StringUtils {
	public final static boolean isEmpty(final String s) {
		return s == null || s.trim().length() == 0
				|| s.equalsIgnoreCase("null");
	}

	public final static String trim(final String s) {
		if (s == null)
			return s;
		else
			return s.trim();
	}

	public final static boolean areEqual(final String s1, final String s2) {
		return areEqual(s1, s2, false);
	}

	public final static boolean areEqual(final String s1, final String s2,
			final boolean ignoreCase) {
		if (s1 != null && s2 != null) {
			if (ignoreCase) {
				return s1.toUpperCase().equals(s2.toUpperCase());
			} else {
				return s1.equals(s2);
			}
		}
		return s1 == s2;
	}

	public static boolean isAlphaNumeric(final String value) {
		final Pattern p = Pattern.compile("[\\w\\d]*", Pattern.CASE_INSENSITIVE);
		return value.matches(p.pattern());
	}

	public static boolean isIncluded(final String[] arr, final String element) {
		boolean result = false;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(element)) {
				result = true;
				break;
			}
		}
		return result;
	}

	public static boolean isIncluded(final Collection<String> coll, final String element) {
		final Set<String> set = new HashSet<String>(coll);
		return set.contains(element);
	}

	public static boolean isParseableGreaterThanZeroInt(final String value) {
		try {
			final int intVal = Integer.parseInt(value);
			if (intVal > 0)
				return true;
			else
				return false;
		}
		catch (final Exception rte) {
			return false;
		}
	}

	public static String substring(final String str, final int maxLength) {
		if (str != null && str.length() > 0) {
			if (maxLength >= str.length()) {
				return str;
			}
			else {
				return str.substring(0, maxLength);
			}
		}
		else
			return null;
	}

	public static String substringWithDots(final String str, final int maxLength) {
		if (str != null && str.length() > 0) {
			if (maxLength >= str.length()) {
				return str;
			}
			else {
				return str.substring(0, maxLength)+"...";
			}
		}
		else
			return null;
	}

	public static String escapeXML(final String input) {
		if (input == null)
			return null;

		final StringBuffer result = new StringBuffer(input);
		int index = result.indexOf("&");
		while (index != -1 && index < result.length()) {
			int end = index + 5;
			if (end > result.length())
				end = result.length();
			final String checkOccurrence = result.substring(index, end);
			if (!checkOccurrence.equalsIgnoreCase("&amp;")) {
				result.insert(index + 1, "amp;");
			}
			index++;
			if (index < result.length()) {
				index = result.indexOf("&", 1 + index);
			}
		}
		return result.toString();
	}

}
