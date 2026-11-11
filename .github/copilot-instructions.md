# Agent instructions — RentGo project

Before creating or changing any code, **read `PRD.md` in the repo root** (and `ARCHITECTURE.md` for layering, flows and design rationale) and follow it. Core rules:

- Java 21 (JavaSE-21) **Eclipse-style** project; write code using only Java ≤ 17 language features (`src/`, `test/`, `bin/`, `lib/`, `.project`, `.classpath`). Do not create Maven/Gradle files or `module-info.java`. Do not overwrite `.project`/`.classpath`.
- Libraries: only FlatLaf 3.8 (`lib/flatlaf-3.8.jar`) and PostgreSQL JDBC 42.7.14 (`lib/postgresql-42.7.14.jar`); database is PostgreSQL on Neon. No other frameworks, no Lombok, no `record` for models.
- MVC + DAO: `view` has no business logic or SQL; `controller` has no SQL; SQL only in `dao`; rental/late-fee rules only in `model`.
- **All code in English**: file, class, method, field, variable, package, enum, table and column names, comments and Javadoc. **User-facing text** (UI labels, dialogs, messages, receipt text) stays **Indonesian**.
- Apply OOP for real: abstract classes, inheritance, overriding + overloading, interfaces, encapsulation with validation in setters, generics, custom exceptions.
- Use `PreparedStatement`; never concatenate SQL. Never commit `bin/`, `build/`, `*.class`, or `config/db.properties` (contains Neon credentials).
- Do not change method contracts in PRD §6, §9, §10, §11 without team approval. For anything the PRD does not specify, pick the simplest option and note it in PRD §15.
- You run in a terminal: compile and test with the commands in PRD §4.4 (output to `build/classes`, never `bin/`). Every file must compile.
