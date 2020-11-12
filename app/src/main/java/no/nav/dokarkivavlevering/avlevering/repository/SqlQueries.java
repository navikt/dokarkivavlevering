package no.nav.dokarkivavlevering.avlevering.repository;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class SqlQueries {
	static final String FINN_SAKER_SQL =
			"select sa.id                    as id,\n" +
					"       sa.tema                  as tema,\n" +
					"       sa.opprettet_tidspunkt   as opprettettidspunkt,\n" +
					"       sa.opprettet_av          as opprettetav,\n" +
					"       case\n" +
					"           when sa.aktoerid is null\n" +
					"               then sa.orgnr\n" +
					"           else sa.aktoerid end as bruker_id,\n" +
					"\n" +
					"       j.journalpost_id         as journalposter_id,\n" +
					"       j.k_journalpost_t        as journalposter_type,\n" +
					"       j.k_journal_s            as journalposter_status,\n" +
					"       j.innhold                as journalposter_innhold,\n" +
					"       j.avsend_mottaker        as journalposter_avsendermottaker,\n" +
					"       j.dato_mottatt           as journalposter_datomottatt,\n" +
					"       j.dato_dokument          as journalposter_datodokument,\n" +
					"       j.dato_journal           as journalposter_datojournal,\n" +
					"       j.dato_opprettet         as journalposter_datoopprettet,\n" +
					"       j.dato_ekspedert         as journalposter_datoekspedert,\n" +
					"       j.dato_sendt_print       as journalposter_datosendtprint,\n" +
					"       j.opprettet_av           as journalposter_opprettetav,\n" +
					"       j.opprettet_av_navn      as journalposter_opprettetavnavn,\n" +
					"       j.endret_av              as journalposter_endretav,\n" +
					"       r.dokument_info_id       as journalposter_dokumenter_id,\n" +
					"       r.k_tilkn_jp_som         as journalposter_dokumenter_relasjontilknyttetsom,\n" +
					"       r.dato_opprettet         as journalposter_dokumenter_relasjondatoopprettet,\n" +
					"       r.opprettet_av           as journalposter_dokumenter_relasjonopprettetav,\n" +
					"       d.opprettet_av           as journalposter_dokumenter_opprettetav,\n" +
					"       d.k_kategori_t           as journalposter_dokumenter_kategori,\n" +
					"       d.k_dokument_s           as journalposter_dokumenter_status,\n" +
					"       d.tittel                 as journalposter_dokumenter_tittel,\n" +
					"       d.dato_opprettet         as journalposter_dokumenter_datoopprettet,\n" +
					"       f.fil_detaljer_id        as journalposter_dokumenter_fildetaljer_id,\n" +
					"       f.fil_uuid               as journalposter_dokumenter_fildetaljer_filuuid,\n" +
					"       f.dato_opprettet         as journalposter_dokumenter_fildetaljer_datoopprettet,\n" +
					"       f.opprettet_av           as journalposter_dokumenter_fildetaljer_opprettetav\n" +
					"from sak sa\n" +
					"         join t_saksrelasjon s on sa.id = to_number(regexp_replace(s.sak_nr_fk, '[^0-9]', ''))\n" +
					"         join t_journalpost j on s.journalpost_id = j.journalpost_id\n" +
					"         join t_jp_dok_info_rel r on j.journalpost_id = r.journalpost_id\n" +
					"         join t_dokument_info d on r.dokument_info_id = d.dokument_info_id\n" +
					"         join t_fil_detaljer f on d.dokument_info_id = f.dokument_info_id\n" +
					"where s.sak_nr_fk in (:sakIds)\n" +
					"  and j.k_journal_s in ('J', 'FS', 'FL', 'E')\n" +
					"  and (s.feilregistrert is null or s.feilregistrert = 0)\n" +
					"  and trunc(j.dato_opprettet) >= :startdato\n" +
					"  and trunc(j.dato_opprettet) <= :sluttdato\n" +
					"order by sa.id, j.journalpost_id, r.tilkn_jp_som, r.dokument_info_id, f.fil_detaljer_id";
}
