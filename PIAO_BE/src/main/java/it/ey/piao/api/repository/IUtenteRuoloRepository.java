package it.ey.piao.api.repository;

import it.ey.entity.UtentePa;
import it.ey.entity.UtenteRuoloPa;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUtenteRuoloRepository extends BaseRepository<UtenteRuoloPa, Long> {


    @Query("""
           select distinct u
           from UtenteRuoloPa u
           join u.codicePA pa
           where pa.codicePa = :codicePa
           """)
    List<UtenteRuoloPa> findByCodicePa(@Param("codicePa") String codicePa);

}
