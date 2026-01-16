package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PiaoRepository extends BaseRepository<Piao, Long> {

    // Recupera l'ultimo PIAO per PA e anno corrente
   public Piao findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
        String codPAFK,
        LocalDate startDate,
        LocalDate endDate
    );

   public List<Piao> findByCodPAFK(String codPAF);
}
