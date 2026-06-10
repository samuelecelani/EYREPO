package it.ey.piao.bff.configuration;


import it.ey.enums.MinervaEndpoint;
import it.ey.enums.WebServiceType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
//Permette di  avere a runtime gli url per chiamate a servizi esterni
//N.B. workerUrl è solo a titolo d'esempio, nel nostro template non è previsto che il modulo BFF chiami direttamente il modulo Worker
@Component
public class ServiceTypeInitializer {

    @Value("${external.service.url.api}")
    private String apiUrl;

    @Value("${external.service.url.worker:}")  //I :  permettono di far partire l'app anche se non configurata la property
    private String workerUrl;
    @Value("${minerva.url}")
    private String minervaUrl;
    @Value("${external.service.url.notifica-be:}")
    private String notificaBeUrl;
    @Value("${external.service.minerva.endpoint.tabella1:}")
    private String tabella11Minerva;
    @Value("${minerva.url.token}")
    private String tokenMinervaUrl;
    @Value("${external.service.url.bip:}")
    private String bipUrl;

    @Value("${external.service.url.ipa:}")
    private String ipaUrl;

    @Value("${external.service.url.opencms:}")
    private String opencmsUrl;
    @Value("${external.service.url.ticket-be:}")
    private String ticketBeUrl;

    @Value("${external.service.url.sync-pp:}")
    private String syncPpUrl;

    @PostConstruct
    public void init() {
        //Setto i servizi che chiama il BFF

        WebServiceType.API.setUrl(apiUrl);
        WebServiceType.WORKER.setUrl(workerUrl);
        WebServiceType.MINERVA.setUrl(minervaUrl);
        WebServiceType.NOTIFICATION_BE.setUrl(notificaBeUrl);
        WebServiceType.TOKEN_MINERVA.setUrl(tokenMinervaUrl);
        WebServiceType.BIP.setUrl(bipUrl);
        WebServiceType.IPA.setUrl(ipaUrl);
        WebServiceType.OPENCMS.setUrl(opencmsUrl);
        WebServiceType.TICKET_BE.setUrl(ticketBeUrl);
        WebServiceType.SYNC_PP.setUrl(syncPpUrl);
        //Setto eventuali endpoint specifici

        MinervaEndpoint.TABELLA11.setEndpoint(tabella11Minerva);
    }
}

