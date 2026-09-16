-- Minimalt oppsett av tabellene som SqlQueries spør mot, for å kunne validere
-- de endrede spørringene (FINN_SAKID_SQL, FINN_SAKER_SQL, FINN_SAKER_UTEN_DOKUMENTER_SQL)
-- mot en embedded H2-database.

create table sak
(
    id                  NUMBER(10, 0) not null,
    tema                VARCHAR2(20),
    opprettet_tidspunkt TIMESTAMP(6),
    opprettet_av        VARCHAR2(40),
    aktoerid            VARCHAR2(40),
    orgnr               VARCHAR2(40),
    administrativ_enhet VARCHAR2(40),
    k_kassasjon_status  VARCHAR2(60)
);

create table t_saksrelasjon
(
    sak_id         NUMBER        not null,
    journalpost_id NUMBER(11, 0) not null,
    k_fagsystem    VARCHAR2(20),
    feilregistrert CHAR(1)
);

create table t_journalpost
(
    journalpost_id     NUMBER(11, 0) not null,
    k_journalpost_t    VARCHAR2(20),
    k_journal_s        VARCHAR2(20)  not null,
    innhold            VARCHAR2(200),
    avsend_mottaker    VARCHAR2(200),
    avsend_mottak_id   VARCHAR2(40),
    k_avsend_mottak_id_t VARCHAR2(20),
    dato_mottatt       TIMESTAMP(6),
    dato_dokument      TIMESTAMP(6),
    dato_journal       TIMESTAMP(6),
    dato_opprettet     TIMESTAMP(6),
    dato_endret        TIMESTAMP(6),
    dato_ekspedert     TIMESTAMP(6),
    dato_sendt_print   TIMESTAMP(6),
    opprettet_av       VARCHAR2(40),
    opprettet_av_navn  VARCHAR2(80),
    endret_av          VARCHAR2(40),
    opprettet_kilde_navn VARCHAR2(80)
);

create table t_jp_dok_info_rel
(
    journalpost_id   NUMBER(11, 0) not null,
    dokument_info_id NUMBER(11, 0) not null,
    k_tilkn_jp_som   VARCHAR2(40),
    dato_opprettet   TIMESTAMP(6),
    opprettet_av     VARCHAR2(40)
);

create table t_dokument_info
(
    dokument_info_id NUMBER(11, 0) not null,
    opprettet_av     VARCHAR2(40),
    k_kategori_t     VARCHAR2(20),
    k_dokument_s     VARCHAR2(20),
    tittel           VARCHAR2(200),
    dato_opprettet   TIMESTAMP(6),
    dato_dok_ferdig  TIMESTAMP(6)
);

create table t_k_kategori_t
(
    k_kategori_t VARCHAR2(20) not null,
    dekode       VARCHAR2(80)
);

create table t_fil_detaljer
(
    fil_detaljer_id  NUMBER(11, 0) not null,
    dokument_info_id NUMBER(11, 0) not null,
    fil_uuid         VARCHAR2(40),
    dato_opprettet   TIMESTAMP(6),
    opprettet_av     VARCHAR2(40),
    k_fil_t          VARCHAR2(20),
    k_variant_format VARCHAR2(20)
);

create table t_dokument_fil
(
    fil_uuid VARCHAR2(40),
    fil      BLOB
);

create table t_k_fagomrade
(
    k_fagomrade    VARCHAR2(20) not null,
    dekode         VARCHAR2(80),
    dato_tom       DATE,
    er_gyldig      VARCHAR2(1),
    dato_opprettet TIMESTAMP(6),
    opprettet_av   VARCHAR2(40)
);

create table t_administrativ_enhet
(
    tema       VARCHAR2(20),
    enhet_navn VARCHAR2(100),
    dato_fom   DATE,
    dato_tom   DATE
);

create table t_k_offentlig_journal_avsender_mottaker
(
    k_offentlig_journal_avsender_mottaker VARCHAR2(200)
);

create table t_aksjonslogg
(
    aksjonslogg_id   NUMBER(11, 0) not null,
    journalpost_id   NUMBER(11, 0),
    dokument_info_id NUMBER(11, 0),
    tidspunkt        TIMESTAMP(6),
    utfoert_av       VARCHAR2(40)
);

create table t_arkiv_element_endring
(
    arkiv_element_endring_id NUMBER(11, 0) not null,
    aksjonslogg_id           NUMBER(11, 0),
    arkiv_element            VARCHAR2(40),
    fra_verdi                VARCHAR2(80),
    til_verdi                VARCHAR2(80),
    tidspunkt                TIMESTAMP(6)
);

