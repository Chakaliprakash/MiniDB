# MiniDB - In-Memory Relational DBMS in Java

A lightweight in-memory relational database management system written in Java 21 from scratch. I built this project to understand how databases work under the hood — how SQL commands get parsed, how tables organize rows, how WHERE clauses evaluate conditions, and how primary key indexing improves query speed.

No external SQL parsing libraries or database frameworks were used.

---

## Features

- **Custom SQL Tokenizer & Parser:** Hand-rolled parser using regular expressions and character scanning (no ANTLR, no Calcite).
- **Core SQL Commands:** Supports `CREATE TABLE`, `INSERT INTO`, `SELECT`, and `DELETE`.
- **Supported Data Types:** `INT`, `STRING`, and `BOOLEAN`.
- **Primary Key Indexing:** Fast $O(1)$ lookups using a `HashMap` on the primary key column when filtered by equality (`id = X`). Falls back to a full table scan for non-indexed columns or range queries.
- **Constraint Enforcement:** Rejects duplicate primary keys on `INSERT` with a clear error message.
- **WHERE Filtering:** Supports comparison operators (`=`, `!=`, `>`, `<`, `>=`, `<=`) and combined conditions with `AND`.
- **Formatted Console Output:** Displays `SELECT` results in an aligned ASCII table with dynamic column sizing.
- **Modern Java 21 Idioms:** Uses `record` for immutable row and condition storage, alongside pattern matching with `instanceof` and `switch` expressions.

---

## Supported Syntax

| Command | Example Syntax |
| :--- | :--- |
| **CREATE TABLE** | `CREATE TABLE users (id INT PRIMARY KEY, name STRING, age INT)` |
| **INSERT INTO** | `INSERT INTO users VALUES (1, 'Alice', 22)` |
| **SELECT (All)** | `SELECT * FROM users` |
| **SELECT (Projected + Filter)** | `SELECT name, age FROM users WHERE age > 20` |
| **SELECT (Index Lookup)** | `SELECT * FROM users WHERE id = 1` |
| **SELECT (Multiple Conditions)** | `SELECT * FROM users WHERE age > 20 AND id = 2` |
| **DELETE** | `DELETE FROM users WHERE id = 1` |

> **Note:** SQL keywords, table names, column names, and string equality comparisons are case-insensitive (e.g. `users`, `USERS`, and `Users` all match). Multiple conditions support `AND` (nested conditions and `OR` are planned for future versions).

---

## How to Compile and Run

### Option 1: Plain `javac` (No Build Tools Required)

From the project root:

```bash
# Compile all source files into the bin directory
javac -d bin src/main/java/minidb/*.java

# Run MiniDB CLI
java -cp bin minidb.Main
```

### Option 2: Maven (Optional)

```bash
# Compile and run with Maven
mvn compile exec:java
```

---

## Sample CLI Session

```text
======================================================
  MiniDB v1.0 - In-Memory Relational DBMS
  Supports: CREATE TABLE, INSERT, SELECT, DELETE
  Type 'exit' or 'quit' to exit.
======================================================
minidb> CREATE TABLE users (id INT PRIMARY KEY, name STRING, age INT)
Table 'users' created successfully.

minidb> INSERT INTO users VALUES (1, 'Alice', 22)
1 row inserted.

minidb> INSERT INTO users VALUES (2, 'Bob', 25)
1 row inserted.

minidb> INSERT INTO users VALUES (3, 'Charlie', 19)
1 row inserted.

minidb> INSERT INTO users VALUES (1, 'DuplicateAlice', 30)
Error: Duplicate key '1' for primary key 'id'.

minidb> SELECT * FROM users
+----+---------+-----+
| id | name    | age |
+----+---------+-----+
| 1  | Alice   | 22  |
| 2  | Bob     | 25  |
| 3  | Charlie | 19  |
+----+---------+-----+
3 rows in set.

minidb> SELECT name, age FROM users WHERE age > 20
+-------+-----+
| name  | age |
+-------+-----+
| Alice | 22  |
| Bob   | 25  |
+-------+-----+
2 rows in set.

minidb> SELECT * FROM users WHERE id = 1
+----+-------+-----+
| id | name  | age |
+----+-------+-----+
| 1  | Alice | 22  |
+----+-------+-----+
1 row in set.

minidb> DELETE FROM users WHERE id = 1
1 row deleted.

minidb> SELECT * FROM users
+----+---------+-----+
| id | name    | age |
+----+---------+-----+
| 2  | Bob     | 25  |
| 3  | Charlie | 19  |
+----+---------+-----+
2 rows in set.

minidb> exit
Goodbye!
```

---

## What I Learned

1. **Hash Index vs B+ Tree Tradeoffs:**
   Indexing the primary key with a `HashMap` makes point lookups (`WHERE id = 1`) lightning fast at $O(1)$ time complexity. However, a hash index cannot speed up range queries like `WHERE age > 20` or `WHERE id >= 10`. Building this helped me understand why real production databases (like MySQL InnoDB or Postgres) default to B+ Trees, which keep keys sorted and support both range scans and point lookups in $O(\log N)$.

2. **Immutable Data Modeling with Java Records:**
   Modeling `Row` as a Java `record` with an unmodifiable `Map` prevents rows from being accidentally modified once stored. It eliminates defensive cloning headaches and ensures that references returned by the query engine reflect a consistent snapshot of the table.

3. **Hand-Crafted Parsing & Tokenization Challenges:**
   Parsing SQL with regex and string splitting requires careful handling of edge cases — especially string literals containing spaces or commas (e.g. `'Alice Smith'`). Building `splitCsv` taught me how a lexer needs state (like `inQuotes`) to avoid corrupting user data.

4. **Cleaner Code with Modern Java 21:**
   Using pattern matching for `instanceof` (e.g. `rowVal instanceof Integer rInt && value instanceof Integer cInt`) combined with enhanced `switch` expressions drastically simplified condition evaluation and command dispatching, removing dozens of lines of repetitive casting boilerplate.

