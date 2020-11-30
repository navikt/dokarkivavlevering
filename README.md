# dokarkivavlevering

## Funksjonelt

Dette er en kubernetes batch [Job](https://kubernetes.io/docs/concepts/workloads/controllers/job/) som kan gjøre [NOARK5](https://www.arkivverket.no/forvaltning-og-utvikling/noark-standarden/noark-5/noark5-standarden#!#block-body-1) avleveringer fra fagarkivet Joark.

[Systemdokumentasjon](https://confluence.adeo.no/display/BOA/dokarkivavlevering?src=contextnavpagetreemode).

## Deploy

Jobben kjøres fra Github Actions.

* [dev-fss](https://github.com/navikt/dokarkivavlevering/actions?query=workflow%3A%22Deploy+dokarkivavlevering+%5Bdev-fss%5D%22)
* prod-fss

Velg `Run workflow`:
* Branch: master
* Log level: warning
* Periode start: YYYY-MM-DD
* Periode slutt: YYYY-MM-DD

## Utvikling

### Forutsetninger

* JDK 8+
* maven
* kubectl

### Bygg

Kjør kommando: `mvn clean package`

### Lokal kjøring og test:

Jobben vil gjøre en avlevering fra Q2 miljøet basert på properties i `application-local.properties`.

Kjør `Application.java` i IntelliJ med VM-options fra: https://vault.adeo.no/ui/vault/secrets/secret/show/dokument/dokarkivavlevering

* `avlevering.filomraade.work` - lokal arbeidsmappe for appen.

Avleveringen vil havne i mappen `/inbound/yyyy-MM-dd_HH_mm/avleveringspakke` på SFTP servern konfigurert i ENV verdi fra: https://vault.adeo.no/ui/vault/secrets/kv%2Fpreprod%2Ffss/list/dokarkivavlevering/ 

## Drift

### Tilganger

Tilgang til SFTP serveren i dev og prod bestilles fra drift. Se eksempel i [IKT-242931](https://jira.adeo.no/browse/IKT-242931).

Personlig tilgang til utviklere gis med personlig public key.

### Kubernetes

Man kan finne poddene til jobben med `kubectl`. Her kan man inspisere config og hente ut logger med vanlige `kubectl` kommandoer.

```
kubectl get po -l app=dokarkivavlevering -n=teamdokumenthandtering
```

Podden avslutter av seg selv etter kjøring.

## Henvendelser
Spørsmål rundt koden eller prosjektet kan rettes til Team Dokumentløsninger på:
* [\#Team Dokumentløsninger](https://nav-it.slack.com/client/T5LNAMWNA/C6W9E5GPJ)