package it.ey.piao.api.repository;

import it.ey.entity.StoricoModifica;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IStoricoModificaRepository extends BaseRepository<StoricoModifica, Long> {

    @Query("""
        SELECT s FROM StoricoModifica s
        WHERE s.idSezione = :idSezione
          AND s.codTipologiaFK = :codTipologiaFK
    """)
    List<StoricoModifica> findByIdSezioneAndCodTipologiaFK(
            @Param("idSezione") Long idSezione,
            @Param("codTipologiaFK") String codTipologiaFK);

    @Query("""
        SELECT s FROM StoricoModifica s
        WHERE s.piao.id = :idPiao
    """)
    List<StoricoModifica> findByPiaoId(@Param("idPiao") Long idPiao);
}
