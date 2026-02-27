package it.ey.piao.api.repository;

import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.entity.DichiarazioneScadenza;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IDichiarazioneScadenzaRepository extends BaseRepository<DichiarazioneScadenza, Long>
{
    @Query(value = """
            SELECT ds.*
                FROM DichiarazioneScadenza ds
            WHERE ds.IdPiao = :idPiao
    """, nativeQuery = true)
    DichiarazioneScadenza findByPiao_Id(
       @Param("idPiao") Long idPiao
    );
}
