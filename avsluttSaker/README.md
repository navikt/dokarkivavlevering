# Skript brukt for testing i q2

### Oppretting av arbeidstabell
```
CREATE TABLE <ARBEIDSTABELL> (
    id NUMBER(10) NOT NULL PRIMARY KEY,
    applikasjon VARCHAR2(40),
    fagsaknr VARCHAR2(40),
    aktoerid VARCHAR2(40),
    orgnr VARCHAR2(9),
    k_sak_status VARCHAR2(40),
    arbeidsstatus VARCHAR2(255)
);
```

### Populering av arbeidstabell
```
INSERT INTO <ARBEIDSTABELL> (id, applikasjon, fagsaknr, aktoerid, orgnr, k_sak_status)
SELECT 
    id,
    applikasjon,
    fagsaknr,
    aktoerid,
    orgnr,
    k_sak_status
FROM joark.sak
WHERE tema = '<TEMA>'
AND k_sak_status is null or k_sak_status = 'AAPEN';
COMMIT;
```

### Opprydding av sak-tabell etter kjøring ved testing
```
UPDATE joark.sak 
SET
    k_sak_status = null, 
    k_avlevering_status = null, 
    k_kassasjon_status = null,
    endret_av = null,
    endret_kilde_navn = null, 
    dato_endret = null,
    dato_avsluttet = null, 
    avsluttet_av = null, 
    avsluttet_kilde_navn = null,
    dato_sak_opprettet = null, 
    administrativ_enhet = null, 
    sak_ansvarlig = null
WHERE endret_av = <REFERANSE FRA GHA-INPUT>;
COMMIT;
```