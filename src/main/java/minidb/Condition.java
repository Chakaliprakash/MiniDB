package minidb;

public record Condition(String column, String operator, Object value) {

    public boolean evaluate(Row row) {
        Object rowVal = row.get(column);
        if (rowVal == null) {
            return false;
        }

        if (rowVal instanceof Integer rInt && value instanceof Integer cInt) {
            return switch (operator) {
                case "=" -> rInt.equals(cInt);
                case "!=" -> !rInt.equals(cInt);
                case ">" -> rInt > cInt;
                case "<" -> rInt < cInt;
                case ">=" -> rInt >= cInt;
                case "<=" -> rInt <= cInt;
                default -> false;
            };
        } else if (rowVal instanceof String rStr && value instanceof String cStr) {
            int cmp = rStr.compareToIgnoreCase(cStr);
            return switch (operator) {
                case "=" -> rStr.equalsIgnoreCase(cStr);
                case "!=" -> !rStr.equalsIgnoreCase(cStr);
                case ">" -> cmp > 0;
                case "<" -> cmp < 0;
                case ">=" -> cmp >= 0;
                case "<=" -> cmp <= 0;
                default -> false;
            };
        } else if (rowVal instanceof Boolean rBool && value instanceof Boolean cBool) {
            return switch (operator) {
                case "=" -> rBool.equals(cBool);
                case "!=" -> !rBool.equals(cBool);
                default -> false;
            };
        }

        return false;
    }
}
