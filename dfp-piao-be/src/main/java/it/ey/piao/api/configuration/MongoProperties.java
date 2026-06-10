package it.ey.piao.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Proprietà di connessione a MongoDB.
 * <p>
 * In ambiente Kubernetes, host e database vengono iniettati via ConfigMap,
 * mentre username e password vengono iniettati via Secret.
 * L'URI viene costruito a runtime a partire dai singoli campi.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "app.second-datasource")
public class MongoProperties {

    /** Schema URI: "mongodb" oppure "mongodb+srv". Default: "mongodb". */
    private String scheme = "mongodb";

    /** Host del server MongoDB (o nome del Service K8s). */
    private String host;

    /**
     * Porta del server MongoDB.
     * Lasciare vuoto quando si usa mongodb+srv (la porta viene risolta via DNS SRV).
     */
    private String port;

    /** Nome del database. */
    private String database;

    /** Username per l'autenticazione. */
    private String username;

    /** Password per l'autenticazione. */
    private String password;

    /**
     * Parametri aggiuntivi della query string dell'URI
     * (es. "tls=true&authMechanism=SCRAM-SHA-256").
     * Lasciare vuoto se non necessari.
     */
    private String params;

    /**
     * URI MongoDB completa, impostabile direttamente via proprietà
     * (es. {@code app.second-datasource.uri=${MONGODB_URL_AUTH}}).
     * Se valorizzata, ha la precedenza sulla costruzione dinamica dai singoli campi.
     */
    private String uri;

    /**
     * Restituisce il valore grezzo del campo {@code uri}, senza fallback.
     * Utile per verificare se è stata fornita una URI diretta.
     */
    public String getDirectUri() {
        if (StringUtils.hasText(uri)
                && (uri.startsWith("mongodb://") || uri.startsWith("mongodb+srv://"))) {
            return uri;
        }
        return null;
    }

    /**
     * Restituisce la URI MongoDB.
     * <p>
     * Se il campo {@code uri} è stato impostato esplicitamente, viene restituito così com'è.
     * Altrimenti l'URI viene costruita a partire dai singoli campi.
     * <p>
     * Formato risultante (quando costruita):
     * <ul>
     *   <li>Con porta: {@code <scheme>://<user>:<pwd>@<host>:<port>/<db>[?<params>]}</li>
     *   <li>Senza porta (mongodb+srv): {@code <scheme>://<user>:<pwd>@<host>/<db>[?<params>]}</li>
     * </ul>
     */
    public String getUri() {
        if (StringUtils.hasText(uri)
                && (uri.startsWith("mongodb://") || uri.startsWith("mongodb+srv://"))) {
            return uri;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://");
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            sb.append(username).append(":").append(password).append("@");
        }
        sb.append(host);
        if (StringUtils.hasText(port)) {
            sb.append(":").append(port);
        }
        sb.append("/");
        if (StringUtils.hasText(params)) {
            sb.append("?").append(params);
        }
        return sb.toString();
    }
}
