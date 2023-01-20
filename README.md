# JeMPI

![Untitled design](https://user-images.githubusercontent.com/41700488/158391814-b78219dc-0359-4024-b7bd-2dec792b5b15.png)

The Jembi MPI, also known as JeMPI, is a standards-based client registry (CR) or master patient index (MPI). JeMPI facilitates the exchange of patient information between different systems and holds patient identifiers that may include patient demographic information. This is a necessary tool for public health to help manage patients, monitor outcomes, and conduct case-based surveillance. JeMPI’s primary goal is to act as a tool in order to solve the issue of multiple or duplicated patient records that are submitted from multiple point of service systems such as electronic medical records, lab systems, radiology systems and other health information systems. This is achieved by matching the various patient records from different systems under a Master Patient record with a unique ID. This allows for downstream applications, such as surveillance, to accurately display data and information on patient records without the worry that the data contains multiple records for the same patient.

### Requirements
- linux (bash)
  - docker (non-root)
  - maven
  - sbt
  - java 17.0.4.1
  - python 3.7
    - wxpython
    - requests

### Installation
- Directory structure
  - \<base>
    - JeMPI           - ```git@github.com:jembi/JeMPI.git```
    - JeMPI_TestData  - ```git@github.com:jembi/JeMPI_TestData.git```
- Requirements
  - ```ping `hostname` ``` must ping a LAN IP address (not 127.x.x.x) 
- Run
  1. **_\<base>/JeMPI/docker/conf/env_**
     1. ```./create-env-linux-1-64.sh```
  2. **_\<base>/JeMPI/docker/helper/scripts_**
     1. ```bash ./x-swarm-a-set-insecure-registries.sh```
        - this clobbers **_/etc/docker/daemon.json_**   
  3. **_\<base>/JeMPI/docker_**
     1. ```./a-images-1-pull-from-hub.sh```
     2. ```./b-swarm-1-init-node1.sh```
     3. ```./c-registry-1-create.sh```
     4. ```./c-registry-2-push-hub-images.sh```
     5. ```./z-stack-3-build-reboot.sh```

# Development
It's possible to run the whole stack local without having to use a local registry using the command : `./launch-local.sh`
