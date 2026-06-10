package it.ey.externaldto;

import it.ey.externaldto.externalenum.StatusCode;
import it.ey.externaldto.externalenum.StatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalStatus {

    private byte id;
    private StatusCode code;
    private StatusType type;
    private String message;
}
