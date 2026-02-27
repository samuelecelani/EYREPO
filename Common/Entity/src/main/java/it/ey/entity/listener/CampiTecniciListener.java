package it.ey.entity.listener;

import it.ey.entity.Anagrafica;
import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDate;

//Peremette di eseguire delle operazioni prima si eseguire insert o update
public class CampiTecniciListener {


    @PrePersist
    public void beforeInsert(Object entity) {
        if (entity instanceof CampiTecnici campiTecnici) {
            campiTecnici.setCreatedTs(LocalDate.now());
            campiTecnici.setValidity(true);
            campiTecnici.setCreatedBy(campiTecnici.getCreatedBy());
            if (campiTecnici.getCreatedBy() == null) {
                campiTecnici.setCreatedBy("ADMIN");
            }
            else {
                campiTecnici.setCreatedBy(campiTecnici.getCreatedBy());
            }

            campiTecnici.setCreatedByRole("Referente");
            campiTecnici.setCreatedByNameSurname("Samuele Celani");

            campiTecnici.setValidity(true);
        }
    }

    @PreUpdate
    public void beforeUpdate(Object entity) {
        if (entity instanceof CampiTecnici campiTecnici) {
            campiTecnici.setUpdatedTs(LocalDate.now());
            campiTecnici.setCreatedTs(LocalDate.now());
            campiTecnici.setValidity(true);
            campiTecnici.setUpdatedBy(campiTecnici.getUpdatedBy());
            //serve per lo storico
            if (campiTecnici.getCreatedBy() == null) {
                campiTecnici.setCreatedBy("ADMIN");
            }
            if (campiTecnici.getUpdatedBy() == null) {
                campiTecnici.setUpdatedBy("ADMIN");
            }
            else {
                campiTecnici.setUpdatedBy(campiTecnici.getUpdatedBy());
            }

            campiTecnici.setUpdatedByRole("Referente");
            campiTecnici.setUpdatedByNameSurname("Samuele Celani");

        }
    }


}
