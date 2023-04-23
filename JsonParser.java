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

    private T object;
    private Class<T> objectClass;
    private Map<String, Field> objectFields;

    public JsonParser(String json, Class<T> clazz) throws InstantiationException {
	this.json = json;
	this.index = 0;

	try { 
	    this.objectClass = clazz;
	    this.object = clazz.newInstance();
	    this.objectFields = new HashMap<>();
	    for (Field f : this.objectClass.getDeclaredFields()) {
		objectFields.put(f.getName(), f);
	    }
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	}
    }

    public T parse() {
	try {
	    tokenize();
	    parseJsonObject();
	} catch (JsonException | IllegalAccessException e) {
	    System.out.println(e);
	}
	
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
	    } else if (c == '{' || c == '}' || c == ',' || c == ':') {
		this.tokens.add(new Token("" + c, TokenKind.Char));
	    } else if (c == '\n' || c == '\t' || c == '\r' || c == ' ') {
		// Ignored
	    } else {
		System.out.println("Unknown token: " + c);
	    }
	}
    }

    private void expect(String s) throws JsonException {
	if (!this.tokens.get(this.index++).data.equals(s)) {
	    throw new JsonException("Expected '" + s + "', got '" + this.tokens.get(this.index - 1).data + "'");
	}
    }

    private Token peek() {
	return this.tokens.get(this.index);
    }

    private Token next() {
	return this.tokens.get(this.index++);
    }

    private void parseKey() throws JsonException {
	Token key = next();
	this.currentKey = key.data;
	if (key.type != TokenKind.String) {
	    throw new JsonException("Expected json key, got '" + this.tokens.get(this.index - 1).data + "'");
	}
    }

    private void parseJsonObject() throws JsonException, IllegalAccessException {
	expect("{");
	while (!peek().data.equals("}")) {
	    parseKey();
	    expect(":");
	    parseIntIfExists();
	    parseStringIfExists();

	    if (peek().data.equals("}")) break;
	    else expect(",");
	}
	expect("}");
    }

    private void parseIntIfExists() throws JsonException, IllegalAccessException {
	Token curr = peek();
	if (curr.type == TokenKind.Integer) {
	    int i = Integer.parseInt(curr.data, 10);
	    Field f = objectFields.get(this.currentKey);
	    if (f != null) {
		f.setInt(this.object, i);
	    } else {
		throw new JsonException("Error: Key '" + this.currentKey + "' does not exist.");
	    }
	    next();
	}
    }

    private void parseStringIfExists() throws JsonException, IllegalAccessException {
	Token curr = peek();
	if (curr.type == TokenKind.String) {
	    Field f = objectFields.get(this.currentKey);
	    if (f != null) {
		f.set(this.object, curr.data);
	    } else {
		throw new JsonException("Error: Key '" + this.currentKey + "' does not exist.");
	    }
	    next();
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
