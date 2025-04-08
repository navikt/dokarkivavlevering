package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository;

public class SqlQueries {

	public static final String FINN_JOURNALPOSTER_FOR_ARKIVSAK = """
			select
				sr.feilregistrert as erFeilregistrert,
				jp.k_journal_s as journalstatus,
				jp.journalf_enhet as journalfoerendeEnhet,
				jp.dato_journal as journaldato,
				jp.dato_opprettet as opprettetdato
			FROM t_saksrelasjon sr
				join t_journalpost jp on jp.journalpost_id = sr.journalpost_id
			where sr.sak_id in (:sakIds)
			""";

	public static final String AVBRYT_SAKER = """
			update sak
			set K_SAK_STATUS = 'AVBRUTT',
				K_AVLEVERING_STATUS = 'AVBRUTT',
				K_KASSASJON_STATUS = 'KLAR_FOR_KASSASJON',
				ENDRET_AV = :referanse,
				DATO_ENDRET = current_timestamp
			where id in (:sakIds)
			""";

}
