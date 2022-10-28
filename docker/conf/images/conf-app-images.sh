JAVA_VERSION=17.0.4.1

# https://registry.hub.docker.com/r/azul/zulu-openjdk
#export JAVA_BASE_IMAGE=azul/zulu-openjdk:$JAVA_VERSION

# https://hub.docker.com/_/eclipse-temurin/tags
export JAVA_BASE_IMAGE=eclipse-temurin:${JAVA_VERSION}_1-jre

export JOURNAL_IMAGE=journal:1.0-SNAPSHOT
export JOURNAL_JAR=Journal-1.0-SNAPSHOT-spring-boot.jar

export NOTIFICATIONS_IMAGE=notifications:1.0-SNAPSHOT
export NOTIFICATIONS_JAR=Notifications-1.0-SNAPSHOT-spring-boot.jar

export TEST_01_IMAGE=test_01:1.0-SNAPSHOT

export STAGING_01_IMAGE=staging_01:1.0-SNAPSHOT
export STAGING_01_JAR=Staging_01-1.0-SNAPSHOT-spring-boot.jar

export INPUT_02_IMAGE=input-02:1.0-SNAPSHOT
export INPUT_02_JAR=Input_02-1.0-SNAPSHOT-spring-boot.jar

export STAGING_02_IMAGE=staging-02:1.0-SNAPSHOT
export STAGING_02_JAR=Staging_02-1.0-SNAPSHOT-spring-boot.jar

export INPUT_DISI_IMAGE=input-disi:1.0-SNAPSHOT
export INPUT_DISI_JAR=InputDISI-1.0-SNAPSHOT-spring-boot.jar

export STAGING_DISI_IMAGE=staging-disi:1.0-SNAPSHOT
export STAGING_DISI_JAR=StagingDISI-1.0-SNAPSHOT-spring-boot.jar

export CONTROLLER_IMAGE=controller:1.0-SNAPSHOT
export CONTROLLER_JAR=Controller-1.0-SNAPSHOT-spring-boot.jar

export EM_IMAGE=em:1.0-SNAPSHOT
export EM_JAR=EM-1.0-SNAPSHOT-spring-boot.jar

export LINKER_IMAGE=linker:1.0-SNAPSHOT
export LINKER_JAR=Linker-1.0-SNAPSHOT-spring-boot.jar

export API_IMAGE=api:1.0-SNAPSHOT
export API_JAR=API-1.0-SNAPSHOT-spring-boot.jar
