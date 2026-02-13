package it.ey.entity.listener;


import it.ey.entity.Piao;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class PiaoEntityListener {

    private static final Logger log = LoggerFactory.getLogger(PiaoEntityListener.class);

    @PrePersist
    public void prePersist(Piao piao) {
        log.info("Inizializzazione PIAO  codPAFK={}", piao.getCodPAFK());

        piao.setCreatedTs(LocalDate.now());
        piao.setUpdatedTs(LocalDate.now()); //Serve al FE

    }

    @PreUpdate
    public void preUpdate(Piao piao) {
        log.info("Aggiornamento PIAO ID={} versione={}", piao.getId(), piao.getVersione());
        piao.setUpdatedTs(LocalDate.now());
    }
}