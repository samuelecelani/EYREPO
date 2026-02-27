package it.ey.piao.bff.httpClient;
import it.ey.enums.WebServiceType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


//Classe atomica e centralizzata per gestire le chiamate api attraverso l'utilizzo di webclient
@Service
public class WebClientService {

    private final WebClient webClient;



    public WebClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> Mono<T> get(String url, WebServiceType type, HttpHeaders headers, Class<T> responseType) {
        return webClient.get()
            .uri(type.getUrl()+url)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .bodyToMono(responseType);
    }

    public <T> Mono<T> get(String url, WebServiceType type, HttpHeaders headers, ParameterizedTypeReference<T> responseType) {
        return webClient
            .get()
            .uri(type.getUrl() + url)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .bodyToMono(responseType);
    }


    public <T> Mono<T> post(String url, WebServiceType type,Object body, HttpHeaders headers, Class<T> responseType) {
        return webClient.post()
            .uri(type.getUrl()+url)
            .headers(h -> h.addAll(headers))
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }

    public <T> Mono<T> post(String url, WebServiceType type, Object body, HttpHeaders headers, ParameterizedTypeReference<T> responseType) {
        return webClient.post()
            .uri(type.getUrl() + url)
            .headers(h -> h.addAll(headers))
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }

    public <T> Mono<T> put (String url, WebServiceType type,Object body, HttpHeaders headers, Class<T> responseType) {
        return webClient.put()
            .uri(type.getUrl()+url)
            .headers(h -> h.addAll(headers))
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }

    public <T> Mono<T> delete(String url, WebServiceType type,HttpHeaders headers, Class<T> responseType) {
        return webClient.delete()
            .uri(type.getUrl()+url)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .bodyToMono(responseType);
    }

    public Mono<Void> delete(String url, WebServiceType type, HttpHeaders headers) {
        return webClient.delete()
            .uri(type.getUrl() + url)
            .headers(h -> h.addAll(headers))
            .retrieve()
            .bodyToMono(Void.class);
    }

    public <T> Mono<T> patch(String url, WebServiceType type,Object body, HttpHeaders headers, Class<T> responseType) {
        return webClient.patch()
            .uri(type.getUrl()+url)
            .headers(h -> h.addAll(headers))
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType);
    }
}
