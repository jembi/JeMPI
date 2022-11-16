FROM eclipse-temurin:17.0.4.1_1-jre

ADD Controller-1.0-SNAPSHOT-spring-boot.jar /app/Controller-1.0-SNAPSHOT-spring-boot.jar

RUN printf "#!/bin/bash\n\
cd /app\n\
java -server --enable-preview -XX:MaxRAMPercentage=80 -XX:+UseZGC \
 -javaagent:/opt/prometheus/jmx_prometheus_javaagent-0.17.2.jar=50021:/opt/prometheus/controller_prometheus.yml \
 -jar /app/Controller-1.0-SNAPSHOT-spring-boot.jar\n" > /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"] 
