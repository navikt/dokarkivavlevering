-- Opprett tabell siden dette ikke er en egen JPA entitet
create table t_saksrelasjon
(
    sak_id         NUMBER        not null,
    feilregistrert CHAR(1),
    journalpost_id NUMBER(11, 0) not null
);

insert into t_saksrelasjon(sak_id, feilregistrert, journalpost_id)
VALUES (123, '0', 123456);

create table t_journalpost
(
    journalpost_id NUMBER(11, 0)     not null,
    k_journal_s    VARCHAR2(20) not null,
    journalf_enhet VARCHAR2(20),
    dato_journal   TIMESTAMP(6)
);

insert into t_journalpost(k_journal_s, journalpost_id, journalf_enhet, dato_journal)
VALUES ('MO', 123456, '1234', '2025-01-01T13:30');

