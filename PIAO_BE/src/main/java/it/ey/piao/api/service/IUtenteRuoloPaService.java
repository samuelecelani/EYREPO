package it.ey.piao.api.service;


import it.ey.dto.UtenteRuoloPaDTO;

import java.util.List;

public interface IUtenteRuoloPaService {

    public  UtenteRuoloPaDTO create(UtenteRuoloPaDTO UtenteRuoloPaDTO);
    public  List<UtenteRuoloPaDTO> findByCodicePa(String codicePa);
    public void delete(Long id);
}
