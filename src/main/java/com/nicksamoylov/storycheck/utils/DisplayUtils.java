package com.nicksamoylov.storycheck.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public final class DisplayUtils {

	public final static String displayMap(final Map<Integer, String[][]> map) {
		if (map == null || map.size() == 0) {
			return "-";
		}
		final StringBuilder buffer = new StringBuilder("{");
		boolean first = true;
		for(int key:map.keySet()){
			if (!first)
				buffer.append(",");
			first = false;
			String a = commaSeparated(map.get(key));
			buffer.append(key).append("[").append(a).append("]");
		}
		return buffer.append("}").toString();
	}

	public final static String upperCaseFirstLetter(String v) {
		char[] stringArray = v.toCharArray();
		stringArray[0] = Character.toUpperCase(stringArray[0]);
		return new String(stringArray);		
	}

	public final static String display(final int v) {
		return v == 0 ? "-" : String.valueOf(v);
	}

	public final static String display(final String v) {
		return (v == null || v.trim().length() == 0) ? "-" : v.trim();
	}

	public static String commaSeparated(final Collection<String> coll) {
		if (coll == null || coll.size() == 0) {
			return "-";
		}
		final StringBuilder buffer = new StringBuilder();
		boolean first = true;
		for (final String s : coll) {
			if (!first)
				buffer.append(",");
			first = false;
			buffer.append(s);
		}
		return buffer.toString();
	}

	public static String commaSeparatedSql(final Collection<String> coll) {
		if (coll == null || coll.size() == 0) {
			return "";
		}
		final StringBuilder buffer = new StringBuilder("'");
		boolean first = true;
		for (final String s : coll) {
			if (!first)
				buffer.append(",'");
			first = false;
			buffer.append(s).append("'");
		}
		return buffer.toString();
	}

	public static String commaSeparated(final Object[] sa) {
		if (sa == null || sa.length == 0) {
			return "-";
		}
		final List<String> l = new ArrayList<String>();
		for (int i = 0, n = sa.length; i < n; ++i) {
			l.add(sa[i].toString());
		}
		return commaSeparated(l);
	}

	public static String commaSeparatedSorted(final Collection coll) {
		return tokenSeparatedSorted(coll, ", ");
    }

	public static String tokenSeparatedSorted(final Collection coll, String token) {
		if (coll == null || coll.size() == 0) {
			return "-";
		}

		final List<String> list = new ArrayList<String>();
		for(Object o: coll){
			list.add(o.toString());
		}
		Collections.sort(list);

		final StringBuffer sb = new StringBuffer();
		boolean first = true;
		final int s = list.size();
		for (int i = 0; i < s; i++) {
			if (first) {
				first = false;
			} else {
				sb.append(token);
			}
			sb.append(list.get(i).toString());
		}
		return sb.toString();
	}

	public static String commaSeparatedSorted(final Object[] sa) {
		if (sa == null || sa.length == 0) {
			return "-";
		}
		final List<String> l = new ArrayList<String>();
		for (int i = 0, n = sa.length; i < n; ++i) {
			l.add(sa[i].toString());
		}
		return commaSeparatedSorted(l);
	}

	public static Set<String> parseCommaSeparatedInt(final String csint) {
		return parseCommaSeparatedInt(csint, new ArrayList<String>());
	}

	public static int dotCommaSeparatedInt(final String dsInt) {
		return Integer.parseInt(dsInt.trim().replace(".", ""));
	}

	public static Set<String> parseCommaSeparatedInt(final String csint, final Collection<String> badTokens) {
		final Set<String> result = new HashSet<String>();
		if (csint == null || "".equals(csint.trim()))
			return result;

		final StringTokenizer tokenizer = new StringTokenizer(csint, ",");
		String str = null;
		while (tokenizer.hasMoreTokens()) {
			try {
				str = tokenizer.nextToken().trim();
				if (new Integer(str).intValue() < 0) {
					throw new NumberFormatException();
				}
				result.add(str);
			}
			catch (final NumberFormatException ex) {
				badTokens.add(str);
			}
		}
		return result;
	}

	public static Set<String> parseCommaSeparatedStrings(final String strings) {
		return parseCommaSeparatedStringsAndUpperCase(strings, false);
	}

	public static Set<String> parseCommaSeparatedStringsAndUpperCase(final String strings) {
		return parseCommaSeparatedStringsAndUpperCase(strings, true);
	}

	private static Set<String> parseCommaSeparatedStringsAndUpperCase(
			final String strings, final boolean upperCaseThem) {
		final Set<String> result = new HashSet<String>();
		if (strings == null || "".equals(strings.trim()))
			return result;

		final StringTokenizer tokenizer = new StringTokenizer(strings, ",");
		String str = null;
		while (tokenizer.hasMoreTokens()) {
			str = tokenizer.nextToken().trim();
			result.add(upperCaseThem ? str.toUpperCase() : str);
		}
		return result;
	}

}
