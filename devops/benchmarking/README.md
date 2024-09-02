### Setup Instructions

Using docker to build the xk6 binary:
```bash
docker run --rm -it -u "$(id -u):$(id -g)" -v "${PWD}:/xk6" grafana/xk6 build v0.43.1 \
  --with github.com/mostafa/xk6-kafka@latest \
  --with github.com/grafana/xk6-exec@latest
```

Benchmarking the Linker: `./k6 run linker-test.js`

Benchmarking the API: 
- Simple reads: `./k6 run api-read.js`
- Adding records and linking: `./k6 run api-write.js`

Monitoring Kafka (WIP, for future reference): `./k6 run kafka-monitor.js`
