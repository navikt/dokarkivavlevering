package no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema;

import no.nav.dokarkivavlevering.avsluttAlleSakerPaaTema.entities.Sak;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Profile("avsluttSaker")
public interface SakRepository extends JpaRepository<Sak, Long> {

	@Query("select sak.sakId FROM Sak sak")
	List<Long> findAllSakIds();

	List<Sak> findSaksBySakIdIn(List<Long> sakIds);
}