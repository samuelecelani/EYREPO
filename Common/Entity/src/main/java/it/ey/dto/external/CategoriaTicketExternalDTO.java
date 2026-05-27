package it.ey.dto.external;

import lombok.*;

/**
 * DTO per lo scambio dati della Categoria Ticket.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaTicketExternalDTO {

    private Long id;
    private String codice;
    private String testo;
    private String idModulo;
}
