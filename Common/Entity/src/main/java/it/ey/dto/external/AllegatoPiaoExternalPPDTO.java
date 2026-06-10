package it.ey.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllegatoPiaoExternalPPDTO {

    private Integer id;
    private String idPiao;
    private String nomeFile;
    private String s3_key;
}

