package it.ey.piao.bff.service;

import reactor.core.publisher.Mono;

public interface IPrincipioGuidaService {

    Mono<Void> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione);
}
