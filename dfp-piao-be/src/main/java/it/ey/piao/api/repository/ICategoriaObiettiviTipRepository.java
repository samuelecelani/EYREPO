package it.ey.piao.api.repository;

import it.ey.entity.CategoriaObiettiviTip;
import it.ey.enums.CodTipologiaCategoria;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ICategoriaObiettiviTipRepository extends BaseRepository<CategoriaObiettiviTip, Long> {

    @Query("SELECT c FROM CategoriaObiettiviTip c WHERE c.codTipologiaFK = :codTipologiaFK")
    List<CategoriaObiettiviTip> getAllCategoriaObiettiviTipPerCodTipologiaFK(@Param("codTipologiaFK") CodTipologiaCategoria codTipologiaFK);
}
