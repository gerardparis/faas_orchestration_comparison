# Microsoft Azure Durable Functions experiments

## Install Visual Studio 2017 tools for Azure Functions

Follow this guide:

- [Create your first durable function in C#](https://docs.microsoft.com/en-us/azure/azure-functions/durable/durable-functions-create-first-csharp)

## Experiments

### Sequential

Find the [Test.cs](src/Test.cs) file in this directory and move it to the project created above.

Follow the steps from the ``Publish the Project to Azure`` subsection of the official guide above.

Execute the ``HttpSyncStart.cs`` with ``Sequential`` as a Parameter either from the Test window of the Azure's function site or following the instructions of the guide above.

The overhead can be obtained from Application Insights.

Modify the ```numTasks``` variable to change the number of functions in parallel.

### Parallel

Find the [Test.cs](src/Test.cs) file in this directory and move it to the project created above.

Follow the steps from the ``Publish the Project to Azure`` subsection of the official guide above.

Execute the ``HttpSyncStart.cs`` with ``Parallel`` as a Parameter either from the Test window of the Azure's function site or following the instructions of the guide above.

The overhead can be obtained from Application Insights.

Modify the ```numTasks``` variable to change the number of functions in parallel.

