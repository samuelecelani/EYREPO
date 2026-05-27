package it.ey.dto;

public class LoginRequestDataDTO {

    private String state;
    private String originUri;
    private String redirectUri;
    private String codeVerifier;

    public LoginRequestDataDTO(String state, String originUri, String redirectUri, String codeVerifier) {
        this.state = state;
        this.originUri = originUri;
        this.redirectUri = redirectUri;
        this.codeVerifier = codeVerifier;
    }

    public String getState() {
        return state;
    }

    public String getOriginUri() {
        return originUri;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }
}
