package it.ey.enums;

public enum WebServiceType {
    API,
    WORKER,
    MINERVA,
    TOKEN_MINERVA,
    BIP,
    NOTIFICATION_BE,
    IPA,
    OPENCMS,
    TICKET_BE,
    SYNC_PP;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}

