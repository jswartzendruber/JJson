public class JsonExample {
    public String user_code;
    public String device_code;
    public String verification_uri;
    public int expires_in;
    public int interval;
    public String message;

    public JsonExample() {}

    @Override
    public String toString() {
	return "user_code=" + user_code + "\n"
	    + "device_code=" + device_code + "\n"
	    + "verification_uri=" + verification_uri + "\n"
	    + "expires_in=" + expires_in + "\n"
	    + "interval=" + interval + "\n"
	    + "message=" + message + "\n";
    }
}
