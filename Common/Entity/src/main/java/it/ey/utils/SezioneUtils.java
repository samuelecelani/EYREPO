package it.ey.utils;

import it.ey.dto.OVPDTO;
import it.ey.dto.Sezione1DTO;
import it.ey.dto.Sezione21DTO;
import it.ey.entity.OVP;
import it.ey.entity.Sezione1;
import it.ey.entity.Sezione21;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SezioneUtils {
    public static   <T> List<T> cleanList(List<T> list) {
        if (list == null) return null;
        return list.stream()
                .filter(Objects::nonNull)
                .filter(item -> {
                    // Se l'oggetto ha almeno un field non-null lo considera valido
                    for (Field f : item.getClass().getDeclaredFields()) {
                        try {
                            f.setAccessible(true);
                            Object v = f.get(item);
                            if (v != null) return true;
                        } catch (IllegalAccessException ignored) {}
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }



    public static Sezione1DTO sanitizeChildLists(Sezione1DTO request) {
        if (request == null) return null;

        // Questi nomi si basano sulle corrispondenze tipiche DTO -> entity; adattare se il DTO ha nomi diversi.
        try {
            request.setAreeOrganizzative(SezioneUtils.cleanList(request.getAreeOrganizzative()));
        } catch (Throwable ignored) {}
        try {
            request.setPrioritaPolitiche(SezioneUtils.cleanList(request.getPrioritaPolitiche()));
        } catch (Throwable ignored) {}
        try {
            request.setPrincipiGuida(SezioneUtils.cleanList(request.getPrincipiGuida()));
        } catch (Throwable ignored) {}
        try {
            request.setOrganiPolitici(SezioneUtils.cleanList(request.getOrganiPolitici()));
        } catch (Throwable ignored) {}
        try {
            request.setIntegrationTeams(SezioneUtils.cleanList(request.getIntegrationTeams()));
        } catch (Throwable ignored) {}

        return request;
    }
    public static void sanitizeRequestLists(Sezione21DTO request) {
        request.setFondiEuropei(cleanList(request.getFondiEuropei()));
        request.setProcedure(cleanList(request.getProcedure()));
    }

    public static void sanitizeJoinChildren(OVPDTO ovp) {
        if (ovp == null) return;

        // AreeOrganizzative: rimuovi null/invalidi e imposta back-reference
        if (ovp.getAreeOrganizzative() != null) {
            ovp.getAreeOrganizzative().removeIf(a ->
                a == null || a.getAreaOrganizzativa() == null || a.getAreaOrganizzativa().getId() == null
            );
            ovp.getAreeOrganizzative().forEach(a -> a.setOvp(ovp));
        }

        // PrioritaPolitiche: rimuovi null/invalidi e imposta back-reference
        if (ovp.getPrioritaPolitiche() != null) {
            ovp.getPrioritaPolitiche().removeIf(p ->
                p == null || p.getPrioritaPolitica() == null || p.getPrioritaPolitica().getId() == null
            );
            ovp.getPrioritaPolitiche().forEach(p -> p.setOvp(ovp));
        }

        // Stakeholders: rimuovi null/invalidi e imposta back-reference
        if (ovp.getStakeholders() != null) {
            ovp.getStakeholders().removeIf(s ->
                s == null || s.getStakeholder() == null || s.getStakeholder().getId() == null
            );
            ovp.getStakeholders().forEach(s -> s.setOvp(ovp));
        }
    }
    public static void sanitizeJoinChildren(Sezione1 sezione1) {
        if (sezione1 == null) return;

        // AreeOrganizzative: rimuovi null/invalidi e imposta back-reference
        if (sezione1.getAreeOrganizzative() != null) {
            sezione1.getAreeOrganizzative().removeIf(a ->
                a == null || a.getSezione1().getId() == null
            );
            sezione1.getAreeOrganizzative().forEach(a -> a.setSezione1(sezione1));
        }

        // PrioritaPolitiche: rimuovi null/invalidi e imposta back-reference
        if (sezione1.getPrioritaPolitiche() != null) {
            sezione1.getPrioritaPolitiche().removeIf(p ->
                p == null || p.getSezione1().getId() == null
            );
            sezione1.getPrioritaPolitiche().forEach(p -> p.setSezione1(sezione1));
        }

        // PrincipiGuida: rimuovi null/invalidi e imposta back-reference
        if (sezione1.getPrincipiGuida() != null) {
            sezione1.getPrincipiGuida().removeIf(pg ->
                pg == null || pg.getSezione1().getId() == null
            );
            sezione1.getPrincipiGuida().forEach(pg -> pg.setSezione1(sezione1));
        }
        if (sezione1.getIntegrationTeams() != null) {
            sezione1.getIntegrationTeams().removeIf(it ->
                it == null || it.getSezione1().getId() == null
            );
            sezione1.getIntegrationTeams().forEach(it -> it.setSezione1(sezione1));
        }
    }
    public static void sanitizeJoinChildren(Sezione21 sezione21) {
        if (sezione21 == null) return;

        // FondiEuropei: rimuovi null/invalidi e imposta back-reference
        if (sezione21.getFondiEuropei() != null) {
            sezione21.getFondiEuropei().removeIf(Objects::isNull
            );
            sezione21.getFondiEuropei().forEach(f -> f.setSezione21(sezione21));
        }
        if (sezione21.getProcedure() != null) {
            sezione21.getProcedure().removeIf(Objects::isNull
            );
            sezione21.getProcedure().forEach(p -> p.setSezione21(sezione21));
        }

    }

    public static void replaceJoinChildren(OVP managed, OVP source) {
        if (managed == null || source == null){
            return;
        }
        // AreeOrganizzative: clear + add con back-reference verso managed
        if (managed.getAreeOrganizzative() == null) {
            managed.setAreeOrganizzative(new ArrayList<>());
        }
        managed.getAreeOrganizzative().clear();
        if (source.getAreeOrganizzative() != null) {
            source.getAreeOrganizzative().forEach(a -> {
                a.setOvp(managed);
                managed.getAreeOrganizzative().add(a);
            });
        }

        // PrioritaPolitiche: clear + add con back-reference verso managed
        if (managed.getPrioritaPolitiche() == null) {
            managed.setPrioritaPolitiche(new ArrayList<>());
        }
        managed.getPrioritaPolitiche().clear();
        if (source.getPrioritaPolitiche() != null) {
            source.getPrioritaPolitiche().forEach(p -> {
                p.setOvp(managed);
                managed.getPrioritaPolitiche().add(p);
            });
        }

        // Stakeholders: clear + add con back-reference verso managed
        if (managed.getStakeholders() == null){
            managed.setStakeholders(new ArrayList<>());
        }
        managed.getStakeholders().clear();
        if (source.getStakeholders() != null) {
            source.getStakeholders().forEach(s -> {
                s.setOvp(managed);
                managed.getStakeholders().add(s);
            });
        }
    }
}
