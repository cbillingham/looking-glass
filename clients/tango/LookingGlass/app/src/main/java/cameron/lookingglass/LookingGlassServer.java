package cameron.lookingglass;

/**
 * Created by cameron on 4/19/16.
 */
//global class for storing and requesting Looking Glass url
public class LookingGlassServer {
    private static String LookingGlassURL = "";

    public static String getURL() {
        return LookingGlassURL;
    }

    public static void setURL(String url) {
        LookingGlassURL = url;
    }
}
