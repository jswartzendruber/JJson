import me.jjson.JsonException;
import me.jjson.JsonExample;
import me.jjson.JsonParser;
import me.jjson.JsonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Json {
    public static void main(String[] args) {
	String json = "{"
	    + "\"user_code\": \"AAAAAAAA\","
	    + "\"device_code\": \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\","
	    + "\"verification_uri\": \"https://www.microsoft.com/link\","
	    + "\"expires_in\": 900,"
	    + "\"interval\": 5,"
	    + "\"array\": [ 500 ],"
	    + "\"long_array\": [ \"asdf\", \"bcdef\" ],"
	    + "\"nested_array\": [ [ 123, 456 ], [ 789 ] ],"
	    + "\"obj\": { \"name\": \"jimmy\", \"age\": 24 },"
	    + "\"objs\": [ { \"name\": \"jimmy\", \"age\": 24 }, {\"name\": \"jenny\", \"age\": 28 } ]," 
	    + "\"message\": \"To sign in, use a web browser to open the page https://www.microsoft.com/link and enter the code AAAAAAAA to authenticate.\""
	    + "}";

	try { 
	    JsonExample obj = (JsonExample) new JsonParser<JsonExample>(json, JsonExample.class).parse();
	    if (obj == null) return;

	    System.out.println(obj);
	    System.out.println("-----------");
	    System.out.println(JsonUtils.toJsonString(obj));
	} catch (InstantiationException | IllegalAccessException | InvocationTargetException | JsonException e) {
	    e.printStackTrace();
	}
    }
}
