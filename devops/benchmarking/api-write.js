import http from 'k6/http';
import exec from 'k6/x/exec';
// import { check, sleep, SharedArray } from 'k6';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Rate } from 'k6/metrics';

const config = {
  patients: 1000,
  records: 5,
  corruption: 0.25, // ie 0 || 0.25 etc., up to 1.
  autoClean: true, // Removes all files from results and async_receiver/csv directories
  verbose: false, // Spits out container logs in this thread
}

// Configure number of requests per second per user:
const requestsPerUserSecond = 1;
// Configure the test scenarios in the options object below...

// Define custom metrics
export let errorRate = new Rate('errors');

// Define options
export let options = {
  scenarios: {
    ramping: {
      executor: 'ramping-vus',
      startVUs: 1,
      stages: [
        { duration: '1m', target: 2000 }, // Ramp up to 100 users over 1 minute
        { duration: '1m', target: 0 }, // Bring back down
      ],
      gracefulRampDown: '1m',
    },
  },
  thresholds: {
    errors: ['rate<0.1'], // <10% errors
    http_req_failed: ['rate<0.01'], // <1% http errors
    http_req_duration: ['avg<1000'], // average duration under 1s
  },
};

export function setup() {
  if (config.autoClean) {
    console.log('AutoClean is enabled. Removing all files from results and async_receiver/csv directories.');
    exec.command('sh', ['-c', 'rm -f ../JeMPI_TestData/Reference/results/*']);
    exec.command('sh', ['-c', 'rm -f ../linux/docker/docker_data/data-apps/async_receiver/csv/*']);
    console.log('Files removed.');
  }

  // Step 1: Trigger the Async Receiver by placing a file in its path
  const testDataFile = exec.command('sh', ['-c', `cd ../JeMPI_TestData/Reference && python3 DataGenerator.py --patients ${config.patients} --records ${config.records} --corruption ${config.corruption}`]).trim();
  console.log(`Generated file: ${testDataFile}`);
  const filePath = `../JeMPI_TestData/Reference/results/${testDataFile}`; // Ensure no single quotes
  console.log(`File: ${filePath}`); // Add debugging information

  // Spit out file facts...
  const fileContent = exec.command('sh', ['-c', `cat ${filePath}`]).trim();

  // Manually parse CSV content
  const lines = fileContent.split('\n');
  const headers = lines[0].split(',');
  const parsedData = lines.slice(1).map(line => {
    const values = line.split(',');
    let record = {};
    headers.forEach((header, index) => {
      record[header.trim()] = values[index].trim();
    });
    return record;
  });

  return { parsedData };
}

// Use SharedArray to share parsed data among VUs
const sharedParsedData = new SharedArray('parsedData', function () {
  return __ENV.PARSED_DATA ? JSON.parse(__ENV.PARSED_DATA) : [];
});

let recordsProcessed = 0;

export default function (data) {
  const parsedData = data.parsedData;
  console.log(`Records processed: ${recordsProcessed}`);

  if (recordsProcessed >= parsedData.length) {
    console.log('All records processed. Exiting test.');
    return;
  }

  const record = parsedData[recordsProcessed];
  if (config.verbose) console.log(`Processing record: ${JSON.stringify(record)}`);

  if (!record) {
    console.log('Record is undefined. Exiting test.');
    return;
  }

  const payload = JSON.stringify({
    candidateThreshold: 0.9,
    sourceId: {
      patient: record.src_id_patient,
    },
    uniqueInteractionData: {
      auxDateCreated: new Date().toISOString(),
    },
    demographicData: {
      givenName: record.given_name,
      familyName: record.family_name,
      gender: record.gender,
      dob: record.dob.replace(/-/g, ''),
      city: record.city,
      nationalId: record.national_id,
    },
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post('http://localhost:50000/JeMPI/crRegister', payload, params);

  const success = check(res, {
    'status is 200': (r) => r.status === 200,
  });

  if (res.status == '200') console.log(res.status, 'Success');
  else if (res.status == '409') console.log(res.status, 'Already exists');
  else {
    errorRate.add(!success);
    console.log(res.status, res.body);
  }

  recordsProcessed++;
  sleep(1 / requestsPerUserSecond);
}