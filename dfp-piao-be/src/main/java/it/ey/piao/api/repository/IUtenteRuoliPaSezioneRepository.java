package it.ey.piao.api.repository;

import it.ey.entity.UtenteRuoliPaSezione;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUtenteRuoliPaSezioneRepository extends BaseRepository<UtenteRuoliPaSezione, Long> {

    @Query("SELECT u FROM UtenteRuoliPaSezione u WHERE u.externalUserId = :externalUserId")
    List<UtenteRuoliPaSezione> findByExternalUserId(@Param("externalUserId") String externalUserId);

    @Query("SELECT u FROM UtenteRuoliPaSezione u WHERE u.idAmministrazione = :idAmministrazione")
    List<UtenteRuoliPaSezione> findByIdAmministrazione(@Param("idAmministrazione") String idAmministrazione);

    @Query("SELECT u FROM UtenteRuoliPaSezione u WHERE u.externalUserId = :externalUserId AND u.idAmministrazione = :idAmministrazione")
    List<UtenteRuoliPaSezione> findByExternalUserIdAndIdAmministrazione(
        @Param("externalUserId") String externalUserId,
        @Param("idAmministrazione") String idAmministrazione);

    @Modifying
    @Query("DELETE FROM UtenteRuoliPaSezione u WHERE u.externalUserId = :externalUserId")
    void deleteByExternalUserId(@Param("externalUserId") String externalUserId);

    @Modifying
    @Query("DELETE FROM UtenteRuoliPaSezione u WHERE u.externalUserId = :externalUserId AND u.idAmministrazione = :idAmministrazione")
    void deleteByExternalUserIdAndIdAmministrazione(
        @Param("externalUserId") String externalUserId,
        @Param("idAmministrazione") String idAmministrazione);
}
