package it.ey.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoPiaoDTO {

    private String id;
    private String codicePiao;
    private String fullName;
    private String codiceIpaRif;
    private Integer versione;
    private LocalDate dataApprovazione;
    private LocalDate dataPubblicazione;
    private String linkEsterno;
    private List<AllegatoPiaoDTO> allegati;
}

