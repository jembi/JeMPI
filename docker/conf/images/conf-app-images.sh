JAVA_VERSION=17.0.4.1

# https://registry.hub.docker.com/r/azul/zulu-openjdk
export JAVA_BASE_IMAGE=azul/zulu-openjdk:$JAVA_VERSION

export JOURNAL_IMAGE=journal:1.0-SNAPSHOT
export JOURNAL_JAR=Journal-1.0-SNAPSHOT-spring-boot.jar

export NOTIFICATIONS_IMAGE=notifications:1.0-SNAPSHOT
export NOTIFICATIONS_JAR=Notifications-1.0-SNAPSHOT-spring-boot.jar

export TEST_01_IMAGE=test_01:1.0-SNAPSHOT

export STAGING_01_IMAGE=staging_01:1.0-SNAPSHOT

export CONTROLLER_IMAGE=controller:1.0-SNAPSHOT

export EM_IMAGE=em:1.0-SNAPSHOT

export LINKER_IMAGE=linker:1.0-SNAPSHOT
export LINKER_JAR=Linker-1.0-SNAPSHOT-spring-boot.jar

export API_IMAGE=api:1.0-SNAPSHOT
