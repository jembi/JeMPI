---
description: Useful for development or custom implementations
---

# Standalone Installation

## Prerequisites <a href="#_4a1yqprqttyw" id="_4a1yqprqttyw"></a>

### Docker <a href="#_rglemv3fug4d" id="_rglemv3fug4d"></a>

Docker should be installed in the machine.\
It is best to grant docker sudo access.

### \[for Windows users] WSL2 <a href="#_ku1gqbfaemlj" id="_ku1gqbfaemlj"></a>

Installing WSL2 is required to be able to develop and test the project.\
It is recommended to limit the memory usage of WSL2.

### Build Utilities <a href="#_ruowqnawk4n7" id="_ruowqnawk4n7"></a>

We need to download [sdkmanager](https://sdkman.io/), check if you already have it by running _sdk_.\
To install it, run the two following commands:

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

To check the installation, you can check the version by running: _sdk version_

We should install the following:

* Maven: Command: _sdk install maven_\
  Version: 3.8.6
* Scala Build Tool: Command: _sdk install sbt_
* Java: Command: _sdk install java 17.0.7-tem_\
  Version: Temerin 17.0.7-tem (See list by running: _sdk list java_)

Check the version of java by running: _java --version_. We should get: Temurin-17.0.7+7.

## Starting JeMPI <a href="#_1yk2dvaqt5h9" id="_1yk2dvaqt5h9"></a>

### #1 Run without local registry <a href="#_k8o7yc6w0hnu" id="_k8o7yc6w0hnu"></a>
Run in the terminal _./launch-local.sh_

[//]: # (You can **run** the script in [**this link**]&#40;https://github.com/jembi/JeMPI/pull/15/files&#41; **OR** follow these steps below:)

### #2 Alternatively run with a local registry <a href="#_k8o7yc6w0hnu" id="_k8o7yc6w0hnu"></a>

**Initialize the environment variables**

Go to: _docker/conf/env/_.

Run in the terminal _./create-env-linux-1.sh_, it is going to create a file _conf.env_ that we will need.

**Make sure you have a clean docker swarm**

It is fine to keep the images, you can either remove all the services, containers, volumes, configs and secrets.

Or it is much easier to run: _docker swarm leave --force_ and then _docker swarm init._

**Run bash scripts**

Go to: _docker/_, and run in the terminal with the same order the following bash scripts:

**1-** _a-images-1-pull-from-hub.sh_: This script is going to pull the needed images

![](../.gitbook/assets/6)

**2-** Make some changes to the _JeMPI\_Apps/build-all.sh_: before running _build-all.sh_ script, you need to **comment the lines where there is a “push”** and keep only the script that will build the images.\
Then you can cd to _JeMPI\_Apps_/ and run: _./build-all.sh_.

Example:

You should keep these:

```bash
pushd JeMPI_Controller
  ./build.sh || exit 1
popd
```

These should be removed/commented:

```bash
# pushd JeMPI_Controller
# ./push.sh
# popd
```

**3-** Edit _docker/conf/stack/docker-stack-0.yml_ and _docker/conf/stack/docker-stack-1.yml_: The images now does exit in the local docker hub, the name convention is same except it will not start with the hostname and a “/”, you can select the first part _“${REGISTRY\_NODE\_IP}/”_ and remove it in all the two files.

As an example, this is the old version of _docker-stack-0.yml_:

```yaml
services:
jempi-kafka-01:
    image: ${REGISTRY_NODE_IP}/$KAFKA_IMAGE
    user: root
    networks:
        - backend-kafka
```

An this is after the changes:

```yaml
services:
jempi-kafka-01:
    image: $KAFKA_IMAGE
    user: root
    networks:
        - backend-kafka
```

**NB:** Don’t forget to update both _docker/conf/stack/docker-stack-0.yml_ and _docker/conf/stack/docker-stack-1.yml_ files.

**5-** _z-stack-2-reboot.sh_: The script will deploy the stack and scale the containers up in order after removing everything.

That's it 🚀

### Running with local docker registry <a href="#_haqbu95umwhz" id="_haqbu95umwhz"></a>

**IP address**

In this case, we are using the local docker registry now. Follow these steps to set the correct ip address to use it.

\[CLUSTER] Run _ip addr_ and get the address of the wifi interface.\
\[ONE NODE] The ip address 127.0.0.1 can be used.

Run _sudo gedit /etc/hosts_ (gedit, vim, vi, any file editor), and comment the line out of the local ip address.

Add the address that we get from the above step with the hostname (hostname in this case in the machine name).

After these changes, ping the hostname and should respond.

Example of the resulted file:

127.0.0.1 localhost\
\#127.0.1.1 \<MACHINE\_NAME> //commented line\
127.0.0.1 \<MACHINE\_NAME> //127.0.0.1 or the other ip address

**Set a local registry**

Go to: _docker/conf/env/_.

Run in the terminal _./create-env-linux-1.sh_, it is going to create a file _conf.env_ that we will need.

Now, we need to tell docker that it is okay to run on the local registry because it is http and not https \[not secure].

To read more about it check this link: [Test an insecure registry](https://docs.docker.com/registry/insecure/).

Go to _docker/helper/scripts._

Run _./x-swarm-o-set-insecure-registries.sh_ (you need to grant it executable access first by running: _chmod +x ./x-swarm-o-set-insecure-registries.sh_), it will edit the file _/etc/docker/daemon.json_ and will restart docker to make changes take effect.

NB: The script will edit the access grants of the _/etc/docker/daemon.json_ file.

**Make sure you have a clean docker swarm**

It is fine to keep the images, you can either remove all the services, containers, volumes, configs and secrets.

Or it is much easier to run: _docker swarm leave --force_.

**Run bash scripts**

Go to: _docker/_, and run in the terminal with the same order the following bash scripts:

**1-** _a-images-1-pull-from-hub.sh_: This script is going to pull the needed images

![](../.gitbook/assets/7)

**2-** _b-swarm-1-init-node1.sh_: This script will initialize swarm with “--advertise-addr”, if you get any error about the address, you may skip the first step.\
You can run the provided tokens in the other nodes if you are running a cluster.\


<figure><img src="../.gitbook/assets/8" alt=""><figcaption></figcaption></figure>

**3-** _c-registry-1-create.sh_: It will create the registry service.

Docker Registry is also an application that we can run with docker, running it that way we can specify the path where to store data and set other configs.

Command to start up the registry service:

```shell
docker service create \
--name registry \
--limit-memory=64M \
--publish published=5000,target=5000,protocol=tcp,mode=host \
--mount type=bind,source=${PWD}/data-registry,destination=/var/lib/registry,readonly=false\
--constraint node.hostname==${PLACEMENT_REGISTRY} \
$REGISTRY_IMAGE
```

PWD, PLACEMENT\_REGISTRY and REGISTRY\_IMAGE are environment variables populated dynamically.

You can check that the service is running: _docker service ls_\


<figure><img src="../.gitbook/assets/9" alt=""><figcaption></figcaption></figure>

**4-** _c-registry-2-push-hub-images-sh_: The external images do not exist in the local registry yet, this script will push these images there.

**5-** _z-stack-3-build-reboot.sh_: The images will be built locally and pushed to the local registry, the script will deploy the stack and scale the containers up in order.

**NB:** Make sure you use a new terminal after the installation of JAVA.

**Other scripts**

* _z-stack-3-build-reboot.sh_: first, it will remove everything and then this will build and push and create needed directories such as logs and configurations (conf) and it will start everything again.
* _z-stack-2-reboot.sh_: This script will only remove everything and start again.
* _z-stack-1-build.sh_: This script will build and push everything to the local docker registry.

That's it 🚀

### Testing <a href="#_o7j185tztv31" id="_o7j185tztv31"></a>

**Check the deploy sanity**

To check for running containers you can run: _docker container ls_\
To check for running services you can run: _docker service ls_\
To list all the containers you can run: _docker ps -a_

Or you can go to docker/helper/scripts and run _d-stack-07-ps.sh_ and it will run:\
_docker stack ps \<NAME\_STACK>_

Example of the stack when running with local docker registry: _(docker stack ps JeMPI)_

![](../.gitbook/assets/10)

Example of the stack when running with docker hub: _(docker service ls)_

![](../.gitbook/assets/11)

**Stop or remove the stack**

To Kill everything \[including the registry]: you can go to _docker/_ and run _b-swarm-3-leave.sh_

To shutdown only: you can go to _docker/_ and run: _z-stack-4-down.sh_
