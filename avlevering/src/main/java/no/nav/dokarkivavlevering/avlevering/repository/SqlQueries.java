package no.nav.dokarkivavlevering.avlevering.repository;

public class SqlQueries {
	public static final String FINN_FAGOMRADE = """
			select
			    fo.k_fagomrade                as fagomrade,
			    fo.dekode                     as dekode,
			    fo.dato_tom                   as dato_tom,
			    fo.er_gyldig                  as er_gyldig,
			    fo.dato_opprettet             as dato_opprettet,
			    fo.opprettet_av               as opprettet_av
			from t_k_fagomrade fo
			where fo.k_fagomrade = :tema
			""";

	public static final String FINN_SAKID_SQL = """
			SELECT /*+ PARALLEL */ distinct s.sak_id as sakId
			from t_saksrelasjon s
			join t_journalpost j on s.journalpost_id = j.journalpost_id
			where s.k_fagsystem = 'FS22' and ( s.feilregistrert IS NULL OR s.feilregistrert = '0' )
			and s.sak_id in(
			        SELECT
			            sa.id
			        FROM
			            sak sa
			        WHERE
			            sa.tema = :tema
			)
			    AND j.k_journal_s IN ( 'J', 'FS', 'FL', 'E' )
			    AND j.dato_opprettet BETWEEN :startdato and :sluttdato
			""";

	static final String FINN_SAKER_SQL = """
			select sa.id                         as id,
					       sa.tema                       as tema,
					       sa.opprettet_tidspunkt        as opprettettidspunkt,
					       sa.opprettet_av               as opprettetav,
					       case
					           when sa.aktoerid is null
					               then sa.orgnr
					           else sa.aktoerid end      as bruker_id,
					
					       j.journalpost_id              as jp_id,
					       j.k_journalpost_t             as jp_type,
					       j.k_journal_s                 as jp_status,
					       j.innhold                     as jp_innhold,
					       j.avsend_mottaker             as jp_avsendermottaker,
					       j.avsend_mottak_id            as jp_avsendermottakerid,
					       j.k_avsend_mottak_id_t         as jp_avsendermottaktype,
					       j.dato_mottatt                as jp_datomottatt,
					       j.dato_dokument               as jp_datodokument,
					       j.dato_journal                as jp_datojournal,
					       j.dato_opprettet              as jp_datoopprettet,
					       j.dato_endret                 as jp_datoendret,
					       j.dato_ekspedert              as jp_datoekspedert,
					       j.dato_sendt_print            as jp_datosendtprint,
					       j.opprettet_av                as jp_opprettetav,
					       j.opprettet_av_navn           as jp_opprettetavnavn,
					       j.endret_av                   as jp_endretav,
					       r.dokument_info_id            as jp_dok_id,
					       r.k_tilkn_jp_som              as jp_dok_reltilknyttetsom,
					       r.dato_opprettet              as jp_dok_reldatoopprettet,
					       r.opprettet_av                as jp_dok_relopprettetav,
					       d.opprettet_av                as jp_dok_opprettetav,
					       k.dekode                      as jp_dok_kategoridecode,
					       d.k_dokument_s                as jp_dok_status,
					       d.tittel                      as jp_dok_tittel,
					       d.dato_opprettet              as jp_dok_datoopprettet,
					       d.dato_dok_ferdig             as jp_dok_datoferdig,
					       f.fil_detaljer_id             as jp_dok_fd_id,
					       f.fil_uuid                    as jp_dok_fd_filuuid,
					       f.dato_opprettet              as jp_dok_fd_datoopprettet,
					       f.opprettet_av                as jp_dok_fd_opprettetav,
					       df.fil                        as jp_dok_fd_fil,
					       fo.k_fagomrade                as fagomrade_fagomrade,
					       fo.dekode                     as fagomrade_dekode,
					       fo.dato_tom                   as fagomrade_dato_tom,
					       fo.er_gyldig                  as fagomrade_er_gyldig,
					       fo.dato_opprettet             as fagomrade_dato_opprettet,
					       fo.opprettet_av               as fagomrade_opprettet_av,
					       aeej.arkiv_element_endring_id as jp_ae_id,
					       aeej.arkiv_element            as jp_ae_element,
					       alj.tidspunkt                 as jp_ae_tidspunkt,
					       alj.utfoert_av                as jp_ae_utfoertav,
					       aeej.fra_verdi                as jp_ae_fraverdi,
					       aeej.til_verdi                as jp_ae_tilverdi,
					       aeed.arkiv_element_endring_id as jp_dok_ae_id,
					       aeed.arkiv_element            as jp_dok_ae_element,
					       ald.tidspunkt                 as jp_dok_ae_tidspunkt,
					       ald.utfoert_av                as jp_dok_ae_utfoertav,
					       aeed.fra_verdi                as jp_dok_ae_fraverdi,
					       aeed.til_verdi                as jp_dok_ae_tilverdi,
					       ojam.k_offentlig_journal_avsender_mottaker as jp_offentligjournalavsendermottaker
					from sak sa
					         join t_saksrelasjon s on sa.id = s.sak_id
					         join t_journalpost j on s.journalpost_id = j.journalpost_id
					         join t_jp_dok_info_rel r on j.journalpost_id = r.journalpost_id
					         join t_dokument_info d on r.dokument_info_id = d.dokument_info_id
					         join t_k_kategori_t k on k.k_kategori_t = d.k_kategori_t
					         join t_fil_detaljer f on d.dokument_info_id = f.dokument_info_id
					         join t_dokument_fil df on f.fil_uuid = df.fil_uuid
					         join t_k_fagomrade fo on sa.tema = fo.k_fagomrade
					         left join t_aksjonslogg alj on alj.journalpost_id = j.journalpost_id and alj.dokument_info_id is null
					         left join t_arkiv_element_endring aeej on alj.aksjonslogg_id = aeej.aksjonslogg_id
					         left join t_aksjonslogg ald on ald.journalpost_id = j.journalpost_id and ald.dokument_info_id = d.dokument_info_id
					         left join t_arkiv_element_endring aeed on ald.aksjonslogg_id = aeed.aksjonslogg_id
					         join t_k_offentlig_journal_avsender_mottaker ojam on lower(trim(j.avsend_mottaker)) = lower(trim(ojam.k_offentlig_journal_avsender_mottaker))
					where s.sak_id in (:sakIds)
					  and j.k_journal_s in ('J', 'FS', 'FL', 'E')
					  and (j.dato_opprettet between :startdato and :sluttdato)
					  and (d.k_dokument_s is null or d.k_dokument_s = 'FERDIGSTILT')
					  and f.k_variant_format = 'ARKIV'
					order by sa.id desc, j.journalpost_id, r.k_tilkn_jp_som, r.dokument_info_id, f.fil_detaljer_id, aeej.arkiv_element_endring_id,
					         alj.tidspunkt, aeed.arkiv_element_endring_id, aeed.tidspunkt
					         """;

