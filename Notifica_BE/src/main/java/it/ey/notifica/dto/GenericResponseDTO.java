package it.ey.notifica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponseDTO<T> {

    private T data;
    private Status status;
    private Error error;
}