-- Felles oppslagsdata
insert into t_k_fagomrade(k_fagomrade, dekode, dato_tom, er_gyldig, dato_opprettet, opprettet_av)
values ('MED', 'Medlemskap', DATEADD('YEAR', 10, CURRENT_DATE), '1', CURRENT_TIMESTAMP, 'testbruker');

insert into t_administrativ_enhet(tema, enhet_navn, dato_fom, dato_tom)
values ('MED', 'Nav Medlemskap', DATEADD('YEAR', -10, CURRENT_DATE), DATEADD('YEAR', 10, CURRENT_DATE));

insert into t_k_kategori_t(k_kategori_t, dekode)
values ('N', 'Notat');

-- ===================================================================================
-- Data for FINN_SAKID_SQL (AvleveringRepository.findSakIds)
-- ===================================================================================

-- sak 101: skal treffes - riktig kassasjonsstatus, gyldig journalstatus, tillatt kilde
insert into sak(id, tema, k_kassasjon_status) values (101, 'MED', 'BEVARINGSTID_PASSERT');
insert into t_saksrelasjon(sak_id, journalpost_id, k_fagsystem, feilregistrert) values (101, 1001, 'FS22', '0');
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (1001, 'FS', '2020-10-02T10:00:00', 'srvdokgenerering');

-- sak 102: skal IKKE treffes - kassasjonsstatus matcher ikke filteret
insert into sak(id, tema, k_kassasjon_status) values (102, 'MED', 'AKTIV');
insert into t_saksrelasjon(sak_id, journalpost_id, k_fagsystem, feilregistrert) values (102, 1002, 'FS22', '0');
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (1002, 'FS', '2020-10-02T10:00:00', 'srvdokgenerering');

-- sak 103: skal IKKE treffes - opprettet fra en ondemand-kilde som skal ekskluderes
insert into sak(id, tema, k_kassasjon_status) values (103, 'MED', 'BEVARINGSTID_PASSERT');
insert into t_saksrelasjon(sak_id, journalpost_id, k_fagsystem, feilregistrert) values (103, 1003, 'FS22', '0');
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (1003, 'FS', '2020-10-02T10:00:00', 'ondemandtojoark');

-- sak 104: skal treffes selv om dato_opprettet ligger langt utenfor avlevering.periode -
-- datofilteret er fjernet fra FINN_SAKID_SQL, så gammel kode ville forkastet denne
insert into sak(id, tema, k_kassasjon_status) values (104, 'MED', 'BEVARINGSTID_PASSERT');
insert into t_saksrelasjon(sak_id, journalpost_id, k_fagsystem, feilregistrert) values (104, 1004, 'FS22', '0');
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (1004, 'FS', '2010-01-01T10:00:00', 'srvdokgenerering');

-- sak 111-113: skal IKKE treffes - dekker de resterende ekskluderte kildene
insert into sak(id, tema, k_kassasjon_status) values (111, 'MED', 'BEVARINGSTID_PASSERT');
insert into t_saksrelasjon(sak_id, journalpost_id, k_fagsystem, feilregistrert) values (111, 1011, 'FS22', '0');
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (1011, 'FS', '2020-10-02T10:00:00', 'srvondemandtojoark');

insert into sak(id, tema, k_kassasjon_status) values (112, 'MED', 'BEVARINGSTID_PASSERT');
insert into t_saksrelasjon(sak_id, journalpost_id, k_fagsystem, feilregistrert) values (112, 1012, 'FS22', '0');
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (1012, 'FS', '2020-10-02T10:00:00', 'teamdokumenthandtering:arenaondemandtojo');

insert into sak(id, tema, k_kassasjon_status) values (113, 'MED', 'BEVARINGSTID_PASSERT');
insert into t_saksrelasjon(sak_id, journalpost_id, k_fagsystem, feilregistrert) values (113, 1013, 'FS22', '0');
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (1013, 'FS', '2020-10-02T10:00:00', 'teamdokumenthandtering:ondemandtojoark');

-- sak 121-122: skal treffes - dekker de resterende gyldige kassasjonsstatusene
insert into sak(id, tema, k_kassasjon_status) values (121, 'MED', 'BEVARINGSTID_PASSERT_DOK_KASSASJON_BESTILT');
insert into t_saksrelasjon(sak_id, journalpost_id, k_fagsystem, feilregistrert) values (121, 1021, 'FS22', '0');
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (1021, 'FS', '2020-10-02T10:00:00', 'srvdokgenerering');

