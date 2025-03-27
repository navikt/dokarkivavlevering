#!/usr/bin/env sh

# database
if test -f /var/run/secrets/nais.io/db_config/jdbc_url;
then
    export SPRING_DATASOURCE_URL=$(cat /var/run/secrets/nais.io/db_config/jdbc_url)
    echo "Setting SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL"
fi

if test -f /var/run/secrets/nais.io/db_creds/username;
then
    echo "Setting SPRING_DATASOURCE_USERNAME"
    export SPRING_DATASOURCE_USERNAME=$(cat /var/run/secrets/nais.io/db_creds/username)
fi
if test -f /var/run/secrets/nais.io/db_creds/password;
then
    echo "Setting SPRING_DATASOURCE_PASSWORD"
    export SPRING_DATASOURCE_PASSWORD=$(cat /var/run/secrets/nais.io/db_creds/password)
fi
# sftp
if test -f /var/run/secrets/nais.io/vault/privateKeyFile;
then
    echo "Setting SFTP_PRIVATEKEYFILE"
    export SFTP_PRIVATEKEYFILE=/var/run/secrets/nais.io/vault/privateKeyFile
fi
if test -f /var/run/secrets/nais.io/vault/privateKeyPassphrase;
then
    echo "Setting SFTP_PRIVATEKEYPASSPHRASE"
    export SFTP_PRIVATEKEYPASSPHRASE=$(cat /var/run/secrets/nais.io/vault/privateKeyPassphrase)
fi
if test -f /var/run/secrets/nais.io/vault/asposeLicense;
then
    echo "Setting aspose license"
    export AVLEVERING_ASPOSELICENSE=$(cat /var/run/secrets/nais.io/vault/asposeLicense)
fi
if test -f /var/run/secrets/nais.io/ldap/username;
then
    echo "Setting SPRING_LDAP_USERNAME"
    export  SPRING_LDAP_USERNAME=$(cat /var/run/secrets/nais.io/ldap/username)
fi
if test -f /var/run/secrets/nais.io/ldap/password;
then
    echo "Setting SPRING_LDAP_PASSWORD"
    export  SPRING_LDAP_PASSWORD=$(cat /var/run/secrets/nais.io/ldap/password)
fi