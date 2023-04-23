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
	    + "\"message\": \"To sign in, use a web browser to open the page https://www.microsoft.com/link and enter the code AAAAAAAA to authenticate.\""
	    + "}";

	try { 
	    JsonExample obj = new JsonParser<JsonExample>(json, JsonExample.class).parse();
	    if (obj == null) {
		System.out.println("{}");
		return;
	    } else {
		System.out.println(obj);
	    }
	} catch (InstantiationException e) {
	    e.printStackTrace();
	}
    }
}
