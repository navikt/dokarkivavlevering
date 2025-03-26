FROM ghcr.io/navikt/baseimages/temurin:21
USER root
RUN sed -i "s#deb http://deb.debian.org/debian bullseye main#deb http://deb.debian.org/debian bullseye main contrib non-free#g" /etc/apt/sources.list
RUN apt-get update && apt-get install -y libfreetype6 fontconfig ttf-mscorefonts-installer
USER apprunner
COPY app/target/app.jar /app/app.jar
COPY export-vault-secrets.sh /init-scripts/50-export-vault-secrets.sh
ENV JAVA_OPTS="-Xmx2048m \
               -Djava.security.egd=file:/dev/./urandom"