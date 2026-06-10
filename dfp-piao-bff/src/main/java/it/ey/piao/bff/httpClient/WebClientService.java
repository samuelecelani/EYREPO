package it.ey.piao.bff.httpClient;
import it.ey.enums.WebServiceType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;


//Classe atomica e centralizzata per gestire le chiamate api attraverso l'utilizzo di webclient
@Service
public class WebClientService {

    private final WebClient webClient;
    private final WebClient insecureWebClient;

    public WebClientService(WebClient webClient,
                            @Qualifier("insecureWebClient") WebClient insecureWebClient) {
        this.webClient = webClient;
        this.insecureWebClient = insecureWebClient;
    }

    private WebClient resolveClient(boolean insecure) {
        return insecure ? insecureWebClient : webClient;
    }

    // ─── GET ────────────────────────────────────────────────────────────────────

    public <T> Mono<T> get(String url, WebServiceType type, HttpHeaders headers, Class<T> responseType) {
        return get(url, type, headers, responseType, false);
    }

    public <T> Mono<T> get(String url, WebServiceType type, HttpHeaders headers, Class<T> responseType, boolean insecure) {
        return resolveClient(insecure).get()
            .uri(type.getUrl() + url)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .bodyToMono(responseType);
    }

    public <T> Mono<T> get(String url, WebServiceType type, HttpHeaders headers, ParameterizedTypeReference<T> responseType) {
        return get(url, type, headers, responseType, false);
    }

    public <T> Mono<T> get(String url, WebServiceType type, HttpHeaders headers, ParameterizedTypeReference<T> responseType, boolean insecure) {
        return resolveClient(insecure).get()
            .uri(type.getUrl() + url)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .bodyToMono(responseType);
    }

    // ─── POST ───────────────────────────────────────────────────────────────────

    public <T> Mono<T> post(String url, WebServiceType type, Object body, HttpHeaders headers, Class<T> responseType) {
        return post(url, type, body, headers, responseType, false);
    }

    public <T> Mono<T> post(String url, WebServiceType type, Object body, HttpHeaders headers, Class<T> responseType, boolean insecure) {
        return resolveClient(insecure).post()
            .uri(URI.create(type.getUrl() + url))
            .headers(h -> h.addAll(headers))
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }

    public <T> Mono<T> post(String url, WebServiceType type, Object body, HttpHeaders headers, ParameterizedTypeReference<T> responseType) {
        return post(url, type, body, headers, responseType, false);
    }

    public <T> Mono<T> post(String url, WebServiceType type, Object body, HttpHeaders headers, ParameterizedTypeReference<T> responseType, boolean insecure) {
        return resolveClient(insecure).post()
            .uri(URI.create(type.getUrl() + url))
            .headers(h -> h.addAll(headers))
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }

    // ─── PUT ────────────────────────────────────────────────────────────────────

    public <T> Mono<T> put(String url, WebServiceType type, Object body, HttpHeaders headers, Class<T> responseType) {
        return put(url, type, body, headers, responseType, false);
    }

    public <T> Mono<T> put(String url, WebServiceType type, Object body, HttpHeaders headers, Class<T> responseType, boolean insecure) {
        return resolveClient(insecure).put()
            .uri(URI.create(type.getUrl() + url))
            .headers(h -> h.addAll(headers))
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }

    // ─── DELETE ─────────────────────────────────────────────────────────────────

    public <T> Mono<T> delete(String url, WebServiceType type, HttpHeaders headers, Class<T> responseType) {
        return delete(url, type, headers, responseType, false);
    }

    public <T> Mono<T> delete(String url, WebServiceType type, HttpHeaders headers, Class<T> responseType, boolean insecure) {
        return resolveClient(insecure).delete()
            .uri(type.getUrl() + url)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .bodyToMono(responseType);
    }

    public Mono<Void> delete(String url, WebServiceType type, HttpHeaders headers) {
        return delete(url, type, headers, false);
    }

    public Mono<Void> delete(String url, WebServiceType type, HttpHeaders headers, boolean insecure) {
        return resolveClient(insecure).delete()
            .uri(type.getUrl() + url)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .bodyToMono(Void.class);
    }

