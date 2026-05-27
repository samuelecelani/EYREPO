package it.ey.sync.enums;

/**
 * Enum per identificare i servizi esterni chiamati dal modulo Notifica_BE.
 * Ogni valore contiene l'URL base del servizio, valorizzato a runtime
 * dal componente {@code ServiceTypeInitializer}.
 */
public enum WebServiceType {
    BFF;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
