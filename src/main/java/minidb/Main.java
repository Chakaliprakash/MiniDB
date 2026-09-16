package minidb;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Database db = new Database();
        Scanner scanner = new Scanner(System.in);

        System.out.println("======================================================");
        System.out.println("  MiniDB v1.0 - In-Memory Relational DBMS");
        System.out.println("  Supports: CREATE TABLE, INSERT, SELECT, DELETE");
        System.out.println("  Type 'exit' or 'quit' to exit.");
        System.out.println("======================================================");

        while (true) {
            System.out.print("minidb> ");
            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!");
                break;
            }

            try {
                db.execute(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }

        scanner.close();
    }
}

