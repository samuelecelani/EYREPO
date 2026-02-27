package it.ey.piao.api.service.event;

import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.IAdempimentoRepository;
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

    // Thread-local per isolare gli snapshot per ogni transazione
    private static final ThreadLocal<Map<Class<?>, List<Object>>> previousStates =
        ThreadLocal.withInitial(ConcurrentHashMap::new);

    @Autowired
    public TransactionEventListener(ISocialRepository socialRepository,
                                    IUlterioriInfoRepository ulterioriInfoRepository,
                                    ISwotPuntiForzaRepository swotPuntiForzaRepository,
                                    ISwotPuntiDebolezzaRepository swotPuntiDebolezzaRepository,
                                    ISwotOpportunitaRepository swotOpportunitaRepository,
                                    ISwotMinacceRepository swotMinacceRepository,
                                    IContributoreInternoRepository contributoreInternoRepository,
                                    IAzioneRepository azioneRepository,
                                    ILogoRepository logoRepository,
                                    IAttoreRepository attoreRepository
    ) {


        // Handler per rollback PiaoDTO
        //TODO : Valutare se ha senso mantenere questo handler
//        handlers.put(Piao.class, obj -> {
//            Piao previous = lastPrev(Piao.class);
//
//            // correlati: Social e UlterioriInfo
//            List<Social> previousSocials = prevList(Social.class);
//            List<UlterioriInfo> previousUlterioriInfos = prevList(UlterioriInfo.class);
//
//            if (!previousSocials.isEmpty()) {
//                System.out.println("Ripristino Socials...");
//                previousSocials.forEach(socialRepository::save);
//            }
//
//            if (!previousUlterioriInfos.isEmpty()) {
//                System.out.println("Ripristino UlterioriInfoDocument...");
//                previousUlterioriInfos.forEach(ulterioriInfoRepository::save);
//            }
//
//            // Se non ci sono snapshot per Piao, fallback alla delete
//            if (previous == null) {
//                Piao entity = (Piao) obj;
//
//                piaoRepository.findById(entity.getId()).ifPresent(piaoRepository::delete);
//
//                Sezione1 sezione1 = sezione1Repository.findByPiao(Piao.builder().id(entity.getId()).build());
//                if (sezione1 != null) {
//                    sezione1Repository.delete(sezione1);
//
//                    // Se non ho snapshot di Social, cancello eventuali Social collegati
//                    if (previousSocials.isEmpty()) {
//                        Optional.ofNullable(socialRepository.getByExternalId(sezione1.getId()))
//                            .ifPresent(e -> socialRepository.deleteByExternalId(sezione1.getId()));
//                    }
//                }
//
//                // Se non ho snapshot di UlterioriInfo, cancello eventuali collegati
//                if (previousUlterioriInfos.isEmpty()) {
//                    Optional.ofNullable(ulterioriInfoRepository.getByExternalId(entity.getId()))
//                        .ifPresent(e -> ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(entity.getId(),Sezione.SEZIONE_1));
//                }
//            }
//        });
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
                    Optional.ofNullable(ulterioriInfoRepository
                        .findByExternalIdAndTipoSezione(previousSezione1.getId(), Sezione.SEZIONE_1)).ifPresent(
                        u -> ulterioriInfoRepository.saveAll(previousUlterioriInfo)
                    );

                }
                if (!previousSocials.isEmpty()) {
                    System.out.println("Ripristino Socials...");
                    Optional.ofNullable(socialRepository.getByExternalId(previousSezione1.getId()))
                        .ifPresent(s -> socialRepository.saveAll(previousSocials));
                }
            }
            Sezione1 entity = (Sezione1) obj;
            if (previousUlterioriInfo.isEmpty()) {
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_1))
                    .ifPresent(e -> ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_1));
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
                        .findByExternalIdAndTipoSezione(previousSezione21.getId(), Sezione.SEZIONE_21)).ifPresent(
                        u -> ulterioriInfoRepository.saveAll(previousUlterioriInfo)
                    );

                }
                if (!previousSwotPuntiForza.isEmpty()) {
                    System.out.println("Ripristino SwotPuntiForza...");
                    Optional.ofNullable(swotPuntiForzaRepository
                            .getByExternalId(previousSezione21.getId()))
                        .ifPresent(s -> swotPuntiForzaRepository.saveAll(previousSwotPuntiForza));
                }
                if (!previousSwotDebolezza.isEmpty()) {
                    System.out.println("Ripristino SwotPuntiDebolezza...");
                    Optional.ofNullable(swotPuntiDebolezzaRepository
                            .getByExternalId(previousSezione21.getId()))
                        .ifPresent(s -> swotPuntiDebolezzaRepository.saveAll(previousSwotDebolezza));
                }
                if (!previousSwotOpportunita.isEmpty()) {
                    System.out.println("Ripristino SwotOpportunita...");
                    Optional.ofNullable(swotOpportunitaRepository
                            .getByExternalId(previousSezione21.getId()))
                        .ifPresent(s -> swotOpportunitaRepository.saveAll(previousSwotOpportunita));
                }
                if (!previousSwotMinacce.isEmpty()) {
                    System.out.println("Ripristino SwotMinacce...");
                    Optional.ofNullable(swotMinacceRepository.getByExternalId(previousSezione21.getId()))
                        .ifPresent(s -> swotMinacceRepository.saveAll(previousSwotMinacce));
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
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_21))
                    .ifPresent(e -> ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_21));
            }
        });

        handlers.put(OVPStrategiaIndicatore.class, obj -> {
            OVPStrategiaIndicatore previousOVPStrategiaIndicatore = lastPrev(OVPStrategiaIndicatore.class);
            List<UlterioriInfo> previousUlterioriInfo = prevList(UlterioriInfo.class);

            if (previousOVPStrategiaIndicatore != null && previousOVPStrategiaIndicatore.getId() != null) {
                // NoSQL correlati: ripristina tutti gli snapshot disponibili
                if (!previousUlterioriInfo.isEmpty()) {
                    System.out.println("Ripristino UlterioriInfo...");
                    Optional.ofNullable(ulterioriInfoRepository
                        .findByExternalIdAndTipoSezione(previousOVPStrategiaIndicatore.getIndicatore().getId(), Sezione.SEZIONE_21_INDICATORE)).ifPresent(
                        u -> ulterioriInfoRepository.saveAll(previousUlterioriInfo)
                    );
                }
            }
            OVPStrategiaIndicatore entity = (OVPStrategiaIndicatore) obj;
            if (previousUlterioriInfo.isEmpty()) {
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(entity.getIndicatore().getId(), Sezione.SEZIONE_21_INDICATORE))
                    .ifPresent(e -> ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(entity.getIndicatore().getId(), Sezione.SEZIONE_21_INDICATORE));
            }
        });

        handlers.put(Sezione22.class, obj -> {

            Sezione22 previousSezione22 = lastPrev(Sezione22.class);
            List<UlterioriInfo> previousUlterioriInfo = prevList(UlterioriInfo.class);

            if (previousSezione22 != null && previousSezione22.getId() != null) {
                // NoSQL correlati: ripristina tutti gli snapshot disponibili
                if (!previousUlterioriInfo.isEmpty()) {
                    System.out.println("Ripristino UlterioriInfo...");
                    Optional.ofNullable(ulterioriInfoRepository
                        .findByExternalIdAndTipoSezione(previousSezione22.getId(), Sezione.SEZIONE_22)).ifPresent(
                        u -> ulterioriInfoRepository.saveAll(previousUlterioriInfo)
                    );
                }
            }
            Sezione22 entity = (Sezione22) obj;
            if (previousUlterioriInfo.isEmpty()) {
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_22))
                    .ifPresent(e -> ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_22));
            }

        });

        handlers.put(Sezione23.class, obj -> {

            Sezione23 previousSezione23 = lastPrev(Sezione23.class);
            List<UlterioriInfo> previousUlterioriInfo = prevList(UlterioriInfo.class);

            if (previousSezione23 != null && previousSezione23.getId() != null) {
                // NoSQL correlati: ripristina tutti gli snapshot disponibili
                if (!previousUlterioriInfo.isEmpty()) {
                    System.out.println("Ripristino UlterioriInfo...");
                    Optional.ofNullable(ulterioriInfoRepository
                        .findByExternalIdAndTipoSezione(previousSezione23.getId(), Sezione.SEZIONE_23)).ifPresent(
                        u -> ulterioriInfoRepository.saveAll(previousUlterioriInfo)
                    );
                }
            }
            Sezione23 entity = (Sezione23) obj;
            if (previousUlterioriInfo.isEmpty()) {
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_23))
                    .ifPresent(e -> ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_23));
            }

        });

        handlers.put(ObbiettivoPerformance.class, obj -> {
                ObbiettivoPerformance previousObbiettivoPerformance = lastPrev(ObbiettivoPerformance.class);
                List<ContributoreInterno> previousContributoriInterni = prevList(ContributoreInterno.class);
                if (previousObbiettivoPerformance != null && previousObbiettivoPerformance.getId() != null) {
                    if (!previousContributoriInterni.isEmpty()) {
                        System.out.println("Ripristino UlterioriInfo...");
                        Optional.ofNullable(contributoreInternoRepository
                            .getByExternalId(previousObbiettivoPerformance.getId())).ifPresent(
                            c -> contributoreInternoRepository.saveAll(previousContributoriInterni)
                        );
                    }
                }

            ObbiettivoPerformance entity = (ObbiettivoPerformance) obj;
            if (previousContributoriInterni.isEmpty()) {
                Optional.ofNullable(contributoreInternoRepository.getByExternalId(entity.getId()))
                    .ifPresent(e -> contributoreInternoRepository.deleteByExternalId(entity.getId()));
            }
            }
        );

        handlers.put(Adempimento.class, obj -> {
            Adempimento previousAdempimento = lastPrev(Adempimento.class);
            List<Azione> previousAzione = prevList(Azione.class);
            List<UlterioriInfo> previousUlterioriInfo = prevList(UlterioriInfo.class);
            if (previousAdempimento != null && previousAdempimento.getId() != null) {
                if (!previousAzione.isEmpty()) {
                    System.out.println("Ripristino Azione...");
                    Optional.ofNullable(azioneRepository
                        .getByExternalId(previousAdempimento.getId())).ifPresent(
                        a -> azioneRepository.saveAll(previousAzione)
                    );
                }

                if (!previousUlterioriInfo.isEmpty()) {
                    System.out.println("Ripristino UlterioriInfo...");
                    Optional.ofNullable(ulterioriInfoRepository
                        .findByExternalIdAndTipoSezione(previousAdempimento.getId(), Sezione.SEZIONE_22_ADEMPIMENTO)).ifPresent(
                        u -> ulterioriInfoRepository.saveAll(previousUlterioriInfo)
                    );
                }
            }

            Adempimento entity = (Adempimento) obj;
            if (previousAzione.isEmpty()) {
                Optional.ofNullable(azioneRepository.getByExternalId(entity.getId()))
                    .ifPresent(a -> azioneRepository.deleteByExternalId(entity.getId()));
            }

            if (previousUlterioriInfo.isEmpty()) {
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_22_ADEMPIMENTO))
                    .ifPresent(e -> ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_22_ADEMPIMENTO));
            }
        });

        handlers.put(Allegato.class, obj -> {
            Allegato previousAllegato = lastPrev(Allegato.class);
            List<Logo> previousLogo = prevList(Logo.class);

            if (previousAllegato != null && previousAllegato.getId() != null) {
                if (!previousLogo.isEmpty()) {
                    System.out.println("Ripristino Logo...");
                    Optional.ofNullable(logoRepository
                        .getByExternalId(previousAllegato.getId())).ifPresent(
                        a -> logoRepository.saveAll(previousLogo)
                    );
                }
            }
            Allegato  entity = (Allegato) obj;
            if (previousLogo.isEmpty()) {
                Optional.ofNullable(logoRepository.getByExternalId(entity.getId()))
                    .ifPresent(a -> logoRepository.deleteByExternalId(entity.getId()));
            }

        });
        handlers.put(AttivitaSensibile.class, obj -> {
            AttivitaSensibile previous = lastPrev(AttivitaSensibile.class);
            List<UlterioriInfo> previousUlterioriInfo = prevList(UlterioriInfo.class);
            List<Attore> previousAttori = prevList(Attore.class);

            // Ripristino snapshot Mongo se presenti
            if (previous != null && previous.getId() != null) {
                if (!previousUlterioriInfo.isEmpty()) {
                    System.out.println("Ripristino UlterioriInfo per AttivitaSensibile...");
                    Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(previous.getId(), Sezione.SEZIONE_23_ATTIVITASENSIBILE))
                        .ifPresent(u -> ulterioriInfoRepository.saveAll(previousUlterioriInfo));
                }
                if (!previousAttori.isEmpty()) {
                    System.out.println("Ripristino Attori per AttivitaSensibile...");
                    Optional.ofNullable(attoreRepository.getByExternalId(previous.getId()))
                        .ifPresent(a -> attoreRepository.saveAll(previousAttori));
                }
            }

            AttivitaSensibile entity = (AttivitaSensibile) obj;

            // Fallback cancellazione se non ci sono snapshot
            if (previousUlterioriInfo.isEmpty()) {
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_23_ATTIVITASENSIBILE))
                    .ifPresent(e -> ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_23_ATTIVITASENSIBILE));
            }
            if (previousAttori.isEmpty()) {
                Optional.ofNullable(attoreRepository.getByExternalId(entity.getId()))
                    .ifPresent(a -> attoreRepository.deleteByExternalId(entity.getId()));
            }
        });


        handlers.put(DatiPubblicati.class, obj -> {
            //ultimo snapshot della transazione precedente
            DatiPubblicati previous = lastPrev(DatiPubblicati.class);

            //reecupera eventuali Mongo collegati  alla transazione precedente
            List<UlterioriInfo> previousUlterioriInfo = prevList(UlterioriInfo.class);

            // ripristino snapshot se esistono
            if (previous != null && previous.getId() != null) {
                if (!previousUlterioriInfo.isEmpty()) {
                    System.out.println("Ripristino UlterioriInfo Dati Pubblicati...");

                   //  Mongo legati all'id della sezione e tipo SEZIONE_23_DATIPUBBLICATI
                    Optional.ofNullable(ulterioriInfoRepository
                            .findByExternalIdAndTipoSezione(previous.getId(), Sezione.SEZIONE_23_DATIPUBBLICATI))
                        .ifPresent(u ->

                                // Salvo  tutti i documenti precedenti per ripristinare lo stato
                            ulterioriInfoRepository.saveAll(previousUlterioriInfo));
                }
            }

            // pulizia se non ci sono snapshot
            DatiPubblicati entity = (DatiPubblicati) obj;
            if (previousUlterioriInfo.isEmpty()) {
                Optional.ofNullable(ulterioriInfoRepository
                        .findByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_23_DATIPUBBLICATI))
                    .ifPresent(e -> ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_23_DATIPUBBLICATI));
            }
        });

        handlers.put(Sezione4.class, obj -> {
            Sezione4 previousSezione4 = lastPrev(Sezione4.class);
            List<UlterioriInfo> previousUlterioriInfo = prevList(UlterioriInfo.class);

            if (previousSezione4 != null && previousSezione4.getId() != null) {
                // NoSQL correlati: ripristina tutti gli snapshot disponibili
                if (!previousUlterioriInfo.isEmpty()) {
                    System.out.println("Ripristino UlterioriInfo Sezione4...");
                    Optional.ofNullable(ulterioriInfoRepository
                        .findByExternalIdAndTipoSezione(previousSezione4.getId(), Sezione.SEZIONE_4)).ifPresent(
                        u -> ulterioriInfoRepository.saveAll(previousUlterioriInfo)
                    );
                }
            }

            Sezione4 entity = (Sezione4) obj;
            if (previousUlterioriInfo.isEmpty()) {
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_4))
                    .ifPresent(e -> ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(entity.getId(), Sezione.SEZIONE_4));
            }
        });

        handlers.put(SottofaseMonitoraggio.class, obj -> {
            SottofaseMonitoraggio previous = lastPrev(SottofaseMonitoraggio.class);
            List<Attore> previousAttori = prevList(Attore.class);

            // Ripristino snapshot Mongo se presenti
            if (previous != null && previous.getId() != null && !previousAttori.isEmpty()) {
                System.out.println("Ripristino Attori per SottofaseMonitoraggio...");
                Optional.ofNullable(attoreRepository.getByExternalId(previous.getId()))
                    .ifPresent(a -> attoreRepository.saveAll(previousAttori));
            }

            SottofaseMonitoraggio entity = (SottofaseMonitoraggio) obj;

            // Fallback cancellazione se non ci sono snapshot
            if (previousAttori.isEmpty()) {
                Optional.ofNullable(attoreRepository.getByExternalId(entity.getId()))
                    .ifPresent(a -> attoreRepository.deleteByExternalId(entity.getId()));
            }
        });





    }
    @EventListener
    public void onBeforeUpdate(BeforeUpdateEvent<?> event) {
        Class<?> type = event.getType();
        List<?> prevList = event.getPreviousState();

        // Accumula nella mappa thread-local
        previousStates.get()
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
        } else {
            System.err.println("Nessun handler per tipo: " + source.getClass());
        }
        // Pulisce il ThreadLocal per questo thread
        previousStates.remove();
    }

    @EventListener
    public void onSuccess(TransactionSuccessEvent<?> event) {
        // Pulisce il ThreadLocal per questo thread
        previousStates.remove();
        System.out.println("Transazione completata con successo per: " + event.getSource().getClass().getSimpleName());
    }




    @SuppressWarnings("unchecked")
    private <E> List<E> prevList(Class<E> type) {
        return (List<E>) previousStates.get().getOrDefault(type, List.of());
    }

    @SuppressWarnings("unchecked")
    private <E> E lastPrev(Class<E> type) {
        List<?> list = previousStates.get().get(type);
        if (list == null || list.isEmpty()) return null;
        return (E) list.get(list.size() - 1); // ultimo snapshot
    }

    private boolean isEmptyPrev(Class<?> type) {
        List<?> list = previousStates.get().get(type);
        return list == null || list.isEmpty();
    }

    private <T> Set<String> extractIds(List<T> list, Function<T, String> idExtractor) {
        return list.stream()
            .map(idExtractor)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

}

