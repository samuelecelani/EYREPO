package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponseDTO<T> {

    private T data;
    private Status status;
    private Error error;
    private List<MetadatoDTO<?>> metadato;

}
