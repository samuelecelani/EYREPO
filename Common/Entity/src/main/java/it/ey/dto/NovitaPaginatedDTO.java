package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO di risposta paginata per le novità (news).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovitaPaginatedDTO {
    private Integer page;
    private Integer limit;
    private Integer total;
    private List<NovitaDTO> items;
}

