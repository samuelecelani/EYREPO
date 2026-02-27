//package it.ey.entity.listener;
//
//
//import it.ey.entity.IntegrationTeam;
//import it.ey.entity.PrincipioGuida;
//import it.ey.entity.Sezione1;
//import jakarta.persistence.PrePersist;
//import jakarta.persistence.PreUpdate;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//
//public class Sezione1EntityListener {
//
//    private static final Logger log = LoggerFactory.getLogger(Sezione1EntityListener.class);
//
//    @PrePersist
//    public void prePersist(Sezione1 sezione1) {
//
//        List<IntegrationTeam> teams = sezione1.getIntegrationTeams();
//        if (teams != null && !teams.isEmpty()) {
//            boolean hasInvalidTeam = teams.stream()
//                    .anyMatch(team -> team == null || team.getSezione1() == null);
//
//            if (hasInvalidTeam) {
//                sezione1.setIntegrationTeams(null);
//            }
//
//            List<PrincipioGuida> principiGuida = sezione1.getPrincipiGuida();
//            if (principiGuida != null && !teams.isEmpty()) {
//                boolean hasInvalidPrincipioGuida = teams.stream()
//                        .anyMatch(principioGuida -> principioGuida == null || principioGuida.getSezione1() == null);
//
//                if (hasInvalidPrincipioGuida) {
//                    sezione1.setPrincipiGuida(null);
//                }
//            }
//        }
//    }
//    @PreUpdate
//    public void preUpdate(Sezione1 sezione1) {
//        List<IntegrationTeam> teams = sezione1.getIntegrationTeams();
//        if (teams != null && !teams.isEmpty()) {
//            boolean hasInvalidTeam = teams.stream()
//                    .anyMatch(team -> team == null || team.getSezione1() == null);
//
//            if (hasInvalidTeam) {
//                sezione1.setIntegrationTeams(null);
//            }
//
//            List<PrincipioGuida> principiGuida = sezione1.getPrincipiGuida();
//            if (principiGuida != null && !teams.isEmpty()) {
//                boolean hasInvalidPrincipioGuida = teams.stream()
//                        .anyMatch(principioGuida -> principioGuida == null || principioGuida.getSezione1() == null);
//
//                if (hasInvalidPrincipioGuida) {
//                    sezione1.setPrincipiGuida(null);
//                }
//            }
//        }
//    }
//}