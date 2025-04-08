-- Opprett tabell siden dette ikke er en egen JPA entitet
create table t_saksrelasjon
(
    sak_id         NUMBER        not null,
    feilregistrert CHAR(1),
    journalpost_id NUMBER(11, 0) not null
);

insert into t_saksrelasjon(sak_id, feilregistrert, journalpost_id)
VALUES (123, '0', 123456);

insert into t_saksrelasjon(sak_id, feilregistrert, journalpost_id)
VALUES (234, '0', 234567);

insert into t_saksrelasjon(sak_id, feilregistrert, journalpost_id)
VALUES (345, '0', 345678);

insert into t_saksrelasjon(sak_id, feilregistrert, journalpost_id)
VALUES (456, '1', 456789);

create table t_journalpost
(
    journalpost_id      NUMBER(11, 0) not null,
    k_journal_s         VARCHAR2(20) not null,
    journalf_enhet      VARCHAR2(20),
    dato_journal        TIMESTAMP(6),
    dato_opprettet      TIMESTAMP(6)
);

insert into t_journalpost(journalpost_id, k_journal_s, journalf_enhet, dato_opprettet, dato_journal)
VALUES ('123456', 'FL', '1234', '2025-01-01T13:30', '2025-01-02T13:30');

insert into t_journalpost(journalpost_id, k_journal_s, journalf_enhet, dato_opprettet, dato_journal)
VALUES ('234567', 'FS', '5678', '2025-02-13T14:45', '2025-02-13T15:00');

insert into t_journalpost(journalpost_id, k_journal_s, journalf_enhet, dato_opprettet, dato_journal)
VALUES ('345678', 'M', '5678', '2025-02-13T14:45', '2025-02-13T15:00');

insert into t_journalpost(journalpost_id, k_journal_s, journalf_enhet, dato_opprettet, dato_journal)
VALUES ('456789', 'FS', '5678', '2025-02-13T14:45', '2025-02-13T15:00');