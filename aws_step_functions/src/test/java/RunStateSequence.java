import com.amazonaws.regions.Regions;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.amazonaws.services.stepfunctions.builder.StateMachine;
import com.amazonaws.services.stepfunctions.model.CreateStateMachineRequest;
import com.amazonaws.services.stepfunctions.model.CreateStateMachineResult;
import com.amazonaws.services.stepfunctions.model.DescribeExecutionRequest;
import com.amazonaws.services.stepfunctions.model.DescribeExecutionResult;
import com.amazonaws.services.stepfunctions.model.ExecutionStatus;
import com.amazonaws.services.stepfunctions.model.GetExecutionHistoryRequest;
import com.amazonaws.services.stepfunctions.model.GetExecutionHistoryResult;
import com.amazonaws.services.stepfunctions.model.HistoryEvent;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.amazonaws.services.stepfunctions.model.StartExecutionResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.end;
import static com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next;
import static com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.stateMachine;
import static com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.taskState;

/**
 * This experiment creates a Step Functions state machine with 5 states in a
 * sequence. Each step forwards the input to the output. Each state is a
 * SlothFunction Lambda. The code is prepared to run for several configurations
 * of payload size (in KiB) and various replicas per configuration. The
 * execution times are obtained from the service logs, parsed, and saved in a
 * file.
 * <p>
 * Note that Step Functions has a payload limit of 32KiB.
 * <p>
 * Date: 2018-06-20
 *
 * @author Daniel
 */
public class RunStateSequence {
    /**
     * Please configure the ARNs.
     */
    private static final String arnTask = "arn:aws:lambda:_REGION_:XACCOUNTX:function:_function_name_";
    private static final String arnRole = "arn:aws:iam::XACCOUNTX:role/service-role/_role_name_";
    private static final int NREPLICAS = 10;

    public static void main(String[] args) throws InterruptedException, IOException {
        int[] sizes = {1, 32}; // KB  256, 512, 1024, 2048, 4096,

        for (int size : sizes) {
            System.out.println(size + "KB Size:");
            doTest(size);
        }
    }

    private static void doTest(final int size) throws InterruptedException, IOException {
        final int NSTEPS = 5;

        // CREATE State machine
        StateMachine.Builder stateMachineBuilder = stateMachine()
                .comment("A Sequence state machine that does sleeps.")
                .startAt("1");

        for (int i = 1; i <= NSTEPS; i++) {
            stateMachineBuilder.state(String.valueOf(i), taskState()
                    .resource(arnTask)
                    .transition((i != NSTEPS) ? next(String.valueOf(i + 1)) : end()));
        }

        final StateMachine stateMachine = stateMachineBuilder.build();
        System.out.println(stateMachine.toPrettyJson());
        final AWSStepFunctions client = AWSStepFunctionsClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        CreateStateMachineResult createMachine = client.createStateMachine(new CreateStateMachineRequest()
                .withName("StateSequence" + NSTEPS)
                .withRoleArn(arnRole)
                .withDefinition(stateMachine));

        List<String> replicaTimes = new LinkedList<>();
        char[] bytes = new char[size == 1 ? 1 : size * 1024 - 2];
        Arrays.fill(bytes, 'f');
        for (int replica = 0; replica < NREPLICAS; replica++) {
            // Run execution
            StartExecutionRequest startExecutionRequest = new StartExecutionRequest();
            startExecutionRequest.setStateMachineArn(createMachine.getStateMachineArn());
            startExecutionRequest.setInput("\"" + new String(bytes) + "\"");

//        long init = System.currentTimeMillis();
            StartExecutionResult executionResult = client.startExecution(startExecutionRequest);

            DescribeExecutionResult describeExecutionResult;
            do {
                Thread.sleep(500L);
                describeExecutionResult =
                        client.describeExecution(new DescribeExecutionRequest()
                                .withExecutionArn(executionResult.getExecutionArn()));
            } while (!describeExecutionResult.getStatus().equals(ExecutionStatus.SUCCEEDED.toString()));

//        long end = System.currentTimeMillis();


            GetExecutionHistoryResult executionHistory =
                    client.getExecutionHistory(new GetExecutionHistoryRequest()
                            .withExecutionArn(executionResult.getExecutionArn()));

//            System.out.println(executionHistory.getEvents());
            long timeInit = executionHistory.getEvents().get(0).getTimestamp().getTime();
            StringBuilder times = new StringBuilder();
            for (HistoryEvent historyEvent : executionHistory.getEvents()) {
                times.append(historyEvent.getTimestamp().getTime()).append("\t");
            }
            while (executionHistory.getNextToken() != null) {
                executionHistory = client.getExecutionHistory(new GetExecutionHistoryRequest()
                        .withExecutionArn(executionResult.getExecutionArn())
                        .withNextToken(executionHistory.getNextToken()));
                for (HistoryEvent historyEvent : executionHistory.getEvents()) {
                    times.append(historyEvent.getTimestamp().getTime()).append("\t");
                }
            }

            long timeEnd = executionHistory.getEvents().get(executionHistory.getEvents().size() - 1)
                    .getTimestamp().getTime();

            replicaTimes.add(times.toString());
            System.out.println("Elapsed: " + (timeEnd - timeInit) + "ms");
//        System.out.println("Real: " + (end-init) + "ms");
        }
        Files.write(Paths.get(size + "statetimes.txt"), replicaTimes);
    }
}
