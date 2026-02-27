package it.ey.enums;

public enum WebServiceType {
    API,
    WORKER,
    MINERVA,
    BIP,
    NOTIFICATION_BE;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}

