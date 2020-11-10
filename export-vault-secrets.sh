#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/service_user/username;
then
    echo "Setting avlevering_serviceuser_username"
    export avlevering_serviceuser_username=$(cat /var/run/secrets/nais.io/service_user/username)
fi
if test -f /var/run/secrets/nais.io/service_user/password;
then
    echo "Setting avlevering_serviceuser_password"
    export avlevering_serviceuser_password=$(cat /var/run/secrets/nais.io/service_user/password)
fi