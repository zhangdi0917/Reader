/**
 * XMLTables.java
 */
package com.beauty.android.reader.utils;

import java.util.HashMap;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;

import android.text.TextUtils;

public class XMLTables {

	private static final String TAG_CATEGORY = "category";
	private static final String CATEGORY_NAME = "name";
	private static final String TAG_PROPERTY = "property";
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_VALUE = "value";
	private static final String PROPERTY_IGNORE = "ignore";
	private static final String PROPERTY_DEFAULT = "default";

	private class Property {
		String name;
		String value;
		boolean ignore;
		String node;

		Property(String k, String v, boolean ignore, String n) {
			name = k;
			value = v;
			this.ignore = ignore;
			node = n;
		}

		@Override
		public String toString() {
			return "Property [name=" + name + ", value=" + value + ", ignore=" + ignore + ", node=" + node + "]";
		}
	}

	private boolean mLoaded;
	private HashMap<String, HashMap<String, Property>> mTable;
	private HashMap<String, String> mCategoryDefault;

	public XMLTables() {
		mTable = new HashMap<String, HashMap<String, Property>>();
		mCategoryDefault = new HashMap<String, String>();
	}

	public Set<String> getCategorys() {
		return mTable.keySet();
	}

	public String getProperty(String category, String property, int code) {
		if (TextUtils.isEmpty(category) || TextUtils.isEmpty(property)) {
			return null;
		}

		String key = property + String.valueOf(code);
		if (mTable.containsKey(category) && mTable.get(category).get(key) != null) {
			Property p = mTable.get(category).get(key);

			if (!p.ignore) {
				return p.node;
			} else {
				return null;
			}
		}

		return mCategoryDefault.get(category);
	}

	public String getDefaultForCategory(String category) {
		if (TextUtils.isEmpty(category)) {
			return "";
		}

		return mCategoryDefault.get(category);
	}

	public void clear() {
		mLoaded = false;
		mTable.clear();
		mCategoryDefault.clear();
	}

	public boolean loadXML(XmlPullParser parser) {
		if (mLoaded) {
			return mLoaded;
		}

		if (parser == null) {
			return mLoaded;
		}
		try {
			HashMap<String, Property> propertyMaps = null;
			String category_name = null;
			String property_name = null;
			String property_value = null;
			String property_node = null;
			boolean ignore = false;
			String defaultValue = null;
			boolean inCategory = false;
			boolean inProperty = false;
			int event;
			while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
				if (event == XmlPullParser.START_TAG) {
					String tag = parser.getName();

					if (TAG_CATEGORY.equals(tag)) {
						inCategory = true;
						category_name = parser.getAttributeValue(null, CATEGORY_NAME);
						defaultValue = parser.getAttributeValue(null, PROPERTY_DEFAULT);
					} else if (TAG_PROPERTY.equals(tag)) {
						inProperty = true;
						property_name = parser.getAttributeValue(null, PROPERTY_NAME);
						property_value = parser.getAttributeValue(null, PROPERTY_VALUE);
						String strIgnore = parser.getAttributeValue(null, PROPERTY_IGNORE);
						if (TextUtils.isEmpty(strIgnore)) {
							ignore = false;
						} else {
							if (strIgnore.equals("false")) {
								ignore = false;
							} else if (strIgnore.equals("true")) {
								ignore = true;
							}
						}
					}
				} else if (event == XmlPullParser.TEXT) {
					property_node = parser.getText();
				} else if (event == XmlPullParser.END_TAG) {
					if (inProperty) {
						inProperty = false;

						if (!TextUtils.isEmpty(property_name)) {
							if (propertyMaps == null) {
								propertyMaps = new HashMap<String, Property>();
							}

							if (!TextUtils.isEmpty(property_value)) {
								if (TextUtils.isEmpty(property_node)) {
									property_node = defaultValue;
								}

								propertyMaps.put(property_name + property_value, new Property(property_name,
										property_value, ignore, property_node));
							}
						}
						property_node = null;
						property_name = null;
						property_value = null;
						ignore = false;
					} else if (inCategory) {
						inCategory = false;

						if (propertyMaps != null && !TextUtils.isEmpty(category_name)) {
							mTable.put(category_name, propertyMaps);
						}
						if (!TextUtils.isEmpty(category_name)) {
							mCategoryDefault.put(category_name, defaultValue);
						}

						propertyMaps = null;
						category_name = null;
						property_name = null;
						property_value = null;
						defaultValue = null;
						ignore = false;
					}
				}
			}
			mLoaded = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mLoaded;
	}

	@Override
	public String toString() {
		return "XMLTables [mLoaded=" + mLoaded + ", mTable=" + mTable + "]";
	}

}
