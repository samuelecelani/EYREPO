package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO interno per una tipologia di news.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovitaTipologiaDTO {
    private String id;
    private String label;
    private Integer count;
}

