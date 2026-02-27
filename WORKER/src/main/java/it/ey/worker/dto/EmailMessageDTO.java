package it.ey.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageDTO {
    private String from;
    private String to;
    private String subject;
    private String body;
}
