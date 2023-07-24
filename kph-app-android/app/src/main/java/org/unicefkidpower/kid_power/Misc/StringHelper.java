package org.unicefkidpower.kid_power.Misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ruifeng Shi on 11/17/2015.
 */
public class StringHelper {
	public static boolean isContainingSpecialCharacters(String string) {
		Pattern p = Pattern.compile("[^a-z0-9 -]", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(string);
		return m.find();
	}
}
