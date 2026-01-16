package it.ey.piao.api.repository;

import it.ey.entity.StoricoStatoSezione;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


    public interface IStoricoStatoSezioneRepository extends BaseRepository<StoricoStatoSezione, Long> {

        @Query("""
    select s
    from StoricoStatoSezione s
    where s.idEntitaFK = :idEntitaFK
      and s.codTipologiaFK = :sezione
""")
        List<StoricoStatoSezione> findByIdEntitaAndCodTipologia(@Param("idEntitaFK") Long idEntitaFK,
                                                                @Param("sezione") String sezione);


    }
