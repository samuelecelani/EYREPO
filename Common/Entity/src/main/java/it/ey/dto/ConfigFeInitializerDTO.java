package it.ey.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigFeInitializerDTO {
    private  String apiEndpoint;
    private boolean isProduction;

    // Auth / OpenID
    private String loginType;
    private String localStorageToken;
    private String tokenHeader;
    private String tokenHeaderPrefix;
    private String openidIssuerUrl;
    private String openidRedirectUri;
    private String openidLogoutUrl;
    private String openidPostlogoutRedirectUri;
    private String openidClientId;
    private String openidResponseType;
    private String openidScope;
    private boolean openidOidc;
    private boolean openidShowDebugInfo;
    private boolean openidRequireHttps;
    private boolean openidSkipIssuerCheck;
    private boolean openidStrictDiscoveryDocValidation;
    private boolean openidPreserveRequestRoute;
    private double openidTimeoutFactor;
    private String publicUrlMinerva;
    private String publicUrlPortalePerformance;
    // PDF quality thresholds
    private int pdfLowTextThreshold;
    private int pdfMinTotalChars;
    private double pdfMinDensityPerKb;
    private int pdfMaxLowPagesPct;
    private String dataCompilazionePiao;
    private String dataScadenzaPiao;
}
