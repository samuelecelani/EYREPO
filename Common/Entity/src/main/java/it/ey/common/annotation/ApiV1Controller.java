package it.ey.common.annotation;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * Annotazione custom per i Controller REST che aggiunge automaticamente
 * il prefisso "api/v1" al path del controller.
 * <p>
 * Esempio di utilizzo:
 * <pre>
 * {@code @ApiV1Controller("/users")}
 * public class UserController {
 *     // Gli endpoint saranno mappati su /api/v1/users/...
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
public @interface ApiV1Controller {

    /**
     * Path del controller che verrà prefissato con api/v1.
     */
    String value() default "";
}
