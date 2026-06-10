package it.ey.enums;

public enum WarningEnum {


    DELETE_WITH_IMPACT(
            "DELETE_WITH_IMPACT",
            "L'eliminazione di questo elemento può avere impatti su altre sezioni, confermare per l'eliminazione dell'elemento"
    );

    private final String code;
    private final String message;

    WarningEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code; }


    public String getMessage() {
        return message; }
}

