package it.ey.utils;



import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DynamicTableUtils {

    public static Map<String, Object> buildTableFromJson(JsonNode jsonArray) {
        if (!jsonArray.isArray()) {
            throw new IllegalArgumentException("Risposta inattesa: non è un array");
        }

        Set<String> columns = new LinkedHashSet<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        for (JsonNode node : jsonArray) {
            Map<String, Object> row = new LinkedHashMap<>();
            node.fieldNames().forEachRemaining(field -> {
                columns.add(field);
                row.put(field, parseValue(node.get(field)));
            });
            rows.add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("columns", columns);
        result.put("rows", rows);
        return result;
    }

    private static Object parseValue(JsonNode valueNode) {
        if (valueNode.isNumber()) {
            return valueNode.numberValue();
        } else if (valueNode.isBoolean()) {
            return valueNode.booleanValue();
        } else if (valueNode.isTextual()) {
            String text = valueNode.asText();
            if (text.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(text);
            } else if (text.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) {
                return LocalDateTime.parse(text);
            }
            return text;
        }
        return valueNode.toString();
    }
}
