# AWS Step Functions experiments

## Install AWS client for credentials

Java AWS SDK uses your installed AWS cli credentials by default. If you don't have the client, follow this guides:

- [Install AWS cli](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html)

- [Configure credentials](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html)

## Experiments

The code is compiled with maven. The AWS Lambda is deployed with the maven plugin [lambda-maven-plugin](https://github.com/SeanRoy/lambda-maven-plugin):

```bash
mvn package shade:shade lambda:deploy-lambda -DskipTests
```

The project uses AWS SDK to create the state machines with code.
All experiments create and run the State Machines directly from code, and obtain the compute times from the returned metadata.
The code is also prepared to run for several configurations and execute several replicas of each experiment.

You can run each experiment by executing each file in the [test/java](src/test/java) folder in a jvm.

```bash
java -cp sf-examples-1.0.jar RunSequence
```

### AWS Lambda Handler

The function simulates work with a sleep. Input is a string and output is the same string, unmodified.

See [SlothFunction](src/main/java/lambdas/SlothFunction.java).

### Sequence

This experiment creates a Step Functions state machine with several states in
a sequence (defined in `steps`). Each state is a SlothFunction Lambda.
The configuration varies the number of steps in the sequence.

See [RunSequence](src/test/java/RunSequence.java).

### Payload overhead

This experiment creates a Step Functions state machine with 5 states in
a sequence. Each state is a SlothFunction Lambda that forwards the input.
The configuration varies the size of the input payload.
In AWS Step Functions, the payload has a size limit of 32KiB.

See [RunStateSequence](src/test/java/RunStateSequence.java).

### Wait Sequence

This experiment creates a Step Functions state machine with several Wait
states in a sequence. A Wait state is a special state in AWS Step Functions that stops the execution for a given amount of time.
The configuration varies the length of the sequence and the waiting time of each state.
The amount of time in a Wait state is unbounded, but the total execution of a state machine is limited to one year.

See [RunWaitSequence](src/test/java/RunWaitSequence.java).

### Parallel

This experiment creates a Step Functions state machine with one parallel state (branch). The estate contains a variable number of parallel states (defined in `steps`).
Each state is a SlothFunction Lambda, performing a sleep operation.
The configuration varies the number of parallel states.

See [RunParallel](src/test/java/RunParallel.java).
