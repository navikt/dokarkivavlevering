package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.repository;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Journalpost;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Profile("avsluttSaker")
public interface ArkivsakJournalpostRepository extends JpaRepository<Journalpost, Long> {
/*
	@Query(value = """
			select sr.feilregistrert as erFeilregistrert,
			jp.journalpostStatus as journalstatus,
			jp.journalf_enhet as journalfoerendeEnhet,
			jp.dato_journal as journaldato
			FROM t_saksrelasjon sr
			join t_journalpost jp on jp.journalpost_id = sr.journalpost_id
			where sr.sak_id in (:sakIds)
			""", nativeQuery = true)
	List<no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.Journalpost> hentAlleJournalposterForArkivsak(@Param("sakIds") List<Long> sakIds);*/

}