<div align="center">

<img src="https://capsule-render.vercel.app/api?type=venom&height=195&color=0:111827,50:7c2d12,100:fb923c&text=MiniDB&fontColor=ffffff&fontSize=40&fontAlignY=40&desc=In-Memory%20Relational%20Database%20%7C%20Java%2021&descAlignY=64&descSize=15" width="100%" alt="MiniDB banner"/>

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge)
![SQL](https://img.shields.io/badge/SQL-Custom%20Parser-336791?style=for-the-badge)
![Indexing](https://img.shields.io/badge/Indexing-HashMap-64748b?style=for-the-badge)

[Repository](https://github.com/Chakaliprakash/MiniDB) · [Issues](https://github.com/Chakaliprakash/MiniDB/issues)

</div>

---

# MiniDB

MiniDB is a lightweight in-memory relational database implemented in Java 21. It demonstrates the main steps involved in parsing and executing a small SQL-like language.

## Features

- Parse `CREATE TABLE`, `INSERT INTO`, `SELECT`, and `DELETE` statements.
- Support `INT`, `STRING`, and `BOOLEAN` columns.
- Evaluate simple comparison conditions in queries.
- Store table rows in memory.
- Use a `HashMap` index for primary-key lookup.

## Design

The code separates SQL parsing, command representation, table storage, row data, and query execution into individual classes.

## Technology

- Java 21
- Maven
- Regular expressions and Java records/sealed interfaces

## Build and Run

Install JDK 21 and Maven, then run:

```bash
mvn compile exec:java
```

Enter SQL statements at the `minidb>` prompt. Use `exit` or `quit` to close the program.

MiniDB is an educational in-memory implementation; data is not persisted after the process exits, and it does not support the full SQL standard.

## Author

[Prakash Chakali](https://github.com/Chakaliprakash)
