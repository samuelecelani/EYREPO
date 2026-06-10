package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO per un indirizzo email (nome + email).
 * Rispecchia la struttura di MailAddress nel modulo dfp-email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailAddressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String name;
}

