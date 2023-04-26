package me.jjson;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.lang.reflect.*;

public class JsonUtils {
    public static <T> String toJsonString(T object) {
	HashMap<String, Field> fields = JsonParser.getFields(object.getClass());
	StringBuilder sb = new StringBuilder();

	try {
	    buildObject(sb, fields, object);
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	}

	return sb.toString();
    }

    private static <T> void buildObject(StringBuilder sb, HashMap<String, Field> fields, T object) throws IllegalAccessException {
	sb.append("{");
	int i = 0;
	for (Map.Entry<String, Field> entry : fields.entrySet()) {
	    sb.append('"' + entry.getKey() + '"');
	    sb.append(":");

	    Field currField = entry.getValue();
	    Object currFieldObject = currField.get(object);
	    if (currFieldObject instanceof String) {
		buildString(sb, currField.get(object).toString());
	    } else if (currFieldObject instanceof Integer) {
		buildInt(sb, currField.getInt(object));
	    } else if (currFieldObject instanceof List) {
		List currList = (List) currField.get(object);
		buildList(sb, currList);
	    } else if (currFieldObject != null) {
		HashMap<String, Field> childFields = JsonParser.getFields(currFieldObject.getClass());
		buildObject(sb, childFields, currFieldObject);
	    }

	    i++;
	    if (i != fields.size()) sb.append(",");
	}
	sb.append("}");
    }

    private static <T> void buildList(StringBuilder sb, List currList) throws IllegalAccessException {
	sb.append("[");
	int i = 0;
	for (Object elem : currList) {
	    if (elem instanceof String) {
		buildString(sb, (String) elem);
	    } else if (elem instanceof Integer) {
		buildInt(sb, (Integer) elem);
	    } else if (elem instanceof List) {
		buildList(sb, (List) elem);
	    } else {
		HashMap<String, Field> fields = JsonParser.getFields(elem.getClass());
		buildObject(sb, fields, elem);
	    }

	    i++;
	    if (i != currList.size()) sb.append(",");
	}
	sb.append("]");
    }
    
    private static <T> void buildString(StringBuilder sb, String s) throws IllegalAccessException {
	sb.append('"' + s + '"');
    }

    private static <T> void buildInt(StringBuilder sb, int v) throws IllegalAccessException {
	sb.append(v);
    }
}
