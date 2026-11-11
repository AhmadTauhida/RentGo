# RentGo

RentGo is a Java Swing desktop vehicle-rental application using MVC + DAO and PostgreSQL on Neon.

## Prerequisites

- JDK 21
- Eclipse Java project with JavaSE-21
- Neon PostgreSQL access
- `lib/flatlaf-3.8.jar`
- `lib/postgresql-42.7.14.jar`

## Setup

1. Copy `config/db.properties.example` to `config/db.properties` and set the local Neon password.
2. Run `sql/schema.sql` once in the Neon SQL Editor.
3. Run `sql/seed.sql` to load demo vehicles and customers (safe before login).
4. Start the application once so `StaffDAO.ensureDefaultAdmin()` can create the initial account.
5. Run `sql/seed_transactions.sql` to load demo transactions (requires the `admin` account).

The initial account is `admin` / `admin123`; change it after the first login.

## Terminal build

```bash
rm -rf build/classes
mkdir -p build/classes
javac -encoding UTF-8 --module-path lib/flatlaf-3.8.jar:lib/postgresql-42.7.14.jar -d build/classes $(find src test -name '*.java')
java --module-path build/classes:lib/flatlaf-3.8.jar:lib/postgresql-42.7.14.jar --module RentGo/rentgo.model.RentalCostTest
```

The GUI requires a graphical desktop. Database credentials are local-only and must never be committed.
