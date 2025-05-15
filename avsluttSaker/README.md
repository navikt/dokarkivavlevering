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
```