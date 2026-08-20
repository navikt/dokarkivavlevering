# Dokarkivavlevering

Dokarkivavlevering tilbyr følgende Nais-jobber for å oppfylle [NOARK5](https://www.arkivverket.no/forvaltning-og-utvikling/noark-standarden/noark5-standarden)-krav fra Arkivverket:
- [avlevering til Arkivverket - produserAvleveringspakkeGenerellSak (Nav-internt)](https://confluence.adeo.no/spaces/BOA/pages/385072865/ProduserAvleveringsPakkeGenerellSak)
- [avslutting av alle saker på et tema - avsluttAlleSakerPaaTema (Nav-internt)](https://confluence.adeo.no/spaces/BOA/pages/603362522/AvsluttAlleSakerPaaTema)

Alle jobbene finnes på [Github Actions for Dokarkivavlevering](https://github.com/navikt/dokarkivavlevering/actions).

Mer detaljert informasjon om funksjonalitet finnes i [systemdokumentasjon for dokarkivavlevering (Nav-internt)](https://confluence.adeo.no/display/BOA/dokarkivavlevering).

## Komme i gang

Kjør tester og bygg appen

```
mvn clean verify
```

### Spesifikt for produserAvleveringspakkeGenerellSak
#### Lokal kjøring og test:

Dette fungerer ikke på naisdevice.

Jobben vil gjøre en avlevering fra Q2 miljøet basert på properties i `application-local.properties`.

Kjør `Application.java` i IntelliJ med VM-options fra Nais Console.

* `avlevering.filomraade.work` - lokal arbeidsmappe for appen.

Avleveringen vil havne i mappen `/inbound/yyyy-MM-dd_HH_mm/avleveringspakke` på SFTP serveren.

#### Tilganger for avlevering til Arkivverket

Tilgang til SFTP serveren i dev og prod bestilles fra drift. Se eksempel i [IKT-242931 (Nav-internt)](https://jira.adeo.no/browse/IKT-242931).

Personlig tilgang til utviklere gis med personlig public key.

### Spesifikt for avsluttAlleSakerPaaTema
#### Før kjøring av avsluttAlleSakerPaaTema
Jobben bruker en arbeidstabell som må bli laget og populert før kjøring i dev og prod.

Før jobben blir kjørt må derfor:
- Arbeidstabell bli opprettet og populert med data. Navn på denne kan være Jira-sak for tema som skal bli avsluttet. Dette kan vi utføre selv i q2, men krever en [Jira-bestilling for prod (Nav-internt)](https://jira.adeo.no/browse/IKT-655353).
- [Arbeidssak.java](avsluttSaker/src/main/java/no/nav/dokarkivavlevering/avsluttAlleSakerPaaTema/entities/Arbeidssak.java) sin Table-annotasjon må få samme navn som arbeidstabellen.

Skript for oppretting av arbeidstabell og innsetting av data for q2- og prod-miljøet finnes i [avsluttSaker/README.md](avsluttSaker/README.md).

---

## Henvendelser

Lag en issue i repository.

### For Nav-ansatte

Spørsmål om appen kan stilles på [#team_dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ)

## Lisens

[MIT](LICENSE.md)
