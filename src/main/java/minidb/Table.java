package minidb;

import java.util.*;

public class Table {
    private final String name;
    private final List<Column> columns;
    private final Column primaryKeyColumn;

    private final List<Row> rows = new ArrayList<>();
    private final Map<Object, Row> pkIndex = new HashMap<>();

    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = columns;

        Column pk = null;
        for (Column col : columns) {
            if (col.isPrimaryKey()) {
                if (pk != null) {
                    throw new IllegalArgumentException("Table '" + name + "' cannot have multiple primary keys.");
                }
                pk = col;
            }
        }
        this.primaryKeyColumn = pk;
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public Column getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    private Object normalizePk(Object key) {
        if (key instanceof String s) {
            return s.toLowerCase();
        }
        return key;
    }

    public Column getColumn(String colName) {
        for (Column col : columns) {
            if (col.name().equalsIgnoreCase(colName)) {
                return col;
            }
        }
        return null;
    }

    public void insert(Row row) {
        if (primaryKeyColumn != null) {
            Object pkVal = row.get(primaryKeyColumn.name());
            if (pkVal == null) {
                throw new IllegalArgumentException("Primary key column '" + primaryKeyColumn.name() + "' cannot be null.");
            }
            Object normalizedPk = normalizePk(pkVal);
            if (pkIndex.containsKey(normalizedPk)) {
                throw new IllegalArgumentException("Duplicate key '" + pkVal + "' for primary key '" + primaryKeyColumn.name() + "'.");
            }
            pkIndex.put(normalizedPk, row);
        }
        rows.add(row);
    }

    public List<Row> select(List<Condition> conditions) {
        List<Row> result = new ArrayList<>();

        Condition pkCond = findPkEqualityCondition(conditions);
        if (pkCond != null && primaryKeyColumn != null) {
            Row row = pkIndex.get(normalizePk(pkCond.value()));
            if (row != null && matchesAll(row, conditions)) {
                result.add(row);
            }
            return result;
        }

        for (Row row : rows) {
            if (matchesAll(row, conditions)) {
                result.add(row);
            }
        }
        return result;
    }

    public int delete(List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            int count = rows.size();
            rows.clear();
            pkIndex.clear();
            return count;
        }

        Condition pkCond = findPkEqualityCondition(conditions);
        if (pkCond != null && primaryKeyColumn != null) {
            Row row = pkIndex.get(normalizePk(pkCond.value()));
            if (row != null && matchesAll(row, conditions)) {
                rows.remove(row);
                pkIndex.remove(normalizePk(pkCond.value()));
                return 1;
            }
            return 0;
        }

        int count = 0;
        Iterator<Row> it = rows.iterator();
        while (it.hasNext()) {
            Row row = it.next();
            if (matchesAll(row, conditions)) {
                it.remove();
                if (primaryKeyColumn != null) {
                    pkIndex.remove(normalizePk(row.get(primaryKeyColumn.name())));
                }
                count++;
            }
        }
        return count;
    }

    private Condition findPkEqualityCondition(List<Condition> conditions) {
        if (primaryKeyColumn == null || conditions == null) {
            return null;
        }
        for (Condition cond : conditions) {
            if (cond.column().equalsIgnoreCase(primaryKeyColumn.name()) && cond.operator().equals("=")) {
                return cond;
            }
        }
        return null;
    }

    private boolean matchesAll(Row row, List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        for (Condition cond : conditions) {
            if (!cond.evaluate(row)) {
                return false;
            }
        }
        return true;
    }
}
