# #!/bin/bash
# Set JEMPI_HOME environment variable
cd ../../../../
export JEMPI_HOME="$(pwd)"
echo "$JEMPI_HOME"
export JAVA_VERSION=21.0.3-tem
echo "Setting JEMPI_HOME to: $JEMPI_HOME"
JEMPI_ENV_CONFIGURATION=create-env-linux-low-1.sh

# Display menu options
echo "Select an option for local deployment:"
echo "1. Deploy JeMPI (For Fresh Start)."
echo "2. Build and Reboot."
echo "3. Restart JeMPI."
echo "4. Stop JeMPI."
echo "5. Backup Postgres & Dgraph."
echo "6. Restore Postgres & Dgraph."
echo "7. ReDeploy JeMPI"
echo "8. Install Prerequisites."
echo "9. Destroy JeMPI (This process will wipe all data and Volumes)."


# Prompt user for choice
read -p "Enter your choice (1-8): " choice

# Function to ask for confirmation
confirm() {
    read -p "Are you sure? (y/n): " response
    case "$response" in
        [yY]|[yY][eE][sS]) 
            return 0  # Return success (true) if confirmed
            ;;
        *)
            return 1  # Return failure (false) if not confirmed
            ;;
    esac
}

install_docker() {
    if command -v docker &> /dev/null; then
        echo "Docker is already installed."
    else
        # Install Docker
        echo "Installing Docker... "
        sudo apt-get update
        sudo apt-get install -y docker.io

        # Add your user to the docker group to run Docker without sudo
        sudo usermod -aG docker $USER

        echo "Docker has been installed."
    fi
}

# Function to install SDKMAN, Java, Maven, and SBT
install_sdkman_and_java_sbt_maven() {
    # Install SDKMAN
    echo "Installing SDKMAN... "
    curl -s "https://get.sdkman.io" | bash

    # Initialize SDKMAN
    source "$HOME/.sdkman/bin/sdkman-init.sh"

    # Install Java, Maven, and SBT using SDKMAN
    echo "Installing Java : $JAVA_VERSION ... "
    sdk install java $JAVA_VERSION
    sdk default java $JAVA_VERSION

    echo "Installing maven... "
    sdk install maven

    echo "Installing sbt... "
    sdk install sbt
}

hostname_setup() {
    pushd "$JEMPI_HOME/devops/linux/docker/deployment/" || exit
        echo "Setting up hostname & IP address in Hosts file"
        source hostname-setup.sh
    popd || exit
}

run_enviroment_configuration_and_helper_script(){
    # Navigate to environment configuration directory
    echo "Navigate to environment configuration directory"
    pushd "$JEMPI_HOME/devops/linux/docker/conf/env/" || exit
        # shellcheck source=path/to/create-env-linux-low-1.sh
        source "$JEMPI_ENV_CONFIGURATION"
    popd || exit

    # Running Docker helper scripts 
    echo "Running Docker helper scripts "
    pushd "$JEMPI_HOME/devops/linux/docker/helper/scripts/" || exit
        source x-swarm-a-set-insecure-registries.sh
    popd || exit
}

initialize_swarm(){
    if docker info | grep -q "Swarm: active"; then
        echo "Docker Swarm is running."
    else
        echo "Docker Swarm is not running."
        echo "Initialize Swarm on node1"
        pushd "$JEMPI_HOME/devops/linux/docker/deployment/common" || exit
            source b-swarm-1-init-node1.sh
        popd || exit
    fi
}

create_registry(){
    pushd "$JEMPI_HOME/devops/linux/docker/deployment/common" || exit
        echo "Create Docker registry"
        source c-registry-1-create.sh
    popd || exit

}

