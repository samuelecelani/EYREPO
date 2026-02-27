package it.ey.piao.api.service;

import it.ey.dto.OVPDTO;
import it.ey.dto.OVPMatriceDataDTO;
import it.ey.entity.OVP;

import java.util.List;

public interface IOVPService {
    void saveOrUpdate(OVPDTO ovp);
    List<OVPDTO> saveOrUpdateAll(List<OVPDTO> ovpList);
    List<OVPDTO> findAllOVPBySezione(Long idSezione);
    List<OVPDTO> findAllOVPByPiao(Long piaoId);
    void deleteById(Long id);
    OVPMatriceDataDTO findOVPMatriceData(Long idSezione, Long idSezione1, Long idPiao);
    OVPDTO enrichWithRelations(OVP entity); // Usato solo per il salvataggio
    OVPDTO loadMongoDataForOVP(OVPDTO ovpDTO); // Carica solo i dati MongoDB (indicatori)
    int updateValoreIndiceAndDescrizione(Long idOvp, Long valoreIndice, String descrizioneIndice);
}
