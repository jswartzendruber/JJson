package me.jjson;

import java.util.List;
import java.util.stream.Collectors;

public class JsonExample {
    public String user_code;
    public String device_code;
    public String verification_uri;
    public int expires_in;
    public int interval;
    public List<Integer> array;
    public List<String> long_array;
    public List<List<Integer>> nested_array;
    public String message;

    public JsonExample() {}

    @Override
    public String toString() {
	return "user_code=" + user_code + "\n"
	    + "device_code=" + device_code + "\n"
	    + "verification_uri=" + verification_uri + "\n"
	    + "expires_in=" + expires_in + "\n"
	    + "interval=" + interval + "\n"
	    + "array=" + stringify(array) + "\n"
	    + "long_array=" + stringify(long_array) + "\n"
	    + "nested_array=" + stringify(nested_array) + "\n"
	    + "message=" + message + "\n";
    }

    private static <T> String stringify(List<T> items) {
	return items.stream()
	    .map(e -> e.toString())
	    .collect(Collectors.joining(", ", "[", "]"));
    }
}