copy_ha_proxy(){
    pushd "$JEMPI_HOME/devops/linux/docker/" || exit
        source conf.env
        echo "Updating haproxy cfg file"
        cp conf/haproxy/*.* ${DATA_HAPROXY_DIR}
    popd || exit
}

pull_docker_images_and_push_local(){
    # Navigate to Docker directory
    pushd "$JEMPI_HOME/devops/linux/docker/deployment/common" || exit
        # Pull Docker images from hub
        echo "Pull Docker images from hub"
        source a-images-1-pull-from-hub.sh

        # Push Docker images to the registry
        echo "Push Docker images to the registry"
        source c-registry-2-push-hub-images.sh
    popd || exit
}

build_all_stack_and_reboot(){
    # Build and reboot the entire stack
    echo "Build and reboot the entire stack"
    pushd "$JEMPI_HOME/devops/linux/docker/deployment/build_and_reboot" || exit
        yes | source d-stack-1-build-all-reboot.sh
    popd || exit
}

initialize_db_build_all_stack_and_reboot(){
    echo "Create DB and Deploy"
    pushd "$JEMPI_HOME/devops/linux/docker/deployment/install_from_scratch" || exit
        yes | source d-stack-1-create-db-build-all-reboot.sh
    popd || exit
}

restore_db(){
    echo "Are you sure you want to restore the Dgraph and Postgres database? It will wipe all data and restore from backup (Ctrl+Y for Yes, any other key for No)"
    read -rsn1 -p "> " answer
        # Call the confirm function
       
    if [[ $answer == $'\x19' ]]; then
        pushd "$JEMPI_HOME/devops/linux/docker/backup_restore" || exit
            echo "Starting Dgraph database restore..."
            bash restore-dgraph-postgres.sh
            echo "Database Dgraph and Postgres restore completed."
        popd || exit
    else
        echo "Database restore cancelled. Moving ahead without restore."
        # Continue with the rest of your script
    fi
}

# Process user choice
case $choice in
    1)
        echo "Deploy JeMPI With Fresh Start"
        hostname_setup
        run_enviroment_configuration_and_helper_script
        initialize_swarm
        create_registry
        pull_docker_images_and_push_local
        initialize_db_build_all_stack_and_reboot
        ;;
    2)
        echo "Build and Reboot"
        build_all_stack_and_reboot
        ;;
    3)
        echo "Restart JeMPI"
        pushd "$JEMPI_HOME/devops/linux/docker/deployment/reboot"
            source d-stack-3-reboot.sh
        popd
        ;;
    4)
        echo "Stop JeMPI"
        pushd "$JEMPI_HOME/devops/linux/docker/deployment/down"
            source d-stack-3-down.sh
        popd
        exit 0
        ;;
    5)
        BACKUP_DATE_TIME=$(date +%Y-%m-%d_%H%M%S)
        echo "Started Backup at- $BACKUP_DATE_TIME"
        pushd "$JEMPI_HOME/devops/linux/docker/backup_restore" || exit
            source dgraph-backup-api.sh "$BACKUP_DATE_TIME" || { echo "Dgraph backup failed"; exit 1; }
            sudo bash postgres-backup.sh "$BACKUP_DATE_TIME" || { echo "Postgres backup failed"; exit 1; }
        popd || exit
        
        ;;
    6)
        echo "Restore Databases"
        restore_db
        ;;
    7)
        echo "Re Deploy JeMPI"
        run_enviroment_configuration_and_helper_script
        copy_ha_proxy
        while true; do
            read -p "Do you want to get the latest docker images? " yn
            case $yn in
                [Yy]* )
                pull_docker_images_and_push_local
                break;;
                [Nn]* ) break;;
                * ) echo "Please answer yes or no.";;
            esac
        done
        build_all_stack_and_reboot
        ;;
    8)
        echo "Deploy JeMPI from Scratch"
        install_docker
        install_sdkman_and_java_sbt_maven
        ;;
    9)
        echo "Destroy"
        # Main script
        echo "Do you want to continue? (Ctrl+Y for Yes, any other key for No)"
        read -rsn1 -p "> " answer
        # Call the confirm function
        if [[ $answer == $'\x19' ]]; then
            pushd "$JEMPI_HOME/devops/linux/docker"
                echo "You confirmed. Proceeding with Destroy JeMPI."
                source b-swarm-2-leave.sh
            popd
        else
            echo "You did not confirm. Exiting without performing the critical action."
        fi
        exit 0
        ;;
    *)
        echo "Invalid choice. Please enter a number."
        ;;
esac