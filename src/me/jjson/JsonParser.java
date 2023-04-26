package me.jjson;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonParser<T> {
    private boolean debug = false;
    private List<Token> tokens;
    private String currentKey;
    private String json;
    private int index;

    private Class<T> topLevelObjectClass;

    public JsonParser(String json, Class<T> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException {
	this.topLevelObjectClass = clazz;
	this.json = json;
	this.index = 0;

	if (debug) System.out.println(json);
    }

    private static Constructor getDefaultConstructor(Class<?> clazz) {
	Constructor[] constructors = clazz.getDeclaredConstructors();
	Constructor defaultConstructor = null;

	for (int i = 0; i < constructors.length; i++) {
	    defaultConstructor = constructors[i];
	    if (defaultConstructor.getGenericParameterTypes().length == 0) break;
	}

	defaultConstructor.setAccessible(true);
	return defaultConstructor;
    }

    protected static HashMap<String, Field> getFields(Class<?> clazz) {
	HashMap<String, Field> fields = new HashMap<>();
	for (Field f : clazz.getDeclaredFields()) {
	    f.setAccessible(true);
	    fields.put(f.getName(), f);
	}
	return fields;
    }

    public T parse() throws IllegalAccessException, JsonException, InstantiationException, InvocationTargetException {
	tokenize();
	return eatObject(this.topLevelObjectClass);
    }

    private void tokenize() {
	this.tokens = new ArrayList<>();

	int idx = 0;
	int len = this.json.length();
	while (idx < len) {
	    char c = this.json.charAt(idx++);
	    if (c == '"') {
		StringBuilder sb = new StringBuilder();
		char last = '\0';
		while (idx < len && !(this.json.charAt(idx) == '"' && last != '\\')) {
		    last = this.json.charAt(idx);
		    sb.append(this.json.charAt(idx++));
		}
		idx++; // trailing quote
		this.tokens.add(new Token(sb.toString(), TokenKind.String));
	    } else if (Character.isDigit(c)) {
		StringBuilder sb = new StringBuilder();
		sb.append(c);
		while (idx < len && Character.isDigit(this.json.charAt(idx))) {
		    sb.append(this.json.charAt(idx++));
		}
		this.tokens.add(new Token(sb.toString(), TokenKind.Integer));
	    } else if (c == '{' || c == '}' || c == ',' || c == ':' || c == '[' || c == ']') {
		this.tokens.add(new Token("" + c, TokenKind.Char));
	    } else if (c == '\n' || c == '\t' || c == '\r' || c == ' ') {
		// Ignored
	    } else {
		System.out.println("Unknown token: " + c);
	    }
	}
    }

    private void expect(String s) throws JsonException {
	if (debug) System.out.println("\t" + this.tokens.get(this.index).data);
	if (!this.tokens.get(this.index++).data.equals(s)) {
	    throw new JsonException("Expected '" + s + "', got '" + this.tokens.get(this.index - 1).data + "'");
	}
    }

    private Token peek() {
	return this.tokens.get(this.index);
    }

    private Token next() {
	if (debug) System.out.println("\t" + this.tokens.get(this.index).data);
	return this.tokens.get(this.index++);
    }

    private String eatString() throws JsonException, IllegalAccessException {
	if (debug) System.out.println("startEatString");
	Token curr = next();
	if (curr.type != TokenKind.String) throw new JsonException("Expected string, got " + curr.data);
	if (debug) System.out.println("endEatString");
	return curr.data;
    }

    private int eatInt() throws JsonException, IllegalAccessException {
	if (debug) System.out.println("startEatInt");
	Token curr = next();
	if (curr.type != TokenKind.Integer) throw new JsonException("Expected int, got " + curr.data);
	if (debug) System.out.println("endEatInt");
	return Integer.parseInt(curr.data, 10);
    }

    @SuppressWarnings("unchecked")
    private List eatArray(HashMap<String, Field> parentObjectFields, T parentObject) throws JsonException, IllegalAccessException, InstantiationException, InvocationTargetException {
	if (debug) System.out.println("startEatArray");
	Token curr = peek();
	if (curr.type != TokenKind.Char && !curr.data.equals("[")) {
	    throw new JsonException("Expected array, got " + curr.data);
	}

	ArrayList array = new ArrayList();
	String objectKey = this.currentKey;

	expect("[");
	while (!peek().data.equals("]")) {
	    curr = peek();
	    if (curr.type == TokenKind.Integer) {
		array.add(eatInt());
	    } else if (curr.type == TokenKind.String) {
		array.add(eatString());
	    } else if (curr.type == TokenKind.Char && curr.data.equals("[")) {
		array.add(eatArray(parentObjectFields, parentObject));
	    } else if (curr.type == TokenKind.Char && curr.data.equals("{")) {
		Field f = parentObjectFields.get(objectKey);
		if (f != null) {
		    ParameterizedType type = (ParameterizedType) f.getGenericType();
		    T innerObjectType = (T) type.getActualTypeArguments()[0];
		    Class cls = (Class) innerObjectType;
		    array.add(eatObject(cls));
		} else {
		    throw new JsonException("Error: Key '" + objectKey + "' does not exist.");
		}
	    }
		
	    if (peek().data.equals("]")) break;
	    else expect(",");
	}
	expect("]");

	if (debug) System.out.println("endEatArray");
	return array;
    }

    @SuppressWarnings("unchecked")
    private T eatObject(Class<T> clazz) throws JsonException, IllegalAccessException, InstantiationException, InvocationTargetException {
	if (debug) System.out.println("startEatObject for " + clazz.toString());
	Token curr = peek();
	if (curr.type != TokenKind.Char && !curr.data.equals("{")) {
	    throw new JsonException("Expected object, got " + curr.data);
	}

	HashMap<String, Field> parentObjectFields = getFields(clazz);
	T parentObject = (T) getDefaultConstructor(clazz).newInstance();

	expect("{");
	while (!peek().data.equals("}")) {
	    this.currentKey = eatString();
	    expect(":");
	    parseIntIfExists(parentObjectFields, parentObject);
	    parseStringIfExists(parentObjectFields, parentObject);
	    parseArrayIfExists(parentObjectFields, parentObject);
	    parseObjectIfExists(parentObjectFields, parentObject);

	    if (peek().data.equals("}")) break;
	    else expect(",");
	}
	expect("}");

	if (debug) System.out.println("endEatObject");
	return parentObject;
    }

    private void getAndSetIntField(HashMap<String, Field> objectFields, T object, String key, int value) throws JsonException, IllegalAccessException {
	Field f = objectFields.get(key);
	if (f != null) {
	    f.setInt(object, value);
	} else {
	    throw new JsonException("Error: Key '" + key + "' does not exist.");
	}
    }

    private void getAndSetObjectField(HashMap<String, Field> objectFields, T object, String key, Object value) throws JsonException, IllegalAccessException {
	Field f = objectFields.get(key);
	if (f != null) {
	    f.set(object, value);
	} else {
	    throw new JsonException("Error: Key '" + key + "' does not exist.");
	}
    }

    private void parseIntIfExists(HashMap<String, Field> objectFields, T object) throws JsonException, IllegalAccessException {
	Token curr = peek();
	if (curr.type == TokenKind.Integer) {
	    getAndSetIntField(objectFields, object, this.currentKey, eatInt());
	}
    }

    private void parseStringIfExists(HashMap<String, Field> objectFields, T object) throws JsonException, IllegalAccessException {
	Token curr = peek();
	if (curr.type == TokenKind.String) {
	    getAndSetObjectField(objectFields, object, this.currentKey, eatString());
	}
    }

    private void parseArrayIfExists(HashMap<String, Field> objectFields, T object) throws JsonException, IllegalAccessException, InstantiationException, InvocationTargetException {
	Token curr = peek();
	if (curr.type == TokenKind.Char && curr.data.equals("[")) {
	    getAndSetObjectField(objectFields, object, this.currentKey, eatArray(objectFields, object));
	}
    }

    @SuppressWarnings("unchecked")
    private void parseObjectIfExists(HashMap<String, Field> parentObjectFields, T parentObject) throws JsonException, IllegalAccessException, InstantiationException, InvocationTargetException {
	Token curr = peek();
	if (curr.type == TokenKind.Char && curr.data.equals("{")) {
	    String objectKey = this.currentKey;
	    Field f = parentObjectFields.get(objectKey);
	    if (f != null) {
		Class cls = f.getType();
		T innerObject = (T) eatObject(cls);
		f.set(parentObject, innerObject);
	    } else {
		throw new JsonException("Error: Key '" + objectKey + "' does not exist.");
	    }
	}
    }
}

class Token {
    String data;
    TokenKind type;

    public Token(String data, TokenKind type) {
	this.data = data;
	this.type = type;
    }
}

enum TokenKind {
    String,
    Integer,
    Char,
}
