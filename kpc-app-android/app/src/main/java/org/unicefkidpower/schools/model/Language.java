package org.unicefkidpower.schools.model;

import java.util.Locale;

/**
 * Created by Ruifeng Shi on 8/20/2015.
 */
public class Language {
	public String				_name;
	public int					_resId;
	public Locale				_locale;

	public Language(String name, int resourceId, Locale locale) {
		this._name = name;
		this._resId = resourceId;
		this._locale = locale;
	}
}
