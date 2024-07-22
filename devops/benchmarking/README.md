### Setup Instructions


docker run --rm -it -u "$(id -u):$(id -g)" -v "${PWD}:/xk6" grafana/xk6 build v0.43.1 \
  --with github.com/mostafa/xk6-kafka@v0.17.0 \
  --with github.com/grafana/xk6-exec@latest

Or to build the exec binary separately:
  xk6 build --with github.com/mostafa/xk6-kafka@v0.17.0
  xk6 build --with github.com/grafana/xk6-exec@latest


./k6 run /path/to/script.js

xk6 build --with github.com/mostafa/xk6-kafka@latest
