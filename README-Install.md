# JeMPI Local Deployment Script


## Overview


This Bash script is designed for deploying JeMPI locally with various options. It performs tasks such as installing Docker, SDKMAN, Java, Maven, and SBT, setting up the environment configuration, creating a Docker registry, pulling and pushing Docker images, initializing the Docker Swarm, building the entire stack, rebooting, restarting, tearing down, Backup & Restore Databases and destroying JeMPI.


## Usage


1. **Set JEMPI_HOME Environment Variable:**
   ```bash
   Location of file - JeMPI/devops/linux/docker/deployment
   File Name - local-deployment.sh

   export JAVA_VERSION=21.0.1-tem
   JEMPI_CONFIGURATION_PATH=$JEMPI_HOME/JeMPI_Apps/JeMPI_Configuration/reference/config-reference.json

   ```


2. **Run the Script:**
   ```bash
   Location of file - JeMPI/devops/linux/docker/deployment
   bash local-deployment.sh
   ```


3. **Select an Option:**
   - **Option 1:** Deploy JeMPI from Scratch (With all installations).
   - **Option 2:** Deploy JeMPI without installations.
   - **Option 3:** Build and Reboot.
   - **Option 4:** Restart JeMPI.
   - **Option 5:** Down JeMPI.
   - **Option 6:** Backup Postgres & Dgraph.
   - **Option 7:** Restore Postgres & Dgraph.
   - **Option 8:** Destroy JeMPI (This process will wipe all data).



## Backup and Restore Functionality

### Postgres

**Backup:**
- **Backup Directory:** `JeMPI/devops/linux/docker/docker_data/data/backups/postgres`
- The backup process creates a folder with a timestamp, and inside it, SQL files are generated for each database.

**Restore:**
- Users need to enter the folder name of the backup directory for Postgres to initiate the restore process.

### Dgraph

**Backup:**
- **Backup Directory:** `JeMPI/devops/linux/docker/docker_data/data/backups/dgraph`
- The backup process creates a folder with a timestamp, and inside it, folders are copied for each Dgraph node.

**Restore:**
- Users need to enter the folder name of the backup directory for Dgraph

## Script Functions


### install_docker()


Check if Docker is installed and install it if not.


### install_sdkman_and_java_sbt_maven()


Installs SDKMAN, Java, Maven, and SBT using SDKMAN.


### hostname_setup()


Sets up hostname and IP address in the Hosts file.


### run_enviroment_configuration_and_helper_script()


Navigate to the environment configuration directory, run environment configuration scripts, and Docker helper scripts.


### run_field_configuration_file()


Runs JeMPI configuration with the specified configuration file path.


### initialize_swarm()


Check if the Docker Swarm is running and initialize it if not.


### pull_docker_images_and_push_local()


Creates a Docker registry, pulls Docker images from the hub, and pushes them to the local registry.


### build_all_stack_and_reboot()


Builds and reboots the entire JeMPI stack.


### initialize_db_build_all_stack_and_reboot()


Creates a database, builds the entire JeMPI stack, and reboots.


## Notes


- The script prompts for user input to select an option.
- Confirmations are requested for critical actions.
- Use Ctrl+Y for "Yes" confirmation to Destroy all systems.
- Customize the script as needed for your specific deployment requirements.
