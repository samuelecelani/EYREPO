package it.ey.externaldto.externalenum;

import io.swagger.v3.oas.annotations.media.Schema;

public enum StatusType {

    @Schema(description = "Operation completed successfully")
    SUCCESS,

    @Schema(description = "Operation failed due to business logic error")
    ERROR,

    @Schema(description = "User lacks required permissions or authentication")
    UNAUTHORIZED,

    @Schema(description = "Invalid request parameters or data format")
    BAD_REQUEST,

    @Schema(description = "Internal server error occurred")
    INTERNAL_ERROR,

    @Schema(description = "Informational status")
    INFORMATION,

    @Schema(description = "Requested resource not found")
    NOT_FOUND
}
