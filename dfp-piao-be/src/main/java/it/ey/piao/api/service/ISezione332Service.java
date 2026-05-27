package it.ey.piao.api.service;

import it.ey.dto.*;

import java.util.List;

public interface ISezione332Service extends ISezioneBaseService<Sezione332DTO>
{
    List<TipologiaAttivitaDTO> getTipologiaAttivita();

    List<AmbitoCompetenzaDTO> getAmbitoCompetenza();

    List<AreaTematicaDTO> getAreaTematica();

    List<TipologiaDestinatariDTO> getTipologiaDestinatari();
}
