package it.ey.piao.bff.mapper;

import it.ey.dto.NovitaDTO;
import it.ey.dto.NovitaPaginatedDTO;
import it.ey.dto.NovitaTipologiaDTO;
import it.ey.dto.external.NewsDetailExternalDTO;
import it.ey.dto.external.NewsItemExternalDTO;
import it.ey.dto.external.NewsSearchResponseExternalDTO;
import it.ey.dto.external.NewsTipologiaExternalDTO;
import it.ey.dto.external.NewsTipologieResponseExternalDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per convertire i DTO external di OpenCms nei DTO interni NovitaDTO.
 */
public class NovitaMapper {

    private NovitaMapper() {
        // utility class
    }

    /**
     * Converte la risposta di news-tipologie in una lista di NovitaTipologiaDTO.
     */
    public static List<NovitaTipologiaDTO> toNovitaTipologiaDTOList(NewsTipologieResponseExternalDTO external) {
        if (external == null || external.getItems() == null) {
            return Collections.emptyList();
        }
        return external.getItems().stream()
            .map(NovitaMapper::toNovitaTipologiaDTO)
            .collect(Collectors.toList());
    }

    /**
     * Converte un singolo NewsTipologiaExternalDTO in NovitaTipologiaDTO.
     */
    public static NovitaTipologiaDTO toNovitaTipologiaDTO(NewsTipologiaExternalDTO ext) {
        NovitaTipologiaDTO dto = new NovitaTipologiaDTO();
        dto.setId(ext.getId());
        dto.setLabel(ext.getLabel());
        dto.setCount(ext.getCount());
        return dto;
    }

    /**
     * Converte la risposta paginata di news-search in NovitaPaginatedDTO.
     */
    public static NovitaPaginatedDTO toNovitaPaginatedDTO(NewsSearchResponseExternalDTO external) {
        NovitaPaginatedDTO dto = new NovitaPaginatedDTO();
        dto.setPage(external.getPage());
        dto.setLimit(external.getLimit());
        dto.setTotal(external.getTotal());
        dto.setItems(toNovitaDTOList(external.getItems()));
        return dto;
    }

    /**
     * Converte una lista di NewsItemExternalDTO in una lista di NovitaDTO.
     */
    public static List<NovitaDTO> toNovitaDTOList(List<NewsItemExternalDTO> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
            .map(NovitaMapper::toNovitaDTO)
            .collect(Collectors.toList());
    }

    /**
     * Converte un singolo NewsItemExternalDTO (search) in NovitaDTO.
     */
    public static NovitaDTO toNovitaDTO(NewsItemExternalDTO item) {
        NovitaDTO dto = new NovitaDTO();
        dto.setId(item.getId());
        dto.setTipologia(item.getTipologia());
        dto.setTitolo(item.getTitolo());
        dto.setAbstractText(item.getAbstractText());
        dto.setData(item.getData());
        return dto;
    }

    /**
     * Converte un NewsDetailExternalDTO (dettaglio) in NovitaDTO.
     */
    public static NovitaDTO toNovitaDetailDTO(NewsDetailExternalDTO detail) {
        NovitaDTO dto = new NovitaDTO();
        dto.setId(detail.getId());
        dto.setTipologia(detail.getTipologia());
        dto.setTitolo(detail.getTitolo());
        dto.setAbstractText(detail.getAbstractText());
        dto.setData(detail.getData());
        dto.setTestoHtml(detail.getTestoHtml());
        return dto;
    }
}

