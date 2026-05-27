package it.ey.piao.bff.service;

import it.ey.dto.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IObiettiviRisultatiFotografiaService {

    Mono<GenericResponseDTO<ObiettiviRisultatiFotografiaDTO>> saveOrUpdate(ObiettiviRisultatiFotografiaDTO obiettiviRisultatiFotografia);

    Mono<GenericResponseDTO<List<ObiettiviRisultatiFotografiaDTO>>> getObiettiviRisultatiBySezione332(Long idSezione332);

    Mono<GenericResponseDTO<List<ObiettiviRisultatiFotografiaDTO>>> getFotografieFormazioneBySezione332(Long idSezione332);

    Mono<Void> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione);

}
