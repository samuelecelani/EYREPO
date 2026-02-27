package it.ey.piao.api.configuration;

import it.ey.common.annotation.ApiV1Controller;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom RequestMappingHandlerMapping che gestisce l'annotazione @ApiV1Controller
 * aggiungendo automaticamente il prefisso "api/v1" ai path dei controller annotati.
 */
public class ApiV1RequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private static final String API_V1_PREFIX = "/api/v1";

    public ApiV1RequestMappingHandlerMapping() {
        // Configura il PathPatternParser per non generare pattern duplicati
        PathPatternParser parser = new PathPatternParser();
        parser.setMatchOptionalTrailingSeparator(false);
        setPatternParser(parser);
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);

        if (info == null) {
            return null;
        }

        ApiV1Controller apiV1Controller = AnnotatedElementUtils.findMergedAnnotation(handlerType, ApiV1Controller.class);

        if (apiV1Controller != null) {
            String controllerPath = apiV1Controller.value();
            String basePath = API_V1_PREFIX;

            if (controllerPath != null && !controllerPath.isEmpty()) {
                if (!controllerPath.startsWith("/")) {
                    controllerPath = "/" + controllerPath;
                }
                basePath = API_V1_PREFIX + controllerPath;
            }

            // Ottieni i pattern esistenti e aggiungi il prefisso manualmente
            Set<PathPattern> pathPatterns = info.getPathPatternsCondition() != null
                ? info.getPathPatternsCondition().getPatterns()
                : Set.of();

            final String finalBasePath = basePath;
            Set<String> patterns = pathPatterns.stream()
                .map(pattern -> {
                    String path = pattern.getPatternString();
                    // Normalizza: tratta "", "/" e null come equivalenti (path vuoto)
                    if (path == null || path.isEmpty() || path.equals("/")) {
                        return finalBasePath;
                    }
                    // Rimuovi trailing slash se presente (eccetto per root)
                    if (path.endsWith("/") && path.length() > 1) {
                        path = path.substring(0, path.length() - 1);
                    }
                    return finalBasePath + (path.startsWith("/") ? path : "/" + path);
                })
                .collect(Collectors.toSet()); // Set elimina automaticamente i duplicati

            // Se non ci sono pattern, usa solo il basePath
            if (patterns.isEmpty()) {
                patterns = Set.of(finalBasePath);
            }

            // Crea nuovo RequestMappingInfo con i path modificati
            return RequestMappingInfo.paths(patterns.toArray(new String[0]))
                .methods(info.getMethodsCondition().getMethods().toArray(new org.springframework.web.bind.annotation.RequestMethod[0]))
                .params(info.getParamsCondition().getExpressions().stream().map(Object::toString).toArray(String[]::new))
                .headers(info.getHeadersCondition().getExpressions().stream().map(Object::toString).toArray(String[]::new))
                .consumes(info.getConsumesCondition().getConsumableMediaTypes().stream().map(Object::toString).toArray(String[]::new))
                .produces(info.getProducesCondition().getProducibleMediaTypes().stream().map(Object::toString).toArray(String[]::new))
                .build();
        }

        return info;
    }
}
