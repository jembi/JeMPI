# Jembi Platform Installation

## Prerequisites <a href="#_4a1yqprqttyw" id="_4a1yqprqttyw"></a>

In the following, we will introduce the software prerequisites to be able to run the client registry jempi on your machine using the platform.

### Docker <a href="#_rglemv3fug4d" id="_rglemv3fug4d"></a>

Please refer to the [official installation guide](https://docs.docker.com/engine/install/ubuntu/) in order to install docker on your machine.
It is best to follow the post installation process so that you grant docker sudo access.

### \[for Windows users] WSL2 <a href="#_ku1gqbfaemlj" id="_ku1gqbfaemlj"></a>

Installing WSL2 is required to be able to develop and test the project.\
It is recommended to limit the memory usage of WSL2.

## Local Setup <a href="#_k8o7yc6w0hnu" id="_k8o7yc6w0hnu"></a>

1. Create /tmp/logs directory
```bash
sudo mkdir -p /tmp/logs/
```
2. Create the docker platform image
```bash
./build-image.sh
```
3. Initialise Docker Swarm
```bash
docker swarm init
```
4. Run 'go cli' binary to launch the project
```bash
./get-cli.sh
```
5. Launch the client registry jempi package profile \
   a. all packages and profiles are configured in the ./config.yaml field \
   b. updates to environment variable can be made in the profile env file ie: mpi.env
```yaml
 - name: mpi
    packages:
      - interoperability-layer-openhim
      - reverse-proxy-nginx
      - message-bus-kafka
      - job-scheduler-ofelia
      - monitoring
      - client-registry-jempi
      - identity-access-manager-keycloak
      - openhim-mapping-mediator
    envFiles:
      - mpi.env
```
```bash
./instant-linux package init -p mpi
```

6. Access : http://localhost:3033/login

![JeMPI Web Keycloak Sign in](../.gitbook/assets/16)

7. Sign in with Keycloak user credentials

![JeMPI Web Keycloak Sign in](../.gitbook/assets/17)
