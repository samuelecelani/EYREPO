package it.ey.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MetadatoDTO<T> {


  private String key;
  private Long idFK; // id oggetto
  private T value; // nome classe
  private  Long idPiao;
}

