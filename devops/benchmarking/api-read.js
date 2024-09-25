import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Configure number of requests per second per user:
const requestsPerUserSecond = 1;
// Configure the test scenarios in the options object below...

// Define custom metrics
export let errorRate = new Rate('errors');

// Define options
export let options = {
  stages: [
    { duration: '1m', target: 100 }, // ramp-up to 100 users
    { duration: '2m', target: 100 }, // stay at 100 users for 2 minutes
    { duration: '1m', target: 0 },  // ramp-down to 0 users
  ],
  thresholds: {
    errors: ['rate<0.1'], // <10% errors
    http_req_failed: ['rate<0.01'], // <1% http errors
    // http_req_duration: ['p(99)<1000'], // 99% of requests under 1s
    http_req_duration: ['avg<1000'], // average duration under 1s
  },
};

export default function () {
  const url = 'http://localhost:50000/JeMPI/countInteractions';
  const payload = JSON.stringify({
    username: 'test_case',
    password: '1234',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // Send a post request and save response as a variable
  // const res = http.post(url, payload, params);
  const res = http.get(url, params);

  // Check if the request was successful
  const success = check(res, {
    'status is 200': (r) => r.status === 200,
  });

  // Record error rate
  errorRate.add(!success);

  // Log the request body
  console.log(res.body);

  // Sleep to maintain the desired RPS
  sleep(1 / requestsPerUserSecond);
}
