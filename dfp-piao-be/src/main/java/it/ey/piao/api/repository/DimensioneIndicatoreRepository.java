package it.ey.piao.api.repository;

import it.ey.entity.DimensioneIndicatore;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DimensioneIndicatoreRepository extends BaseRepository<DimensioneIndicatore, Long> {

    @Query("SELECT d FROM DimensioneIndicatore d WHERE d.codTipologiaFK LIKE CONCAT(:codTipologiaFK, '%')")
    List<DimensioneIndicatore> findByCodTipologiaFKStartingWith(String codTipologiaFK);

}
