import com.google.gson.JsonObject;
import java.util.concurrent.TimeUnit;

public class FPassPayload {
    public static JsonObject main(JsonObject args) {
        String payload = null;
        if (args.has("payload"))
            payload = args.getAsJsonPrimitive("payload").getAsString();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JsonObject response = new JsonObject();
        response.addProperty("payload", payload);
        return response;
    }
}
