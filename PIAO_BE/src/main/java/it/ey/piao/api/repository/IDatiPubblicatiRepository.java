package it.ey.piao.api.repository;

import it.ey.entity.DatiPubblicati;
import it.ey.entity.ObbligoLegge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDatiPubblicatiRepository extends JpaRepository<DatiPubblicati,Long> {
    List<DatiPubblicati> findByObbligoLegge(ObbligoLegge obbligoLegge);
}