insert into sak(id, tema, k_kassasjon_status) values (122, 'MED', 'BEVARINGSTID_PASSERT_DOK_KASSERT');
insert into t_saksrelasjon(sak_id, journalpost_id, k_fagsystem, feilregistrert) values (122, 1022, 'FS22', '0');
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (1022, 'FS', '2020-10-02T10:00:00', 'srvdokgenerering');

-- ===================================================================================
-- Data for FINN_SAKER_SQL / FINN_SAKER_UTEN_DOKUMENTER_SQL
-- (AvleveringRepository.findSakerMedDokumenter / findSakerUtenDokumenter)
-- ===================================================================================

-- sak 201: journalstatus 'R' - skal kun være med i "med dokumenter"-spørringen
insert into sak(id, tema, opprettet_tidspunkt, opprettet_av, aktoerid) values (201, 'MED', '2020-10-02T10:00:00', 'testbruker', '11111111111');
insert into t_saksrelasjon(sak_id, journalpost_id) values (201, 2001);
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (2001, 'R', '2020-10-02T10:00:00', 'srvdokgenerering');
insert into t_jp_dok_info_rel(journalpost_id, dokument_info_id, k_tilkn_jp_som, dato_opprettet, opprettet_av)
values (2001, 3001, 'HOVEDDOKUMENT', '2020-10-02T10:00:00', 'testbruker');
insert into t_dokument_info(dokument_info_id, opprettet_av, k_kategori_t, k_dokument_s, tittel, dato_opprettet)
values (3001, 'testbruker', 'N', 'FERDIGSTILT', 'Tittel 201', '2020-10-02T10:00:00');
insert into t_fil_detaljer(fil_detaljer_id, dokument_info_id, fil_uuid, dato_opprettet, opprettet_av, k_variant_format)
values (4001, 3001, 'uuid-201', '2020-10-02T10:00:00', 'testbruker', 'ARKIV');
insert into t_dokument_fil(fil_uuid, fil) values ('uuid-201', null);

-- sak 202: opprettet fra en ondemand-kilde - skal ekskluderes fra begge spørringene
insert into sak(id, tema, opprettet_tidspunkt, opprettet_av, aktoerid) values (202, 'MED', '2020-10-02T10:00:00', 'testbruker', '22222222222');
insert into t_saksrelasjon(sak_id, journalpost_id) values (202, 2002);
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (2002, 'FS', '2020-10-02T10:00:00', 'ondemandtojoark');
insert into t_jp_dok_info_rel(journalpost_id, dokument_info_id, k_tilkn_jp_som, dato_opprettet, opprettet_av)
values (2002, 3002, 'HOVEDDOKUMENT', '2020-10-02T10:00:00', 'testbruker');
insert into t_dokument_info(dokument_info_id, opprettet_av, k_kategori_t, k_dokument_s, tittel, dato_opprettet)
values (3002, 'testbruker', 'N', 'FERDIGSTILT', 'Tittel 202', '2020-10-02T10:00:00');
insert into t_fil_detaljer(fil_detaljer_id, dokument_info_id, fil_uuid, dato_opprettet, opprettet_av, k_variant_format)
values (4002, 3002, 'uuid-202', '2020-10-02T10:00:00', 'testbruker', 'ARKIV');
insert into t_dokument_fil(fil_uuid, fil) values ('uuid-202', null);

-- sak 203: positiv kontroll - skal være med i begge spørringene
insert into sak(id, tema, opprettet_tidspunkt, opprettet_av, aktoerid) values (203, 'MED', '2020-10-02T10:00:00', 'testbruker', '33333333333');
insert into t_saksrelasjon(sak_id, journalpost_id) values (203, 2003);
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (2003, 'FS', '2020-10-02T10:00:00', 'srvdokgenerering');
insert into t_jp_dok_info_rel(journalpost_id, dokument_info_id, k_tilkn_jp_som, dato_opprettet, opprettet_av)
values (2003, 3003, 'HOVEDDOKUMENT', '2020-10-02T10:00:00', 'testbruker');
insert into t_dokument_info(dokument_info_id, opprettet_av, k_kategori_t, k_dokument_s, tittel, dato_opprettet)
values (3003, 'testbruker', 'N', 'FERDIGSTILT', 'Tittel 203', '2020-10-02T10:00:00');
insert into t_fil_detaljer(fil_detaljer_id, dokument_info_id, fil_uuid, dato_opprettet, opprettet_av, k_variant_format)
values (4003, 3003, 'uuid-203', '2020-10-02T10:00:00', 'testbruker', 'ARKIV');
insert into t_dokument_fil(fil_uuid, fil) values ('uuid-203', null);

