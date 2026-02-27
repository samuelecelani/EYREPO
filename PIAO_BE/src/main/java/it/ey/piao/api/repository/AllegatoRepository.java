package it.ey.piao.api.repository;

import it.ey.entity.Allegato;
import it.ey.enums.CodTipologia;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllegatoRepository extends BaseRepository<Allegato, Long> {

    @Query("SELECT a FROM Allegato a WHERE a.codTipologiaFK = :codTipologia AND a.codTipologiaAllegato = :codTipologiaAllegato AND a.idEntitaFK = :idPiao")
    List<Allegato> getAllegatiByTipologiaFK(@Param("codTipologia") CodTipologia codTipologia,
                                            @Param("codTipologiaAllegato") CodTipologiaAllegato codTipologiaAllegato,
                                            @Param("idPiao") Long idPiao);
}
