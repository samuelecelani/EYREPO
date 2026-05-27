package it.ey.externaldto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalApiResponse<T> {

    @Schema(description = "Response status information", requiredMode = Schema.RequiredMode.REQUIRED)
    private ExternalStatus status;

    @Schema(description = "Response data payload", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private T data;
}
