package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LabelValueDTO {
    private String label;
    private String value;
}
