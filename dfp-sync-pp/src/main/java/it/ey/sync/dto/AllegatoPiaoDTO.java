package it.ey.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllegatoPiaoDTO {

    private Integer id;
    private String idPiao;
    private String nomeFile;
    private String s3_key;
}

