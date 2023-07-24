package org.unicefkidpower.schools.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dayong Li on 3/22/2017.
 */

public class HelperCategoryItem {
	public long id;
	public String description;
	public String locale;
	public String htmlUrl;
	public String name;
	public int position;
	public List<HelperChildItem> items = new ArrayList<HelperChildItem>();
}
