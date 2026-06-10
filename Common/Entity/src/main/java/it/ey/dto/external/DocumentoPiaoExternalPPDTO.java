package it.ey.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoPiaoExternalPPDTO {

    private String id;
    private String codicePiao;
    private String fullName;
    private String codiceIpaRif;
    private Integer versione;
    private LocalDate dataApprovazione;
    private LocalDate dataPubblicazione;
    private String linkEsterno;
    private List<AllegatoPiaoExternalPPDTO> allegati;
}

