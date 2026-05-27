package com.project.dfp.dfpGatewayService.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.dfp.dfpGatewayService.util.GlobalVariables;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Status {

    @EqualsAndHashCode.Include
    private byte id;

    @EqualsAndHashCode.Include
    private StatusCode code;

    @EqualsAndHashCode.Include
    private StatusType type;

    private String message;

    public Status(int id, StatusCode code, StatusType type) {
        this.id = (byte) id;
        this.code = code;
        this.type = type;
        this.message = code.getDescription();
    }

    public static Status createSuccessStatus() {
        return new Status(GlobalVariables.STATUS_OK, StatusCode.GEN_200, StatusType.SUCCESS);
    }

    public static Status createSuccessStatus(StatusCode statusCode, StatusType statusType) {
        return new Status(GlobalVariables.STATUS_OK, statusCode, statusType);
    }

    public static Status createErrorStatus(StatusCode code, StatusType type) {
        return new Status(GlobalVariables.STATUS_ERROR, code, type);
    }

    public static Status createErrorStatus(StatusCode statusCode, StatusType statusType, String message) {
        Status status = new Status(GlobalVariables.STATUS_ERROR, statusCode, statusType);
        status.setMessage(message);
        return status;
    }

    @JsonIgnore
    public boolean isErrorStatus() { return this.id == GlobalVariables.STATUS_ERROR; }

    @JsonIgnore
    public boolean isOKStatus() { return this.id == GlobalVariables.STATUS_OK; }
}
