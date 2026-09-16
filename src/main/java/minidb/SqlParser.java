package minidb;

import java.util.*;
import java.util.regex.*;

public class SqlParser {

    public sealed interface Command permits CreateTableCmd, InsertCmd, SelectCmd, DeleteCmd {}

    public record CreateTableCmd(String tableName, List<Column> columns) implements Command {}
    public record InsertCmd(String tableName, List<String> rawValues) implements Command {}
    public record SelectCmd(String tableName, List<String> columns, List<Condition> conditions) implements Command {}
    public record DeleteCmd(String tableName, List<Condition> conditions) implements Command {}

    private static final Pattern CREATE_PATTERN = Pattern.compile(
        "^CREATE\\s+TABLE\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\((.*)\\)$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern INSERT_PATTERN = Pattern.compile(
        "^INSERT\\s+INTO\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s+VALUES\\s*\\((.*)\\)$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SELECT_PATTERN = Pattern.compile(
        "^SELECT\\s+(.+?)\\s+FROM\\s+([a-zA-Z_][a-zA-Z0-9_]*)(?:\\s+WHERE\\s+(.+))?$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DELETE_PATTERN = Pattern.compile(
        "^DELETE\\s+FROM\\s+([a-zA-Z_][a-zA-Z0-9_]*)(?:\\s+WHERE\\s+(.+))?$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CONDITION_PATTERN = Pattern.compile(
        "^([a-zA-Z_][a-zA-Z0-9_]*)\\s*(<=|>=|!=|<|>|=)\\s*(.+)$"
    );

    public static Command parse(String rawSql) {
        if (rawSql == null || rawSql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query cannot be empty.");
        }

        String sql = rawSql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }

        Matcher createMatcher = CREATE_PATTERN.matcher(sql);
        if (createMatcher.matches()) {
            return parseCreate(createMatcher.group(1), createMatcher.group(2));
        }

        Matcher insertMatcher = INSERT_PATTERN.matcher(sql);
        if (insertMatcher.matches()) {
            return parseInsert(insertMatcher.group(1), insertMatcher.group(2));
        }

        Matcher selectMatcher = SELECT_PATTERN.matcher(sql);
        if (selectMatcher.matches()) {
            return parseSelect(selectMatcher.group(1), selectMatcher.group(2), selectMatcher.group(3));
        }

        Matcher deleteMatcher = DELETE_PATTERN.matcher(sql);
        if (deleteMatcher.matches()) {
            return parseDelete(deleteMatcher.group(1), deleteMatcher.group(2));
        }

        throw new IllegalArgumentException("Syntax error or unsupported SQL statement: '" + sql + "'");
    }

    private static CreateTableCmd parseCreate(String tableName, String colDefs) {
        List<Column> columns = new ArrayList<>();
        String[] parts = colDefs.split(",");
        for (String part : parts) {
            String[] tokens = part.trim().split("\\s+");
            if (tokens.length < 2) {
                throw new IllegalArgumentException("Invalid column definition: '" + part.trim() + "'");
            }
            String colName = tokens[0];
            DataType type;
            try {
                type = DataType.valueOf(tokens[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unsupported data type '" + tokens[1] + "'. Supported types: INT, STRING, BOOLEAN");
            }

            boolean isPk = false;
            if (tokens.length >= 4 &&
                tokens[2].equalsIgnoreCase("PRIMARY") &&
                tokens[3].equalsIgnoreCase("KEY")) {
                isPk = true;
            }
            columns.add(new Column(colName, type, isPk));
        }
        return new CreateTableCmd(tableName, columns);
    }

    private static InsertCmd parseInsert(String tableName, String valuesInside) {
        List<String> rawValues = splitCsv(valuesInside);
        return new InsertCmd(tableName, rawValues);
    }

    private static SelectCmd parseSelect(String colList, String tableName, String whereClause) {
        List<String> columns = new ArrayList<>();
        if (colList.trim().equals("*")) {
            columns.add("*");
        } else {
            for (String col : colList.split(",")) {
                columns.add(col.trim());
            }
        }
        List<Condition> conditions = parseWhere(whereClause);
        return new SelectCmd(tableName, columns, conditions);
    }

    private static DeleteCmd parseDelete(String tableName, String whereClause) {
        List<Condition> conditions = parseWhere(whereClause);
        return new DeleteCmd(tableName, conditions);
    }

    private static List<Condition> parseWhere(String whereClause) {
        List<Condition> conditions = new ArrayList<>();
        if (whereClause == null || whereClause.trim().isEmpty()) {
            return conditions;
        }

        String[] parts = whereClause.split("(?i)\\s+AND\\s+");
        for (String part : parts) {
            Matcher m = CONDITION_PATTERN.matcher(part.trim());
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid condition in WHERE clause: '" + part.trim() + "'");
            }
            String col = m.group(1).trim();
            String op = m.group(2).trim();
            String rawVal = m.group(3).trim();
            Object value = parseLiteral(rawVal);
            conditions.add(new Condition(col, op, value));
        }
        return conditions;
    }

    private static List<String> splitCsv(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '\'' || c == '"') && !inQuotes) {
                inQuotes = true;
                quoteChar = c;
                current.append(c);
            } else if (c == quoteChar && inQuotes) {
                inQuotes = false;
                quoteChar = 0;
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        return result;
    }

    private static Object parseLiteral(String raw) {
        raw = raw.trim();
        if ((raw.startsWith("'") && raw.endsWith("'")) ||
            (raw.startsWith("\"") && raw.endsWith("\""))) {
            return raw.substring(1, raw.length() - 1);
        }
        if (raw.equalsIgnoreCase("true")) return Boolean.TRUE;
        if (raw.equalsIgnoreCase("false")) return Boolean.FALSE;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return raw;
        }
    }
}
