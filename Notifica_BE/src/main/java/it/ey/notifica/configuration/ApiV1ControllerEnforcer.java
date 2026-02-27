package it.ey.notifica.configuration;

import it.ey.notifica.annotation.ApiV1Controller;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BeanPostProcessor che verifica che tutti i controller REST nel modulo
 * usino @ApiV1Controller invece di @RestController + @RequestMapping.
 */
@Component
public class ApiV1ControllerEnforcer implements BeanPostProcessor {

    private static final String CONTROLLER_PACKAGE = "it.ey.notifica.controller.rest";

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if (beanClass.getPackageName().startsWith(CONTROLLER_PACKAGE)) {
            boolean hasRestController = beanClass.isAnnotationPresent(RestController.class);
            boolean hasRequestMapping = beanClass.isAnnotationPresent(RequestMapping.class);
            boolean hasApiV1Controller = beanClass.isAnnotationPresent(ApiV1Controller.class);
            if (hasRestController && hasRequestMapping && !hasApiV1Controller) {
                throw new IllegalStateException(String.format(
                    "Il controller '%s' deve usare @ApiV1Controller invece di @RestController + @RequestMapping.",
                    beanClass.getSimpleName()));
            }
            if (hasRestController && !hasApiV1Controller) {
                throw new IllegalStateException(String.format(
                    "Il controller '%s' deve usare @ApiV1Controller.",
                    beanClass.getSimpleName()));
            }
        }
        return bean;
    }
}
