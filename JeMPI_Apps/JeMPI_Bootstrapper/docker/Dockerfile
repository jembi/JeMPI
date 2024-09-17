ARG JAVA_VERSION=21.0.1-tem

FROM eclipse-temurin:${JAVA_VERSION}-jre

ADD Bootstrapper-1.0-SNAPSHOT-spring-boot.jar /app/Bootstrapper-1.0-SNAPSHOT-spring-boot.jar

RUN printf "#!/bin/bash\n\
cd /app\n\
java_args=\"\${@:1}\" \n\
java --enable-preview -XX:MaxRAMPercentage=80 -XX:+UseZGC -jar /app/Bootstrapper-1.0-SNAPSHOT-spring-boot.jar \$java_args \n" > /bootstrapper.sh

RUN chmod +x /bootstrapper.sh

ENTRYPOINT tail -f /dev/null
