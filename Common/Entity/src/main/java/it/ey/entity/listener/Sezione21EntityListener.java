package it.ey.entity.listener;


import it.ey.entity.Sezione21;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class Sezione21EntityListener {

    private static final Logger log = LoggerFactory.getLogger(Sezione21EntityListener.class);

    @PrePersist
    public void prePersist(Sezione21 sezione21) {

        sezione21.setCreatedTs(LocalDate.now());
        sezione21.setValidity(true);
        sezione21.setCreatedBy(sezione21.getCreatedBy());
        if (sezione21.getCreatedBy() == null) {
            sezione21.setCreatedBy("ADMIN");
        } else {
            sezione21.setCreatedBy(sezione21.getCreatedBy());
        }
        sezione21.setValidity(true);
    }


    @PreUpdate
    public void preUpdate(Sezione21 sezione21) {
        sezione21.setUpdatedTs(LocalDate.now());
        sezione21.setValidity(true);
        sezione21.setCreatedBy(sezione21.getCreatedBy());
        if (sezione21.getCreatedBy() == null) {
            sezione21.setCreatedBy("ADMIN");
        } else {
            sezione21.setCreatedBy(sezione21.getCreatedBy());
        }
        sezione21.setValidity(true);
    }
}