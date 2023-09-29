---
description: Useful for development or custom implementations
---

# Standalone Installation

## Prerequisites <a href="#_4a1yqprqttyw" id="_4a1yqprqttyw"></a>

In the following, we will introduce the software prerequisites to be able to run the JeMPI client registry on your machine.

### Docker <a href="#_rglemv3fug4d" id="_rglemv3fug4d"></a>

Please refer to the [official installation guide](https://docs.docker.com/engine/install/ubuntu/) in order to install docker on your machine.
It is best to follow the post installation process so that you grant docker sudo access.

### \[for Windows users] WSL2 <a href="#_ku1gqbfaemlj" id="_ku1gqbfaemlj"></a>

Installing WSL2 is required to be able to develop and test the project.\
It is recommended to limit the memory usage of WSL2.

### Build Utilities <a href="#_ruowqnawk4n7" id="_ruowqnawk4n7"></a>

Follow the steps to install sdk [sdkmanager](https://sdkman.io/), check if you already have it by running _sdk_.\
To install it, run the two following commands:

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

To check the installation, you can check the version by running: _sdk version_

We should install the following:

- Maven: Command: _sdk install maven_\
  Version: 3.8.6
- Scala Build Tool: Command: _sdk install sbt_
- Java: Command: _sdk install java 17.0.6-tem_\
  Version: Temerin 17.0.8-tem (See list by running: _sdk list java_)

Check the version of java by running: _java --version_. We should get: Temurin-17.0.8+7.

## Starting JeMPI <a href="#_1yk2dvaqt5h9" id="_1yk2dvaqt5h9"></a>

In the following section, we will discuss the steps for running JeMPI on you machine, start by cloning the JeMPI repository on your machine and navigate to JeMPI by running the following command in you terminal of choice

```bash
git clone https://github.com/jembi/JeMPI.git && cd JeMPI/
```

When the execution of the command is complete, you can choose between running JeMPI without a local registry, or run it with a local registry.

### #1 Run JeMPI without local registry <a href="#_k8o7yc6w0hnu" id="_k8o7yc6w0hnu"></a>

Run in the terminal _./launch-local.sh_

```bash
./launch-local.sh

# or

bash launch-local.sh
```

[//]: # "You can **run** the script in [**this link**](https://github.com/jembi/JeMPI/pull/15/files) **OR** follow these steps below:"

### #2 Run JeMPI with a local registry <a href="#_k8o7yc6w0hnu" id="_k8o7yc6w0hnu"></a>

**Setup an IP address**\
Before starting the process of running JeMPI, you will need to setup an IP address for your machine.

On your terminal of choice, run the `ip a` command and retrieve the ip address from your wi-fi or ethernet interfaces

```bash
skunk@skunks-server:~$ ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
2: enp0s3: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 08:00:27:bf:f5:e2 brd ff:ff:ff:ff:ff:ff
    inet 192.168.1.137/24 metric 100 brd 192.168.1.255 scope global dynamic enp0s3
       valid_lft 86346sec preferred_lft 86346sec
    inet6 fdc2:75c1:449e:9200:a00:27ff:febf:f5e2/64 scope global dynamic mngtmpaddr noprefixroute
       valid_lft 7145sec preferred_lft 3545sec
    inet6 fe80::a00:27ff:febf:f5e2/64 scope link
       valid_lft forever preferred_lft forever
```

In our case, we are using the `enp0s3` interface, the IP address that we will need is `192.168.1.137`.

Next, you will need to setup the hostname for your machine. to do so, run the command bellow, it will open the `hosts` file under `/etc/` directory using the `nano` text editor (you can use any other editor e.g. `VIM`, `VI`, `Emacs`, `Helix`, etc.) :

```bash
sudo vim /etc/hosts
```

Keep the localhost IP and Comment any other IP address, follow the screenshot bellow.

<figure><img src="../.gitbook/assets/12" alt=""><figcaption></figcaption></figure>

**Initialize the environment variables**\
In the JeMPI directory, navigate to: _docker/conf/env/_ directory.

```bash
cd /docker/conf/env
```

if you have less than 32Gbs of ram run the _./create-env-linux-low-1.sh_. If you have 32Gb or more, run the _./create-env-linux-high-1.sh_. both those script will create _conf.env_ file that we will need.

```bash
# If you have less than 32Gb ram, run the follwing script

./create-env-linux-low-1.sh

# If you have 32Gb ram or more, run the follwing script

./create-env-linux-high-1.sh
```

**Pull the latest images**

Pull the latest image versions form docker hub using the `a-images-1-pull-from-hub.sh`

**Make sure you have a clean docker swarm**

It is fine to keep the images, you can either remove all the services, containers, volumes, configs and secrets.

Run the `b-swarm-3-leave.sh` to leave your current swarm

```bash
./b-swarm-3-leave.sh
```

After runing the previous script, initialize a new swarm by running the `b-swarm-1-init-node1.sh` script locacted in the _JeMPI/docker/_ directory.

```bash
./b-swarm-1-init-node1.sh
```

**Add the ability to use local regestries**

Now, we need to tell docker that it is okay to run on the local registry because it is http and not https \[not secure].

To read more about it check this link: [Test an insecure registry](https://docs.docker.com/registry/insecure/).

Go to _docker/helper/scripts._

Run _./x-swarm-o-set-insecure-registries.sh_ (you need to grant it executable access first by running: _chmod +x ./x-swarm-o-set-insecure-registries.sh_), it will edit the file _/etc/docker/daemon.json_ and will restart docker to make changes take effect.

NB: The script will edit the access grants of the _/etc/docker/daemon.json_ file.

```bash
./x-swarm-o-set-insecure-registries.sh
```

**Create a local registry**
Now that you can use local Docker registries, run the `c-registry-1-create.sh` script to create a registry service. This service will host the docker images that we will use in our stack.

```bash
./c-registry-1-create.sh
```

**Push the images to the local registry**
We will need to pull images from docker hub then push them to the local registry :

```bash
./a-images-1-pull-from-hub.sh
./c-registry-2-push-hub-images.sh
```

**Generate patient record fields configuration reference**
The fields configuration reference is a json file that allows JeMPI to generate custom Java classes based on the fields but also the way we would to configure the matching algorithms.

```bash
cd JeMPI_Apps/JeMPI_Configuration/
./create.sh reference/config-reference.json
```
**Run the stack**

After pushing the images into the local registry, we are ready to run the app, we have several options, we can run the whole stack (UI + Backend) by running the `z-stack-3-build-all-reboot.sh`, Or run each of the backend (`z-stack-3-build-java-reboot.sh`) and the UI (`z-stack-3-build-ui-reboot`) seperatly.

```bash 
# build, push and run the whole stack (backend + ui)
./z-stack-3-build-all-reboot

# build, push and run the backend services only
./z-stack-3-build-java-reboot

# build, push and run the UI
./z-stack-3-build-ui-reboot
```

**Other scripts**

- _z-stack-1-build-java.sh_: This script will build and push the backend services to the local docker registry.
- _z-stack-1-build-ui.sh_: This script will build and push the UI service to the local docker registry.
- _z-stack-2-reboot.sh_: This script will only remove everything and start again.

That's it 🚀

### Testing <a href="#_o7j185tztv31" id="_o7j185tztv31"></a>

**Check the deploy sanity**

To check for running containers you can run: _docker container ls_\
To check for running services you can run: _docker service ls_\
To list all the containers you can run: _docker ps -a_

Or you can go to docker/helper/scripts and run _d-stack-07-ps.sh_ and it will run:\
_docker stack ps \<NAME_STACK>_

Example of the stack when running with local docker registry: _(docker stack ps JeMPI)_

![](../.gitbook/assets/10)

Example of the stack when running with docker hub: _(docker service ls)_

![](../.gitbook/assets/11)

**Stop or remove the stack**

To Kill everything \[including the registry]: you can go to _docker/_ and run _b-swarm-3-leave.sh_

To shutdown only: you can go to _docker/_ and run: _z-stack-4-down.sh_
