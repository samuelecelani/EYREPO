package it.ey.piao.api.service.impl;

import it.ey.dto.OVPDTO;
import it.ey.entity.*;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IOVPService;
import it.ey.utils.SezioneUtils; // usa la utils condivisa
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OVPServiceImpl implements IOVPService {

    private final OVPRepository ovpRepository;
    private final GenericMapper genericMapper;
    private final ISezione21Repository sezione21Repository;
    private final IAreaOrganizzativaRepository areaOrganizzativaRepository;
    private final IPrioritaPoliticaRepository prioritaPoliticaRepository;
    private final IStakeHolderRepository stakeHolderRepository;
    public OVPServiceImpl(OVPRepository ovpRepository, GenericMapper genericMapper, ISezione21Repository sezione21Repository, IAreaOrganizzativaRepository areaOrganizzativaRepository, IPrioritaPoliticaRepository prioritaPoliticaRepository, IStakeHolderRepository stakeHolderRepository) {
        this.ovpRepository = ovpRepository;
        this.genericMapper = genericMapper;
        this.sezione21Repository = sezione21Repository;
        this.areaOrganizzativaRepository = areaOrganizzativaRepository;
        this.prioritaPoliticaRepository = prioritaPoliticaRepository;
        this.stakeHolderRepository = stakeHolderRepository;
    }

    @Override

    public OVPDTO saveOrUpdate(OVPDTO ovp) {
        if (ovp == null) return null;


        SezioneUtils.sanitizeJoinChildren(ovp);

        OVP entity = genericMapper.map(ovp, OVP.class);

        Sezione21 s21 = sezione21Repository.findById(ovp.getSezione21Id())
            .orElseThrow(() -> new IllegalArgumentException("Sezione21 non trovata: " + ovp.getSezione21Id()));
        entity.setSezione21(s21);

        entity.setAreeOrganizzative(
            Optional.ofNullable(ovp.getAreeOrganizzative())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(Objects::nonNull)
                .filter(a -> a.getAreaOrganizzativa().getId() != null)
                .map(a -> {
                    Long idAo = a.getAreaOrganizzativa().getId();
                    AreaOrganizzativa ao = areaOrganizzativaRepository.findById(idAo)
                        .orElseThrow(() -> new IllegalArgumentException("AreaOrganizzativa non trovata: " + idAo));

                    OVPAreaOrganizzativa child = OVPAreaOrganizzativa.builder()
                        .areaOrganizzativa(ao)
                        .build();

                    child.setOvp(entity);
                    return child;
                })
                .collect(Collectors.toCollection(ArrayList::new))
        );
        entity.setPrioritaPolitiche(
            Optional.ofNullable(ovp.getPrioritaPolitiche())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getPrioritaPolitica().getId() != null)
                .map(p -> {
                    Long idPp = p.getPrioritaPolitica().getId();

                    PrioritaPolitica pp = prioritaPoliticaRepository.findById(idPp)
                        .orElseThrow(() -> new IllegalArgumentException("PrioritaPolitica non trovata: " + idPp));

                    OVPPrioritaPolitica child = OVPPrioritaPolitica.builder()
                        .prioritaPolitica(pp)
                        .build();

                    child.setOvp(entity); // owning side
                    return child;
                })
                .collect(Collectors.toCollection(ArrayList::new))
        );

        // 6) Stakeholder (supporta sia stakeholderId sia nested stakeholder.id)
        entity.setStakeholders(
            Optional.ofNullable(ovp.getStakeholders())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getStakeholder() != null)
                .map(s -> {
                    Long idSh = s.getStakeholder().getId();

                    StakeHolder sh = stakeHolderRepository.findById(idSh)
                        .orElseThrow(() -> new IllegalArgumentException("StakeHolder non trovato: " + idSh));
                    // In alternativa: entityManager.getReference(StakeHolder.class, idSh);

                    OVPStakeHolder child = OVPStakeHolder.builder()
                        .stakeHolder(sh)
                        .build();

                    child.setOvp(entity); // owning side
                    return child;
                })
                .collect(Collectors.toCollection(ArrayList::new))
        );

        if (ovp.getId() == null) {

            return genericMapper.map(ovpRepository.save(entity), OVPDTO.class);
        }

        // Update: attacca e sostituisci collezioni conservando owning side
        OVP managed = ovpRepository.findById(ovp.getId())
            .orElseThrow(() -> new IllegalArgumentException("OVP non trovata: " + ovp.getId()));

        // campi semplici
        managed.setSezione21(entity.getSezione21());
        managed.setCodice(entity.getCodice());
        managed.setDescrizione(entity.getDescrizione());
        managed.setContesto(entity.getContesto());
        managed.setAmbito(entity.getAmbito());
        managed.setResponsabilePolitico(entity.getResponsabilePolitico());
        managed.setResponsabileAmministrativo(entity.getResponsabileAmministrativo());
        managed.setValoreIndice(entity.getValoreIndice());
        managed.setDescrizioneIndice(entity.getDescrizioneIndice());

        // sostituisci integralmente le collezioni figlie
        SezioneUtils.replaceJoinChildren(managed, entity);


        return genericMapper.map(ovpRepository.save(managed), OVPDTO.class);
    }

    @Override
    public List<OVPDTO> findAllOVPBySezione(Long idSezione) {
        return ovpRepository.findBySezione21Id(idSezione).stream().map(o -> genericMapper.map(o, OVPDTO.class)).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id OVP non può essere null");
        }
        if (!ovpRepository.existsById(id)) {
            throw new IllegalArgumentException("OVP non trovata: " + id);
        }
        try {
            ovpRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Impossibile eliminare l'OVP per vincoli di integrità (FK). Id: " + id, ex);
        }
    }
}
