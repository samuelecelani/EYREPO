package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per entità comuni usate in più sezioni.
 * Usa CycleAvoidingMappingContext per prevenire cicli infiniti.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CommonMapper {
    ;

    /**
     * Mappa AllegatoDTO → Allegato entity
     */
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    Allegato allegatoDtoToEntity(AllegatoDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa Allegato entity → AllegatoDTO
     */
    AllegatoDTO allegatoEntityToDto(Allegato entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa UlterioriInfoDTO → UlterioriInfo entity
     */

    UlterioriInfo ulterioriInfoDtoToEntity(UlterioriInfoDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa UlterioriInfo entity → UlterioriInfoDTO
     */
    UlterioriInfoDTO ulterioriInfoEntityToDto(UlterioriInfo entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa SocialDTO → Social entity
     */

    Social socialDtoToEntity(SocialDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa Social entity → SocialDTO
     */
    SocialDTO socialEntityToDto(Social entity, @Context CycleAvoidingMappingContext context);





    @Mapping(target = "sezione1.id", source = "idSezione1")
    PrincipioGuida principioGuidaDtoToEntity(PrincipioGuidaDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa PrincipioGuida entity → PrincipioGuidaDTO
     */
    @Mapping(target = "idSezione1", source = "sezione1.id")
    PrincipioGuidaDTO principioGuidaEntityToDto(PrincipioGuida entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa OrganoPoliticoDTO → OrganoPolitico entity nel contesto di Sezione1
     */
    @Mapping(target = "sezione1.id", source = "idSezione1")
    OrganoPolitico organoPoliticoDtoToEntity(OrganoPoliticoDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa OrganoPolitico entity → OrganoPoliticoDTO
     */
    @Mapping(target = "idSezione1", source = "sezione1.id")
    OrganoPoliticoDTO organoPoliticoEntityToDto(OrganoPolitico entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa IntegrationTeamDTO → IntegrationTeam entity nel contesto di Sezione1
     */
    @Mapping(target = "sezione1.id", source = "idSezione1")
    IntegrationTeam integrationTeamDtoToEntity(IntegrationTeamDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa IntegrationTeam entity → IntegrationTeamDTO
     */
    @Mapping(target = "idSezione1", source = "sezione1.id")
    IntegrationTeamDTO integrationTeamEntityToDto(IntegrationTeam entity, @Context CycleAvoidingMappingContext context);





    /**
     * Mapper per entità SWOT
     */

    SwotPuntiForza swotPuntiForzaDtoToEntity(SwotPuntiForzaDTO dto, @Context CycleAvoidingMappingContext context);

    SwotPuntiForzaDTO swotPuntiForzaEntityToDto(SwotPuntiForza entity, @Context CycleAvoidingMappingContext context);


    SwotPuntiDebolezza swotPuntiDebolezzaDtoToEntity(SwotPuntiDebolezzaDTO dto, @Context CycleAvoidingMappingContext context);

    SwotPuntiDebolezzaDTO swotPuntiDebolezzaEntityToDto(SwotPuntiDebolezza entity, @Context CycleAvoidingMappingContext context);


    SwotOpportunita swotOpportunitaDtoToEntity(SwotOpportunitaDTO dto, @Context CycleAvoidingMappingContext context);

    SwotOpportunitaDTO swotOpportunitaEntityToDto(SwotOpportunita entity, @Context CycleAvoidingMappingContext context);


    SwotMinacce swotMinacceDtoToEntity(SwotMinacceDTO dto, @Context CycleAvoidingMappingContext context);

    SwotMinacceDTO swotMinacceEntityToDto(SwotMinacce entity, @Context CycleAvoidingMappingContext context);


    ContributoreInterno contributoreInternoDtoToEntity(ContributoreInternoDTO dto, @Context CycleAvoidingMappingContext context);
    ContributoreInternoDTO contributoreInternoEntityToDto(ContributoreInterno entity, @Context CycleAvoidingMappingContext context);




    /**
     * Mappa Sezione21 entity → Sezione21DTO (per saveOrUpdate e richiediValidazione)
     */
    @Mapping(target = "ovp", ignore = true)
    @Mapping(target = "procedure", ignore = true)
    @Mapping(target = "fondiEuropei", ignore = true)
    @Mapping(target = "statoSezione", ignore = true)
    @Mapping(target = "swotPuntiForza", ignore = true)
    @Mapping(target = "swotPuntiDebolezza", ignore = true)
    @Mapping(target = "swotOpportunita", ignore = true)
    @Mapping(target = "swotMinacce", ignore = true)
    @Mapping(target = "ulterioriInfo", ignore = true)
    @Mapping(target = "allegati", ignore = true)
    Sezione21DTO sezione21ToDto(Sezione21 entity, @Context CycleAvoidingMappingContext context);


    /**
     * Mapper per Azione (MongoDB)
     */

    Azione azioneDtoToEntity(AzioneDTO dto, @Context CycleAvoidingMappingContext context);

    AzioneDTO azioneEntityToDto(Azione entity, @Context CycleAvoidingMappingContext context);






    Fattore toEntity(FattoreDTO dto, @Context CycleAvoidingMappingContext context);
    FattoreDTO toDto(Fattore entity, @Context CycleAvoidingMappingContext context);

    List<Fattore> toEntityList(List<FattoreDTO> dtoList, @Context CycleAvoidingMappingContext context);
    List<FattoreDTO> toDtoList(List<Fattore> entityList, @Context CycleAvoidingMappingContext context);

    /**
     * Mapper per Attore (MongoDB)
     */
    Attore attoreDtoToEntity(AttoreDTO dto, @Context CycleAvoidingMappingContext context);
    AttoreDTO attoreEntityToDto(Attore entity, @Context CycleAvoidingMappingContext context);

    List<Attore> attoreDtoListToEntityList(List<AttoreDTO> dtoList, @Context CycleAvoidingMappingContext context);
    List<AttoreDTO> attoreEntityListToDtoList(List<Attore> entityList, @Context CycleAvoidingMappingContext context);

    /**
     * Mapper per Attivita (MongoDB)
     */
    Attivita attivitaDtoToEntity(AttivitaDTO dto, @Context CycleAvoidingMappingContext context);
    AttivitaDTO attivitaEntityToDto(Attivita entity, @Context CycleAvoidingMappingContext context);

    List<Attivita> attivitaDtoListToEntityList(List<AttivitaDTO> dtoList, @Context CycleAvoidingMappingContext context);
    List<AttivitaDTO> attivitaEntityListToDtoList(List<Attivita> entityList, @Context CycleAvoidingMappingContext context);
}
