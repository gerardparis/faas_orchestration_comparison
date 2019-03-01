package lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


/**
 * AWS Lambda function handler.
 * <p>
 * The function simulates work with a sleep. Input is a string and output is the
 * same string, unmodified.
 * <p>
 * Date: 2018-06-20
 *
 * @author Daniel
 */
public final class SlothFunction implements RequestHandler<String, String> {
    /**
     * This function will sleep for this number of milliseconds.
     */
    private static final int TIME_TO_WAIT = 20000;

    @Override
    public String handleRequest(final String s, final Context context) {
        try {
            Thread.sleep(TIME_TO_WAIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return s;
    }
}
