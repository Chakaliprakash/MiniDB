package minidb;

import java.util.*;

public class Database {
    private final Map<String, Table> tables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public void execute(String sql) {
        SqlParser.Command cmd = SqlParser.parse(sql);

        switch (cmd) {
            case SqlParser.CreateTableCmd create -> executeCreate(create);
            case SqlParser.InsertCmd insert -> executeInsert(insert);
            case SqlParser.SelectCmd select -> executeSelect(select);
            case SqlParser.DeleteCmd delete -> executeDelete(delete);
        }
    }

    private void executeCreate(SqlParser.CreateTableCmd cmd) {
        if (tables.containsKey(cmd.tableName())) {
            throw new IllegalArgumentException("Table '" + cmd.tableName() + "' already exists.");
        }
        Table table = new Table(cmd.tableName(), cmd.columns());
        tables.put(cmd.tableName(), table);
        System.out.println("Table '" + cmd.tableName() + "' created successfully.");
    }

    private void executeInsert(SqlParser.InsertCmd cmd) {
        Table table = getTableOrThrow(cmd.tableName());
        List<Column> columns = table.getColumns();

        if (cmd.rawValues().size() != columns.size()) {
            throw new IllegalArgumentException("Column count mismatch. Expected " + columns.size() +
                    " values, but got " + cmd.rawValues().size() + ".");
        }

        Map<String, Object> rowData = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);
            String rawVal = cmd.rawValues().get(i);
            Object parsedVal = col.type().parse(rawVal);
            rowData.put(col.name(), parsedVal);
        }

        table.insert(new Row(rowData));
        System.out.println("1 row inserted.");
    }

    private void executeSelect(SqlParser.SelectCmd cmd) {
        Table table = getTableOrThrow(cmd.tableName());

        List<String> displayCols = new ArrayList<>();
        if (cmd.columns().contains("*")) {
            for (Column col : table.getColumns()) {
                displayCols.add(col.name());
            }
        } else {
            for (String colName : cmd.columns()) {
                Column col = table.getColumn(colName);
                if (col == null) {
                    throw new IllegalArgumentException("Unknown column '" + colName + "' in table '" + table.getName() + "'.");
                }
                displayCols.add(col.name());
            }
        }

        for (Condition cond : cmd.conditions()) {
            if (table.getColumn(cond.column()) == null) {
                throw new IllegalArgumentException("Unknown column '" + cond.column() + "' in WHERE clause.");
            }
        }

        List<Row> rows = table.select(cmd.conditions());
        printTable(displayCols, rows);
    }

    private void executeDelete(SqlParser.DeleteCmd cmd) {
        Table table = getTableOrThrow(cmd.tableName());

        for (Condition cond : cmd.conditions()) {
            if (table.getColumn(cond.column()) == null) {
                throw new IllegalArgumentException("Unknown column '" + cond.column() + "' in WHERE clause.");
            }
        }

        int count = table.delete(cmd.conditions());
        System.out.println(count + (count == 1 ? " row deleted." : " rows deleted."));
    }

    private Table getTableOrThrow(String tableName) {
        Table table = tables.get(tableName);
        if (table == null) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
        }
        return table;
    }

    private void printTable(List<String> headers, List<Row> rows) {
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            widths[i] = headers.get(i).length();
        }

        for (Row row : rows) {
            for (int i = 0; i < headers.size(); i++) {
                Object val = row.get(headers.get(i));
                String text = (val != null) ? val.toString() : "NULL";
                if (text.length() > widths[i]) {
                    widths[i] = text.length();
                }
            }
        }

        StringBuilder border = new StringBuilder("+");
        for (int w : widths) {
            border.append("-".repeat(w + 2)).append("+");
        }
        String separator = border.toString();

        System.out.println(separator);

        System.out.print("|");
        for (int i = 0; i < headers.size(); i++) {
            System.out.printf(" %-" + widths[i] + "s |", headers.get(i));
        }
        System.out.println();
        System.out.println(separator);

        for (Row row : rows) {
            System.out.print("|");
            for (int i = 0; i < headers.size(); i++) {
                Object val = row.get(headers.get(i));
                String text = (val != null) ? val.toString() : "NULL";
                System.out.printf(" %-" + widths[i] + "s |", text);
            }
            System.out.println();
        }

        System.out.println(separator);
        System.out.println(rows.size() + (rows.size() == 1 ? " row in set." : " rows in set."));
    }
}
