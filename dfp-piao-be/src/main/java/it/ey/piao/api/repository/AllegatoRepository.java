package it.ey.piao.api.repository;

import it.ey.entity.Allegato;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.enums.Sezione;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AllegatoRepository extends BaseRepository<Allegato, Long> {

    @Query("SELECT a FROM Allegato a WHERE a.codTipologiaFK IN :codTipologia AND a.codTipologiaAllegato IN :codTipologiaAllegato AND a.idEntitaFK = :idPiao")
    List<Allegato> getAllegatiByTipologiaFK(@Param("codTipologia") List<Sezione> codTipologia,
                                            @Param("codTipologiaAllegato") List<CodTipologiaAllegato> codTipologiaAllegato,
                                            @Param("idPiao") Long idPiao);

    @Query("SELECT a FROM Allegato a WHERE a.idEntitaFK = :idPiao")
    List<Allegato> findByIdPiao(@Param("idPiao") Long idPiao);

    @Modifying
    @Query("UPDATE Allegato a SET a.active = false, a.deactivationTime = :deactivationTime WHERE a.id = :idAllegato")
    void softDeleteById(@Param("idAllegato") Long idAllegato,
                        @Param("deactivationTime") LocalDateTime deactivationTime);


   // Recupera l'allegato con  il codDocumento indicato
    //Usato per la cancellazione della bozza PDF dopo il click del download
    @Query("SELECT a FROM Allegato a WHERE a.codDocumento = :codDocumento AND a.active = true")
    Optional<Allegato> findActiveByCodDocumento(@Param("codDocumento") String codDocumento);
}
