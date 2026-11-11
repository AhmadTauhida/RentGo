# PRD — RentGo: Aplikasi Desktop Rental Kendaraan (Java Swing)

> **Dokumen ini dibaca oleh AI agent (berjalan di terminal) dan manusia.**
> Jika ada konflik antara dokumen ini dan asumsi Anda, **dokumen ini yang benar**.
> Jika ada hal yang tidak tercantum, ikuti [§15 Keputusan & Asumsi](#15-keputusan--asumsi) lalu pilih opsi paling sederhana. Jangan menambah framework/library di luar [§3](#3-tech-stack--dependensi).
>
> Dokumen ini berbahasa Indonesia, tetapi **seluruh kode, nama file, dan nama tabel/kolom memakai bahasa Inggris** (lihat §0 dan §13).

| Item | Nilai |
|---|---|
| Nama proyek (Eclipse) | **RentGo** |
| Root package | `rentgo` |
| Mata kuliah | Pemrograman Berorientasi Objek (PBO) — Tugas Proyek GUI, Teknik Komputer UNDIP |
| Tim | 3 orang (Anggota 1 = Model & DB, Anggota 2 = DAO & Controller, Anggota 3 = View) |
| Bahasa / UI toolkit | Java 21 (JavaSE-21), Swing + FlatLaf **3.8** |
| Arsitektur | MVC + DAO |
| Database | PostgreSQL di **Neon** (dipakai bersama tim) via JDBC (**pgjdbc 42.7.14**) |
| IDE | **Eclipse** (proyek Java biasa, bukan Maven/Gradle) |
| AI agent | Dijalankan dari **terminal** di root proyek |
| Batas laporan | 18 Nov 2026 · Presentasi & demo 10 menit: Minggu IV Nov 2026 |

---

## 0. Aturan Emas untuk Agent

1. **Struktur Eclipse, bukan Maven/Gradle.** Folder `src/`, `test/`, `bin/`, `lib/`, plus `.project` & `.classpath` yang **dibuat oleh Eclipse (manusia)**. Jangan buat `pom.xml`, `build.gradle`, `module-info.java`, atau struktur `src/main/java`. Jangan menimpa `.project`/`.classpath`; ubah `.classpath` hanya untuk menambah jar (§4.2).
2. **Hanya JDK + 2 library**: FlatLaf 3.8 dan PostgreSQL JDBC 42.7.14. Tanpa Lombok, Spring, Hibernate, JavaFX, dsb.
3. **MVC ketat.** `view` tidak boleh berisi logika bisnis maupun SQL. `controller` tidak boleh berisi SQL. SQL hanya di `dao`. Aturan hitung tarif/denda hanya di `model`.
4. **OOP harus nyata**, karena dinilai: abstract class, inheritance, polymorphism (overriding + overloading), interface, encapsulation (field `private`, setter memvalidasi), generics/collection, custom exception. Jangan menyederhanakannya menjadi `if (type == ...)`.
5. **Bahasa Inggris untuk semua yang berupa kode**: nama file, class, method, field, variabel, package, konstanta, nama tabel/kolom DB, nilai enum, serta komentar & Javadoc. **Teks yang tampil ke pengguna** (label UI, judul dialog, pesan error/sukses, isi struk) **tetap Bahasa Indonesia**.
6. **Class biasa untuk model** (bukan `record`, bukan Lombok). Proyek memakai JDK 21, tetapi **tulis kode hanya dengan fitur Java ≤ 17** (tanpa pattern matching untuk `switch`, record pattern, virtual thread, sequenced collections, dsb.) agar tetap bisa dikompilasi di JDK 17 bila dosen memakainya.
7. **Setiap file harus compile.** Stub yang belum diisi memakai `// TODO(Anggota N): ...` dan tetap valid secara sintaks.
8. **Jangan commit** `bin/`, `build/`, `*.class`, `config/db.properties`. Jangan hardcode kredensial database.
9. Jangan mengubah *kontrak* di §6, §9, §10, §11 tanpa persetujuan tim (3 orang bekerja paralel di atas kontrak itu).
10. **Agent berjalan di terminal, bukan di IDE.** Compile & uji dengan perintah di §4.4 yang output-nya ke `build/classes` (bukan `bin/`, milik Eclipse). Jangan mengandalkan fitur IDE.

---

## 1. Ringkasan Produk

RentGo dipakai **petugas rental** (bukan pelanggan) untuk mencatat kendaraan, pelanggan, transaksi sewa, pembayaran, dan pengembalian. Sistem menghitung biaya sewa secara **polimorfik** sesuai jenis kendaraan, menghitung denda keterlambatan, dan menjaga status kendaraan.

Dua peran: **Admin** (semua data + kelola akun staff) dan **Staff** (petugas; fokus pelayanan transaksi).

### Alur bisnis utama
1. Login → 2. pilih/daftarkan pelanggan → 3. pilih kendaraan `AVAILABLE`, isi tanggal sewa & tanggal kembali, (opsi sopir), total dihitung otomatis → 4. simpan transaksi: status kendaraan `RENTED`, pembayaran dicatat → 5. saat kendaraan kembali: catat pengembalian, hitung denda bila terlambat, kendaraan kembali `AVAILABLE`.

### Di luar cakupan
Aplikasi web/mobile, akses pelanggan langsung, integrasi payment gateway, jenis kendaraan selain mobil/motor/bus, cetak ke printer fisik (struk hanya tampil sebagai teks).

---

## 2. Cakupan & Prioritas

Waktu terbatas, jadi kerjakan berurutan. **Seluruh P0 wajib selesai sebelum P1 dimulai.**

| Prioritas | Isi |
|---|---|
| **P0 (inti, wajib demo)** | DB + seed; model lengkap (10 kelas + interface + enum + exception); `DatabaseConnection`; DAO Vehicle/Customer/Staff/Rental/Payment/VehicleReturn; Login (+ pembatasan peran); CRUD Vehicle; CRUD Customer; buat Rental dengan hitung biaya polimorfik; VehicleReturn + denda; validasi + dialog konfirmasi/error |
| **P1** | Pencarian & filter di tabel; batalkan/ubah Rental; catat pembayaran + struk (`PrintableReceipt`); `ReportPanel` (pendapatan + kendaraan terlaris); status bar |
| **P2 (bonus)** | `UserManagementPanel` kelola akun staff (Admin); catat pembayaran denda; polish tampilan; unit test JUnit |

### Kriteria tugas yang harus terlihat di kode (Definition of Done)
- ≥ 8 kelas model (rancangan: 10) dengan 2 hierarki pewarisan.
- 4 pilar OOP terlihat jelas (§6.3).
- ≥ 3 tampilan GUI (rancangan: 7), ≥ 3 entitas dengan CRUD (Vehicle, Customer, Rental).
- `List<T>` / `Map<K,V>` bertipe generik; ada `interface` generik `CrudDAO<T>`.
- Exception handling: custom exception + `try-catch` di DAO/controller; aplikasi tidak crash saat DB tidak terjangkau atau input salah.
- Tata letak tetap rapi saat jendela di-resize.
- Data permanen di PostgreSQL (Neon).
- Git: branch per fitur, PR ke `main`.

---

## 3. Tech Stack & Dependensi

| Komponen | Pilihan | Catatan |
|---|---|---|
| JDK | **21** (LTS) — Eclipse Execution Environment `JavaSE-21` | Semua anggota memakai JDK 21. Kode dibatasi fitur ≤ Java 17 (§0 no. 6) |
| UI | Java Swing + **FlatLaf 3.8** (`com.formdev:flatlaf:3.8`) | Tema `FlatLightLaf`; `FlatLightLaf.setup()` dipanggil sebelum frame dibuat |
| Database | **PostgreSQL** di Neon (serverless). Kode & SQL harus juga jalan di PostgreSQL lokal | Hanya SQL standar + sintaks Postgres di §8 |
| JDBC | **PostgreSQL JDBC Driver 42.7.14** (`org.postgresql:postgresql:42.7.14`) | Driver `org.postgresql.Driver` |
| Hash password | JDK `javax.crypto` **PBKDF2WithHmacSHA256** | Tanpa library tambahan |
| Test | Console test berbasis `main()` di `test/`; JUnit opsional (P2) | |

**Library dikelola manual di folder `lib/`** (gaya Eclipse). Nama file **persis**:
- `lib/flatlaf-3.8.jar` — `https://repo1.maven.org/maven2/com/formdev/flatlaf/3.8/flatlaf-3.8.jar`
- `lib/postgresql-42.7.14.jar` — `https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar`

> **Agent:** versi di atas sudah ditetapkan tim; jangan diganti atau "diperbarui". Jika jar belum ada di `lib/`, berhenti di langkah yang membutuhkannya dan minta manusia menambahkannya ke `lib/` dan Build Path Eclipse.

Konfigurasi koneksi (tidak di-commit): `config/db.properties`
```properties
# Neon: salin host, database, user dari dashboard Neon -> Connect (pilih Java/JDBC). SSL wajib.
db.url=jdbc:postgresql://<ep-xxxx>.<region>.aws.neon.tech/<dbname>?sslmode=require&connectTimeout=15&socketTimeout=30
db.user=<neon_user>
db.password=<neon_password>
# Postgres lokal (cadangan saat tanpa internet): jdbc:postgresql://localhost:5432/rentgo
```
- `config/db.properties` berisi **kredensial asli → dilarang di-commit** (ada di `.gitignore`). Bagikan ke anggota lewat chat pribadi, bukan repo/PR/issue. Commit hanya `config/db.properties.example` dengan placeholder.
- Jangan memakai format URL `postgresql://user:pass@host/db` dari dashboard langsung; JDBC butuh `jdbc:postgresql://host/db?...` dengan `db.user`/`db.password` terpisah.
- Neon menghentikan compute yang menganggur (*autosuspend*): koneksi pertama setelah lama diam bisa memakan beberapa detik. Karena itu `connectTimeout` ≥ 10 detik dan `DatabaseConnection` mencoba ulang **sekali** sebelum melempar `DatabaseException`.

---

## 4. Struktur Proyek (Eclipse Java Project)

Satu *Java Project* biasa di Eclipse: source folder `src` dan `test`, output `bin`, library di `lib` lewat Build Path. `.project` + `.classpath` **dibuat Eclipse dan di-commit** agar anggota lain cukup *Import → Existing Projects into Workspace*.

```
RentGo/                              ← root repo = root Eclipse project
├── .project                         (dibuat Eclipse; di-commit)
├── .classpath                       (dibuat Eclipse; di-commit)
├── .gitignore
├── .github/
│   └── copilot-instructions.md      (instruksi singkat agent → menunjuk ke PRD.md)
├── PRD.md
├── README.md
├── docs/
│   ├── DEV-GUIDE-1-MODEL-DB.md          (panduan langkah Anggota 1)
│   ├── DEV-GUIDE-2-DAO-CONTROLLER.md    (panduan langkah Anggota 2)
│   └── DEV-GUIDE-3-VIEW.md              (panduan langkah Anggota 3)
├── config/
│   ├── db.properties.example
│   └── db.properties                (lokal, di-ignore)
├── lib/
│   ├── flatlaf-3.8.jar
│   └── postgresql-42.7.14.jar
├── sql/
│   ├── schema.sql                   (DDL, §8)
│   └── seed.sql                     (data contoh)
├── src/                             (source folder 1)
│   └── rentgo/
│       ├── Main.java
│       ├── model/
│       │   ├── Person.java              (abstract)
│       │   ├── Customer.java
│       │   ├── Staff.java
│       │   ├── Vehicle.java             (abstract)
│       │   ├── Car.java
│       │   ├── Motorcycle.java
│       │   ├── Bus.java
│       │   ├── Rental.java
│       │   ├── Payment.java
│       │   ├── VehicleReturn.java
│       │   ├── PrintableReceipt.java    (interface)
│       │   ├── VehicleStatus.java       (enum)
│       │   ├── VehicleType.java         (enum)
│       │   ├── PaymentMethod.java       (enum)
│       │   ├── Role.java                (enum)
│       │   ├── RentalStatus.java        (enum)
│       │   └── VehicleCondition.java    (enum)
│       ├── exception/
│       │   ├── InvalidDataException.java
│       │   ├── VehicleUnavailableException.java
│       │   └── DatabaseException.java
│       ├── dao/
│       │   ├── DatabaseConnection.java  (Singleton)
│       │   ├── CrudDAO.java             (interface generik)
│       │   ├── VehicleDAO.java
│       │   ├── CustomerDAO.java
│       │   ├── StaffDAO.java
│       │   ├── RentalDAO.java
│       │   ├── PaymentDAO.java
│       │   ├── VehicleReturnDAO.java
│       │   └── ReportDAO.java
│       ├── controller/
│       │   ├── LoginController.java
│       │   ├── VehicleController.java
│       │   ├── CustomerController.java
│       │   ├── RentalController.java
│       │   ├── VehicleReturnController.java
│       │   ├── ReportController.java
│       │   └── UserManagementController.java   (P2)
│       ├── view/
│       │   ├── LoginFrame.java
│       │   ├── MainFrame.java
│       │   ├── VehiclePanel.java
│       │   ├── CustomerPanel.java
│       │   ├── RentalPanel.java
│       │   ├── VehicleReturnPanel.java
│       │   ├── ReportPanel.java
│       │   └── UserManagementPanel.java        (P2)
│       └── util/
│           ├── Validator.java
│           ├── CurrencyFormatter.java
│           ├── PasswordHasher.java
│           └── Session.java
├── test/                            (source folder 2)
│   └── rentgo/
│       ├── model/RentalCostTest.java       (console; cek tarif/denda)
│       ├── view/ViewPreview.java           (opsional; peluncur pratinjau UI dengan data dummy)
│       └── dao/DaoConsoleTest.java         (console; cek CRUD ke DB)
├── build/                           (output compile agent; di-ignore)
└── bin/                             (output compile Eclipse; di-ignore)
```

### 4.1 `.project` (referensi — dibuat Eclipse)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
	<name>RentGo</name>
	<comment></comment>
	<projects></projects>
	<buildSpec>
		<buildCommand>
			<name>org.eclipse.jdt.core.javabuilder</name>
			<arguments></arguments>
		</buildCommand>
	</buildSpec>
	<natures>
		<nature>org.eclipse.jdt.core.javanature</nature>
	</natures>
</projectDescription>
```

### 4.2 `.classpath` (referensi bentuk akhir)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
	<classpathentry kind="src" path="src"/>
	<classpathentry kind="src" path="test"/>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-21"/>
	<classpathentry kind="lib" path="lib/flatlaf-3.8.jar"/>
	<classpathentry kind="lib" path="lib/postgresql-42.7.14.jar"/>
	<classpathentry kind="output" path="bin"/>
</classpath>
```
Tidak ada wildcard; jar baru = salin ke `lib/` **dan** tambahkan baris `kind="lib"` (atau lewat Eclipse: Build Path → Add JARs).

### 4.3 `.gitignore`
```
/bin/
/build/
.idea/
*.class
.settings/
config/db.properties
*.log
```

### 4.4 Build & run dari terminal (untuk agent)
Output ke `build/classes` agar tidak bentrok dengan `bin/` milik Eclipse.
```bash
# Linux/macOS/Git Bash
mkdir -p build/classes
javac -encoding UTF-8 -cp "lib/*" -d build/classes $(find src test -name "*.java")
java -cp "build/classes:lib/*" rentgo.Main
java -cp "build/classes:lib/*" rentgo.model.RentalCostTest
# Windows (PowerShell)
New-Item -ItemType Directory -Force build/classes | Out-Null
javac -encoding UTF-8 -cp "lib/*" -d build/classes (Get-ChildItem -Recurse src,test -Filter *.java).FullName
java -cp "build/classes;lib/*" rentgo.Main
java -cp "build/classes;lib/*" rentgo.model.RentalCostTest
```
Working directory harus root proyek agar `config/db.properties` terbaca (default di Eclipse maupun terminal). Perintah `rentgo.Main` membuka GUI; di lingkungan tanpa layar (headless) cukup verifikasi compile dan jalankan test console.

---

## 5. Arsitektur

```
 ┌────────┐ event  ┌────────────┐ panggil  ┌───────┐   SQL    ┌──────────┐
 │  view  │ ─────► │ controller │ ───────► │  dao  │ ───────► │PostgreSQL│
 │ (Swing)│ ◄───── │            │ ◄─────── │ (JDBC)│ ◄─────── │  (Neon)  │
 └────────┘ update └─────┬──────┘  objek   └───┬───┘  rows    └──────────┘
                         │                     │
                         └────────► model ◄────┘      util / exception dipakai semua lapisan
                              (domain + aturan bisnis)
```

| Lapisan | Boleh | Dilarang |
|---|---|---|
| `model` | Atribut, validasi setter, **aturan hitung tarif/denda**, `printReceipt()` | `javax.swing`, `java.sql`, akses DB |
| `dao` | JDBC, SQL, mapping `ResultSet` ↔ objek model, transaksi JDBC | `javax.swing`, dialog, aturan bisnis |
| `controller` | Validasi input (via `Validator`), memanggil model & DAO, menangkap exception lalu memberi tahu view | SQL, membuat komponen Swing baru |
| `view` | Komponen Swing, layout, memasang listener, menampilkan data/dialog | SQL, `if` aturan bisnis, memanggil DAO langsung |

**Wiring** (di `MainFrame`): view dibuat dulu, lalu controller dibuat dengan view + DAO, lalu `view.setController(controller)`. Listener di view memanggil `controller.method()`:
```java
VehiclePanel panel = new VehiclePanel();
VehicleController controller = new VehicleController(panel, new VehicleDAO());
panel.setController(controller);
controller.loadData();
```
**Alur start (`Main`)**: `FlatLightLaf.setup()` → `SwingUtilities.invokeLater` → cek `DatabaseConnection` (gagal ⇒ dialog error jelas, bukan stack trace) → `StaffDAO.ensureDefaultAdmin()` → `LoginFrame` → sukses ⇒ `Session.setStaff(...)`, tutup login, buka `MainFrame`.

Operasi DB berjalan di EDT (cukup untuk skala tugas; `SwingWorker` tidak wajib).

---

## 6. Spesifikasi Model (paket `rentgo.model`)

### 6.1 Diagram kelas
```mermaid
classDiagram
  class Person { <<abstract>> -int id; -String name; -String phoneNumber; +getDisplayInfo()* String }
  Person <|-- Customer
  Person <|-- Staff
  class Customer { -String nationalId; -String address }
  class Staff { -String username; -String passwordHash; -Role role }

  class Vehicle { <<abstract>> -int id; -String plateNumber; -String brand; -int manufactureYear; -double baseRate; -VehicleStatus status; +calculateRent(int)* double; +calculateRent(int, boolean) double; +getType()* VehicleType }
  Vehicle <|-- Car
  Vehicle <|-- Motorcycle
  Vehicle <|-- Bus
  class Car { -int passengerCapacity; -String transmission }
  class Motorcycle { -int engineCc }
  class Bus { -int seatCount }

  class PrintableReceipt { <<interface>> +printReceipt() String }
  class Rental { -int id; -LocalDate startDate; -LocalDate dueDate; -boolean withDriver; -double totalCost; -RentalStatus status; +getRentalDays() int; +calculateTotalCost() double }
  class Payment { -int id; -PaymentMethod method; -double amount; -LocalDateTime paidAt; -String note }
  class VehicleReturn { -int id; -LocalDate actualReturnDate; -VehicleCondition condition; -double lateFee; +calculateLateDays() int; +calculateLateFee() double }
  PrintableReceipt <|.. Rental
  PrintableReceipt <|.. Payment
  PrintableReceipt <|.. VehicleReturn
  Customer "1" --> "0..*" Rental : makes
  Vehicle "1" --> "0..*" Rental : rented in
  Staff "1" --> "0..*" Rental : processes
  Rental "1" --> "1..*" Payment : has
  Rental "1" --> "0..1" VehicleReturn : ends with
```

### 6.2 Spesifikasi per kelas

**Aturan umum:** field `private`; getter publik; setter publik **memvalidasi** dan melempar `InvalidDataException` (unchecked) bila tidak valid (pesan Bahasa Indonesia); konstruktor memanggil setter (bukan assign langsung) agar validasi konsisten. Semua model punya konstruktor dengan `id` (dari DB) dan konstruktor tanpa `id` (objek baru, `id = 0`). Beri `toString()` yang ramah tampilan (dipakai `JComboBox`).

| Kelas | Jenis | Atribut | Method penting & validasi |
|---|---|---|---|
| `Person` | `abstract` | `int id`, `String name`, `String phoneNumber` | `abstract String getDisplayInfo()`. `name` wajib isi; `phoneNumber` hanya digit/`+`, 8–15 karakter (boleh kosong) |
| `Customer` | extends `Person` | `String nationalId` (NIK), `String address` | `super(...)`; `nationalId` 16 digit; `address` wajib. Override `getDisplayInfo()` |
| `Staff` | extends `Person` | `String username`, `String passwordHash`, `Role role` | `super(...)`; `username` 3–50 karakter tanpa spasi. `boolean isAdmin()`. Override `getDisplayInfo()` |
| `Vehicle` | `abstract` | `int id`, `String plateNumber`, `String brand`, `int manufactureYear`, `double baseRate`, `VehicleStatus status` | `abstract double calculateRent(int days)`; `double calculateRent(int days, boolean withDriver)` **konkret**, default `return calculateRent(days)`; `abstract VehicleType getType()`; `boolean isAvailable()`; `protected void validateDays(int days)` (days ≥ 1). Validasi: `plateNumber` tidak kosong, `baseRate >= 0`, `manufactureYear` ∈ [1980, tahun sekarang + 1] |
| `Car` | extends `Vehicle` | `int passengerCapacity`, `String transmission` (`"MANUAL"`/`"AUTOMATIC"`) | override `calculateRent(int)`; **override** `calculateRent(int, boolean)` menambah sopir |
| `Motorcycle` | extends `Vehicle` | `int engineCc` | override `calculateRent(int)` |
| `Bus` | extends `Vehicle` | `int seatCount` | override `calculateRent(int)` (sopir **wajib**, sudah termasuk) |
| `Rental` | transaksi, implements `PrintableReceipt` | `int id`, `Customer customer`, `Vehicle vehicle`, `Staff staff`, `LocalDate startDate`, `LocalDate dueDate` *(rencana kembali)*, `boolean withDriver`, `double totalCost`, `RentalStatus status` | `int getRentalDays()`; `double calculateTotalCost()` = `vehicle.calculateRent(getRentalDays(), withDriver)` (**tanpa `instanceof`**). Setter `dueDate` menolak tanggal sebelum `startDate`. `printReceipt()` |
| `Payment` | transaksi, implements `PrintableReceipt` | `int id`, `Rental rental`, `PaymentMethod method`, `double amount`, `LocalDateTime paidAt`, `String note` | `amount > 0`. `printReceipt()` |
| `VehicleReturn` | transaksi, implements `PrintableReceipt` | `int id`, `Rental rental`, `LocalDate actualReturnDate`, `VehicleCondition condition`, `double lateFee` | `int calculateLateDays()`; `double calculateLateFee()`. `printReceipt()` |

**Interface & enum**

| Nama | Isi |
|---|---|
| `PrintableReceipt` | `String printReceipt();` — mengembalikan teks struk (monospace, Bahasa Indonesia) yang ditampilkan view di dialog |
| `VehicleStatus` | `AVAILABLE, RENTED, MAINTENANCE` |
| `VehicleType` | `CAR, MOTORCYCLE, BUS` |
| `PaymentMethod` | `CASH, TRANSFER, EWALLET` |
| `Role` | `ADMIN, STAFF` |
| `RentalStatus` | `ACTIVE, COMPLETED, CANCELLED` |
| `VehicleCondition` | `GOOD, MINOR_DAMAGE, MAJOR_DAMAGE` |

Setiap enum menyediakan `getLabel()` (teks Indonesia untuk UI, mis. `AVAILABLE` → `"Tersedia"`), `toString()` mengembalikan label itu, dan `static X fromString(String)` yang cocok dengan `name()` (nilai di DB).

### 6.3 Penerapan 4 pilar (harus terlihat di kode)

| Pilar | Wujud |
|---|---|
| Enkapsulasi | Semua field `private`; setter memvalidasi |
| Pewarisan | `Car/Motorcycle/Bus → Vehicle` dan `Customer/Staff → Person`, konstruktor memanggil `super(...)` |
| Abstraksi | `Vehicle.calculateRent(int)` & `Person.getDisplayInfo()` abstrak; interface `PrintableReceipt` diimplementasi 3 kelas |
| Polimorfisme | *Overriding*: `calculateRent(int)` di 3 subclass, dipanggil lewat referensi `Vehicle`. *Overloading*: `calculateRent(int)` vs `calculateRent(int, boolean)` |

### 6.4 Referensi implementasi (bentuk, bukan kode final)
```java
public abstract class Vehicle {
    private double baseRate;
    public abstract double calculateRent(int days);
    /** Overload; Car meng-override untuk menambah biaya sopir. */
    public double calculateRent(int days, boolean withDriver) { return calculateRent(days); }
    public void setBaseRate(double baseRate) {
        if (baseRate < 0) throw new InvalidDataException("Tarif dasar tidak boleh negatif");
        this.baseRate = baseRate;
    }
}
public class Car extends Vehicle {
    public static final int DISCOUNT_THRESHOLD_DAYS = 7;
    public static final double DISCOUNT_RATE = 0.10;
    public static final double DRIVER_FEE_PER_DAY = 150_000;
    @Override public double calculateRent(int days) {
        validateDays(days);
        double total = getBaseRate() * days;
        return days >= DISCOUNT_THRESHOLD_DAYS ? total * (1 - DISCOUNT_RATE) : total;
    }
    @Override public double calculateRent(int days, boolean withDriver) {
        return calculateRent(days) + (withDriver ? DRIVER_FEE_PER_DAY * days : 0);
    }
}
// Pemanggilan polimorfik
Vehicle v = vehicles.get(0);
double cost = v.calculateRent(5);
```

---

## 7. Aturan Bisnis

### 7.1 Tarif sewa (contoh dari proposal; konstanta ada di kelas masing-masing)

| Jenis | Tarif dasar/hari | Aturan |
|---|---|---|
| Car (mobil) | Rp350.000 | Diskon **10%** atas biaya dasar bila `days >= 7`; opsi sopir **+Rp150.000/hari** (tidak didiskon) |
| Motorcycle (motor) | Rp100.000 | Diskon **15%** bila `days >= 7` |
| Bus | Rp1.500.000 | Sopir **wajib +Rp300.000/hari**; tanpa diskon |

Rumus:
- Car: `baseRate×days × (days≥7 ? 0.9 : 1) + (withDriver ? 150000×days : 0)`
- Motorcycle: `baseRate×days × (days≥7 ? 0.85 : 1)`
- Bus: `baseRate×days + 300000×days`

`baseRate` diambil dari data kendaraan (bukan konstanta), sehingga admin bisa mengubahnya.

### 7.2 Lama sewa & denda
- `rentalDays = max(1, DAYS.between(startDate, dueDate))` (tanggal sama = 1 hari).
- `lateDays = max(0, DAYS.between(dueDate, actualReturnDate))`.
- `lateFee = lateDays × 1.5 × vehicle.getBaseRate()` (konstanta `LATE_FEE_MULTIPLIER = 1.5`, 150% tarif dasar per hari).

### 7.3 Status & transisi
- **Vehicle**: `AVAILABLE → RENTED` (transaksi tersimpan) `→ AVAILABLE` (pengembalian atau pembatalan). `MAINTENANCE` hanya diubah manual (Admin) lewat form kendaraan.
- **Rental**: `ACTIVE → COMPLETED` (pengembalian) atau `ACTIVE → CANCELLED`. Hanya `ACTIVE` yang boleh diubah/dibatalkan.
- Menyewa kendaraan berstatus `RENTED`/`MAINTENANCE` ⇒ lempar `VehicleUnavailableException`.
- Kendaraan/pelanggan yang sudah punya riwayat transaksi **tidak dapat dihapus** (FK) ⇒ tampilkan pesan ramah, jangan stack trace. Kendaraan berstatus `RENTED` tidak boleh dihapus/diubah jenisnya.
- Mengubah Rental `ACTIVE`: boleh mengubah `startDate`, `dueDate`, `withDriver` (total dihitung ulang). **Tidak** boleh mengganti kendaraan/pelanggan (batalkan lalu buat baru).
- Bus: `withDriver` dipaksa `true`.

### 7.4 Transaksi database (atomik)
- **Simpan rental** = `INSERT rental` + `UPDATE vehicle SET status='RENTED'` + `INSERT payment` dalam **satu transaksi JDBC** (`setAutoCommit(false)`, `commit`, `rollback` di `catch`, kembalikan `autoCommit(true)` di `finally`).
- **Simpan pengembalian** = `INSERT vehicle_return` + `UPDATE rental SET status='COMPLETED'` + `UPDATE vehicle SET status='AVAILABLE'` (+ `INSERT payment` denda bila P2).
- **Batalkan** = `UPDATE rental SET status='CANCELLED'` + `UPDATE vehicle SET status='AVAILABLE'`.
- Cek ketersediaan dilakukan **di dalam transaksi** (baca status terbaru) sebelum insert.

### 7.5 Hak akses

| Fitur | Admin | Staff |
|---|:-:|:-:|
| Vehicle: lihat | ✓ | ✓ |
| Vehicle: tambah/ubah/hapus | ✓ | ✗ (tombol dinonaktifkan) |
| Customer: CRUD | ✓ | ✓ |
| Rental, VehicleReturn, Payment | ✓ | ✓ |
| Report | ✓ | ✓ |
| Kelola akun staff (menu "Akun") | ✓ | ✗ (menu disembunyikan) |

Pembatasan diterapkan di `MainFrame` (menu) dan panel (tombol) berdasarkan `Session.getStaff().isAdmin()`.

---

## 8. Database (`sql/schema.sql`)

Pewarisan `Vehicle` dipetakan ke **satu tabel** (`vehicle_type` sebagai pembeda; kolom khusus subclass boleh `NULL`). `Customer` dan `Staff` masing-masing tabel sendiri.

```sql
-- Jalankan SEKALI pada database Neon bersama (SQL Editor Neon atau psql).
-- Neon sudah menyediakan database default; tidak perlu CREATE DATABASE.

CREATE TABLE staff (
  id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  phone_number  VARCHAR(20),
  username      VARCHAR(50)  NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(10)  NOT NULL DEFAULT 'STAFF' CHECK (role IN ('ADMIN','STAFF'))
);

CREATE TABLE customer (
  id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name         VARCHAR(100) NOT NULL,
  phone_number VARCHAR(20),
  national_id  CHAR(16)     NOT NULL UNIQUE,
  address      TEXT         NOT NULL
);

CREATE TABLE vehicle (
  id                 INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  vehicle_type       VARCHAR(12)   NOT NULL CHECK (vehicle_type IN ('CAR','MOTORCYCLE','BUS')),
  plate_number       VARCHAR(15)   NOT NULL UNIQUE,
  brand              VARCHAR(50)   NOT NULL,
  manufacture_year   SMALLINT      NOT NULL,
  base_rate          NUMERIC(12,2) NOT NULL CHECK (base_rate >= 0),
  status             VARCHAR(12)   NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE','RENTED','MAINTENANCE')),
  passenger_capacity INT NULL,          -- CAR
  transmission       VARCHAR(10) NULL,  -- CAR
  engine_cc          INT NULL,          -- MOTORCYCLE
  seat_count         INT NULL           -- BUS
);

CREATE TABLE rental (
  id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  customer_id INT NOT NULL REFERENCES customer(id),
  vehicle_id  INT NOT NULL REFERENCES vehicle(id),
  staff_id    INT NOT NULL REFERENCES staff(id),
  start_date  DATE NOT NULL,
  due_date    DATE NOT NULL,              -- rencana kembali
  with_driver BOOLEAN NOT NULL DEFAULT FALSE,
  total_cost  NUMERIC(14,2) NOT NULL,
  status      VARCHAR(12) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','COMPLETED','CANCELLED')),
  CONSTRAINT chk_rental_dates CHECK (due_date >= start_date)
);

CREATE TABLE payment (
  id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  rental_id INT NOT NULL REFERENCES rental(id),
  method    VARCHAR(10)   NOT NULL CHECK (method IN ('CASH','TRANSFER','EWALLET')),
  amount    NUMERIC(14,2) NOT NULL,
  paid_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  note      VARCHAR(100) NULL             -- 'RENTAL' atau 'LATE_FEE'
);

CREATE TABLE vehicle_return (
  id                  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  rental_id           INT NOT NULL UNIQUE REFERENCES rental(id),  -- maks. satu pengembalian per transaksi
  actual_return_date  DATE NOT NULL,
  vehicle_condition   VARCHAR(15)   NOT NULL DEFAULT 'GOOD' CHECK (vehicle_condition IN ('GOOD','MINOR_DAMAGE','MAJOR_DAMAGE')),
  late_fee            NUMERIC(14,2) NOT NULL DEFAULT 0
);
```

Catatan Postgres:
- Kolom pilihan memakai `VARCHAR + CHECK`, **bukan** tipe `ENUM` Postgres, agar `PreparedStatement.setString(...)` langsung bisa dipakai tanpa *cast*. Di Java simpan `enum.name()`.
- Ambil `id` hasil insert dengan `prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)` atau klausa `RETURNING id`.
- Nama tabel/kolom huruf kecil tanpa tanda kutip.
- Tipe `DATE`/`TIMESTAMP` dibaca dengan `rs.getObject(column, LocalDate.class)` / `LocalDateTime.class` (didukung pgjdbc).

**`sql/seed.sql`** (jalankan sekali, setelah `schema.sql`): minimal 3 car, 3 motorcycle, 1 bus (status campuran termasuk satu `MAINTENANCE`), 5 customer, serta beberapa transaksi contoh (satu aktif, satu selesai terlambat) agar laporan punya data saat demo. Akun staff **tidak** di-seed lewat SQL (hash harus dari Java) — lihat di bawah.

**Akun awal:** saat start, `StaffDAO.ensureDefaultAdmin()` membuat `admin` / `admin123` (role `ADMIN`) **hanya bila tabel `staff` kosong**, hash lewat `PasswordHasher`. Cantumkan kredensial ini di `README.md`.

---

## 9. Kontrak DAO (paket `rentgo.dao`)

Aturan: method melempar **`DatabaseException`** (unchecked, membungkus `SQLException` dengan pesan Indonesia). Gunakan `PreparedStatement` + try-with-resources; dilarang menyambung string SQL dengan input.

```java
public interface CrudDAO<T> {
    void add(T obj);                 // set id hasil generated key (RETURN_GENERATED_KEYS / RETURNING) ke obj
    void update(T obj);
    void delete(int id);
    T findById(int id);              // null bila tidak ada
    List<T> findAll();
}
```

| Kelas | Implements | Method tambahan |
|---|---|---|
| `DatabaseConnection` | — | **Singleton**: `static DatabaseConnection getInstance()`, `Connection getConnection()` (buat ulang bila `null`/`isClosed()`/`!isValid(2)`), `void close()`. Memuat `config/db.properties`, `Class.forName("org.postgresql.Driver")`. Gagal sekali ⇒ coba ulang satu kali (Neon *cold start*) ⇒ `DatabaseException` dengan pesan yang menyebut cek internet, kredensial/URL di `config/db.properties`, dan status database Neon |
| `VehicleDAO` | `CrudDAO<Vehicle>` | `List<Vehicle> search(String keyword, VehicleType type, VehicleStatus status)` (parameter `null` = tidak difilter; keyword cocok ke plat/merek); `List<Vehicle> findAvailable()`; `void updateStatus(int id, VehicleStatus status)`. Mapping baris → `Car/Motorcycle/Bus` berdasarkan kolom `vehicle_type` (satu *factory method* privat) |
| `CustomerDAO` | `CrudDAO<Customer>` | `List<Customer> search(String nameOrNationalId)`; `Map<String, Customer> mapByNationalId()` |
| `StaffDAO` | `CrudDAO<Staff>` | `Staff findByUsername(String username)` (null bila tidak ada); `void ensureDefaultAdmin()` |
| `RentalDAO` | `CrudDAO<Rental>` | `void saveRental(Rental rental, Payment payment)` (§7.4, cek ketersediaan, lempar `VehicleUnavailableException`); `void cancel(int rentalId)`; `List<Rental> findActive()`; `List<Rental> findHistory()` — `add/delete` dari `CrudDAO` boleh `UnsupportedOperationException` karena `saveRental` dipakai; `update` hanya untuk status `ACTIVE` |
| `PaymentDAO` | `CrudDAO<Payment>` | `List<Payment> findByRentalId(int rentalId)` |
| `VehicleReturnDAO` | — | `void saveReturn(VehicleReturn vehicleReturn)` (transaksi §7.4); `VehicleReturn findByRentalId(int rentalId)` |
| `ReportDAO` | — | `List<Object[]> revenue(LocalDate from, LocalDate to)` → baris `{date, transactionCount, totalRevenue}`; `List<Object[]> topVehicles(int limit)` → `{plateNumber, brand, vehicleType, rentalCount}` |

Objek `Rental` hasil DAO harus berisi `Customer`, `Vehicle`, `Staff` yang sudah terisi (JOIN atau query lanjutan), agar `printReceipt()` dan tabel riwayat tidak perlu menyentuh DB lagi.

---

## 10. Kontrak Controller (paket `rentgo.controller`)

Controller menerima view + DAO lewat konstruktor. Setiap method publik menangkap `InvalidDataException`, `VehicleUnavailableException`, `DatabaseException`, `NumberFormatException` dan menampilkannya lewat `view.showError(String)`; keberhasilan lewat `view.showInfo(String)`. Tidak ada exception yang lolos ke Swing.

| Controller | Konstruktor | Method publik |
|---|---|---|
| `LoginController` | `(LoginFrame, StaffDAO)` | `void login()` (ambil username/password dari view, verifikasi `PasswordHasher`, set `Session`, buka `MainFrame`); `void cancel()`; `static void logout()` |
| `VehicleController` | `(VehiclePanel, VehicleDAO)` | `loadData()`, `addVehicle()`, `updateVehicle()`, `deleteVehicle()` (konfirmasi dulu), `search()`, `clearForm()`, `selectRow(int index)` |
| `CustomerController` | `(CustomerPanel, CustomerDAO)` | `loadData()`, `addCustomer()`, `updateCustomer()`, `deleteCustomer()`, `search()`, `clearForm()`, `selectRow(int)` |
| `RentalController` | `(RentalPanel, RentalDAO, CustomerDAO, VehicleDAO, PaymentDAO)` | `loadOptions()` (isi combo customer & kendaraan tersedia), `calculateTotal()` (pratinjau biaya langsung), `saveRental()`, `updateRental()`, `cancelRental()`, `loadHistory()`, `printReceipt()`, `selectRow(int)` |
| `VehicleReturnController` | `(VehicleReturnPanel, RentalDAO, VehicleReturnDAO)` | `loadActiveRentals()`, `calculateLateFee()` (pratinjau), `saveReturn()` |
| `ReportController` | `(ReportPanel, ReportDAO)` | `loadReport()` (membaca periode terpilih) |
| *(P2)* `UserManagementController` | `(UserManagementPanel, StaffDAO)` | CRUD akun staff; password disimpan via `PasswordHasher` |

Validasi input memakai `util.Validator`; konversi teks → angka/tanggal terjadi **di controller** (view hanya memberi `String`/objek komponen).

---

## 11. Spesifikasi View (paket `rentgo.view`)

### 11.1 Aturan umum
- Semua kelas `extends JFrame` / `JPanel`. FlatLaf `FlatLightLaf`.
- Layout: `GridBagLayout` untuk form, `BorderLayout` untuk kerangka halaman, `CardLayout` di `MainFrame`. **Tidak ada layout `null` / posisi absolut.** Tabel dalam `JScrollPane`. Tata letak harus rapi saat di-resize; `MainFrame` punya `setMinimumSize(new Dimension(1000, 650))`.
- Setiap panel menyimpan `private XController controller` + `public void setController(XController c)`. Listener: `addButton.addActionListener(e -> controller.addVehicle());` — satu aksi satu method.
- Setiap panel menyediakan method standar (dipakai controller): `void showError(String)`, `void showInfo(String)`, `boolean confirm(String message)` (`JOptionPane.showConfirmDialog`).
- Tabel memakai `DefaultTableModel` non-editable; panel menyediakan `void setTableData(List<T>)` dan `T getSelectedItem()` / `int getSelectedIndex()`.
- Uang ditampilkan lewat `CurrencyFormatter.formatRupiah(double)`; tanggal `dd-MM-yyyy`.
- Hapus data **wajib** `confirm(...)` lebih dulu.
- **Teks yang tampil (label, judul, pesan) Bahasa Indonesia**; nama komponen/variabel Inggris (`addButton`, `plateNumberField`, `vehicleTable`).

### 11.2 Daftar tampilan

| View | Layout | Komponen utama (label UI Indonesia) | Getter/setter yang diekspos ke controller |
|---|---|---|---|
| `LoginFrame` | `GridBagLayout` di tengah | `JTextField` username, `JPasswordField`, `JButton` "Masuk" & "Batal" | `getUsername()`, `getPassword()` (`char[]`), tombol di-bind ke `login()`/`cancel()`; Enter = Masuk |
| `MainFrame` | `BorderLayout`: `JMenuBar` atas, `CardLayout` tengah, status bar bawah | Menu **Data** (Kendaraan, Pelanggan), **Transaksi** (Penyewaan, Pengembalian), **Laporan**, **Akun** (Admin; berisi Pengguna & Logout) | Konstanta nama kartu: `"VEHICLE"`, `"CUSTOMER"`, `"RENTAL"`, `"RETURN"`, `"REPORT"`, `"USERS"`; `showCard(String name)`; status bar: "Masuk sebagai {nama} ({role})". Saat kartu dibuka, panggil `loadData()` controller terkait agar data segar |
| `VehiclePanel` | `BorderLayout`: pencarian/filter di atas, form kiri (`GridBagLayout`), tabel tengah | `JTextField` plat, merek, tarif, cari; `JComboBox<VehicleType>`, `JComboBox<VehicleStatus>`; `JSpinner` tahun, jumlah penumpang/kursi, kapasitas CC; `JRadioButton` transmisi (Manual/Otomatis); `JTable`; tombol Tambah, Ubah, Hapus, Bersihkan | Field khusus subclass **ditampilkan/disembunyikan sesuai jenis terpilih** (Car: penumpang+transmisi; Motorcycle: CC; Bus: kursi). Getter per field bertipe `String` |
| `CustomerPanel` | pola sama `VehiclePanel` | `JTextField` nama, NIK, telepon, cari; `JTextArea` alamat (dalam `JScrollPane`); `JTable`; tombol CRUD | getter per field |
| `RentalPanel` | `BorderLayout`: form transaksi atas, tabel riwayat bawah | `JComboBox<Customer>`, `JComboBox<Vehicle>` (hanya `AVAILABLE`), `JSpinner` + `SpinnerDateModel` tanggal sewa & kembali, `JCheckBox` "Dengan sopir" (dikunci & dicentang untuk Bus), `JComboBox<PaymentMethod>`, `JLabel` total biaya (live), `JTable` riwayat; tombol Simpan, Ubah, Batalkan, Cetak Struk | Ubah combo/tanggal/checkbox ⇒ `controller.calculateTotal()`. `setTotalCost(String)`. Konversi `Date`→`LocalDate` dilakukan di controller |
| `VehicleReturnPanel` | `GridBagLayout` | `JComboBox<Rental>` (transaksi `ACTIVE`), `JSpinner` tanggal kembali aktual, `JComboBox<VehicleCondition>`, `JLabel` info sewa (rencana kembali, hari terlambat), `JLabel` denda; tombol Simpan | `setLateFeeInfo(int lateDays, String lateFeeText)` |
| `ReportPanel` | `BorderLayout` | `JComboBox` periode (Hari ini / 7 hari terakhir / Bulan ini / Tahun ini / Semua), `JTable` ringkasan pendapatan, `JTable` kendaraan terlaris, `JLabel` total | `setRevenueData(List<Object[]>)`, `setTopVehiclesData(List<Object[]>)`, `getPeriod()` |
| `UserManagementPanel` *(P2)* | pola `CustomerPanel` | `JTextField` nama/username/telepon, `JPasswordField`, `JComboBox<Role>`, `JTable` | — |

Struk ditampilkan lewat `JOptionPane` berisi `JTextArea` (font monospaced, tidak editable) dari `Rental.printReceipt()`.

---

## 12. Exception & Validasi

| Exception | Tipe | Dilempar saat |
|---|---|---|
| `InvalidDataException` | `extends RuntimeException` | Input/setter tidak valid (kosong, negatif, format salah, tanggal terbalik) |
| `VehicleUnavailableException` | `extends RuntimeException` | Kendaraan tidak `AVAILABLE` saat transaksi dibuat |
| `DatabaseException` | `extends RuntimeException` | `SQLException` di DAO/`DatabaseConnection` dibungkus (simpan `cause`); pesan Indonesia yang informatif; pelanggaran FK/UNIQUE diterjemahkan lewat `SQLException.getSQLState()` (`23505` = UNIQUE, `23503` = FK; mis. "Plat nomor sudah terdaftar", "Data masih dipakai transaksi lain") |

Semua exception punya konstruktor `(String message)` dan `(String message, Throwable cause)`.

`util.Validator` (static): `requireNotBlank(String value, String fieldName)`, `parsePositiveDouble(String text, String fieldName)`, `parseInt(String text, String fieldName)`, `validateNationalId(String)` (16 digit), `validatePhone(String)`, `validatePlate(String)`, `validateYear(int)`. Semuanya melempar `InvalidDataException` dengan pesan (Indonesia) yang menyebut **nama field** yang salah. `NumberFormatException` ditangkap dan dikonversi menjadi pesan yang sama.

`util.CurrencyFormatter`: `static String formatRupiah(double)` → `"Rp350.000"` (`NumberFormat` locale `id-ID`, tanpa desimal); `static double parseRupiah(String)`.

`util.PasswordHasher`: `static String hash(char[] password)` (PBKDF2WithHmacSHA256, salt acak 16 byte, ≥ 120.000 iterasi, format `iterations:saltBase64:hashBase64`), `static boolean verify(char[] password, String stored)`.

`util.Session`: holder statis `Staff getStaff()`, `void setStaff(Staff)`, `void clear()`.

---

## 13. Konvensi Kode

- Paket root `rentgo`; satu kelas publik per file; encoding UTF-8; indentasi 4 spasi.
- **Bahasa Inggris** untuk semua identifier, nama file, komentar, Javadoc, nama tabel/kolom, nilai enum. **Bahasa Indonesia** hanya untuk teks yang tampil ke pengguna (label UI, dialog, pesan exception yang ditampilkan, isi struk).
- Kelas `PascalCase`, method/field `camelCase`, konstanta `UPPER_SNAKE_CASE`, paket huruf kecil, tabel/kolom DB `snake_case`.
- Tidak ada `System.out.println` selain di `test/` dan log error sementara; jangan menelan exception kosong (`catch (Exception e) {}` dilarang).
- Javadoc Inggris satu-dua kalimat pada setiap kelas publik; komentar hanya untuk "mengapa".
- Magic number tarif/diskon/denda → `public static final` di kelas model.
- Nilai default UI (ukuran font, warna) mengikuti FlatLaf; jangan di-hardcode berlebihan.

---

## 14. Git, Kepemilikan, & Timeline

### 14.1 Branch & aturan
Dilarang commit langsung ke `main`; semua lewat Pull Request yang di-review bersama.

| Anggota | Branch | Memiliki |
|---|---|---|
| 1 — Database Architect & Core OOP | `feature/model-oop` | `model/`, `exception/`, `sql/`, setup repo awal (`.gitignore`, `.project`, `.classpath`, `PRD.md`) |
| 2 — DAO & Integration | `feature/dao-controller` | `dao/`, `controller/`, `util/` (`Validator`, `CurrencyFormatter`, `PasswordHasher`, `Session`), `config/`, `test/` |
| 3 — GUI & UX | `feature/gui-view` | `view/`, `Main.java` |

Anggota 1 menyiapkan **kerangka awal (§16)** dan merge ke `main` lebih dulu, sehingga kedua branch lain mulai dari kerangka yang sama dan tidak berkonflik. Perubahan pada kontrak (§6/§9/§10/§11) = PR terpisah + persetujuan semua anggota.

### 14.2 Timeline (mengikuti dokumen Pembagian Tugas)

| Minggu | Target |
|---|---|
| 1 | DDL PostgreSQL di Neon + paket `model` + interface + exception di-commit |
| 2 | `dao` berjalan + layout Swing (FlatLaf) + controller terhubung |
| 3 | Test alur penuh, laporan final → **kumpul 18 Nov 2026** |
| 4 | Finalisasi PPT + latihan demo 10 menit (Login → CRUD → Sewa → Kembali → Laporan) |

---

## 15. Keputusan & Asumsi

Hal-hal berikut **tidak eksplisit** di dokumen sumber; diputuskan di sini agar agent tidak menebak. Tim boleh mengubahnya (lewat PR yang mengedit bagian ini).

| # | Keputusan |
|---|---|
| A1 | Root package `rentgo`; Java 21 (JavaSE-21) dengan kode dibatasi fitur ≤ Java 17; proyek Eclipse bernama `RentGo`. |
| A2 | **Penamaan Inggris** untuk semua kode, nama file, tabel/kolom, enum, komentar. Teks tampilan ke pengguna tetap Indonesia. Pemetaan istilah proposal → kode: Orang→`Person`, Pelanggan→`Customer`, Petugas→`Staff`, Kendaraan→`Vehicle`, Mobil→`Car`, Motor→`Motorcycle`, Penyewaan→`Rental`, Pembayaran→`Payment`, Pengembalian→`VehicleReturn` (`Return` hindari karena kata kunci Java), DapatDicetak→`PrintableReceipt` (bukan `Printable` agar tidak bentrok dengan `java.awt.print.Printable`), `hitungSewa`→`calculateRent`, `tampilkanInfo`→`getDisplayInfo`, `cetakStruk`→`printReceipt`, `KoneksiDB`→`DatabaseConnection`. Proposal/laporan tetap memakai istilah Indonesia; sertakan tabel pemetaan ini di laporan (Bab 1) agar dosen mudah mengikuti. |
| A3 | Enum tambahan di luar daftar proposal: `VehicleType`, `RentalStatus`, `VehicleCondition`. |
| A4 | Kelas tambahan di luar daftar proposal: `VehicleReturnDAO`, `ReportDAO`, `ReportController`, `CrudDAO<T>`, `PasswordHasher`, `Session`, `UserManagementPanel` (P2). Semuanya dibutuhkan fitur yang sudah tercantum (pengembalian, laporan, generics, login, kelola akun). |
| A5 | `calculateRent(int, boolean)` dideklarasikan **konkret di `Vehicle`** (default memanggil `calculateRent(int)`) dan di-override `Car`, agar `Rental` tidak butuh `instanceof`. Overloading tetap terlihat di `Car`. |
| A6 | `printReceipt()` mengembalikan `String` (tanpa printer). |
| A7 | `payment.note` (nullable) ditambahkan untuk membedakan pembayaran sewa vs denda. |
| A8 | Matriks hak akses §7.5 adalah usulan; dokumen sumber hanya menyebut Admin mengelola seluruh data & akun, Staff fokus transaksi. |
| A9 | Password di-hash dengan PBKDF2 (JDK); akun awal `admin/admin123` dibuat otomatis bila tabel `staff` kosong. |
| A10 | Kerusakan kendaraan (`vehicle_condition`) **tidak** memengaruhi biaya; kendaraan selalu kembali `AVAILABLE` setelah pengembalian (sesuai proposal). |
| A11 | Pembayaran saat membuat sewa dicatat penuh sebesar `totalCost` (metode dipilih di `RentalPanel`). Cicilan/DP di luar cakupan. |
| A12 | `LocalDate`/`LocalDateTime` dipakai di model; `JSpinner(SpinnerDateModel)` memakai `java.util.Date` sehingga konversi dilakukan di controller. |
| A13 | Tipe uang `double` di model (sesuai contoh proposal), `NUMERIC` di DB. |
| A14 | Dokumen sumber punya jadwal berbeda (proposal vs Pembagian Tugas). **Pembagian Tugas dipakai** karena lebih baru. |
| A15 | Dokumen Pembagian Tugas tidak mencantumkan `StaffDAO` dan `DatabaseException`; keduanya tetap dibuat karena login dan exception handling DAO membutuhkannya. |
| A16 | Database **PostgreSQL di Neon** (satu database bersama tim; menggantikan MySQL di proposal). Kredensial tidak masuk repo. Untuk demo tanpa internet, ganti `db.url` ke Postgres lokal tanpa mengubah kode. Opsional: branch Neon terpisah untuk development dan demo. |
| A17 | Versi dependensi ditetapkan tim: FlatLaf **3.8**, pgjdbc **42.7.14**. |
| A18 | IDE **Eclipse** saja; agent dijalankan dari terminal dan compile ke `build/classes`. Setelah agent mengubah file, anggota tim me-*refresh* proyek di Eclipse (F5). |

---

## 16. Tugas Awal untuk Agent: Mengisi Kerangka (Tahap 0)

**Prasyarat (dikerjakan manusia di Eclipse sebelum agent dijalankan):** proyek `RentGo` sudah dibuat dengan source folder `src` & `test`, output `bin`, package kosong sesuai §4, folder `lib/` berisi dua jar yang sudah masuk Build Path, serta folder `config/` dan `sql/`. `.project` dan `.classpath` sudah ada.

Kerjakan **berurutan**, lalu berhenti dan laporkan. Jangan mengimplementasikan logika fitur penuh di tahap ini.

1. **Periksa prasyarat**: pastikan struktur di §4 ada, `lib/flatlaf-3.8.jar` dan `lib/postgresql-42.7.14.jar` ada. Jangan mengubah `.project`/`.classpath`.
2. **Berkas pendukung**: `.gitignore` (§4.3), `config/db.properties.example` (§3), `README.md` (tujuan, prasyarat JDK 21 + akses database Neon tim, cara import ke Eclipse, cara menjalankan, langkah setup DB — `sql/schema.sql` lalu `seed.sql` sekali — dan akun awal), `.github/copilot-instructions.md` bila belum ada.
3. **SQL**: tulis `sql/schema.sql` dari §8 dan `sql/seed.sql`.
4. **File Java di `src/rentgo/**`** sesuai pohon §4 dengan paket benar:
   - `model/` & `exception/`: implementasi **lengkap** sesuai §6, §7, §12 (kecil dan menjadi dasar semua orang), termasuk enum.
   - `dao/`, `controller/`, `util/`, `view/`: **kerangka** — deklarasi kelas, konstruktor, dan **signature method sesuai kontrak §9–§11** dengan badan `// TODO(Anggota N)` (atau `throw new UnsupportedOperationException("TODO")`). Harus compile.
   - `Main.java`: `FlatLightLaf.setup()` lalu menampilkan `LoginFrame` (boleh kosong).
5. **`test/`**: `RentalCostTest` (main) yang memverifikasi tarif & denda §7 (skenario §17 no. 4–6) dan `DaoConsoleTest` (kerangka).
6. **Verifikasi** dengan perintah §4.4: semua harus compile tanpa error; jalankan `RentalCostTest` dan laporkan hasilnya.
7. **Jangan** membuat commit atau branch; serahkan ke manusia.

**Tahap berikutnya**: ikuti panduan langkah per anggota di `docs/DEV-GUIDE-1-MODEL-DB.md`, `docs/DEV-GUIDE-2-DAO-CONTROLLER.md`, `docs/DEV-GUIDE-3-VIEW.md` (urutan Gate, jadwal, prompt per langkah). Prompt ringkas (per anggota, di branch masing-masing):
- Anggota 1: *"Baca PRD.md. Lengkapi dan rapikan paket `model` + `RentalCostTest` sampai semua skenario §17 no. 4–6 lulus."*
- Anggota 2: *"Baca PRD.md. Implementasikan `DatabaseConnection`, `VehicleDAO`, … sesuai §9 dan lengkapi `DaoConsoleTest`; lalu controller sesuai §10."*
- Anggota 3: *"Baca PRD.md. Implementasikan `LoginFrame`, `MainFrame`, `VehiclePanel`, … sesuai §11 memakai FlatLaf."*

---

## 17. Skenario Uji (Acceptance)

| # | Skenario | Hasil yang diharapkan |
|---|---|---|
| 1 | Login dengan password salah | Pesan kesalahan, akses ditolak |
| 2 | Tambah kendaraan dengan tarif negatif | Ditolak dengan pesan validasi (menyebut "Tarif") |
| 3 | Sewa kendaraan berstatus `RENTED` atau `MAINTENANCE` | `VehicleUnavailableException`, ditampilkan sebagai dialog |
| 4 | Sewa **Car** tarif 350.000 selama **7 hari**, tanpa sopir | Total **Rp2.205.000** (350.000×7=2.450.000, diskon 10%) |
| 4b | Car 350.000, 3 hari, dengan sopir | Total **Rp1.500.000** (1.050.000 + 450.000) |
| 4c | Motorcycle 100.000, 7 hari | Total **Rp595.000** (700.000 × 0,85) |
| 5 | Sewa **Bus** tarif 1.500.000 selama **2 hari** | Total **Rp3.600.000** (3.000.000 + sopir wajib 600.000) |
| 6 | Kembalikan kendaraan **terlambat 2 hari** (Car 350.000) | Denda **Rp1.050.000** (2 × 1,5 × 350.000); rental `COMPLETED`; kendaraan `AVAILABLE` |
| 7 | Hapus data pelanggan | Dialog konfirmasi muncul sebelum data dihapus; pelanggan dengan riwayat → pesan ramah, data tetap |
| 8 | Ubah ukuran jendela | Tata letak tetap rapi (tanpa komponen terpotong/menumpuk) |
| 9 | Internet/database diputus (atau kredensial salah) lalu aplikasi dijalankan / aksi dilakukan | Dialog error informatif, aplikasi tidak crash |
| 10 | Simpan rental lalu cek DB | Baris `rental` + `payment` ada **dan** `vehicle.status = 'RENTED'`; bila salah satu gagal, tidak ada yang tersimpan (rollback) |
| 11 | Login sebagai Staff | Menu "Akun" tersembunyi; tombol tambah/ubah/hapus Kendaraan nonaktif |
| 12 | Cetak struk Rental | Dialog teks berisi pelanggan, kendaraan, tanggal, lama sewa, total (format rupiah) |

---

## Lampiran A — Jejak ke Dokumen Sumber

| Sumber | Dipakai di |
|---|---|
| Proposal §2.2 Daftar Fitur | §2, §11 |
| Proposal §2.3.3 Aturan Bisnis | §7.1, §7.2 |
| Proposal §3.1 Desain OOP, 3.1.5 Exception | §6, §12 |
| Proposal §3.2 MVC, §3.3 Basis Data | §5, §8, §9 |
| Proposal §3.4 Desain GUI | §11 |
| Proposal §3.5 Rencana Pengujian | §17 |
| Pembagian Tugas (Java Swing + FlatLaf, Singleton koneksi DB, `.gitignore`, branch) | §3, §4.3, §9, §14 |
| Perubahan keputusan tim (Eclipse, PostgreSQL/Neon, penamaan Inggris, versi dependensi) | §0, §3, §4, §15 |
