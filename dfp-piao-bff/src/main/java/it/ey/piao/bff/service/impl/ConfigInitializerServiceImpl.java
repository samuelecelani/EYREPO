package it.ey.piao.bff.service.impl;

import it.ey.dto.ConfigFeInitializerDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.piao.bff.service.IConfigInitializerService;
import it.ey.piao.bff.service.IConfigurazioniService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ConfigInitializerServiceImpl implements IConfigInitializerService {

    private static final Logger log = LoggerFactory.getLogger(ConfigInitializerServiceImpl.class);

    private static final String DATA_SCADENZA_PIAO = "DATA_SCADENZA_PIAO";
    private static final String DATA_COMPILAZIONE_PIAO = "DATA_COMPILAZIONE_PIAO";
    @Value("${internal.service.url.api}")
    private String baseUrl;
    @Value("${internal.service.isProduction}")
    private boolean isProduction;
    @Value("${internal.service.loginType}")
    private String loginType;
    @Value("${internal.service.localStorageToken}")
    private String localStorageToken;
    @Value("${internal.service.tokenHeader}")
    private String tokenHeader;
    @Value("${internal.service.tokenHeaderPrefix}")
    private String tokenHeaderPrefix;
    @Value("${internal.service.openidIssuerUrl}")
    private String openidIssuerUrl;
    @Value("${internal.service.openidRedirectUri}")
    private String openidRedirectUri;
    @Value("${internal.service.openidLogoutUrl}")
    private String openidLogoutUrl;
    @Value("${internal.service.openidPostlogoutRedirectUri}")
    private String openidPostlogoutRedirectUri;
    @Value("${internal.service.openidClientId}")
    private String openidClientId;
    @Value("${internal.service.openidResponseType}")
    private String openidResponseType;
    @Value("${internal.service.openidScope}")
    private String openidScope;
    @Value("${internal.service.openidOidc}")
    private boolean openidOidc;
    @Value("${internal.service.openidShowDebugInfo}")
    private boolean openidShowDebugInfo;
    @Value("${internal.service.openidRequireHttps}")
    private boolean openidRequireHttps;
    @Value("${internal.service.openidSkipIssuerCheck}")
    private boolean openidSkipIssuerCheck;
    @Value("${internal.service.openidStrictDiscoveryDocValidation}")
    private boolean openidStrictDiscoveryDocValidation;
    @Value("${internal.service.openidPreserveRequestRoute}")
    private boolean openidPreserveRequestRoute;
    @Value("${internal.service.openidTimeoutFactor}")
    private double openidTimeoutFactor;
    @Value("${internal.service.pdfLowTextThreshold}")
    private int pdfLowTextThreshold;
    @Value("${internal.service.pdfMinTotalChars}")
    private int pdfMinTotalChars;
    @Value("${internal.service.pdfMinDensityPerKb}")
    private double pdfMinDensityPerKb;
    @Value("${internal.service.pdfMaxLowPagesPct}")
    private int pdfMaxLowPagesPct;
    @Value("${internal.service.url.public.minerva}")
    private String publicUrlMinerva;
    @Value("${internal.service.url.public.performance}")
    private String publicUrlPortalePerformance;

    private final IConfigurazioniService configurazioniService;

    public ConfigInitializerServiceImpl(IConfigurazioniService configurazioniService) {
        this.configurazioniService = configurazioniService;
    }

    @Override
    public Mono<GenericResponseDTO<ConfigFeInitializerDTO>> configInitializer(){
        return configurazioniService.getPiaoDatesFree()
            .map(dates -> buildResponse(
                dates.getOrDefault(DATA_COMPILAZIONE_PIAO, ""),
                dates.getOrDefault(DATA_SCADENZA_PIAO, "")))
            .onErrorResume(e -> {
                log.error("Errore recupero date PIAO da BE, fallback a stringhe vuote: {}", e.getMessage(), e);
                return Mono.just(buildResponse("", ""));
            });
    }


    private GenericResponseDTO<ConfigFeInitializerDTO> buildResponse(String dataCompilazione, String dataScadenza) {
        GenericResponseDTO<ConfigFeInitializerDTO> response = new GenericResponseDTO<>();
        response.setStatus(Status.builder().isSuccess(true).build());
        response.setData(ConfigFeInitializerDTO.builder()
                .apiEndpoint(baseUrl)
                .isProduction(isProduction)
                .loginType(loginType)
                .localStorageToken(localStorageToken)
                .tokenHeader(tokenHeader)
                .tokenHeaderPrefix(tokenHeaderPrefix)
                .openidIssuerUrl(openidIssuerUrl)
                .openidRedirectUri(openidRedirectUri)
                .openidLogoutUrl(openidLogoutUrl)
                .openidPostlogoutRedirectUri(openidPostlogoutRedirectUri)
                .openidClientId(openidClientId)
                .openidResponseType(openidResponseType)
                .openidScope(openidScope)
                .openidOidc(openidOidc)
                .openidShowDebugInfo(openidShowDebugInfo)
                .openidRequireHttps(openidRequireHttps)
                .openidSkipIssuerCheck(openidSkipIssuerCheck)
                .openidStrictDiscoveryDocValidation(openidStrictDiscoveryDocValidation)
                .openidPreserveRequestRoute(openidPreserveRequestRoute)
                .openidTimeoutFactor(openidTimeoutFactor)
                .pdfLowTextThreshold(pdfLowTextThreshold)
                .pdfMinTotalChars(pdfMinTotalChars)
                .pdfMinDensityPerKb(pdfMinDensityPerKb)
                .pdfMaxLowPagesPct(pdfMaxLowPagesPct)
                .dataCompilazionePiao(dataCompilazione != null ? dataCompilazione : "")
                .dataScadenzaPiao(dataScadenza != null ? dataScadenza : "")
                .publicUrlMinerva(publicUrlMinerva)
                .publicUrlPortalePerformance(publicUrlPortalePerformance)
                .build());
        return response;
    }

}
