package me.jjson;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonParser<T> {
    private List<Token> tokens;
    private String currentKey;
    private String json;
    private int index;
    private boolean debug = false;

    private Object object;
    private Class<T> objectClass;
    private Map<String, Field> objectFields;

    public JsonParser(String json, Class<T> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException {
	this.json = json;
	this.index = 0;

	if (debug) System.out.println(json);

	Constructor[] ctors = clazz.getDeclaredConstructors();
	Constructor defCtor = null;
	for (int i = 0; i < ctors.length; i++) {
	    defCtor = ctors[i];
	    if (defCtor.getGenericParameterTypes().length == 0) break;
	}

	defCtor.setAccessible(true);
	this.objectClass = clazz;
	this.object = defCtor.newInstance();
	this.objectFields = new HashMap<>();
	for (Field f : this.objectClass.getDeclaredFields()) {
	    f.setAccessible(true);
	    objectFields.put(f.getName(), f);
	}
    }

    public Object parse() throws IllegalAccessException, JsonException {
	tokenize();
	parseJsonObject();
	
	return object;
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

    private List eatArray() throws JsonException, IllegalAccessException {
	if (debug) System.out.println("startEatArray");
	Token curr = peek();
	if (curr.type != TokenKind.Char && !curr.data.equals("[")) {
	    throw new JsonException("Expected array, got " + curr.data);
	}

	expect("[");
	ArrayList array = new ArrayList();
	
	while (!peek().data.equals("]")) {
	    curr = peek();
	    if (curr.type == TokenKind.Integer) {
		array.add(eatInt());
	    } else if (curr.type == TokenKind.String) {
		array.add(eatString());
	    } else if (curr.type == TokenKind.Char && curr.data.equals("[")) {
		System.out.println("nested eat");
		array.add(eatArray());
	    }
		
	    if (peek().data.equals("]")) break;
	    else expect(",");
	}
	expect("]");

	if (debug) System.out.println("endEatArray");
	return array;
    }

    private void parseJsonObject() throws JsonException, IllegalAccessException {
	expect("{");
	while (!peek().data.equals("}")) {
	    this.currentKey = eatString();
	    expect(":");
	    parseIntIfExists();
	    parseStringIfExists();
	    parseArrayIfExists();

	    if (peek().data.equals("}")) break;
	    else expect(",");
	}
	expect("}");
    }

    private void getAndSetIntField(String key, int value) throws JsonException, IllegalAccessException {
	Field f = this.objectFields.get(key);
	if (f != null) {
	    f.setInt(this.object, value);
	} else {
	    throw new JsonException("Error: Key '" + key + "' does not exist.");
	}
    }

    private void getAndSetObjectField(String key, Object value) throws JsonException, IllegalAccessException {
	Field f = this.objectFields.get(key);
	if (f != null) {
	    f.set(this.object, value);
	} else {
	    throw new JsonException("Error: Key '" + key + "' does not exist.");
	}
    }

    private void parseIntIfExists() throws JsonException, IllegalAccessException {
	Token curr = peek();
	if (curr.type == TokenKind.Integer) {
	    getAndSetIntField(this.currentKey, eatInt());
	}
    }

    private void parseStringIfExists() throws JsonException, IllegalAccessException {
	Token curr = peek();
	if (curr.type == TokenKind.String) {
	    getAndSetObjectField(this.currentKey, eatString());
	}
    }

    // @SuppressWarnings("unchecked")
    private void parseArrayIfExists() throws JsonException, IllegalAccessException {
	Token curr = peek();
	if (curr.type == TokenKind.Char && curr.data.equals("[")) {
	    getAndSetObjectField(this.currentKey, eatArray());
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
