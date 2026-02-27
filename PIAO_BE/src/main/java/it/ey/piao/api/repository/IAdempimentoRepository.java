package it.ey.piao.api.repository;

import it.ey.dto.AdempimentoDTO;
import it.ey.entity.Adempimento;
import it.ey.entity.Sezione22;
import it.ey.enums.TipologiaAdempimento;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAdempimentoRepository extends BaseRepository<Adempimento, Long>
{
    public Adempimento getAdempimentoBySezione22AndTipologia(Sezione22 sezione22, TipologiaAdempimento tipologia);
}
