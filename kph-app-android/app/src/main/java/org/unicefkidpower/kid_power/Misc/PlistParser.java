package org.unicefkidpower.kid_power.Misc;

import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dayong Li on 10/31/2015.
 * UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class PlistParser {
	// constructor for  to getV1 the context object from where you are using this plist parsing
	public PlistParser() {
	}

	static public List<HashMap<String, Object>> parse(XmlResourceParser xml) {
		XmlResourceParser parser = xml;

		// flag points to find key and value tags .
		boolean keyTag = false;
		boolean valueTag = false;
		boolean arrayTag = false;

		String keyStaring = null;
		String stringValue = null;
		ArrayList<Object> valueArrary = null;

		HashMap<String, Object> hashmap = new HashMap<>();
		List<HashMap<String, Object>> listResult = new ArrayList<>();
		int event;
		try {
			event = parser.getEventType();

			// repeating the loop at the end of the doccument

			while (event != XmlPullParser.END_DOCUMENT) {

				String name = parser.getName();
				switch (event) {
					//use switch case than the if ,else statements
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						if (name.equals("key")) {
							keyTag = true;
							valueTag = false;
						}
						if (name.equals("string")) {
							valueTag = true;
						}
						if (name.equals("integer")) {
							valueTag = true;
						}
						if (name.equals("false")) {
							valueTag = true;
						}
						if (name.equals("array")) {
							if (keyTag) {
								arrayTag = true;
								valueArrary = new ArrayList<>();
							} else {
								arrayTag = false;
							}
						}
						break;
					case XmlPullParser.END_TAG:
						if (name.equals("dict")) {
							listResult.add(hashmap);
							hashmap = new HashMap<String, Object>();
						}
						if (keyTag && name.equals("array")) {
							hashmap.put(keyStaring, valueArrary);
							arrayTag = false;
							keyTag = false;
							valueTag = false;
						}
						break;
					case XmlPullParser.TEXT:
						if (keyTag) {
							if (valueTag == false) {
								keyStaring = parser.getText();
							}
						}
						if (valueTag && keyTag && !arrayTag) {
							stringValue = parser.getText();

							hashmap.put(keyStaring, stringValue);
							valueTag = false;
							keyTag = false;

						}
						if (valueTag && arrayTag) {
							stringValue = parser.getText();
							valueArrary.add(Integer.valueOf(stringValue));
						}
						break;
					default:
						break;
				}
				event = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//here you getV1 the plistValues.
		return listResult;
	}
}