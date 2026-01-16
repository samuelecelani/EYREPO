package it.ey.piao.api.service.event;

import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.ISezione1Repository;
import it.ey.piao.api.repository.ISezione21Repository;
import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.repository.mongo.*;
import it.ey.piao.api.service.IAdditionalInfoService;
import it.ey.piao.api.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class TransactionEventListener {

    private final Map<Class<?>, Consumer<Object>> handlers = new HashMap<>();

    //  thread-safe se gli eventi arrivano in concorrenza
    private final Map<Class<?>, List<Object>> previousStates = new ConcurrentHashMap<>();




    @Autowired
    public TransactionEventListener(PiaoRepository piaoRepository,
                                    ITestService testService,
                                    IAdditionalInfoService additionalInfoService,
                                    GenericMapper genericMapper,
                                    ISezione1Repository sezione1Repository,
                                    ISezione21Repository sezione21Repository,
                                    ISocialRepository socialRepository,
                                    IUlterioriInfoRepository ulterioriInfoRepository,
                                    ISwotPuntiForzaRepository swotPuntiForzaRepository,
                                    ISwotPuntiDebolezzaRepository swotPuntiDebolezzaRepository,
                                    ISwotOpportunitaRepository swotOpportunitaRepository,
                                    ISwotMinacceRepository swotMinacceRepository

    ) {



        // Handler per rollback PiaoDTO
        handlers.put(Piao.class, obj -> {
            Piao previous = lastPrev(Piao.class);

            // correlati: Social e UlterioriInfo
            List<Social> previousSocials = prevList(Social.class);
            List<UlterioriInfo> previousUlterioriInfos = prevList(UlterioriInfo.class);

            if (!previousSocials.isEmpty()) {
                System.out.println("Ripristino Socials...");
                previousSocials.forEach(socialRepository::save);
            }

            if (!previousUlterioriInfos.isEmpty()) {
                System.out.println("Ripristino UlterioriInfoDocument...");
                previousUlterioriInfos.forEach(ulterioriInfoRepository::save);
            }

            // Se non ci sono snapshot per Piao, fallback alla delete
            if (previous == null) {
                Piao entity = (Piao) obj;

                piaoRepository.findById(entity.getId()).ifPresent(piaoRepository::delete);

                Sezione1 sezione1 = sezione1Repository.findByPiao(Piao.builder().id(entity.getId()).build());
                if (sezione1 != null) {
                    sezione1Repository.delete(sezione1);

                    // Se non ho snapshot di Social, cancello eventuali Social collegati
                    if (previousSocials.isEmpty()) {
                        Optional.ofNullable(socialRepository.getByExternalId(sezione1.getId()))
                            .ifPresent(e -> socialRepository.deleteByExternalId(sezione1.getId()));
                    }
                }

                // Se non ho snapshot di UlterioriInfo, cancello eventuali collegati
                if (previousUlterioriInfos.isEmpty()) {
                    Optional.ofNullable(ulterioriInfoRepository.getByExternalId(entity.getId()))
                        .ifPresent(e -> ulterioriInfoRepository.deleteByExternalId(entity.getId()));
                }
            }
        });
        handlers.put(Sezione1.class, obj -> {
            Sezione1 previousSezione1 = lastPrev(Sezione1.class);
            List<UlterioriInfo> previousUlterioriInfo = prevList(UlterioriInfo.class);
            List<Social> previousSocials = prevList(Social.class);
            if (previousSezione1 != null) {
                //TODO: Valutare se ha senso avere un roolback anche della parte sql

    //            if (previousSezione1 != null) {
    //                System.out.println("Ripristino Sezione1...");
    //                sezione1Repository.save(previousSezione1);
    //            }
                // NoSQL correlati: ripristina tutti gli snapshot disponibili
                if (!previousUlterioriInfo.isEmpty()) {
                    System.out.println("Ripristino UlterioriInfo...");
                  Optional.ofNullable(  ulterioriInfoRepository
                        .findByExternalIdAndTipoSezione(previousSezione1.getId(), Sezione.SEZIONE_1)).ifPresent(
                     u-> ulterioriInfoRepository.saveAll(previousUlterioriInfo)
                  );

                }
                if (!previousSocials.isEmpty()) {
                    System.out.println("Ripristino Socials...");
                    Optional.ofNullable( socialRepository.getByExternalId(previousSezione1.getId()))
                        .ifPresent(s ->socialRepository.saveAll(previousSocials));
                }
            }
                Sezione1 entity = (Sezione1) obj;
                if (previousUlterioriInfo.isEmpty()) {
                    Optional.ofNullable(ulterioriInfoRepository.getByExternalId(entity.getId()))
                        .ifPresent(e -> ulterioriInfoRepository.deleteByExternalId(entity.getId()));
                }
                if (previousSocials.isEmpty()) {
                    Optional.ofNullable(socialRepository.getByExternalId(entity.getId()))
                        .ifPresent(e -> socialRepository.deleteByExternalId(entity.getId()));
                }



        });

        handlers.put(Sezione21.class, obj -> {
            // ultimo o tutti?
            Sezione21 previousSezione21 = lastPrev(Sezione21.class);

            List<UlterioriInfo> previousUlterioriInfo = prevList(UlterioriInfo.class);
            List<SwotPuntiForza> previousSwotPuntiForza = prevList(SwotPuntiForza.class);
            List<SwotPuntiDebolezza> previousSwotDebolezza = prevList(SwotPuntiDebolezza.class);
            List<SwotOpportunita> previousSwotOpportunita = prevList(SwotOpportunita.class);
            List<SwotMinacce> previousSwotMinacce = prevList(SwotMinacce.class);
            if (previousSezione21 != null && previousSezione21.getId() != null) {
                //TODO: Valutare se ha senso avere un roolback anche della parte sql
//            if (previousSezione21 != null) {
//                System.out.println("Ripristino Sezione21...");
//                sezione21Repository.save(previousSezione21);
//            }

                // NoSQL correlati: ripristina tutti gli snapshot disponibili
                if (!previousUlterioriInfo.isEmpty()) {
                    System.out.println("Ripristino UlterioriInfo...");
                    Optional.ofNullable(ulterioriInfoRepository
                        .findByExternalIdAndTipoSezione(previousSezione21.getId(), Sezione.SEZIONE_1)).ifPresent(
                        u-> ulterioriInfoRepository.saveAll(previousUlterioriInfo)
                    );

                }
                if (!previousSwotPuntiForza.isEmpty()) {
                    System.out.println("Ripristino SwotPuntiForza...");
                Optional.ofNullable(swotPuntiForzaRepository
                        .getByExternalId(previousSezione21.getId()))
                    .ifPresent(s ->swotPuntiForzaRepository.saveAll(previousSwotPuntiForza));
                }
                if (!previousSwotDebolezza.isEmpty()) {
                    System.out.println("Ripristino SwotPuntiDebolezza...");
                    Optional.ofNullable(swotPuntiDebolezzaRepository
                        .getByExternalId(previousSezione21.getId()))
                        .ifPresent(s ->swotPuntiDebolezzaRepository.saveAll(previousSwotDebolezza));
                }
                if (!previousSwotOpportunita.isEmpty()) {
                    System.out.println("Ripristino SwotOpportunita...");
                    Optional.ofNullable(swotOpportunitaRepository
                        .getByExternalId(previousSezione21.getId()))
                        .ifPresent(s ->swotOpportunitaRepository.saveAll(previousSwotOpportunita));
                }
                if (!previousSwotMinacce.isEmpty()) {
                    System.out.println("Ripristino SwotMinacce...");
                    Optional.ofNullable(   swotMinacceRepository.getByExternalId(previousSezione21.getId()))
                        .ifPresent(s ->swotMinacceRepository.saveAll(previousSwotMinacce));
                }
            }
            //TODO: Valutare se ha senso avere un roolback anche della parte sql

                // Fallback: nessuno snapshot di Sezione21 -> cancella
                Sezione21 entity = (Sezione21) obj;
//                if (previousSezione21 == null) {
//                    sezione1Repository.findById(entity.getId()).ifPresent(sezione1Repository::delete);
//                }
                if (previousSwotPuntiForza.isEmpty()) {
                    Optional.ofNullable(swotPuntiForzaRepository.getByExternalId(entity.getId()))
                        .ifPresent(e -> swotPuntiForzaRepository.deleteByExternalId(entity.getId()));
                }
                if (previousSwotDebolezza.isEmpty()) {
                    Optional.ofNullable(swotPuntiDebolezzaRepository.getByExternalId(entity.getId()))
                        .ifPresent(e -> swotPuntiDebolezzaRepository.deleteByExternalId(entity.getId()));
                }
                if (previousSwotOpportunita.isEmpty()) {
                    Optional.ofNullable(swotOpportunitaRepository.getByExternalId(entity.getId()))
                        .ifPresent(e -> swotOpportunitaRepository.deleteByExternalId(entity.getId()));
                }
                if (previousSwotMinacce.isEmpty()) {
                    Optional.ofNullable(swotMinacceRepository.getByExternalId(entity.getId()))
                        .ifPresent(e -> swotMinacceRepository.deleteByExternalId(entity.getId()));
                }
                if (previousUlterioriInfo.isEmpty()) {
                    Optional.ofNullable(ulterioriInfoRepository.getByExternalId(entity.getId()))
                        .ifPresent(e -> ulterioriInfoRepository.deleteByExternalId(entity.getId()));
                }


        });

    }

    @EventListener
    public void onBeforeUpdate(BeforeUpdateEvent<?> event) {
        Class<?> type = event.getType();
        List<?> prevList = event.getPreviousState();

        // Accumula nella mappa
        previousStates
            .computeIfAbsent(type, t -> new ArrayList<>())
            .addAll(prevList);

    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onFailure(TransactionFailureEvent<?> event) {
        Object source = event.getSource();
        Consumer<Object> handler = handlers.get(source.getClass());
        if (handler != null) {
            handler.accept(source);
            previousStates.clear();
        } else {
            System.err.println("Nessun handler per tipo: " + source.getClass());
        }
    }

    @EventListener
    public void onSuccess(TransactionSuccessEvent<?> event) {
        previousStates.clear(); // pulizia dopo successo
        System.out.println("Transazione completata con successo per: " + event.getSource().getClass().getSimpleName());
    }




    @SuppressWarnings("unchecked")
    private <E> List<E> prevList(Class<E> type) {
        return (List<E>) previousStates.getOrDefault(type, List.of());
    }

    @SuppressWarnings("unchecked")
    private <E> E lastPrev(Class<E> type) {
        List<?> list = previousStates.get(type);
        if (list == null || list.isEmpty()) return null;
        return (E) list.get(list.size() - 1); // ultimo snapshot
    }

    private boolean isEmptyPrev(Class<?> type) {
        List<?> list = previousStates.get(type);
        return list == null || list.isEmpty();
    }

    private <T> Set<String> extractIds(List<T> list, Function<T, String> idExtractor) {
        return list.stream()
            .map(idExtractor)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

}

