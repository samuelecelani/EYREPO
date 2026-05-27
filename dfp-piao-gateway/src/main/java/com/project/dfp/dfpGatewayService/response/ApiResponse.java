package com.project.dfp.dfpGatewayService.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    private Status status;

    private T data;

    public static <T> ApiResponse<T> successResponse() {
        return ApiResponse.<T>builder()
                .status(Status.createSuccessStatus())
                .build();
    }

    public static <T> ApiResponse<T> successResponse(T data) {
        return ApiResponse.<T>builder()
                .status(Status.createSuccessStatus())
                .data(data)
                .build();
    }

}
