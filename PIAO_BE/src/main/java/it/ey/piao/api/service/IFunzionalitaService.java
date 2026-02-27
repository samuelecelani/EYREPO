package it.ey.piao.api.service;


import it.ey.dto.FunzionalitaDTO;

import java.util.List;

public interface IFunzionalitaService {
    List<FunzionalitaDTO> getFunzionalitaByRuolo(List<String> ruoli);
}

