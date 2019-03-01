# IBM Cloud Functions experiments

## Prerequisites

Install [IBM Cloud CLI](https://console.bluemix.net/docs/cli/reference/ibmcloud/download_cli.html#install_use) and [Composer](https://github.com/apache/incubator-openwhisk-composer).

Download google gson dependency:
```
wget https://search.maven.org/remotecontent?filepath=com/google/code/gson/gson/2.8.5/gson-2.8.5.jar
```

Compile and deploy the Java actions:

```
javac -cp gson-2.8.5.jar FSeqSleep1s.java
jar cvf fsleep1s.jar FSeqSleep1s.class
ibmcloud fn action create fsleep1s fsleep1s.jar --main FSeqSleep1s
```

```
javac -cp gson-2.8.5.jar FPassPayload.java
jar cvf fpasspayload.jar FPassPayload.class
ibmcloud fn action create fpasspayload fpasspayload.jar --main FPassPayload
```


## Sequences

### Testing the overheads of a sequence of actions

Create a sequence of 5 actions:
```
ibmcloud fn action create sequenceFiveActions --sequence fsleep1s,fsleep1s,fsleep1s,fsleep1s,fsleep1s
```

Invoke the sequence of actions:
```
ibmcloud fn action invoke --result sequenceFiveActions
```

### Testing payload overheads of a sequence of actions

Create a sequence of 5 actions:
```
ibmcloud fn action create sequenceFiveActionsPayload --sequence fpasspayload,fpasspayload,fpasspayload,fpasspayload,fpasspayload
```

Invoke the sequence of actions:
```
ibmcloud fn action invoke --result sequenceFiveActionsPayload --param-file payload\_params.json
```

## Composer

### Testing the overheads of a sequence using the `repeat` combinator

Deploy the compositions:
```
compose sequence.js > sequence.json
deploy sequence sequence.json -w
```

Invoke the composition:
```
ibmcloud fn action invoke --result sequence
```

You can obtain the execution traces with:
```
ibmcloud fn activation list
```

We recommend using [Kui Shell](https://github.com/IBM/kui) to visualize execution traces.

### Testing payload overheads

Deploy the compositions:
```
compose payload.js > payload.json
deploy payload payload.json -w
```

Invoke the composition:

```
ibmcloud fn action invoke --result payload --param-file payload\_params.json
```
