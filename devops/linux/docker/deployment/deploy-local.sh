# #!/bin/bash
# Set JEMPI_HOME environment variable
cd ../../../../
export JEMPI_HOME="$(pwd)"
echo "$JEMPI_HOME"
export JAVA_VERSION=21.0.1-tem
echo "Setting JEMPI_HOME to: $JEMPI_HOME"
JEMPI_CONFIGURATION_PATH=$JEMPI_HOME/JeMPI_Apps/JeMPI_Configuration/reference/config-reference.json
JEMPI_ENV_CONFIGURATION=create-env-linux-low-1.sh

down_dir="$JEMPI_HOME/devops/linux/docker/deployment/down"
reboot_dir="$JEMPI_HOME/devops/linux/docker/deployment/reboot"
backup_restore_dir="$JEMPI_HOME/devops/linux/docker/backup_restore"

python_cmd=$(which python3 || which python)
echo $python_cmd

# Display menu options
echo "Select an option for local deployment:"
echo "1. Deploy JeMPI from Scratch (With all installations...)."
echo "2. Deploy JeMPI without installations"
echo "3. Build and Reboot."
echo "4. Restart JeMPI."
echo "5. Down the JeMPI."
echo "6. Backup Postgres & Dgraph."
echo "7. Restore Postgres & Dgraph."
echo "8. Destroy JeMPI (This process will wipe all data)."


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
    pushd "$JEMPI_HOME/devops/linux/docker/deployment/"
        echo "Setting up hostname & IP address in Hosts file"
        source hostname-setup.sh
    popd
}

run_enviroment_configuration_and_helper_script(){
    # Navigate to environment configuration directory
    echo "Navigate to environment configuration directory"
    pushd "$JEMPI_HOME/devops/linux/docker/conf/env/"
        source $JEMPI_ENV_CONFIGURATION
    popd    

    # Running Docker helper scripts 
    echo "Running Docker helper scripts "
    pushd "$JEMPI_HOME/devops/linux/docker/helper/scripts/"
        source x-swarm-a-set-insecure-registries.sh
    popd
}

run_field_configuration_file() {
    # Running Docker helper scripts
    echo "Running JeMPI configuration with path: $JEMPI_CONFIGURATION_PATH"
    pushd "$JEMPI_HOME/JeMPI_Apps/JeMPI_Configuration/"
        source create.sh $JEMPI_CONFIGURATION_PATH
    popd
}

initialize_swarm(){
    if docker info | grep -q "Swarm: active"; then
        echo "Docker Swarm is running."
    else
        echo "Docker Swarm is not running."
        echo "Initialize Swarm on node1"
        pushd "$JEMPI_HOME/devops/linux/docker/deployment/common"
            source b-swarm-1-init-node1.sh
        popd
    fi
}

pull_docker_images_and_push_local(){
    # Navigate to Docker directory
    pushd "$JEMPI_HOME/devops/linux/docker/deployment/common"
        echo "Create Docker registry"
        source c-registry-1-create.sh

        # Pull Docker images from hub
        echo "Pull Docker images from hub"
        source a-images-1-pull-from-hub.sh

        # Push Docker images to the registry
        echo "Push Docker images to the registry"
        source c-registry-2-push-hub-images.sh
    popd
}
build_all_stack_and_reboot(){
    # run_enviroment_configuration_and_helper_script
    run_field_configuration_file
    # Build and reboot the entire stack
    echo "Build and reboot the entire stack"
    pushd "$JEMPI_HOME/devops/linux/docker/deployment/build_and_reboot"
        yes | source d-stack-1-build-all-reboot.sh
    popd
}
initialize_db_build_all_stack_and_reboot(){
    echo "Create DB and Deploy"
    pushd "$JEMPI_HOME/devops/linux/docker/deployment/from_scratch"
        yes | source d-stack-1-create-db-build-all-reboot.sh
    popd
}
restore_dgraph_db(){
    echo "Are you sure you want to restore the Dgraph database? (yes/no)"
    read dgraph_confirmation
    dgraph_confirmation=$(echo "$dgraph_confirmation" | tr '[:upper:]' '[:lower:]')

    if [ "$dgraph_confirmation" == "yes" ] || [ "$dgraph_confirmation" == "y" ]; then
        pushd "$JEMPI_HOME/devops/linux/docker/backup_restore"
            echo "Starting Dgraph database restore..."
            bash dgraph-restore.sh
            echo "Database Dgraph restore completed."
        popd
    else
        echo "Dgraph Database restore cancelled. Moving ahead without restore."
 cd de         # Continue with the rest of your script
    fi
}
restore_postgres_db(){
    echo "Are you sure you want to restore the Postgres database? (yes/no)"
    read postgres_confirmation
    postgres_confirmation=$(echo "$postgres_confirmation" | tr '[:upper:]' '[:lower:]')

    if [ "$postgres_confirmation" == "yes" ] || [ "$postgres_confirmation" == "y" ]; then
        pushd "$JEMPI_HOME/devops/linux/docker/backup_restore"
            echo "Starting Postgres database restore..."
            sudo bash postgres-restore.sh
            echo "Database Postgres restore completed."
        popd
        
    else
        echo "Postgres Database restore cancelled. Moving ahead without restore."
        # Continue with the rest of your script
    fi
}

# Function to stop services
stop_services() {
    pushd "$down_dir"
    echo "Stopping API service"
    source d-stack-stop-services.sh
    popd
}

# Function to start backup restore API service
start_backup_restore_service() {
    pushd "$reboot_dir"
    echo "Starting Backup Restore API service"
    source d-stack-start-backup-restore-api-services.sh
    popd
}

# Function to backup data
backup_data() {
    pushd "$backup_restore_dir"
    sleep 20
    echo "Started Backup through API"
    $python_cmd dgraph-backup.py
    sleep 10
    # sudo bash dgraph-backup.sh
    # sudo bash postgres-backup.sh
    popd
}

# Function to start services
start_services() {
    pushd "$reboot_dir"
    echo "Starting API service"
    source d-stack-start-services.sh
    popd
}

# Function to stop backup restore API service
stop_backup_restore_service() {
    pushd "$down_dir"
    echo "Stopping Backup Restore API service"
    source d-stack-stop-backup-restore-api-services.sh
    popd
}




# Process user choice
case $choice in
    1)
        echo "Deploy JeMPI from Scratch"
        install_docker
        install_sdkman_and_java_sbt_maven
        hostname_setup
        run_enviroment_configuration_and_helper_script
        run_field_configuration_file
        initialize_swarm
        pull_docker_images_and_push_local
        initialize_db_build_all_stack_and_reboot
        ;;
    2)
        echo "Deploy JeMPI"
        hostname_setup
        run_enviroment_configuration_and_helper_script
        run_field_configuration_file
        initialize_swarm
        pull_docker_images_and_push_local
        initialize_db_build_all_stack_and_reboot
        ;;
    3)
        echo "Build and Reboot"
        build_all_stack_and_reboot
        ;;
    4)
        echo "Restart JeMPI"
        pushd "$JEMPI_HOME/devops/linux/docker/deployment/reboot"
            source d-stack-3-reboot.sh
        popd
        ;;
    5)
        echo "Down JeMPI"
        pushd "$JEMPI_HOME/devops/linux/docker/deployment/down"
            source d-stack-3-down.sh
        popd
        exit 0
        ;;
    6)
        echo "Backup Using Backup Restore API"
        
        stop_services
        start_backup_restore_service
        backup_data
        start_services
        stop_backup_restore_service
        
        ;;
    7)
        echo "Restore Databases"
        restore_postgres_db
        restore_dgraph_db
        ;;
    8)
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