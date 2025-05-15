# Dokarkivavlevering

Dokarkivavlevering tilbyr følgende Nais-jobber for å oppfylle [NOARK5](https://www.arkivverket.no/forvaltning-og-utvikling/noark-standarden/noark5-standarden)-krav fra Arkivverket:
- [avlevering til Arkivverket - produserAvleveringspakkeGenerellSak](https://confluence.adeo.no/spaces/BOA/pages/385072865/ProduserAvleveringsPakkeGenerellSak)
- [avslutting av alle saker på et tema - avsluttAlleSakerPaaTema](https://confluence.adeo.no/spaces/BOA/pages/603362522/AvsluttAlleSakerPaaTema)

Alle jobbene finnes på [Github Actions for Dokarkivavlevering](https://github.com/navikt/dokarkivavlevering/actions).

Mer detaljert informasjon om funksjonalitet finnes i [systemdokumentasjon for dokarkivavlevering](https://confluence.adeo.no/display/BOA/dokarkivavlevering).


## Spesifikt for produserAvleveringspakkeGenerellSak
### Lokal kjøring og test:

Dette fungerer ikke på naisdevice.

Jobben vil gjøre en avlevering fra Q2 miljøet basert på properties i `application-local.properties`.

Kjør `Application.java` i IntelliJ med VM-options fra: https://vault.adeo.no/ui/vault/secrets/secret/show/dokument/dokarkivavlevering

* `avlevering.filomraade.work` - lokal arbeidsmappe for appen.

Avleveringen vil havne i mappen `/inbound/yyyy-MM-dd_HH_mm/avleveringspakke` på SFTP servern konfigurert i ENV verdi fra: https://vault.adeo.no/ui/vault/secrets/kv%2Fpreprod%2Ffss/list/dokarkivavlevering/ 

### Tilganger for avlevering til Arkivverket

Tilgang til SFTP serveren i dev og prod bestilles fra drift. Se eksempel i [IKT-242931](https://jira.adeo.no/browse/IKT-242931).

Personlig tilgang til utviklere gis med personlig public key.

## Spesifikt for avsluttAlleSakerPaaTema
### Før kjøring av avsluttAlleSakerPaaTema
Jobben bruker en arbeidstabell som må bli laget og populert før kjøring i dev og prod.

Før jobben blir kjørt må derfor:
- Arbeidstabell bli opprettet og populert med data. Navn på denne kan være Jira-sak for tema som skal bli avsluttet. Dette kan vi utføre selv i q2, men krever en [Jira-bestilling for prod](https://jira.adeo.no/browse/IKT-649254).
- [Arbeidssak.java](avsluttSaker/src/main/java/no/nav/dokarkivavlevering/avsluttAlleSakerPaaTema/entities/Arbeidssak.java) sin Table-annotasjon må få samme navn som arbeidstabellen.

Skript for oppretting av arbeidstabell og innsetting av data for q2-miljøet finnes i [avsluttSaker/README.md](avsluttSaker/README.md).


## Henvendelser
Spørsmål rundt koden eller prosjektet kan rettes til Team Dokumentløsninger på [\#Team Dokumentløsninger](https://nav-it.slack.com/client/T5LNAMWNA/C6W9E5GPJ).