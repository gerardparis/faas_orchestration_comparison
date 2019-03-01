import java.util.concurrent.TimeUnit;
import com.google.gson.JsonObject;

public class FSeqSleep1s {
    public static JsonObject main(JsonObject args) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JsonObject response = new JsonObject();
        response.addProperty("done", "OK");
        return response;
    }
}
