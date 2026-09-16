package minidb;

public enum DataType {
    INT,
    STRING,
    BOOLEAN;

    public Object parse(String token) {
        token = token.trim();
        return switch (this) {
            case INT -> {
                try {
                    yield Integer.parseInt(token);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid integer value: '" + token + "'");
                }
            }
            case STRING -> {
                if ((token.startsWith("'") && token.endsWith("'")) ||
                    (token.startsWith("\"") && token.endsWith("\""))) {
                    yield token.substring(1, token.length() - 1);
                }
                yield token;
            }
            case BOOLEAN -> {
                if (token.equalsIgnoreCase("true")) {
                    yield Boolean.TRUE;
                } else if (token.equalsIgnoreCase("false")) {
                    yield Boolean.FALSE;
                }
                throw new IllegalArgumentException("Invalid boolean value: '" + token + "'. Expected true or false.");
            }
        };
    }
}
