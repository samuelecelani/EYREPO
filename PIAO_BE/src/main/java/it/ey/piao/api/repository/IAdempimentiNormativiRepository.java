package it.ey.piao.api.repository;

import it.ey.entity.AdempimentiNormativi;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAdempimentiNormativiRepository extends BaseRepository<AdempimentiNormativi, Long>
{
    AdempimentiNormativi getAdempimentiNormativiBySezione23(Sezione23 sezione23);
}
