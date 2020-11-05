#!/usr/bin/env sh

if test -f /secrets/serviceuser/srvdokavlevering/username;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /secrets/serviceuser/srvdokavlevering/username)
fi
if test -f /secrets/serviceuser/srvdokavlevering/password;
then
    echo "Setting serviceuser_password"
    export  serviceuser_password=$(cat /secrets/serviceuser/srvdokavlevering/password)
fi