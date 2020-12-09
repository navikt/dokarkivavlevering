package no.nav.dokarkivavlevering.avlevering.repository;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class SqlQueries {
	static final String JOURNALPOST_ID_RANGE =
			"select min(j.journalpost_id), max(j.journalpost_id), max(sa.id)\n" +
					"from t_journalpost j \n" +
					"         join t_saksrelasjon s on j.journalpost_id = s.journalpost_id\n" +
					"         join sak sa on sa.id = to_number(regexp_replace(s.sak_nr_fk, '[^0-9]', ''))\n" +
					"where \n" +
					"    (s.feilregistrert is null or s.feilregistrert = 0)\n" +
					"  and sa.tema = :tema\n" +
					"  and j.k_journal_s in ('J', 'FS', 'FL', 'E')\n" +
					"  and (trunc(j.dato_opprettet) between :startdato and :sluttdato)";
	static final String FINN_SAK_PAGE =
			"select distinct sa.id\n" +
					"from sak sa\n" +
					"where sa.tema = :tema\n" +
					"  and sa.id < :lastSakId\n" +
					"  and sa.id in (\n" +
					"    select to_number(regexp_replace(s.sak_nr_fk, '[^0-9]', ''))\n" +
					"    from t_saksrelasjon s\n" +
					"             join t_journalpost j on s.journalpost_id = j.journalpost_id\n" +
					"    where (j.journalpost_id between :minJournalpostId and :maxJournalpostId)\n" +
					"      and (s.feilregistrert is null or s.feilregistrert = 0)\n" +
					"      and j.k_journal_s in ('J', 'FS', 'FL', 'E')\n" +
					"      and (trunc(j.dato_opprettet) between :startdato and :sluttdato)\n" +
					")\n" +
					"order by sa.id desc\n" +
					"    fetch first :batchsize rows only";

	static final String FINN_SAKER_SQL =
			"select sa.id                         as id,\n" +
					"       sa.tema                       as tema,\n" +
					"       sa.opprettet_tidspunkt        as opprettettidspunkt,\n" +
					"       sa.opprettet_av               as opprettetav,\n" +
					"       case\n" +
					"           when sa.aktoerid is null\n" +
					"               then sa.orgnr\n" +
					"           else sa.aktoerid end      as bruker_id,\n" +
					"\n" +
					"       j.journalpost_id              as jp_id,\n" +
					"       j.k_journalpost_t             as jp_type,\n" +
					"       j.k_journal_s                 as jp_status,\n" +
					"       j.innhold                     as jp_innhold,\n" +
					"       j.avsend_mottaker             as jp_avsendermottaker,\n" +
					"       j.dato_mottatt                as jp_datomottatt,\n" +
					"       j.dato_dokument               as jp_datodokument,\n" +
					"       j.dato_journal                as jp_datojournal,\n" +
					"       j.dato_opprettet              as jp_datoopprettet,\n" +
					"       j.dato_endret                 as jp_datoendret,\n" +
					"       j.dato_ekspedert              as jp_datoekspedert,\n" +
					"       j.dato_sendt_print            as jp_datosendtprint,\n" +
					"       j.opprettet_av                as jp_opprettetav,\n" +
					"       j.opprettet_av_navn           as jp_opprettetavnavn,\n" +
					"       j.endret_av                   as jp_endretav,\n" +
					"       r.dokument_info_id            as jp_dok_id,\n" +
					"       r.k_tilkn_jp_som              as jp_dok_reltilknyttetsom,\n" +
					"       r.dato_opprettet              as jp_dok_reldatoopprettet,\n" +
					"       r.opprettet_av                as jp_dok_relopprettetav,\n" +
					"       d.opprettet_av                as jp_dok_opprettetav,\n" +
					"       d.k_kategori_t                as jp_dok_kategori,\n" +
					"       d.k_dokument_s                as jp_dok_status,\n" +
					"       d.tittel                      as jp_dok_tittel,\n" +
					"       d.dato_opprettet              as jp_dok_datoopprettet,\n" +
					"       d.dato_dok_ferdig             as jp_dok_datoferdig,\n" +
					"       f.fil_detaljer_id             as jp_dok_fd_id,\n" +
					"       f.fil_uuid                    as jp_dok_fd_filuuid,\n" +
					"       f.dato_opprettet              as jp_dok_fd_datoopprettet,\n" +
					"       f.opprettet_av                as jp_dok_fd_opprettetav,\n" +
					"       df.fil                        as jp_dok_fd_fil,\n" +
					"       aeej.arkiv_element_endring_id as jp_ae_id,\n" +
					"       aeej.arkiv_element            as jp_ae_element,\n" +
					"       alj.tidspunkt                 as jp_ae_tidspunkt,\n" +
					"       alj.utfoert_av                as jp_ae_utfoertav,\n" +
					"       aeej.fra_verdi                as jp_ae_fraverdi,\n" +
					"       aeej.til_verdi                as jp_ae_tilverdi,\n" +
					"       aeed.arkiv_element_endring_id as jp_dok_ae_id,\n" +
					"       aeed.arkiv_element            as jp_dok_ae_element,\n" +
					"       ald.tidspunkt                 as jp_dok_ae_tidspunkt,\n" +
					"       ald.utfoert_av                as jp_dok_ae_utfoertav,\n" +
					"       aeed.fra_verdi                as jp_dok_ae_fraverdi,\n" +
					"       aeed.til_verdi                as jp_dok_ae_tilverdi\n" +
					"from sak sa\n" +
					"         join t_saksrelasjon s on sa.id = to_number(regexp_replace(s.sak_nr_fk, '[^0-9]', ''))\n" +
					"         join t_journalpost j on s.journalpost_id = j.journalpost_id\n" +
					"         join t_jp_dok_info_rel r on j.journalpost_id = r.journalpost_id\n" +
					"         join t_dokument_info d on r.dokument_info_id = d.dokument_info_id\n" +
					"         join t_fil_detaljer f on d.dokument_info_id = f.dokument_info_id\n" +
					"         join t_dokument_fil df on f.fil_uuid = df.fil_uuid\n" +
					"         left join t_aksjonslogg alj on alj.journalpost_id = j.journalpost_id and alj.dokument_info_id is null\n" +
					"         left join t_arkiv_element_endring aeej on alj.aksjonslogg_id = aeej.aksjonslogg_id\n" +
					"         left join t_aksjonslogg ald on ald.journalpost_id = j.journalpost_id and ald.dokument_info_id = d.dokument_info_id\n" +
					"         left join t_arkiv_element_endring aeed on ald.aksjonslogg_id = aeed.aksjonslogg_id\n" +
					"where s.sak_nr_fk in (:sakIds)\n" +
					"  and j.k_journal_s in ('J', 'FS', 'FL', 'E')\n" +
					"  and (s.feilregistrert is null or s.feilregistrert = 0)\n" +
					"  and (trunc(j.dato_opprettet) between :startdato and :sluttdato)\n" +
					"  and d.k_dokument_s = 'FERDIGSTILT'\n" +
					"  and f.k_variant_format = 'ARKIV'\n" +
					"order by sa.id desc, j.journalpost_id, r.k_tilkn_jp_som, r.dokument_info_id, f.fil_detaljer_id, aeej.arkiv_element_endring_id,\n" +
					"         alj.tidspunkt, aeed.arkiv_element_endring_id, aeed.tidspunkt";
}
