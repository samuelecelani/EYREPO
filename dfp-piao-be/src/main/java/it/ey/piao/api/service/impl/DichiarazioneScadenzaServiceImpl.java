package it.ey.piao.api.service.impl;

import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.SollecitiDichiarazioniDFPDTO;
import it.ey.dto.StoricoDichiarazioneDFPDTO;
import it.ey.entity.DichiarazioneScadenza;
import it.ey.entity.Piao;
import it.ey.enums.StatoDichiarazioneEnum;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.DichiarazioneScadenzaMapper;
import it.ey.piao.api.mapper.MotivazioneDichiarazioneMapper;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.StoricoDichiarazioneDFPMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IDichiarazioneScadenzaRepository;
import it.ey.piao.api.repository.IMotivazioneDichiarazioneRepository;
import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.repository.projection.SollecitoDichiarazioneProjection;
import it.ey.piao.api.service.IDichiarazioneScadenzaService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DichiarazioneScadenzaServiceImpl implements IDichiarazioneScadenzaService
{
    private final IDichiarazioneScadenzaRepository dichiarazioneScadenzaRepository;
    private final DichiarazioneScadenzaMapper dichiarazioneScadenzaMapper;
    private final IMotivazioneDichiarazioneRepository motivazioneDichiarazioneRepository;
    private final PiaoRepository piaoRepository;
    private final StoricoDichiarazioneDFPMapper storicoDichiarazioneDFPMapper;

    private static final Logger log = LoggerFactory.getLogger(DichiarazioneScadenzaServiceImpl.class);

    public DichiarazioneScadenzaServiceImpl(IDichiarazioneScadenzaRepository dichiarazioneScadenzaRepository,
                                            DichiarazioneScadenzaMapper dichiarazioneScadenzaMapper,
                                            IMotivazioneDichiarazioneRepository motivazioneDichiarazioneRepository,
                                            PiaoRepository piaoRepository,
                                            StoricoDichiarazioneDFPMapper storicoDichiarazioneDFPMapper)
    {
        this.dichiarazioneScadenzaRepository = dichiarazioneScadenzaRepository;
        this.dichiarazioneScadenzaMapper = dichiarazioneScadenzaMapper;
        this.motivazioneDichiarazioneRepository = motivazioneDichiarazioneRepository;
        this.piaoRepository = piaoRepository;
        this.storicoDichiarazioneDFPMapper = storicoDichiarazioneDFPMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DichiarazioneScadenzaDTO saveOrUpdate(DichiarazioneScadenzaDTO dto)
    {
        // Verifica che il dto passato esista

        if (dto == null)
        {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        // Verifica che il codice PAFK esista

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        DichiarazioneScadenzaDTO response;
        try {
                // DTO in entity JPA
                DichiarazioneScadenza entity = dichiarazioneScadenzaMapper.toEntity(dto,context);

                // Relazione con MotivazioneDichiarazione
                if (dto.getIdMotivazioneDichiarazione() != null)
                {
                    entity.setMotivazioneDichiarazione(motivazioneDichiarazioneRepository.getReferenceById(dto.getIdMotivazioneDichiarazione()));
                }

                // Salvo l'entity principale nel DB relazionale
                DichiarazioneScadenza savedEntity = dichiarazioneScadenzaRepository.save(entity);

                // Mappo l'entity salvata in DTO di risposta
                response = dichiarazioneScadenzaMapper.toDto(savedEntity,context);

            } catch (Exception e) {
                log.error("Errore durante Save o update per DichiarazioneScadenza id={}: {}", dto.getId(), e.getMessage(), e);
                throw new RuntimeException("Errore durante il save o update della DichiarazioneScadenza", e);
            }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DichiarazioneScadenzaDTO getExistingDichiarazioneScadenza(String codPAFK)
    {
        if (!StringUtils.isNotBlank(codPAFK))
        {
            log.error("CodPAFK mancante nel PiaoDTO");
            throw new IllegalArgumentException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        LocalDate today = LocalDate.now();
        LocalDate targetDate = LocalDate.of(today.getYear(), Month.DECEMBER, 1);
        LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
        boolean isNewYear = !today.isBefore(targetDate);
        LocalDate endOfYear =  isNewYear ?  LocalDate.of(today.getYear() + 1, Month.DECEMBER, 1) : LocalDate.now().withMonth(12).withDayOfMonth(31) ;

        log.info("Ricerca PIAO per PA={} tra {} e {}",codPAFK, startOfYear, endOfYear);

        try
        {
            Piao existing = piaoRepository.findPiaoByMancataDichiarazione(
                codPAFK, startOfYear, endOfYear, 7L
            );

            if(existing == null)
            {
                log.error("CodPAFK mancante nel PiaoDTO");
                throw new IllegalArgumentException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
            }

            DichiarazioneScadenza d = dichiarazioneScadenzaRepository.findByPiao_Id(existing.getId());
            if(d != null)
            {
                return dichiarazioneScadenzaMapper.toDto(d, context);
            }
            return(DichiarazioneScadenzaDTO.builder()
                .idPiao(existing.getId())
                .build());
        } catch (Exception e) {
            log.error("Errore durante il get per codPAFK ={}: {}", codPAFK, e.getMessage(), e);
            throw new RuntimeException("Errore durante il get della DichiarazioneScadenza", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DichiarazioneScadenzaDTO findByIdPiao(Long idPiao)
    {
        if (idPiao == null) {
            throw new IllegalArgumentException("L'idPiao non può essere nullo");
        }
        log.info("Recupero DichiarazioneScadenza per idPiao={}", idPiao);
        try {
            DichiarazioneScadenza d = dichiarazioneScadenzaRepository.findByPiao_Id(idPiao);
            if (d == null) {
                log.info("Nessuna DichiarazioneScadenza trovata per idPiao={}", idPiao);
                return null;
            }
            return dichiarazioneScadenzaMapper.toDto(d, new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore durante il recupero della DichiarazioneScadenza per idPiao={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della DichiarazioneScadenza per idPiao", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long id)
    {
        // Verifica se l'id esiste

        if (id == null)
        {
            throw new IllegalArgumentException("L'ID della DichiarazioneScadenza non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<DichiarazioneScadenza> existing = dichiarazioneScadenzaRepository.findById(id);
            if (existing.isEmpty())
            {
                log.warn("Tentativo di cancellare un DichiarazioneScadenza non esistente con id={}", id);
                throw new RuntimeException("DichiarazioneScadenza non trovato con id: " + id);
            }

            // Cancellazione da Postgres
            dichiarazioneScadenzaRepository.softDeleteById(id, LocalDateTime.now());

            log.info("DichiarazioneScadenza con id={} cancellato con successo", id);

        } catch (Exception e)
        {
            log.error("Errore durante la cancellazione della DichiarazioneScadenza id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione della DichiarazioneScadenza", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoricoDichiarazioneDFPDTO> findAllStorico()
    {
        log.info("Recupero storico dichiarazioni DFP");
        try {
            return storicoDichiarazioneDFPMapper.toDtoList(dichiarazioneScadenzaRepository.findAllStorico());
        } catch (Exception e) {
            log.error("Errore durante il recupero dello storico dichiarazioni DFP: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dello storico dichiarazioni DFP", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateStato(Long id, Boolean stato)
    {
        if (id == null) {
            throw new IllegalArgumentException("L'ID della DichiarazioneScadenza non può essere nullo");
        }
        try {
            Optional<DichiarazioneScadenza> existing = dichiarazioneScadenzaRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("DichiarazioneScadenza non trovata con id={}", id);
                throw new RuntimeException("DichiarazioneScadenza non trovata con id: " + id);
            }
            dichiarazioneScadenzaRepository.updateStato(id, stato);
        } catch (Exception e) {
            log.error("Errore durante l'aggiornamento dello stato per id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante l'aggiornamento dello stato della DichiarazioneScadenza", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SollecitiDichiarazioniDFPDTO> searchDichiarazioni(String denominazionePiao,
                                                                  String tipologiaIstat,
                                                                  String codPAFK,
                                                                  StatoDichiarazioneEnum statoDichiarazione)
    {
        if (StringUtils.isBlank(denominazionePiao))
        {
            throw new IllegalArgumentException("La denominazione del PIAO è obbligatoria");
        }

        // Normalizzazione filtri opzionali (stringhe vuote -> null per coerenza nelle query)
        String tipologiaParam = StringUtils.isBlank(tipologiaIstat) ? null : tipologiaIstat;
        String codPAFKParam   = StringUtils.isBlank(codPAFK) ? null : codPAFK;

        log.info("Ricerca dichiarazioni: denominazione='{}', tipologiaIstat='{}', codPAFK='{}', stato='{}'",
            denominazionePiao, tipologiaParam, codPAFKParam, statoDichiarazione);

        try
        {
            List<Object[]> rows = dichiarazioneScadenzaRepository.searchDichiarazioniByPiao(
                denominazionePiao, codPAFKParam, tipologiaParam);

            List<SollecitiDichiarazioniDFPDTO> result = new ArrayList<>();

            for (Object[] row : rows)
            {
                Piao piao = (Piao) row[0];
                DichiarazioneScadenza ds = (DichiarazioneScadenza) row[1];

                boolean inviata = ds != null;

                // Filtro su stato dichiarazione
                if (statoDichiarazione == StatoDichiarazioneEnum.INVIATA && !inviata)
                {
                    continue;
                }
                if (statoDichiarazione == StatoDichiarazioneEnum.NON_INVIATA && inviata)
                {
                    continue;
                }

                SollecitiDichiarazioniDFPDTO dto = SollecitiDichiarazioniDFPDTO.builder()
                    .idPiao(piao.getId())
                    .codePA(piao.getCodPAFK())
                    .amministrazione(piao.getDenominazionePA())
                    .statoDichiarazione(inviata ? StatoDichiarazioneEnum.INVIATA : StatoDichiarazioneEnum.NON_INVIATA)
                    .build();

                result.add(dto);
            }

            return result;
        }
        catch (Exception e)
        {
            log.error("Errore durante la ricerca delle dichiarazioni: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca delle dichiarazioni", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SollecitiDichiarazioniDFPDTO> searchDichiarazioniPaged(String denominazionePiao,
                                                                       String tipologiaIstat,
                                                                       String codPAFK,
                                                                       StatoDichiarazioneEnum statoDichiarazione,
                                                                       Pageable pageable)
    {
        if (StringUtils.isBlank(denominazionePiao))
        {
            throw new IllegalArgumentException("La denominazione del PIAO è obbligatoria");
        }

        String tipologiaParam = StringUtils.isBlank(tipologiaIstat) ? null : tipologiaIstat;
        String codPAFKParam   = StringUtils.isBlank(codPAFK) ? null : codPAFK;
        // Filtro stato applicato in SQL via EXISTS (null = nessun filtro)
        Boolean inviataParam  = (statoDichiarazione == null)
            ? null
            : (statoDichiarazione == StatoDichiarazioneEnum.INVIATA);

        log.info("Ricerca PAGED dichiarazioni: denominazione='{}', tipologia='{}', codPAFK='{}', stato='{}', page={}, size={}, sort={}",
            denominazionePiao, tipologiaParam, codPAFKParam, statoDichiarazione,
            pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        // Se il client chiede un sort che include 'statoDichiarazione' (campo derivato via EXISTS,
        // NON presente sull'entity Piao), non possiamo affidarci all'ORDER BY del DB. In tal caso
        // carichiamo tutti i risultati filtrati e ordiniamo+paginiamo lato Java.
        Sort.Order statoOrder = findStatoDichiarazioneOrder(pageable);
        if (statoOrder != null) {
            log.info(">>> statoDichiarazione order rilevato: property={}, direction={}, isDescending={}",
                statoOrder.getProperty(), statoOrder.getDirection(), statoOrder.isDescending());
            return searchPagedWithJavaSort(denominazionePiao, tipologiaParam, codPAFKParam, inviataParam, pageable, statoOrder);
        }

        // Path "fast" classico: sort gestito interamente dal DB (solo per campi reali di Piao).
        Pageable safePageable = sanitizeSort(pageable);

        try
        {
            Page<SollecitoDichiarazioneProjection> page = dichiarazioneScadenzaRepository.searchDichiarazioniByPiaoPaged(
                denominazionePiao, codPAFKParam, tipologiaParam, inviataParam, safePageable);

            return page.map(this::toSollecitoDTO);
        }
        catch (Exception e)
        {
            log.error("Errore durante la ricerca PAGED delle dichiarazioni: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca PAGED delle dichiarazioni", e);
        }
    }

    /**
     * Path con ordinamento per {@code statoDichiarazione} eseguito lato Java:
     * <ol>
     *   <li>carica TUTTE le righe filtrate (un'unica pagina con size enorme, sort applicato solo
     *       sui campi reali di Piao per stabilità dell'ordine secondario)</li>
     *   <li>ordina la lista per 'inviata' (asc/desc) come criterio primario, mantenendo
     *       gli ordinamenti secondari richiesti dal client (se mappabili)</li>
     *   <li>esegue lo slicing manuale sulla pagina richiesta</li>
     * </ol>
     */
    private Page<SollecitiDichiarazioniDFPDTO> searchPagedWithJavaSort(String denominazionePiao,
                                                                      String tipologiaIstat,
                                                                      String codPAFK,
                                                                      Boolean inviataParam,
                                                                      Pageable originalPageable,
                                                                      Sort.Order statoOrder)
    {
        log.info("Sort per 'statoDichiarazione' richiesto ({}): fetch completo + sort+slice lato Java",
            statoOrder.getDirection());

        // Costruisco un sort DB con SOLO gli ordinamenti secondari supportati (escludo statoDichiarazione).
        // Serve per avere un secondary sort stabile prima di applicare il sort primario in Java.
        Sort dbSort = buildDbSortExcludingStato(originalPageable);
        Pageable unpaged = PageRequest.of(0, Integer.MAX_VALUE, dbSort);

        try
        {
            Page<SollecitoDichiarazioneProjection> all = dichiarazioneScadenzaRepository.searchDichiarazioniByPiaoPaged(
                denominazionePiao, codPAFK, tipologiaIstat, inviataParam, unpaged);

            // Mappa in DTO
            List<SollecitiDichiarazioniDFPDTO> mapped = new ArrayList<>(all.getContent().size());
            for (SollecitoDichiarazioneProjection row : all.getContent()) {
                mapped.add(toSollecitoDTO(row));
            }

            // Ordino per statoDichiarazione (NON_INVIATA < INVIATA in ordine naturale dell'enum).
            // Comparator esplicito (Integer.compare + flip su desc) per evitare ambiguità di type inference.
            final boolean desc = statoOrder.isDescending();
            mapped.sort((a, b) -> {
                int ordA = a.getStatoDichiarazione() != null ? a.getStatoDichiarazione().ordinal() : Integer.MAX_VALUE;
                int ordB = b.getStatoDichiarazione() != null ? b.getStatoDichiarazione().ordinal() : Integer.MAX_VALUE;
                int cmp = Integer.compare(ordA, ordB);
                return desc ? -cmp : cmp;
            });

            if (!mapped.isEmpty()) {
                log.info(">>> Java-sort applicato (desc={}): first={}, last={}, total={}",
                    desc, mapped.get(0).getStatoDichiarazione(),
                    mapped.get(mapped.size() - 1).getStatoDichiarazione(),
                    mapped.size());
            }

            // Slicing manuale sulla pagina richiesta
            int total = mapped.size();
            int from = (int) Math.min((long) originalPageable.getPageNumber() * originalPageable.getPageSize(), total);
            int to = (int) Math.min(from + (long) originalPageable.getPageSize(), total);
            List<SollecitiDichiarazioniDFPDTO> slice = mapped.subList(from, to);

            return new PageImpl<>(slice, originalPageable, total);
        }
        catch (Exception e)
        {
            log.error("Errore durante la ricerca PAGED (java-sort) delle dichiarazioni: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca PAGED delle dichiarazioni", e);
        }
    }

    private SollecitiDichiarazioniDFPDTO toSollecitoDTO(SollecitoDichiarazioneProjection row)
    {
        return SollecitiDichiarazioniDFPDTO.builder()
            .idPiao(row.getIdPiao())
            .codePA(row.getCodePA())
            .amministrazione(row.getAmministrazione())
            .statoDichiarazione(Boolean.TRUE.equals(row.getInviata())
                ? StatoDichiarazioneEnum.INVIATA
                : StatoDichiarazioneEnum.NON_INVIATA)
            .build();
    }

    /** Restituisce l'eventuale Sort.Order per la property 'statoDichiarazione', null se assente. */
    private Sort.Order findStatoDichiarazioneOrder(Pageable pageable)
    {
        if (pageable == null || pageable.getSort().isUnsorted()) return null;
        for (Sort.Order o : pageable.getSort()) {
            if ("statoDichiarazione".equals(o.getProperty())) return o;
        }
        return null;
    }

    /**
     * Costruisce un Sort per la query DB tenendo solo le property supportate, escludendo
     * 'statoDichiarazione' (che verrà gestito in Java).
     */
    private Sort buildDbSortExcludingStato(Pageable pageable)
    {
        if (pageable == null || pageable.getSort().isUnsorted()) return Sort.unsorted();
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order o : pageable.getSort()) {
            if ("statoDichiarazione".equals(o.getProperty())) continue; // gestito lato Java
            String mapped = ALLOWED_SORT_PROPERTIES.get(o.getProperty());
            if (mapped != null) {
                orders.add(new Sort.Order(o.getDirection(), mapped));
            }
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    /**
     * Sort properties consentite (mappano i campi della proiezione / Piao):
     * il client può ordinare per uno di questi. Eventuali altre property vengono ignorate
     * (in particolare 'statoDichiarazione' che è calcolato via EXISTS e non è un attributo JPA di Piao).
     * <p>
     * La chiave è la property accettata dal client (FE), il value è la property JPQL effettiva
     * dell'entity {@code Piao}.
     */
    private static final Map<String, String> ALLOWED_SORT_PROPERTIES = Map.of(
        "idPiao",          "id",
        "codePA",          "codPAFK",
        "amministrazione", "denominazionePA",
        // alias passthrough sui campi reali
        "id",              "id",
        "codPAFK",         "codPAFK",
        "denominazionePA", "denominazionePA"
    );

    /**
     * Filtra/traduce gli ordinamenti del Pageable conservando solo le property supportate
     * dalla query JPQL. Se nessun ordinamento valido resta, ritorna un Pageable senza sort
     * (la query userà l'ORDER BY di default).
     */
    private Pageable sanitizeSort(Pageable pageable)
    {
        if (pageable == null || pageable.getSort() == null || pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> safeOrders = new ArrayList<>();
        for (Sort.Order o : pageable.getSort()) {
            String mapped = ALLOWED_SORT_PROPERTIES.get(o.getProperty());
            if (mapped != null) {
                safeOrders.add(new Sort.Order(o.getDirection(), mapped));
            } else {
                log.warn("Sort property '{}' non supportata sulla ricerca paginata: verrà ignorata. " +
                    "Property supportate: {}", o.getProperty(), ALLOWED_SORT_PROPERTIES.keySet());
            }
        }

        Sort safeSort = safeOrders.isEmpty() ? Sort.unsorted() : Sort.by(safeOrders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    }
}
