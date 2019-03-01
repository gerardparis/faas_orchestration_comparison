import com.amazonaws.regions.Regions;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.amazonaws.services.stepfunctions.builder.StateMachine;
import com.amazonaws.services.stepfunctions.builder.states.Branch;
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
import java.util.LinkedList;
import java.util.List;

import static com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.branch;
import static com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.end;
import static com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.parallelState;
import static com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.stateMachine;
import static com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.taskState;

/**
 * This experiment creates a Step Functions state machine with one parallel
 * state (branch). The state contains a variable number of parallel states
 * (defined in `steps`). Each state is a SlothFunction Lambda. The code is
 * prepared to run for several configurations of parallelism and various
 * replicas per configuration. The execution times are obtained from the service
 * logs, parsed, and saved in a file.
 * <p>
 * Date: 2018-06-20
 *
 * @author Daniel
 */
public class RunParallel {
    /**
     * Please configure the ARNs.
     */
    private static final String arnTask = "arn:aws:lambda:_REGION_:XACCOUNTX:function:_function_name_";
    private static final String arnRole = "arn:aws:iam::XACCOUNTX:role/service-role/_role_name_";
    private static final int NREPLICAS = 10;

    public static void main(String[] args) throws InterruptedException, IOException {
        int[] steps = {5, 10, 20, 40, 80};

        for (int step : steps) {
            System.out.println(step + " STEPS:");
            doTest(step);
        }
    }

    private static void doTest(final int steps) throws IOException, InterruptedException {
        // CREATE State machine
        StateMachine.Builder stateMachineBuilder = stateMachine()
                .comment("A state machine with parallel states that do sleeps.")
                .startAt("Parallel");

        Branch.Builder[] branchBuilders = new Branch.Builder[steps];

        for (int i = 0; i < steps; i++) {
            branchBuilders[i] = branch()
                    .startAt(String.valueOf(i + 1))
                    .state(String.valueOf(i + 1), taskState().resource(arnTask).transition(end()));
        }

        stateMachineBuilder.state("Parallel", parallelState().branches(branchBuilders).transition(end()));

        final StateMachine stateMachine = stateMachineBuilder.build();
        System.out.println(stateMachine.toPrettyJson());
        final AWSStepFunctions client = AWSStepFunctionsClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        CreateStateMachineResult createMachine = client.createStateMachine(new CreateStateMachineRequest()
                .withName("Parallel" + steps)
                .withRoleArn(arnRole)
                .withDefinition(stateMachine));

        List<String> replicaTimes = new LinkedList<>();
        for (int replica = 0; replica < NREPLICAS; replica++) {
            // Run execution
            StartExecutionRequest startExecutionRequest = new StartExecutionRequest();
            startExecutionRequest.setStateMachineArn(createMachine.getStateMachineArn());
            startExecutionRequest.setInput("\"ASTP\"");

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
        Files.write(Paths.get(steps + "partimes.txt"), replicaTimes);
    }
}
