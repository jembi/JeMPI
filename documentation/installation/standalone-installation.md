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

Add an exception when installing with a non-root user account:

```
source "/home/${USER}/.sdkman/candidates/java/current"

```

To check the installation, you can check the version by running: _sdk version_

We should install the following:

- Maven: Command: _sdk install maven_\
  Version: 3.9.8
- Scala Build Tool: Command: _sdk install sbt_
- Java: Command: _sdk install java 17.0.6-tem_\
  Version: Temerin 21.0.3-tem (See list by running: _sdk list java_)

Check the version of java by running: _java --version_. We should get: Temurin-21.0.3+9.

## Starting JeMPI <a href="#_1yk2dvaqt5h9" id="_1yk2dvaqt5h9"></a>

In the following section, we will discuss the steps for running JeMPI on you machine, start by cloning the JeMPI repository on your machine and navigate to JeMPI by running the following command in you terminal of choice

```bash
git clone https://github.com/jembi/JeMPI.git && cd JeMPI/
```

### #1 Semi-Automated - Local Setup <a href="#_k8o7yc6w0hnu" id="_k8o7yc6w0hnu"></a>

#### Overview
This Bash script is designed for deploying JeMPI locally with various options. It performs tasks such as installing Docker, SDKMAN, Java, Maven, and SBT, setting up the environment configuration, creating a Docker registry, pulling and pushing Docker images, initializing the Docker Swarm, building the entire stack, rebooting, restarting, tearing down, Backup & Restore Databases and destroying JeMPI.

#### Usage
**Location of file** - JeMPI/devops/linux/docker/deployment \
**File Name** - local-deployment.sh

**Set following variables**\
JAVA_VERSION=21.0.3-tem \
JEMPI_ENV_CONFIGURATION=create-env-linux-low-1.sh

#### Installation Process

*This script must be run from the following path and will not work if executed from a different location* 

**Location of file** - JeMPI/devops/linux/docker/deployment

```bash
./local-deployment.sh
```

![Deployment Script Options](../.gitbook/assets/13)

**Option 1: Deploy JeMPI (For Fresh Start)** \
*This Option used to install JeMPI from Scratch or Fresh setup*
- Set up hostname and IP address in the Hosts file.
- Docker Swarm Initialization.
- Creates a Docker registry, pulls Docker images from the hub, and pushes them to the local registry.
- Builds and reboots the entire JeMPI stack

**Option 2: Build and Reboot**
- Builds and reboots the entire JeMPI stack.

**Option 3: Restart JeMPI**
- Reboots the entire JeMPI stack

**Option 4: Down JeMPI**
- Stop entire stack

**Option 5: Backup Postgres & Dgraph**
- Postgres backup process creates a folder with a timestamp, and inside it, SQL files are generated for each postgres database.
- Backup Directory: JeMPI/devops/linux/docker/docker_data/data/backups/postgres
- Dgraph backup process creates a folder with a timestamp, and inside it generates the Json file of data.
- Backup Directory: JeMPI/devops/linux/docker/docker_data/data/backups/dgraph

**Option 6: Restore Postgres & Dgraph**
- Users need to confirm with “ctr + Y” for restore.
- This process will wipe all existing data from both Postgres and Draph DB’s and restore new from backup.
- Users need to enter the folder name of the backup directory to initiate the restore process.

**Option 7: Re-Deploy JeMPI**
- Updates environment configuration settings
- Update HAProxy settings
- Pulls Docker images from the hub, and pushes them to the local registry.
- Builds and reboots the entire JeMPI stack

**Option 8: Install Prerequisites**
- Install SDKMAN - SDK Manager
- Install Docker
- Install Java, Maven, and SBT using SDKMAN

**Option 9: Destroy JeMPI (This process will wipe all data)**
- This process will remove all stack from swarm and leave the swarm.
- Remove all data and volumes

#### Notes
- The script prompts for user input to select an option.
- Confirmations are requested for critical actions.
- Use Ctrl+Y for "Yes" confirmation to Destroy all systems and Restore DB.
- Customize the script as needed for your specific deployment requirements.

