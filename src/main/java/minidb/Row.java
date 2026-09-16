package minidb;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public record Row(Map<String, Object> values) {

    public Row {
        Map<String, Object> caseInsensitive = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitive.putAll(values);
        values = Collections.unmodifiableMap(caseInsensitive);
    }

    public Object get(String column) {
        return values.get(column);
    }
}