    public <T> Mono<T> delete(String url, WebServiceType type, HttpHeaders headers, ParameterizedTypeReference<T> responseType) {
        return delete(url, type, headers, responseType, false);
    }

    public <T> Mono<T> delete(String url, WebServiceType type, HttpHeaders headers, ParameterizedTypeReference<T> responseType, boolean insecure) {
        return resolveClient(insecure).delete()
            .uri(type.getUrl() + url)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .bodyToMono(responseType);
    }

    /**
     * DELETE con pieno controllo sulla risposta tramite exchangeToMono.
     * Deserializza il body su qualsiasi status code (inclusi 4xx/5xx con body JSON).
     * Se non c'è body (es. 204 No Content), restituisce un oggetto vuoto del tipo richiesto.

     * Il metodo deleteWithExchange serve a risolvere un limite strutturale di WebFlux
     * WebClient di default non distingue correttamente 200 WARNING da SUCCESS,
     * e per le DELETE 204 ritorna Mono.empty(), quindi:
     * non puoi leggere il body,
     * la map() non viene invocata,perdi la possibilità di aggiungere il metadato,il FE non riceve informazioni coerenti.

     * deleteWithExchange risolve questo problema usando exchangeToMono:
     * intercetta la raw response
     * decide se restituire un body (WARNING/ERROR)
     * o un Mono.empty (SUCCESS)
     */
    public <T> Mono<T> deleteWithExchange(String url, WebServiceType type, HttpHeaders headers, ParameterizedTypeReference<T> responseType) {
        return deleteWithExchange(url, type, headers, responseType, false);
    }

    public <T> Mono<T> deleteWithExchange(String url, WebServiceType type, HttpHeaders headers, ParameterizedTypeReference<T> responseType, boolean insecure) {
        return resolveClient(insecure).delete()
            .uri(URI.create(type.getUrl() + url))
            .headers(h -> h.addAll(headers))
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful() && response.statusCode().value() == 204) {
                    return response.releaseBody().then(Mono.empty());
                }
                return response.bodyToMono(responseType);
            });
    }

    // ─── PATCH ──────────────────────────────────────────────────────────────────

    public <T> Mono<T> patch(String url, WebServiceType type, Object body, HttpHeaders headers, Class<T> responseType) {
        return patch(url, type, body, headers, responseType, false);
    }

    public <T> Mono<T> patch(String url, WebServiceType type, Object body, HttpHeaders headers, Class<T> responseType, boolean insecure) {
        return resolveClient(insecure).patch()
            .uri(URI.create(type.getUrl() + url))
            .headers(h -> h.addAll(headers))
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }

    // ─── POST FORM URL ENCODED ──────────────────────────────────────────────────

    public <T> Mono<T> postFormUrlEncoded(String url, WebServiceType type, MultiValueMap<String, String> formData, HttpHeaders headers, Class<T> responseType) {
        return postFormUrlEncoded(url, type, formData, headers, responseType, false);
    }

    public <T> Mono<T> postFormUrlEncoded(String url, WebServiceType type, MultiValueMap<String, String> formData, HttpHeaders headers, Class<T> responseType, boolean insecure) {
        return resolveClient(insecure).post()
            .uri(URI.create(type.getUrl() + url))
            .headers(h -> h.addAll(headers))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .retrieve()
            .bodyToMono(responseType);
    }

    // ─── POST WITH CREDENTIAL ───────────────────────────────────────────────────

    public Mono<Map> postWithCredential(WebServiceType type, String clientId, String clientSecret) {
        return postWithCredential(type, clientId, clientSecret, false);
    }

    public Mono<Map> postWithCredential(WebServiceType type, String clientId, String clientSecret, boolean insecure) {
        String basicAuth = java.util.Base64.getEncoder()
            .encodeToString((clientId + ":" + clientSecret).getBytes(java.nio.charset.StandardCharsets.UTF_8));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        return resolveClient(insecure).post()
            .uri(URI.create(type.getUrl()))
            .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromFormData(body))
            .retrieve()
            .bodyToMono(Map.class);
    }
}