-- sak 204: dato_opprettet langt utenfor avlevering.periode - datofilteret er fjernet
-- fra både FINN_SAKER_SQL og FINN_SAKER_UTEN_DOKUMENTER_SQL, så saken skal være med
-- i begge spørringene selv om den ligger utenfor perioden
insert into sak(id, tema, opprettet_tidspunkt, opprettet_av, aktoerid) values (204, 'MED', '2010-01-01T10:00:00', 'testbruker', '44444444444');
insert into t_saksrelasjon(sak_id, journalpost_id) values (204, 2004);
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (2004, 'FS', '2010-01-01T10:00:00', 'srvdokgenerering');
insert into t_jp_dok_info_rel(journalpost_id, dokument_info_id, k_tilkn_jp_som, dato_opprettet, opprettet_av)
values (2004, 3004, 'HOVEDDOKUMENT', '2010-01-01T10:00:00', 'testbruker');
insert into t_dokument_info(dokument_info_id, opprettet_av, k_kategori_t, k_dokument_s, tittel, dato_opprettet)
values (3004, 'testbruker', 'N', 'FERDIGSTILT', 'Tittel 204', '2010-01-01T10:00:00');
insert into t_fil_detaljer(fil_detaljer_id, dokument_info_id, fil_uuid, dato_opprettet, opprettet_av, k_variant_format)
values (4004, 3004, 'uuid-204', '2010-01-01T10:00:00', 'testbruker', 'ARKIV');
insert into t_dokument_fil(fil_uuid, fil) values ('uuid-204', null);

-- sak 205: har to journalposter - en fra en tillatt kilde og en fra en ekskludert
-- ondemand-kilde. Kun journalposten med tillatt kilde skal hentes ut for saken.
insert into sak(id, tema, opprettet_tidspunkt, opprettet_av, aktoerid) values (205, 'MED', '2020-10-02T10:00:00', 'testbruker', '55555555555');

insert into t_saksrelasjon(sak_id, journalpost_id) values (205, 2005);
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (2005, 'FS', '2020-10-02T10:00:00', 'srvdokgenerering');
insert into t_jp_dok_info_rel(journalpost_id, dokument_info_id, k_tilkn_jp_som, dato_opprettet, opprettet_av)
values (2005, 3005, 'HOVEDDOKUMENT', '2020-10-02T10:00:00', 'testbruker');
insert into t_dokument_info(dokument_info_id, opprettet_av, k_kategori_t, k_dokument_s, tittel, dato_opprettet)
values (3005, 'testbruker', 'N', 'FERDIGSTILT', 'Tittel 205 - OK kilde', '2020-10-02T10:00:00');
insert into t_fil_detaljer(fil_detaljer_id, dokument_info_id, fil_uuid, dato_opprettet, opprettet_av, k_variant_format)
values (4005, 3005, 'uuid-205', '2020-10-02T10:00:00', 'testbruker', 'ARKIV');
insert into t_dokument_fil(fil_uuid, fil) values ('uuid-205', null);

insert into t_saksrelasjon(sak_id, journalpost_id) values (205, 2006);
insert into t_journalpost(journalpost_id, k_journal_s, dato_opprettet, opprettet_kilde_navn)
values (2006, 'FS', '2020-10-02T10:00:00', 'ondemandtojoark');
insert into t_jp_dok_info_rel(journalpost_id, dokument_info_id, k_tilkn_jp_som, dato_opprettet, opprettet_av)
values (2006, 3006, 'HOVEDDOKUMENT', '2020-10-02T10:00:00', 'testbruker');
insert into t_dokument_info(dokument_info_id, opprettet_av, k_kategori_t, k_dokument_s, tittel, dato_opprettet)
values (3006, 'testbruker', 'N', 'FERDIGSTILT', 'Tittel 205 - dårlig kilde', '2020-10-02T10:00:00');
insert into t_fil_detaljer(fil_detaljer_id, dokument_info_id, fil_uuid, dato_opprettet, opprettet_av, k_variant_format)
values (4006, 3006, 'uuid-206', '2020-10-02T10:00:00', 'testbruker', 'ARKIV');
insert into t_dokument_fil(fil_uuid, fil) values ('uuid-206', null);
