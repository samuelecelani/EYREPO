package it.ey.piao.api.configuration;

import it.ey.common.annotation.ApiV1Controller;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BeanPostProcessor che verifica che tutti i controller REST usino l'annotazione @ApiV1Controller
 * invece di @RestController + @RequestMapping.
 *
 * Lancia un'eccezione all'avvio dell'applicazione se trova controller non conformi.
 */
@Component
public class ApiV1ControllerEnforcer implements BeanPostProcessor {

    private static final String CONTROLLER_PACKAGE = "it.ey.piao.api.controller.rest";

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        // Controlla solo i bean nel package dei controller REST
        if (beanClass.getPackageName().startsWith(CONTROLLER_PACKAGE)) {

            boolean hasRestController = beanClass.isAnnotationPresent(RestController.class);
            boolean hasRequestMapping = beanClass.isAnnotationPresent(RequestMapping.class);
            boolean hasApiV1Controller = beanClass.isAnnotationPresent(ApiV1Controller.class);

            // Se ha @RestController + @RequestMapping ma NON ha @ApiV1Controller, lancia eccezione
            if (hasRestController && hasRequestMapping && !hasApiV1Controller) {
                throw new IllegalStateException(
                    String.format(
                        "Il controller '%s' nel package '%s' deve usare @ApiV1Controller invece di @RestController + @RequestMapping. " +
                        "Esempio: @ApiV1Controller(\"/path\") invece di @RestController + @RequestMapping(\"/path\")",
                        beanClass.getSimpleName(),
                        beanClass.getPackageName()
                    )
                );
            }

            // Se ha solo @RestController senza @ApiV1Controller (controller senza path)
            if (hasRestController && !hasApiV1Controller) {
                throw new IllegalStateException(
                    String.format(
                        "Il controller '%s' nel package '%s' deve usare @ApiV1Controller. " +
                        "Tutti i controller REST devono essere annotati con @ApiV1Controller per garantire il prefisso api/v1.",
                        beanClass.getSimpleName(),
                        beanClass.getPackageName()
                    )
                );
            }
        }

        return bean;
    }
}
