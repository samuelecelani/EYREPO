package it.ey.piao.api.exception;

import it.ey.enums.TypeErrorEnum;

public class BusinessException extends RuntimeException {

    public static final String INTERNAL_ERROR = "ERRORE_INTERNO_CONFLITTI_CANCELLAZIONE";

    private final String errorCode;
    private final TypeErrorEnum typeEnum;

    public BusinessException(String errorCode, String message, TypeErrorEnum typeEnum) {
        super(message);
        this.errorCode = errorCode;
        this.typeEnum = typeEnum;
    }

    public String getErrorCode() {
        return errorCode;
    }



    public TypeErrorEnum getTypeEnum() {
        return typeEnum;
    }

}
