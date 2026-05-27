package it.ey.piao.bff.configuration;


import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import it.ey.piao.bff.filter.ReactiveRequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.List;

//Configura il webClient per chiamare servizi rest da spring
@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${webclient.max-in-memory-size-kb:512}")
    private int maxInMemorySizeKb;

    /**
     * Headers iniettati dal UserAuthoritiesWebFilter che devono essere
     * propagati automaticamente a tutte le chiamate downstream.
     */
    private static final List<String> HEADERS_TO_PROPAGATE = List.of(
        "X-Fiscal-Code",
        "X-Updated-By",
        "X-Updated-Ts",
        "X-Updated-By-Name-Surname",
        "X-Updated-By-Role",
        "X-Testo",
        "X-Campi-Modificati",
        "X-Stato-Sezione"
    );

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        int maxBytes = maxInMemorySizeKb * 1024;
        log.info("WebClient configurato con maxInMemorySize={} KB ({} bytes)", maxInMemorySizeKb, maxBytes);

        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxBytes))
            .build();

        return builder.clone()
            .exchangeStrategies(strategies)
            .filter(logResponseSizeFilter())
            .filter(propagateHeadersFilter())
            .filter(logOutgoingHeadersFilter())
            .build();
    }

    @Bean("insecureWebClient")
    public WebClient insecureWebClient(WebClient.Builder builder) throws SSLException {
        int maxBytes = maxInMemorySizeKb * 1024;
        log.info("InsecureWebClient configurato con maxInMemorySize={} KB ({} bytes) - SSL verification DISABLED", maxInMemorySizeKb, maxBytes);

        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxBytes))
            .build();

        SslContext sslContext = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();
        HttpClient httpClient = HttpClient.create()
            .secure(spec -> spec.sslContext(sslContext))
            .responseTimeout(Duration.ofSeconds(30));

        return builder.clone()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .filter(logResponseSizeFilter())
            .filter(propagateHeadersFilter())
            .filter(logOutgoingHeadersFilter())
            .build();
    }

    /**
     * Logga la dimensione del body di risposta per ogni chiamata WebClient.
     */
    private ExchangeFilterFunction logResponseSizeFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            long contentLength = response.headers().contentLength().orElse(-1);
            String url = response.request().getURI().toString();
            if (contentLength >= 0) {
                log.info("WebClient response: {} {} - Content-Length: {} bytes ({} KB)",
                    response.request().getMethod(), url, contentLength, contentLength / 1024);
            } else {
                log.info("WebClient response: {} {} - Content-Length: unknown (chunked/streamed)",
                    response.request().getMethod(), url);
            }
            return reactor.core.publisher.Mono.just(response);
        });
    }

    /**
     * ExchangeFilterFunction che recupera la ServerHttpRequest dal contesto
     * Reactor (salvata dal UserAuthoritiesWebFilter) e propaga gli headers
     * rilevanti a tutte le chiamate WebClient in modo automatico.
     * Nessun service deve cambiare: gli headers arrivano trasparentemente.
     */
    private ExchangeFilterFunction propagateHeadersFilter() {
        return (request, next) ->
            ReactiveRequestContextHolder.getRequest()
                .map(serverRequest -> {
                    ClientRequest.Builder mutated = ClientRequest.from(request);
                    for (String headerName : HEADERS_TO_PROPAGATE) {
                        List<String> values = serverRequest.getHeaders().get(headerName);
                        if (values != null && !values.isEmpty()) {
                            // Aggiungi solo se non già presente nella request del WebClient
                            if (!request.headers().containsKey(headerName)) {
                                mutated.header(headerName, values.toArray(String[]::new));
                            }
                        }
                    }
                    return mutated.build();
                })
                .defaultIfEmpty(request)
                .flatMap(next::exchange);
    }

    /**
     * Logga gli header outgoing per ogni chiamata WebClient (utile per debug 401).
     */
    private ExchangeFilterFunction logOutgoingHeadersFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            String url = clientRequest.url().toString();
            boolean hasAuth = clientRequest.headers().containsKey("Authorization");
            log.info(">>> WebClient OUT: {} {} | Authorization={} | headers={}",
                clientRequest.method(), url,
                hasAuth ? "PRESENT (len=" + clientRequest.headers().getFirst("Authorization").length() + ")" : "ABSENT",
                clientRequest.headers().keySet());
            return reactor.core.publisher.Mono.just(clientRequest);
        });
    }
}
