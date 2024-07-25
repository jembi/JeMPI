import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/x/exec';

const config = {
  patients: 1000,
  records: 10,
  corruption: 0.25, // ie 0 || 0.25 etc., up to 1.
  autoClean: true, // Removes all files from results and async_receiver/csv directories
  verbose: false, // Spits out container logs in this thread
}

// Add options object to set the duration
export const options = {
  duration: '24h', // Set the test cap to n minutes/hours (e.g. 10m or 24h)
  iterations: 10, // How many time to run this test
};

export default function () {
  // Housekeeping
  if (config.autoClean) {
    console.log('AutoClean is enabled. Removing all files from results and async_receiver/csv directories.');
    exec.command('sh', ['-c', 'rm -f ../JeMPI_TestData/Reference/results/*']);
    exec.command('sh', ['-c', 'rm -f ../linux/docker/docker_data/data-apps/async_receiver/csv/*']);
    console.log('Files removed.');
  }

  // Step 1: Trigger the Async Receiver by placing a file in its path
  const randomSeed = Math.floor(Math.random() * 900000) + 100000;
  console.log(`Generated random seed: ${randomSeed}`);
  const testDataFile = exec.command('sh', ['-c', `cd ../JeMPI_TestData/Reference && python3 DataGenerator.py --patients ${config.patients} --records ${config.records} --corruption ${config.corruption} --seed ${randomSeed}`]).trim();
  console.log(`Generated file: ${testDataFile}`);
  const filePath = `../JeMPI_TestData/Reference/results/${testDataFile}`; // Ensure no single quotes
  console.log(`File: ${filePath}`); // Add debugging information
  
  // Spit out file facts...
  const fileContent = exec.command('wc', ['-l', filePath]).trim();
  const numberOfLines = parseInt(fileContent.split(' ')[0]) - 1;
  console.log(`Number of records: ${numberOfLines}\n\n`);

  // Pre-process file if needed
  // ie shuffle?
  
  // Send it to the async receiver dropzone
  const destinationFolder = '../linux/docker/docker_data/data-apps/async_receiver/csv';

  // Log out file system info
  console.log(`Checking for file: ${filePath}`);
  if (config.verbose) console.log(`Current working directory: ${exec.command('pwd')}`);
  if (config.verbose) console.log(`Contents of current directory: ${exec.command('ls', ['-l'])}`);

  let retries = 3; // In case there's a race condition between the file generated vs placed in the path
  let fileExists = false;
  while (retries > 0 && !fileExists) {
    sleep(2);
    const fileCheckCommand = `sh -c '[ -f "${filePath}" ] && echo "true" || echo "false"'`;
    const fileCheckOutput = exec.command('sh', ['-c', fileCheckCommand]).trim();
    fileExists = fileCheckOutput === "true";
    console.log(`File exists check (${retries} retries left): ${fileExists}`);
    console.log(`File check command: ${fileCheckCommand}`);
    console.log(`File check output: ${fileCheckOutput}`);
    if (!fileExists) {
      console.log(`Contents of current directory: ${exec.command('ls', ['-l'])}`);
    }
    retries--;
  }

  if (fileExists) {
    console.log(`File found. Attempting to copy...`);
    const copyResponse = exec.command('sh', ['-c', `cp "${filePath}" "${destinationFolder}" 2>&1 || echo "Copy failed with exit code $?"`]);
    console.log(`Copy response: ${copyResponse}`);
  } else {
    console.error(`File not found after multiple retries: ${filePath}`);
    console.log(`Final check of current directory: ${exec.command('ls', ['-l'])}`);
    exec.command('sh', ['-c', `exit 1`]);
  }

  console.log(`Datestamp: ${exec.command("date")}`);

  // Step 2: Detect the number of linkers in the swarm
  const linkersResponse = exec.command('docker', ['service', 'ls', '--filter', 'name=jempi_linker', '--format', '{{.Replicas}}']);
  console.log(linkersResponse);
  let numLinkers = parseInt(linkersResponse.trim().split('/')[1]);

  console.log(`Number of linkers: ${numLinkers}`);

  // Step 3: Monitor completion and report time taken
  const startTime = new Date().toISOString();
  const linkersSet = new Map(); // Use a Map to store container info

  // Launch logs for each linker in a new terminal
  for (let i = 1; i <= numLinkers; i++) {
    const serviceName = `jempi_linker`;
    const instanceFilter = `name=${serviceName}.${i}`;
    const taskIdsCommand = `docker service ps -q --filter ${instanceFilter} ${serviceName}`;
    const taskIds = exec.command('sh', ['-c', taskIdsCommand]).trim().split('\n');

    for (const taskId of taskIds) {
      if (taskId) {
        const containerIdCommand = `docker inspect --format '{{.Status.ContainerStatus.ContainerID}}' ${taskId}`;
        const containerId = exec.command('sh', ['-c', containerIdCommand]).trim().substring(0, 12);

        if (containerId) {
          const logsCommand = `docker logs -f ${containerId} --tail=300`;
          if (!linkersSet.has(containerId)) {
            linkersSet.set(containerId, { logsCommand: logsCommand, lastCheckTime: startTime, completedTime: null, completedTimestamp: null });
          }
        }
      }
    }
  }

  while (Array.from(linkersSet.values()).filter(info => info.completedTimestamp).length < numLinkers) {
    for (let i = 1; i <= numLinkers; i++) {
      const serviceName = `jempi_linker`;
      const instanceFilter = `name=${serviceName}.${i}`;
      if (config.verbose) console.log(`Checking tasks for service: ${serviceName} with filter: ${instanceFilter}`);
      const taskIdsCommand = `docker service ps -q --filter ${instanceFilter} ${serviceName}`;
      if (config.verbose) console.log(`Task IDs command: ${taskIdsCommand}`);
      const taskIds = exec.command('sh', ['-c', taskIdsCommand]).trim().split('\n');
      //console.log(`Task IDs for service ${serviceName} instance ${i}: ${taskIds}`);

      for (const taskId of taskIds) {
        if (taskId) {
          const containerIdCommand = `docker inspect --format '{{.Status.ContainerStatus.ContainerID}}' ${taskId}`;
          if (config.verbose) console.log(`Container ID command: ${containerIdCommand}`);
          const containerId = exec.command('sh', ['-c', containerIdCommand]).trim().substring(0, 12);
          if (config.verbose) console.log(`Container ID for task ${taskId}: ${containerId}`);

          if (containerId) {
            const containerInfo = linkersSet.get(containerId);

            sleep(1); // Add a slight delay before fetching logs

            const logsCommand = `docker logs --since ${containerInfo.lastCheckTime} ${containerId}`;
            if (config.verbose) console.log(`Logs command: ${logsCommand}`);
            const logs = exec.command('sh', ['-c', logsCommand]);
            if (config.verbose) console.log(`Container ${containerId}: ${logs}`);

            // Update lastCheckTime for this container
            containerInfo.lastCheckTime = new Date().toISOString();

            // Check if the linker has completed
            if ((logs.match(/TEA TIME/g) || []).length > 0 || (logs.match(/Stream closed/g) || []).length > 0) {
              if (!config.verbose) console.log(`Logs for container ${containerId}: ${logs}`); // Logs summary portion only in quiet mode
              containerInfo.completedTimestamp = new Date().toISOString(); // Store finish time
              const completedDate = new Date(containerInfo.completedTimestamp);
              const hours = String(completedDate.getHours()).padStart(2, '0');
              const minutes = String(completedDate.getMinutes()).padStart(2, '0');
              containerInfo.completedTime = `${hours}:${minutes}`; // Store finish time
            }
            if ((logs.match(/Batch End Sentinel/g) || []).length > 0) {
              console.log(`Received Batch End Sentinel: ${containerId}: ${logs}`);
            }

            linkersSet.set(containerId, containerInfo); // Update the map with the new info
          }
        }
      }
    }

    const completedLinkers = Array.from(linkersSet.values()).filter(info => info.completedTimestamp).length;
    console.log(`\n\nCompleted linkers: ${completedLinkers}/${numLinkers}`);
    for (const [containerId, info] of linkersSet.entries()) {
      const clickableLink = `\x1b]8;;${info.logsCommand}\x1b\\${info.logsCommand}\x1b]8;;\x1b\\`;
      const status = info.completedTimestamp ? ` (Completed at ${info.completedTime})` : ' (In Progress)';
      console.log(`${clickableLink} ${status}`);
    }
    
    sleep(1); // Wait n seconds before checking again
  }

  const endTime = new Date().getTime();
  const duration = (endTime - new Date(startTime).getTime()) / 1000;

  if (duration < 60) {
    console.log(`All linkers completed in ${duration} sec`);
  } else if (duration < 3600) {
    const minutes = Math.floor(duration / 60);
    const seconds = (duration % 60).toFixed(0).padStart(2, '0');
    console.log(`All linkers completed in ${minutes} min ${seconds} sec`);
  } else {
    const hours = Math.floor(duration / 3600);
    const minutes = Math.floor((duration % 3600) / 60).toFixed(0).padStart(2, '0');
    console.log(`All linkers completed in ${hours} hr ${minutes} min`);
  }

  return; // Ensure the function exits
}