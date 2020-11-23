#!/usr/bin/env sh

# serviceuser
if test -f /var/run/secrets/nais.io/service_user/username;
then
    echo "Setting AVLEVERING_SERVICEUSER_USERNAME"
    export AVLEVERING_SERVICEUSER_USERNAME=$(cat /var/run/secrets/nais.io/service_user/username)
fi
if test -f /var/run/secrets/nais.io/service_user/password;
then
    echo "Setting AVLEVERING_SERVICEUSER_PASSWORD"
    export AVLEVERING_SERVICEUSER_PASSWORD=$(cat /var/run/secrets/nais.io/service_user/password)
fi
# database
if test -f /var/run/secrets/nais.io/database_user/username;
then
    echo "Setting SPRING_DATASOURCE_USERNAME"
    export SPRING_DATASOURCE_USERNAME=$(cat /var/run/secrets/nais.io/database_user/username)
fi
if test -f /var/run/secrets/nais.io/database_user/password;
then
    echo "Setting SPRING_DATASOURCE_PASSWORD"
    export SPRING_DATASOURCE_PASSWORD=$(cat /var/run/secrets/nais.io/database_user/password)
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
if test -f /var/run/secrets/nais.io/ldap/username;
then
    echo "Setting SPRING_LDAP_USERNAME"
    export  SPRING_LDAP_USERNAME=$(cat /var/run/secrets/nais.io/service_user_linux/username)
fi
if test -f /var/run/secrets/nais.io/ldap/password;
then
    echo "Setting SPRING_LDAP_PASSWORD"
    export  SPRING_LDAP_PASSWORD=$(cat /var/run/secrets/nais.io/service_user_linux/password)
fi