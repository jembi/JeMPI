GlobalKTable Tests
==================

To run these test you need to have a kafka instance running on you machine on port 9097
Below is a docker-compose file you can use as a sample kafka instance

```yaml
version: "3.8"

services:
  kafka:
    image: docker.io/bitnami/kafka:3.6
    hostname: kafka0
    container_name: kafka0
    ports:
      - "9097:9097"
    volumes:
      - "kafka_data:/bitnami"
    environment:
      # KRaft settings
      - KAFKA_CFG_NODE_ID=0
      - KAFKA_CFG_PROCESS_ROLES=controller,broker
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@localhost:9093
      # Listeners
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9097,CONTROLLER://:9093,PLAINTEXT_OTHER://:29092
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT_OTHER://kafka0:29092,PLAINTEXT://localhost:9097
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_OTHER:PLAINTEXT
      - KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER
      - KAFKA_CFG_INTER_BROKER_LISTENER_NAME=PLAINTEXT
  kafka-ui:
    container_name: kafka-ui
    image: provectuslabs/kafka-ui:latest
    ports:
      - 7474:8080
    environment:
      DYNAMIC_CONFIG_ENABLED: 'true'
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka0:29092
    depends_on:
      - kafka
volumes:
  kafka_data:
    driver: local
```