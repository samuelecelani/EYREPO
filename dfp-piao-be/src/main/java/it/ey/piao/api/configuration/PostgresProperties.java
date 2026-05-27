package it.ey.piao.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Proprietà di connessione a PostgreSQL.
 * <p>
 * In ambiente Kubernetes:
 * <ul>
 *   <li>host, port, db &rarr; ConfigMap</li>
 *   <li>username, password &rarr; Secret</li>
 * </ul>
 * Se viene fornita una URL completa tramite {@code app.datasource.postgres.url},
 * questa ha la precedenza. Altrimenti la JDBC URL viene costruita a runtime
 * dal metodo {@link #buildJdbcUrl()}.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "app.datasource.postgres")
public class PostgresProperties {

    /** URL JDBC completa (opzionale). Se valorizzata e valida, ha priorita sui singoli campi. */
    private String url;

    /** Host del server PostgreSQL (o nome del Service K8s). */
    private String host;

    /** Porta del server PostgreSQL. Default: 5432. */
    private String port = "5432";

    /** Nome del database. */
    private String db;

    /** Username per l'autenticazione. */
    private String username;

    /** Password per l'autenticazione. */
    private String password;

    /** Schema di default. Se valorizzato viene impostato come search_path. */
    private String schema;

    /**
     * Costruisce la JDBC URL.
     * <p>
     * Se il campo {@code url} e' stato impostato e inizia con {@code jdbc:},
     * viene restituito direttamente. Altrimenti l'URL viene costruita
     * dai singoli campi: {@code jdbc:postgresql://<host>:<port>/<db>}
     */
    public String buildJdbcUrl() {
        if (StringUtils.hasText(url) && url.startsWith("jdbc:")) {
            return url;
        }
        String baseUrl = "jdbc:postgresql://" + host + ":" + port + "/" + db;
        if (StringUtils.hasText(schema)) {
            baseUrl += "?currentSchema=" + schema;
        }
        return baseUrl;
    }
}
