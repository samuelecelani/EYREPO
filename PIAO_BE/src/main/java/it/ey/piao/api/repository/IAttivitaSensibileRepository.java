package it.ey.piao.api.repository;

import it.ey.entity.AttivitaSensibile;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAttivitaSensibileRepository  extends BaseRepository <AttivitaSensibile,Long> {

    List<AttivitaSensibile> getAttivitaSensibileBySezione23(Sezione23 sezione23);
}
