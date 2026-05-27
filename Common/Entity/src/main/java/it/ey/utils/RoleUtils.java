package it.ey.utils;

import java.util.Map;

public class RoleUtils {

    /**
     * Priorità dei ruoli: più basso il numero, più alto il ruolo.
     * ROLE_REFERENTE > ROLE_VALIDATORE > ROLE_REDATTORE
     */
    public static final Map<String, Integer> ROLE_PRIORITY = Map.of(
            "ROLE_REFERENTE",  0,
            "ROLE_VALIDATORE", 1,
            "ROLE_REDATTORE",  2
    );

    public static int rolePriority(String roleName) {
        return ROLE_PRIORITY.getOrDefault(roleName, Integer.MAX_VALUE);
    }
}
