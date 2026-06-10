package it.ey.piao.api.filter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(0) // Deve essere eseguito presto (prima di altri aspetti) così il filtro è già attivo quando partono le query
public class HibernateActiveFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;
      // Questa classe ha lo scopo di : Abilitare automaticamente l'Hibernate Filter "activeFilter" (X_ACTIVE = true)

     //Intercettiamo sia @Transactional sul metodo che @Transactional a livello di classe.
    @Around("@annotation(org.springframework.transaction.annotation.Transactional) || " +
        "@within(org.springframework.transaction.annotation.Transactional)")
    public Object aroundTransactional(ProceedingJoinPoint pjp) throws Throwable {

        // Recupero della Session Hibernate associata all'EntityManager corrente
        // (questa è la Session che verrà usata dai repository durante la transazione)
        Session session = entityManager.unwrap(Session.class);

        boolean enabledHere = false;

        try {
            // Abilitiamo il filtro solo se non è già attivo:
            // - evita duplicazioni
            // - gestisce chiamate annidate (un metodo @Transactional che chiama un altro @Transactional)
            if (session.getEnabledFilter("activeFilter") == null) {
                session.enableFilter("activeFilter");
                enabledHere = true;
                log.debug("Enabled activeFilter for {}", pjp.getSignature());
            }


            return pjp.proceed();

        } finally {
            // Disabilitiamo il filtro SOLO se lo abbiamo abilitato noi in questo advice:
            // in caso di chiamate annidate non vogliamo spegnere un filtro attivato da un livello esterno.
            if (enabledHere) {
                session.disableFilter("activeFilter");
                log.debug("Disabled activeFilter for {}", pjp.getSignature());
            }
        }
    }
}