### #2 Manual - Local Setup <a href="#_k8o7yc6w0hnu" id="_k8o7yc6w0hnu"></a>

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

Next, you will need to set up the hostname for your machine. to do so, run the command bellow, it will open the `hosts` file under `/etc/` directory using the `nano` text editor (you can use any other editor e.g. `VIM`, `VI`, `Emacs`, `Helix`, etc.) :

```bash
sudo vim /etc/hosts
```

Keep the localhost IP and Comment any other IP address, follow the screenshot bellow.

<figure><img src="../.gitbook/assets/12" alt=""><figcaption></figcaption></figure>

**Initialize the environment variables**\
In the JeMPI directory, navigate to: _docker/conf/env/_ directory.

```bash
cd /devops/linux/docker/conf/env
```

if you have less than 32Gbs of ram run the _./create-env-linux-low-1.sh_. If you have 32Gb or more, run the _./create-env-linux-high-1.sh_. both those script will create _conf.env_ file that we will need.

```bash
# If you have less than 32Gb ram, run the following script

./create-env-linux-low-1.sh

# If you have 32Gb ram or more, run the following script

./create-env-linux-high-1.sh
```

**Pull the latest images**

Pull the latest image versions form docker hub using the `a-images-1-pull-from-hub.sh`

**Make sure you have a clean docker swarm**

It is fine to keep the images, you can either remove all the services, containers, volumes, configs and secrets.

Run the `b-swarm-2-leave.sh` to leave your current swarm

```bash
./b-swarm-2-leave.sh
```

After running the previous script, initialize a new swarm by running the `b-swarm-1-init-node1.sh` script locacted in the _JeMPI/docker/_ directory.

```bash
./b-swarm-1-init-node1.sh
```

**Add the ability to use local registries**

Now, we need to tell docker that it is okay to run on the local registry because it is http and not https.

To read more about it check this link: [Test an insecure registry](https://docs.docker.com/registry/insecure/).

Go to _devops/linux/docker/helper/scripts._

Run _./x-swarm-a-set-insecure-registries.sh_ (you need to grant it executable access first by running: _chmod +x ./x-swarm-o-set-insecure-registries.sh_), it will edit the file _/etc/docker/daemon.json_ and will restart docker to make changes take effect.

NB: The script will edit the access grants of the _/etc/docker/daemon.json_ file.

```bash
./x-swarm-a-set-insecure-registries.sh
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

**Run the stack**

After pushing the images into the local registry, we are ready to run the app, we have several options, we can run the whole stack (UI + Backend) by running the `d-stack-1-build-all-reboot.sh`, Or run each of the backend (`d-stack-1-build-java-reboot.sh`) and the UI (`d-stack-1-build-ui-reboot`) seperatly.

```bash 
# build, push and run the whole stack (backend + ui)
./d-stack-1-build-all-reboot

# build, push and run the backend services only
./d-stack-1-build-java-reboot

# build, push and run the UI
./d-stack-1-build-ui-reboot
```

**Other scripts**

- _d-stack-2-build-java.sh_: This script will build and push the backend services to the local docker registry.
- _d-stack-3-down.sh_: This script will remove all services from the stack.
- _d-stack-3-reboot.sh_: This script will only remove everything and start again.

That's it 🚀

### Testing <a href="#_o7j185tztv31" id="_o7j185tztv31"></a>

**Check the deployment sanity**

To check for running containers you can run: _docker container ls_\
To check for running services you can run: _docker service ls_\
To list all the containers you can run: _docker ps -a_

Or you can go to devops/linux/docker/helper/scripts and run _d-stack-ps.sh_ and it will run:\
_docker stack ps \<NAME_STACK>_

Example of the stack when running with local docker registry: _(docker stack ps jempi)_

![Docker JeMPI Stack View](../.gitbook/assets/10)

Example of the stack when running with docker hub: _(docker service ls)_

![Docker Service List View](../.gitbook/assets/11)

**Stop or remove the stack**

To remove everything in the swarm: you can go to _devops/linux/docker/_ and run _b-swarm-2-leave.sh_

To shut down the stack: you can go to _devops/linux/docker/_ and run: _d-stack-3-down.sh_
