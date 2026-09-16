package minidb;

public class Column {
    private final String name;
    private final DataType type;
    private final boolean isPrimaryKey;

    public Column(String name, DataType type, boolean isPrimaryKey) {
        this.name = name;
        this.type = type;
        this.isPrimaryKey = isPrimaryKey;
    }

    public String name() {
        return name;
    }

    public DataType type() {
        return type;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    @Override
    public String toString() {
        return name + " " + type + (isPrimaryKey ? " PRIMARY KEY" : "");
    }
}

