// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the MIT License. See LICENSE in the project root for license information.

using System;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Azure.WebJobs;
using Microsoft.Extensions.Logging;
using Microsoft.WindowsAzure.Storage.Blob;


namespace VSSample
{
    public static class Test
    {
        [FunctionName("Sequential")]
        public static async Task<long> Run(
            [OrchestrationTrigger] DurableOrchestrationContext backupContext,
            ILogger log      
            )
        {
            var start = DateTime.UtcNow;
            int numTasks = 5;
            log.LogInformation($" start time: "+start.ToLongTimeString());
           

            for (int i = 0; i < numTasks; i++)
            {
                await backupContext.CallActivityAsync<long>(
                    "mySleep",
                    i
                    );
            }
            var end = DateTime.UtcNow;
            log.LogInformation($" final time: " + start.ToLongTimeString());
            TimeSpan timeDiff = end - start;

            return (Convert.ToInt32(timeDiff.TotalMilliseconds) - 2000*numTasks);
        }

        [FunctionName("Parallel")]
        public static async Task<long> ParallelF(
            [OrchestrationTrigger] DurableOrchestrationContext backupContext,
            ILogger log
            )
        {
            var start = DateTime.UtcNow;
            var numTasks = 5;
            log.LogInformation($" start time: " + start.ToLongTimeString());
            var tasks = new Task<long>[numTasks];

            for (int i = 0; i < tasks.Length; i++)
            {
                tasks[i] = backupContext.CallActivityAsync<long>(
                    "mySleep",
                    i
                    );
            }
            await Task.WhenAll(tasks);
            var end = DateTime.UtcNow;
            log.LogInformation($" final time: " + start.ToLongTimeString());
            TimeSpan timeDiff = end - start;

            return (Convert.ToInt32(timeDiff.TotalMilliseconds) - 2000);
        }

        [FunctionName("mySleep")]
        public static async Task<long> Sleep(
            [ActivityTrigger] string a,
            Binder binder,
            ILogger log)
        {
            System.Threading.Thread.Sleep(2000);
            return 0;
        }
    }

}
