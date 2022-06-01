FROM navikt/java:11
USER root
RUN apt-get update && apt-get install -y libfreetype6 fontconfig fonts-liberation
USER apprunner
COPY app/target/app.jar /app/app.jar
COPY export-vault-secrets.sh /init-scripts/50-export-vault-secrets.sh
ENV JAVA_OPTS="-Xmx1024m \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=nais"