	static final String FINN_SAKER_UTEN_DOKUMENTER_SQL = """
			select sa.id                         as id,
					       sa.tema                       as tema,
					       sa.opprettet_tidspunkt        as opprettettidspunkt,
					       sa.opprettet_av               as opprettetav,
					       case
					           when sa.aktoerid is null
					               then sa.orgnr
					           else sa.aktoerid end      as bruker_id,
					
					       j.journalpost_id              as jp_id,
					       j.k_journalpost_t             as jp_type,
					       j.k_journal_s                 as jp_status,
					       j.innhold                     as jp_innhold,
					       j.avsend_mottaker             as jp_avsendermottaker,
					       j.avsend_mottak_id            as jp_avsendermottakerid,
					       j.k_avsend_mottak_id_t         as jp_avsendermottaktype,
					       j.dato_mottatt                as jp_datomottatt,
					       j.dato_dokument               as jp_datodokument,
					       j.dato_journal                as jp_datojournal,
					       j.dato_opprettet              as jp_datoopprettet,
					       j.dato_endret                 as jp_datoendret,
					       j.dato_ekspedert              as jp_datoekspedert,
					       j.dato_sendt_print            as jp_datosendtprint,
					       j.opprettet_av                as jp_opprettetav,
					       j.opprettet_av_navn           as jp_opprettetavnavn,
					       j.endret_av                   as jp_endretav,
					       r.dokument_info_id            as jp_dok_id,
					       r.k_tilkn_jp_som              as jp_dok_reltilknyttetsom,
					       r.dato_opprettet              as jp_dok_reldatoopprettet,
					       r.opprettet_av                as jp_dok_relopprettetav,
					       d.opprettet_av                as jp_dok_opprettetav,
					       k.dekode                      as jp_dok_kategoridecode,
					       d.k_dokument_s                as jp_dok_status,
					       d.tittel                      as jp_dok_tittel,
					       d.dato_opprettet              as jp_dok_datoopprettet,
					       d.dato_dok_ferdig             as jp_dok_datoferdig,
					       f.fil_detaljer_id             as jp_dok_fd_id,
					       f.fil_uuid                    as jp_dok_fd_filuuid,
					       f.dato_opprettet              as jp_dok_fd_datoopprettet,
					       f.opprettet_av                as jp_dok_fd_opprettetav,
					       fo.k_fagomrade                as fagomrade_fagomrade,
					       fo.dekode                     as fagomrade_dekode,
					       fo.dato_tom                   as fagomrade_dato_tom,
					       fo.er_gyldig                  as fagomrade_er_gyldig,
					       fo.dato_opprettet             as fagomrade_dato_opprettet,
					       fo.opprettet_av               as fagomrade_opprettet_av,
					       aeej.arkiv_element_endring_id as jp_ae_id,
					       aeej.arkiv_element            as jp_ae_element,
					       alj.tidspunkt                 as jp_ae_tidspunkt,
					       alj.utfoert_av                as jp_ae_utfoertav,
					       aeej.fra_verdi                as jp_ae_fraverdi,
					       aeej.til_verdi                as jp_ae_tilverdi,
					       aeed.arkiv_element_endring_id as jp_dok_ae_id,
					       aeed.arkiv_element            as jp_dok_ae_element,
					       ald.tidspunkt                 as jp_dok_ae_tidspunkt,
					       ald.utfoert_av                as jp_dok_ae_utfoertav,
					       aeed.fra_verdi                as jp_dok_ae_fraverdi,
					       aeed.til_verdi                as jp_dok_ae_tilverdi,
					       ojam.k_offentlig_journal_avsender_mottaker as jp_offentligjournalavsendermottaker
					from sak sa
					         join t_saksrelasjon s on sa.id = s.sak_id
					         join t_journalpost j on s.journalpost_id = j.journalpost_id
					         join t_jp_dok_info_rel r on j.journalpost_id = r.journalpost_id
					         join t_dokument_info d on r.dokument_info_id = d.dokument_info_id
					         join t_k_kategori_t k on k.k_kategori_t = d.k_kategori_t
					         join t_fil_detaljer f on d.dokument_info_id = f.dokument_info_id
					         join t_k_fagomrade fo on sa.tema = fo.k_fagomrade
					         left join t_aksjonslogg alj on alj.journalpost_id = j.journalpost_id and alj.dokument_info_id is null
					         left join t_arkiv_element_endring aeej on alj.aksjonslogg_id = aeej.aksjonslogg_id
					         left join t_aksjonslogg ald on ald.journalpost_id = j.journalpost_id and ald.dokument_info_id = d.dokument_info_id
					         left join t_arkiv_element_endring aeed on ald.aksjonslogg_id = aeed.aksjonslogg_id
					         join t_k_offentlig_journal_avsender_mottaker ojam on lower(trim(j.avsend_mottaker)) = lower(trim(ojam.k_offentlig_journal_avsender_mottaker))
					where s.sak_id in (:sakIds)
					  and j.k_journal_s in ('J', 'FS', 'FL', 'E')
					  and (j.dato_opprettet between :startdato and :sluttdato)
					  and (d.k_dokument_s is null or d.k_dokument_s = 'FERDIGSTILT')
					  and f.k_variant_format = 'ARKIV'
					order by sa.id desc, j.journalpost_id, r.k_tilkn_jp_som, r.dokument_info_id, f.fil_detaljer_id, aeej.arkiv_element_endring_id,
					         alj.tidspunkt, aeed.arkiv_element_endring_id, aeed.tidspunkt
					         """;
}
