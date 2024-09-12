ARG JAVA_VERSION=21.0.1-tem

FROM eclipse-temurin:${JAVA_VERSION}-jre

ADD em-scala-fatjar-1.0.jar /app/em-scala-fatjar-1.0.jar

RUN printf "#!/bin/bash\n\
cd /app\n\
java -server -XX:MaxRAMPercentage=80 -jar /app/em-scala-fatjar-1.0.jar\n" > /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"] 
