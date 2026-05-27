package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * DTO POJO equivalente a {@link org.springframework.data.domain.Page} usato per
 * serializzare/deserializzare risposte paginate fra BE, BFF e FE.
 * Evita di esporre {@code PageImpl} (che richiederebbe config particolari per la
 * deserializzazione lato WebClient).
 *
 * @param <T> tipo del contenuto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T>
{
    /** Elementi della pagina. */
    private List<T> content;
    /** Numero totale di elementi (su tutte le pagine). */
    private long totalElements;
    /** Numero totale di pagine. */
    private int totalPages;
    /** Indice pagina corrente (0-based). */
    private int number;
    /** Numero di elementi per pagina richiesti. */
    private int size;
    /** True se è la prima pagina. */
    private boolean first;
    /** True se è l'ultima pagina. */
    private boolean last;
    /** True se la pagina è vuota. */
    private boolean empty;

    /** Costruisce un PageDTO da una Spring {@link org.springframework.data.domain.Page}. */
    public static <T> PageDTO<T> from(org.springframework.data.domain.Page<T> page)
    {
        if (page == null) {
            return new PageDTO<>(Collections.emptyList(), 0L, 0, 0, 0, true, true, true);
        }
        return new PageDTO<>(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty()
        );
    }

    @JsonCreator
    public static <T> PageDTO<T> jsonCreator(
        @JsonProperty("content") List<T> content,
        @JsonProperty("totalElements") long totalElements,
        @JsonProperty("totalPages") int totalPages,
        @JsonProperty("number") int number,
        @JsonProperty("size") int size,
        @JsonProperty("first") boolean first,
        @JsonProperty("last") boolean last,
        @JsonProperty("empty") boolean empty)
    {
        return new PageDTO<>(content, totalElements, totalPages, number, size, first, last, empty);
    }
}

