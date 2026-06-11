package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository;

public class SqlQueries {

	public static final String FINN_JOURNALPOSTER_FOR_ARKIVSAK = """
			select
				sr.feilregistrert as erFeilregistrert,
				jp.k_journal_s as journalstatus,
				jp.journalf_enhet as journalfoerendeEnhet,
				jp.dato_journal as journaldato,
				jp.dato_opprettet as opprettetdato
			FROM joark.t_saksrelasjon sr
				join joark.t_journalpost jp on jp.journalpost_id = sr.journalpost_id
			where sr.sak_id in (:sakIds)
			""";

	public static final String AVBRYT_SAKER = """
			update joark.sak
			set K_SAK_STATUS = 'AVBRUTT',
				K_AVLEVERING_STATUS = 'AVBRUTT',
				K_KASSASJON_STATUS = 'KLAR_FOR_KASSASJON',
				ENDRET_AV = :referanse,
				ENDRET_KILDE_NAVN = 'AvsluttSakerPaaTema',
				DATO_ENDRET = current_timestamp
			where id in (:sakIds)
			""";

	public static final String AVSLUTT_SAKER = """
			update joark.sak
			set K_SAK_STATUS = 'AVSLUTTET',
				K_AVLEVERING_STATUS = null,
				K_KASSASJON_STATUS = null,
				ENDRET_AV = :referanse,
				ENDRET_KILDE_NAVN = 'AvsluttSakerPaaTema',
				DATO_ENDRET = current_timestamp,
				DATO_AVSLUTTET = :datoAvsluttet,
				AVSLUTTET_AV = 'JOARK',
				AVSLUTTET_KILDE_NAVN = 'JOARK',
				DATO_SAK_OPPRETTET = :datoSakOpprettet,
				ADMINISTRATIV_ENHET = :administrativEnhet,
				SAK_ANSVARLIG = :sakAnsvarlig
			where id in (:sakIds)
			""";

	public static final String HENT_NAVN_FOR_ADMINISTRATIV_ENHET = """
			select enhet_navn
			from joark.t_administrativ_enhet
			where tema = :fagomraade
			and dato_fom <= :opprettet_tidspunkt
			and dato_tom >= :opprettet_tidspunkt
			""";

}
