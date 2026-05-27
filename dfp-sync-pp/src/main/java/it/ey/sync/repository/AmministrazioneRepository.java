package it.ey.sync.repository;

import it.ey.sync.entity.Amministrazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmministrazioneRepository extends JpaRepository<Amministrazione, String> {

    @Modifying
    @Query("DELETE FROM Amministrazione a WHERE a.codiceIpa NOT IN :codici")
    void deleteByCodiceIpaNotIn(@Param("codici") List<String> codici);
}

