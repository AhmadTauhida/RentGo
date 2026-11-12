# DEV-GUIDE — RentGo

> Dokumen kerja terpadu untuk developer dan AI agent yang melanjutkan pengembangan RentGo.
> Dokumen ini menggabungkan `DEV-GUIDE-1-MODEL-DB.md`, `DEV-GUIDE-2-DAO-CONTROLLER.md`, dan
> `DEV-GUIDE-3-VIEW.md`, lalu mengurutkannya menurut **ketergantungan arsitektur**, bukan
> menurut pembagian anggota. Ketiga dokumen lama tetap disimpan sebagai arsip.
>
> Urutan kebenaran: **`PRD.md` > `ARCHITECTURE.md` > `DEV-GUIDE.md`**.
> Jika ada konflik, PRD yang benar. Perbaiki dokumen ini lewat PR, bukan dengan mengubah
> implementasi secara diam-diam.

| Item | Nilai |
|---|---|
| Status | Dokumen eksekusi utama |
| Pembaca | Developer manusia dan AI agent |
| Root package | `rentgo` |
| Arsitektur | MVC + DAO, aplikasi desktop monolit |
| Platform | Java 21 (fitur kode dibatasi ≤ Java 17), Swing + FlatLaf 3.8 |
| Database | PostgreSQL di Neon melalui pgjdbc 42.7.14 |
| Referensi implementasi | `.ai/DEVELOPMENT-REFERENCE.md` (rujukan kode, bukan pengganti PRD) |
| Panduan arsip | `.ai/prd/DEV-GUIDE-1-MODEL-DB.md`, `DEV-GUIDE-2-DAO-CONTROLLER.md`, `DEV-GUIDE-3-VIEW.md` |
| Terakhir diperbarui | 2026-11-12 |

---

## 0. Cara Pakai Dokumen Ini

1. Tentukan milestone pertama yang **belum lulus exit criteria**.
2. Baca bagian PRD dan ARCHITECTURE yang dirujuk milestone tersebut.
3. Kerjakan checklist milestone secara berurutan. Jangan melompati dependensi.
4. Compile dan jalankan test dengan perintah resmi di PRD §4.4.
5. Buat PR kecil. Jangan commit langsung ke `main`.
6. Laporkan hasil: file yang berubah, hasil compile, hasil test, dan gap yang tersisa.

Aturan penting: **satu prompt agent = satu milestone atau satu sub-milestone**. Prompt
"kerjakan semuanya" menghasilkan perubahan besar yang sulit direview dan rawan melanggar
kontrak.

---

## 1. Tujuan dan Ruang Lingkup

Dokumen ini menjawab satu pertanyaan: **apa yang harus dikerjakan berikutnya, dalam urutan
apa, dan bagaimana membuktikan bahwa pekerjaan itu selesai**.

Cakupan:

- Menuntun implementasi dari kondisi repository saat ini sampai seluruh P0 dapat didemokan.
- Menjaga batas lapisan MVC + DAO, kontrak method, aturan bisnis, dan transaksi database.
- Menyediakan milestone relatif, dependency, gate, dan acceptance criteria.
- Menyatukan aturan Git, review, penggunaan agent, serta aktivitas laporan dan demo.

Di luar cakupan:

- Mengubah requirement produk atau kontrak di PRD.
- Menambah framework, library, atau pola arsitektur baru.
- Menduplikasi isi kode implementasi. Untuk contoh kode, rujuk
  `.ai/DEVELOPMENT-REFERENCE.md`.

---

## 2. Sumber Kebenaran dan Urutan Baca

| Kebutuhan | Dokumen utama | Bagian yang dibaca |
|---|---|---|
| Requirement, prioritas, aturan bisnis, kontrak | `.ai/prd/PRD.md` | §2, §6, §7, §9, §10, §11, §12, §17 |
| Keputusan arsitektur, alur runtime, transaksi | `.ai/architecture/ARCHITECTURE.md` | §2, §5, §7, §8, §10, §12 |
| Urutan eksekusi dan milestone | `.ai/prd/DEV-GUIDE.md` | Dokumen ini |
| Contoh implementasi per kelas | `.ai/DEVELOPMENT-REFERENCE.md` | §2–§7 sesuai kebutuhan |
| Tugas laporan, PPT, demo per anggota | Panduan arsip DEV-GUIDE 1–3 | Bagian laporan dan demo |

Jangan memakai panduan arsip sebagai urutan kerja utama. Panduan arsip dipakai untuk detail
tugas non-kode dan pembagian tanggung jawab per anggota.

---

## 3. Aturan Emas

Aturan berikut tidak bisa dinegosiasikan tanpa PR kontrak terpisah.

1. **Struktur Eclipse, bukan Maven/Gradle.** Folder `src/`, `test/`, `bin/`, `lib/`; jangan
   membuat `pom.xml`, `build.gradle`, `module-info.java`, atau `src/main/java`.
2. **Hanya JDK + FlatLaf 3.8 + pgjdbc 42.7.14.** Jangan menambah Lombok, Spring, Hibernate,
   JavaFX, atau library lain.
3. **MVC ketat.** SQL hanya di `dao`. Aturan tarif, diskon, sopir, dan denda hanya di `model`.
   `view` tidak boleh berisi logika bisnis atau SQL.
4. **OOP harus nyata.** Abstract class, inheritance, overriding, overloading, interface,
   encapsulation, generics/collection, dan custom exception harus terlihat di kode.
5. **Kode berbahasa Inggris; teks UI berbahasa Indonesia.** Nama file, class, method, field,
   tabel, kolom, enum, komentar, dan Javadoc memakai bahasa Inggris. Label, dialog, pesan
   error, dan struk memakai bahasa Indonesia.
6. **Kode dibatasi fitur Java ≤ 17** agar tetap dapat dikompilasi di JDK 17.
7. **Setiap file harus compile.** Stub yang belum selesai memakai TODO yang valid secara
   sintaks, bukan kode yang merusak build.
8. **Jangan commit rahasia atau hasil build.** `config/db.properties`, `bin/`, `build/`,
   `*.class`, dan log tidak boleh masuk repository.
9. **Kontrak method tidak berubah diam-diam.** Perubahan signature PRD §6, §9, §10, §11 harus
   lewat PR terpisah dan disetujui seluruh anggota.
10. **Agent berjalan dari terminal.** Compile dan test memakai perintah PRD §4.4, bukan fitur
    IDE. Manusia melakukan refresh Eclipse (F5) setelah file berubah.

### 3.1 Perintah build resmi

```bash
rm -rf build/classes
mkdir -p build/classes
javac -encoding UTF-8 -cp "lib/*" -d build/classes $(find src test -name "*.java")
java -cp "build/classes:lib/*" rentgo.model.RentalCostTest
java -cp "build/classes:lib/*" rentgo.dao.DaoConsoleTest
```

Untuk Windows, gunakan varian PowerShell di PRD §4.4. Working directory harus root proyek
agar `config/db.properties` terbaca. README saat ini masih memakai `--module-path`/`--module`;
samakan dengan perintah di atas pada M0.

---

## 4. Kondisi Baseline Repository

Snapshot per 2026-11-12. Perbarui tabel ini hanya jika ada perubahan besar yang memengaruhi
urutan milestone.

| Lapisan | Kondisi aktual | Konsekuensi |
|---|---|---|
| `model` | 10 kelas + `PrintableReceipt` + 6 enum sudah terisi | Perlu verifikasi kontrak dan edge case, bukan implementasi dari nol |
| `exception` | 3 custom exception sudah ada | Perlu dipakai konsisten di DAO/controller |
| `util` | `Validator`, `CurrencyFormatter`, `PasswordHasher`, `Session` terisi; ada perubahan belum di-commit | Review dan commit sebelum M2 |
| `dao` | `DatabaseConnection` + `CrudDAO<T>` ada; semua DAO konkret masih stub `TODO(Anggota 2)` | Login dan seluruh operasi DB belum bisa berjalan |
| `controller` | Semua controller masih placeholder; `LoginController` melempar `UnsupportedOperationException` | `LoginFrame` belum bisa dipakai end-to-end |
| `view` | `LoginFrame` cukup lengkap; `MainFrame` dan 6 panel masih placeholder | Navigasi, CRUD, dan transaksi belum tersedia |
| `Main` | Baru `FlatLightLaf.setup()` dan `LoginFrame` | Belum ada cek koneksi, `ensureDefaultAdmin`, dan wiring controller |
| SQL | `schema.sql`, `seed.sql`, `seed_transactions.sql` ada | Skema perlu dijalankan dan diverifikasi di Neon |
| Test | `RentalCostTest` menguji skenario inti + edge case M1; `DaoConsoleTest` 23 cek util lulus | Test DAO dan transaksi belum ada |
| Dokumentasi | `.ai/DEVELOPMENT-REFERENCE.md` dan `.ai/prd/DEV-GUIDE.md` belum ter-track | Commit menunggu keputusan tim |

Gap yang harus ditutup lebih dulu:

- Perubahan util dan `DaoConsoleTest` sudah direview serta lulus test; commit terpisah belum dilakukan.
- `Main` belum memeriksa database dan belum membuat akun admin awal.
- `seed_transactions.sql` baru bisa dijalankan setelah `StaffDAO.ensureDefaultAdmin()` berjalan.
- README sudah disamakan dengan perintah build PRD §4.4.
- `MainFrame` belum memiliki menu, `CardLayout`, status bar, atau wiring panel.

### 4.1 Status Milestone

| Milestone | Status | Bukti |
|---|---|---|
| M0 | Lulus teknis; commit belum dilakukan | Compile bersih; `RentalCostTest` lulus; `DaoConsoleTest` 23/23 util; README build diperbaiki; kontrak dan branch tersedia |
| M1 | Lulus | Edge case model lulus; 6 tabel terverifikasi di Neon; seed 3 Car/3 Motorcycle/1 Bus/5 Customer; 1 `MAINTENANCE`; constraint `23514`/`23505` menolak data salah |
| M2 | Belum | Util lulus, tetapi `DatabaseConnection.translate(SQLException)` belum ada dan uji kegagalan koneksi belum dilakukan |
| M3 | Belum | Semua DAO konkret masih stub |
| M4 | Belum | Controller masih placeholder |
| M5 | Belum | `MainFrame` dan panel masih placeholder |
| M6 | Belum | Menunggu M5 |

---

## 5. Prinsip Urutan Arsitektur

Urutan pengerjaan mengikuti arah dependensi:

```text
exception ──► model ──► util ──► dao ──► controller ──► view ──► wiring/integrasi
                    └──► sql/schema + seed ─────────────┘
```

Alasannya:

- `model` adalah dasar aturan bisnis; DAO dan controller tidak boleh menebak tarif/denda.
- `util` menyediakan validasi, format uang, hash password, dan sesi yang dipakai controller.
- `dao` membutuhkan model, exception, util, dan skema database yang sudah stabil.
- `controller` membutuhkan DAO dan signature view yang disepakati.
- `view` dapat dibangun dengan data dummy setelah model stabil, tetapi baru di-wire penuh
  setelah controller P0 tersedia.
- Integrasi dan acceptance dilakukan setelah semua lapisan P0 tersedia.

Lapisan di bawah harus lulus exit criteria sebelum lapisan di atas bergantung padanya. Namun,
pekerjaan dapat berjalan paralel selama tidak melanggar dependensi:

- M1 (domain/database) dan M2 (util/koneksi) boleh berjalan bersamaan setelah M0 lulus.
- M3 (DAO) menunggu M1 dan M2.
- M4 (controller) menunggu M3 untuk DAO yang dipakai.
- M5A (kerangka `Main`, `LoginFrame`, `MainFrame`) boleh dimulai dengan data dummy setelah M1;
  wiring penuh menunggu M4.
- M6 menunggu M5 lulus.

### 5.1 Gate

| Gate | Isi | Pemilik | Status awal | Milestone |
|---|---|---|---|---|
| A | Kerangka proyek + model + exception lengkap, semua compile | Anggota 1 | Selesai dan diverifikasi di M0 | M0–M1 |
| B | Skema Neon dijalankan + seed tersedia | Anggota 1 | Terbuka: 6 tabel, seed, dan constraint terverifikasi | M1 |
| C | Util + `DatabaseConnection` siap dipakai | Anggota 2 | Sebagian: util lulus; `translate` dan uji koneksi belum | M2 |
| D | DAO + controller P0 selesai | Anggota 2 | Belum | M3–M4 |
| E | View P0 + wiring `MainFrame` selesai | Anggota 3 | Belum | M5 |
| F | Acceptance PRD §17 no. 1–12 lulus + siap demo | Tim | Belum | M6 |

Jangan membuka gate berikutnya sebelum exit criteria gate sebelumnya terpenuhi.

---

## 6. Peta Milestone

Milestone memakai penomoran relatif. Tanggal konkret dikelola tim di luar dokumen ini.

| Milestone | Fokus | Dependensi | Gate |
|---|---|---|---|
| M0 | Baseline, guardrails, dan penutupan pekerjaan Tahap 0 | — | A divalidasi |
| M1 | Domain, aturan bisnis, skema, dan seed | M0 | B |
| M2 | Util dan koneksi database | M0 | C |
| M3 | Lapisan DAO dan pengujian data | M1, M2 | D (DAO) |
| M4 | Lapisan controller | M3 | D (selesai) |
| M5 | View dan wiring | M1, M4 | E |
| M6 | Integrasi, acceptance, P1/P2, kesiapan demo | M5 | F |

Prioritas fitur tetap mengikuti PRD §2: **seluruh P0 selesai sebelum P1 dimulai**. Jika waktu
sempit, potong P1/P2, jangan potong pengujian P0.

---

## 7. Detail Milestone

### M0 — Baseline dan Guardrails

**Tujuan:** memastikan titik awal bersih, dapat dikompilasi, dan disepakati sebelum lapisan
baru dikerjakan.

**Prasyarat:** repository sudah di-clone, branch utama berisi kerangka Tahap 0.

**Pekerjaan:**

- [ ] Verifikasi JDK 21, Eclipse Execution Environment `JavaSE-21`, `lib/flatlaf-3.8.jar`,
      `lib/postgresql-42.7.14.jar`, dan `.classpath`.
- [ ] Pastikan tidak ada `pom.xml`, `build.gradle`, `module-info.java`, atau struktur Maven.
- [ ] Compile seluruh `src` dan `test` memakai perintah PRD §4.4.
- [ ] Jalankan `RentalCostTest` dan `DaoConsoleTest`. Catat hasilnya.
- [ ] Review perubahan util yang belum di-commit. Commit terpisah setelah lulus test.
- [ ] Putuskan status `.ai/DEVELOPMENT-REFERENCE.md`: ikut di-commit sebagai referensi atau
      tetap lokal. Jika di-commit, tambahkan ke indeks `.ai/README.md`.
- [ ] Samakan perintah build di `README.md` dengan PRD §4.4.
- [ ] Bacakan dan bekukan kontrak PRD §6, §9, §10, §11, §12 kepada seluruh anggota.
- [ ] Buat branch kerja dari `main`: `feature/model-oop`, `feature/dao-controller`,
      `feature/gui-view`.
- [ ] Pastikan `config/db.properties` tidak ter-track dan tidak ada kredensial di diff.

**Deliverable:** build bersih, dua test berjalan, kontrak disepakati, branch siap.

**Exit criteria:** compile tanpa error; `RentalCostTest` lulus; `DaoConsoleTest` bagian util
lulus; tidak ada file rahasia atau artefak build di `git status`.

**Pemilik utama:** tim; koordinasi Anggota 1, verifikasi util oleh Anggota 2, verifikasi
dokumentasi oleh Anggota 3.

**Prompt agent:**

```text
Baca .ai/prd/PRD.md, .ai/architecture/ARCHITECTURE.md, dan .ai/prd/DEV-GUIDE.md.
Kerjakan MILESTONE M0 saja. Jangan menambah library, jangan mengubah kontrak,
jangan commit, dan jangan membuat branch. Compile dengan PRD §4.4, jalankan
RentalCostTest dan DaoConsoleTest, lalu laporkan gap dan hasil test.
```

**Jebakan:** memakai perintah build dari README lama; mencampur commit util dengan pekerjaan
DAO; menambahkan library "agar cepat"; mengubah `.project`/`.classpath` di luar penambahan jar.

---

### M1 — Domain dan Database

**Tujuan:** memastikan model benar-benar menjadi sumber aturan bisnis dan database siap
menerima data.

**Prasyarat:** M0 lulus.

**Pekerjaan:**

- [ ] Verifikasi setiap kelas model terhadap PRD §6.2 dan §7. Fokus pada validasi setter,
      konstruktor yang memanggil setter, serta pesan `InvalidDataException` berbahasa Indonesia.
- [ ] Pastikan `Vehicle.calculateRent(int)` abstrak, `calculateRent(int, boolean)` konkret di
      `Vehicle`, dan `Car` meng-override versi dengan sopir.
- [ ] Pastikan `Rental.calculateTotalCost()` memanggil `vehicle.calculateRent(getRentalDays(),
      withDriver)` tanpa `instanceof`.
- [ ] Pastikan `Rental.getRentalDays()` minimal 1 dan `VehicleReturn.calculateLateDays()` tidak
      pernah negatif.
- [ ] Pastikan aturan Bus (sopir wajib), diskon Car/Motorcycle, dan denda memakai konstanta
      `public static final`, bukan magic number.
- [ ] Pastikan semua enum punya `getLabel()`, `toString()` = label, dan `fromString(String)`.
- [ ] Perkuat `RentalCostTest` dengan kasus tepi: Car 6 vs 7 hari, Motorcycle 7 hari, Bus 2 hari,
      `calculateRent(0)`, `calculateRent(-1)`, `setBaseRate(-1)`, plat kosong, tahun 1900, NIK
      bukan 16 digit, `dueDate` sebelum `startDate`, tanggal sama = 1 hari, dan denda 2 hari.
- [ ] Jalankan `sql/schema.sql` di Neon. Pastikan enam tabel terbentuk.
- [ ] Jalankan `sql/seed.sql`. Pastikan ada Car, Motorcycle, Bus, dan satu kendaraan
      `MAINTENANCE`.
- [ ] Uji constraint: tarif negatif ditolak, `due_date < start_date` ditolak, plat duplikat
      ditolak.
- [ ] Jalankan `sql/seed_transactions.sql` **setelah** akun admin dibuat oleh aplikasi atau
      `StaffDAO.ensureDefaultAdmin()`.
- [ ] Jika skema berubah, ubah `sql/schema.sql` dan kabari tim. Jangan mengedit tabel langsung
      di Neon tanpa memperbarui file SQL.

**Deliverable:** model tervalidasi, test edge case lulus, database Neon berisi skema dan seed.

**Exit criteria:** `RentalCostTest` lulus semua kasus; `schema.sql` + `seed.sql` berhasil;
constraint terbukti menolak data salah; tidak ada aturan tarif/denda di luar `model`.

**Gate:** B.

**Pemilik utama:** Anggota 1. Review pemakaian model oleh Anggota 2 dan 3.

**Prompt agent:**

```text
Baca .ai/prd/PRD.md §6, §7, §8, §17 dan .ai/prd/DEV-GUIDE.md MILESTONE M1.
Verifikasi dan lengkapi model serta RentalCostTest sesuai kontrak. Jangan menambah
library, jangan mengubah signature tanpa persetujuan, jangan commit. Compile dan
jalankan RentalCostTest, lalu laporkan hasil dan gap.
```

**Jebakan:** menunda M1 demi menyempurnakan model; setter diisi langsung di konstruktor;
mengedit tabel Neon tanpa memperbarui `schema.sql`; memakai `record`, Lombok, atau collection
di luar ketentuan; menambah aturan tarif di tempat selain `model`.

---

### M2 — Util dan Koneksi Database

**Tujuan:** menyediakan fondasi lintas lapisan yang stabil dan aman.

**Prasyarat:** M0 lulus. M2 boleh berjalan paralel dengan M1.

**Pekerjaan:**

- [ ] Finalisasi `Validator`: semua method PRD §12, pesan error menyebut nama field,
      `NumberFormatException` dikonversi menjadi `InvalidDataException`.
- [ ] Verifikasi `CurrencyFormatter.formatRupiah(350000)` menghasilkan `Rp350.000` dan
      `parseRupiah("Rp350.000")` menghasilkan `350000.0`.
- [ ] Verifikasi `PasswordHasher`: hash berbeda tiap panggilan karena salt acak, `verify` benar
      untuk password yang sama, dan `false` untuk password salah atau format rusak.
- [ ] Verifikasi `Session` set/get/clear.
- [ ] Tambahkan `DatabaseConnection.translate(SQLException)` untuk menerjemahkan SQLState
      `23505` (UNIQUE), `23503` (FK), dan error umum menjadi pesan Indonesia yang ramah.
- [ ] Pastikan `DatabaseConnection` memuat `config/db.properties`, memanggil
      `Class.forName("org.postgresql.Driver")`, membuat ulang koneksi yang tertutup/tidak valid,
      dan mencoba ulang satu kali untuk Neon cold start.
- [ ] Jangan menutup `Connection` bersama dengan try-with-resources. Gunakan try-with-resources
      hanya untuk `PreparedStatement` dan `ResultSet`.
- [ ] Jangan mencetak password, URL lengkap, atau stack trace ke pengguna.
- [ ] Uji kegagalan: password salah, file config hilang, dan database tidak terjangkau harus
      menghasilkan `DatabaseException` yang informatif.
- [ ] Lengkapi bagian util di `DaoConsoleTest`.

**Deliverable:** util final, koneksi database tangguh, penerjemah SQLException siap dipakai DAO.

**Exit criteria:** test util lulus; koneksi sukses ke Neon; kegagalan koneksi menghasilkan pesan
yang menyebut cek internet, kredensial/URL, dan status Neon; tidak ada kebocoran kredensial.

**Gate:** C.

**Pemilik utama:** Anggota 2.

**Referensi implementasi:** `.ai/DEVELOPMENT-REFERENCE.md` §2.1, §2.2, §2.3.

**Prompt agent:**

```text
Baca .ai/prd/PRD.md §12 dan .ai/prd/DEV-GUIDE.md MILESTONE M2. Implementasikan atau
rapikan util dan DatabaseConnection (termasuk translate SQLException) sesuai
DEVELOPMENT-REFERENCE §2. Jangan mengubah paket lain, jangan commit. Compile dan
jalankan DaoConsoleTest, lalu laporkan hasil.
```

**Jebakan:** try-with-resources pada koneksi singleton; menelan `SQLException`; lupa memulihkan
`autoCommit`; timeout terlalu pendek; mencetak kredensial; menganggap cold start Neon sebagai
error permanen.

---

### M3 — Lapisan DAO

**Tujuan:** mengimplementasikan seluruh kontrak persistensi PRD §9, termasuk transaksi atomik.

**Prasyarat:** M1 dan M2 lulus. Gate B dan Gate C terbuka.

**Pecah menjadi tiga PR/sub-milestone** agar mudah direview.

#### M3A — DAO master

- [ ] `StaffDAO`: `CrudDAO<Staff>`, `findByUsername`, `ensureDefaultAdmin` yang hanya membuat
      `admin/admin123` bila tabel `staff` kosong. Hash memakai `PasswordHasher`.
- [ ] `VehicleDAO`: factory method privat untuk memetakan baris menjadi `Car`, `Motorcycle`,
      atau `Bus`; `add` mengisi id hasil generated key; kolom subclass diisi sesuai jenis dan
      sisanya `NULL`; `search(keyword, type, status)` dengan parameter `null` = tidak difilter;
      `findAvailable()`; `updateStatus(id, status)`.
- [ ] `CustomerDAO`: CRUD, `search(nameOrNationalId)`, `mapByNationalId()`.
- [ ] Gunakan `PreparedStatement` untuk semua query. Jangan menyambung string SQL dengan input.
- [ ] Terjemahkan UNIQUE/FK memakai `DatabaseConnection.translate(...)`.

#### M3B — DAO transaksi

- [ ] `RentalDAO.saveRental(Rental, Payment)` dalam satu transaksi JDBC:
      `setAutoCommit(false)` → cek status kendaraan di dalam transaksi → tolak dengan
      `VehicleUnavailableException` bila tidak `AVAILABLE` → `INSERT rental` →
      `UPDATE vehicle SET status='RENTED'` → `INSERT payment` → `commit`.
- [ ] `rollback` di `catch`, `setAutoCommit(true)` di `finally`.
- [ ] `RentalDAO.cancel(rentalId)`: hanya rental `ACTIVE`; rental menjadi `CANCELLED`, kendaraan
      kembali `AVAILABLE`, atomik.
- [ ] `findActive()` dan `findHistory()` mengembalikan `Rental` yang sudah berisi `Customer`,
      `Vehicle`, dan `Staff` lengkap.
- [ ] `update` hanya untuk rental `ACTIVE`. `add`/`delete` boleh
      `UnsupportedOperationException` karena `saveRental` adalah jalur utama.
- [ ] `PaymentDAO.findByRentalId(int)`.
- [ ] `VehicleReturnDAO.saveReturn(VehicleReturn)`: satu transaksi `INSERT vehicle_return` →
      rental `COMPLETED` → kendaraan `AVAILABLE`. Pengembalian kedua ditolak ramah karena
      `UNIQUE(rental_id)`.
- [ ] `VehicleReturnDAO.findByRentalId(int)`.

#### M3C — Laporan dan pengujian DAO

- [ ] `ReportDAO.revenue(from, to)` menghasilkan baris `{date, transactionCount, totalRevenue}`.
- [ ] `ReportDAO.topVehicles(limit)` menghasilkan `{plateNumber, brand, vehicleType,
      rentalCount}`. Ini P1; boleh dijadwalkan di M6C bila waktu sempit.
- [ ] Lengkapi `DaoConsoleTest` untuk CRUD, filter, tipe dinamis kendaraan, UNIQUE, FK,
      transaksi + rollback, pembatalan, dan pengembalian.
- [ ] Uji wajib transaksi: simpan rental normal; sewa kendaraan yang sudah `RENTED` tidak
      menambah baris; kegagalan di tengah transaksi tidak mengubah status kendaraan; `cancel`
      mengembalikan kendaraan ke `AVAILABLE`.

**Deliverable:** semua DAO P0 berfungsi dan teruji; DAO laporan tersedia bila P1 dikerjakan.

**Exit criteria:** `DaoConsoleTest` lulus; tidak ada stack trace ke pengguna; UNIQUE/FK memberi
pesan ramah; transaksi menyimpan atau membatalkan seluruh perubahan; tidak ada SQL di luar
`dao`.

**Gate:** D bagian DAO.

**Pemilik utama:** Anggota 2. Review pemakaian model oleh Anggota 1.

**Referensi implementasi:** `.ai/DEVELOPMENT-REFERENCE.md` §3.1–§3.7 dan §7.

**Prompt agent per sub-milestone:**

```text
Baca .ai/prd/PRD.md §7.4, §9 dan .ai/prd/DEV-GUIDE.md MILESTONE M3A. Implementasikan
hanya DAO master (Staff, Vehicle, Customer) sesuai kontrak. Jangan mengubah view atau
controller, jangan commit. Compile dan jalankan DaoConsoleTest, lalu laporkan hasil.
```

Ganti `M3A` dengan `M3B` atau `M3C` untuk tahap berikutnya.

**Jebakan:** cek ketersediaan di luar transaksi; lupa `setAutoCommit(true)`; memakai `Statement`
dan penyambungan string; tidak menutup statement/resultset; membaca `DATE`/`TIMESTAMP` dengan
`getDate`; menganggap FK sebagai error tak dikenal; menelan `SQLException`.

---

### M4 — Lapisan Controller

**Tujuan:** menghubungkan view, model, dan DAO tanpa membocorkan SQL atau aturan bisnis.

**Prasyarat:** M3A dan M3B lulus. Gate D bagian DAO terbuka.

**Pekerjaan:**

- [ ] `LoginController`: ambil username/password dari view, cari staff, verifikasi dengan
      `PasswordHasher`, set `Session`, buka `MainFrame`; pesan gagal seragam agar tidak
      membocorkan apakah username atau password yang salah; `cancel()` dan `logout()`.
- [ ] `VehicleController`: `loadData`, `addVehicle`, `updateVehicle`, `deleteVehicle`,
      `search`, `clearForm`, `selectRow`. Hapus selalu meminta `view.confirm(...)`.
- [ ] `CustomerController`: kontrak yang sama untuk pelanggan.
- [ ] `RentalController`: `loadOptions`, `calculateTotal` (pratinjau saja), `saveRental`,
      `updateRental`, `cancelRental`, `loadHistory`, `printReceipt`, `selectRow`.
- [ ] `VehicleReturnController`: `loadActiveRentals`, `calculateLateFee` (pratinjau), `saveReturn`.
- [ ] `ReportController`: `loadReport` (P1).
- [ ] `UserManagementController`: CRUD akun staff dan hashing password (P2).
- [ ] Konversi teks ke angka dan `java.util.Date` ke `LocalDate` hanya di controller.
- [ ] Tangkap `InvalidDataException`, `VehicleUnavailableException`, `DatabaseException`, dan
      `NumberFormatException`, lalu tampilkan lewat `view.showError(...)`. Sukses memakai
      `view.showInfo(...)`.
- [ ] Tidak ada SQL, tidak ada aturan tarif/denda, dan tidak ada pembuatan komponen Swing baru
      di controller.
- [ ] `saveRental`: `Session.getStaff()` menjadi staff transaksi; Bus dipaksa memakai sopir;
      pembayaran dicatat penuh sebesar `totalCost`.
- [ ] `calculateTotal` dan `calculateLateFee` hanya menghitung pratinjau, tidak menyimpan.

**Deliverable:** seluruh controller P0 selesai; controller P1/P2 sesuai prioritas.

**Exit criteria:** controller mengikuti signature PRD §10; semua exception tertangkap; tidak
ada exception yang lolos ke Swing; tidak ada SQL atau aturan bisnis di controller.

**Gate:** D selesai.

**Pemilik utama:** Anggota 2.

**Referensi implementasi:** `.ai/DEVELOPMENT-REFERENCE.md` §4.1–§4.7.

**Prompt agent:**

```text
Baca .ai/prd/PRD.md §10 dan .ai/prd/DEV-GUIDE.md MILESTONE M4. Implementasikan hanya
controller yang diminta, sesuai signature kontrak. Controller tidak boleh berisi SQL
atau aturan tarif/denda. Jangan commit. Compile dan laporkan hasil serta exception
yang sudah ditangani.
```

**Jebakan:** membiarkan exception lolos; menghitung total di controller; memanggil DAO langsung
dari view; mengubah signature diam-diam; lupa `confirm` sebelum hapus; menampilkan pesan login
yang membedakan username dan password.

---

### M5 — View dan Wiring

**Tujuan:** membangun GUI P0, menghubungkannya ke controller, dan menegakkan hak akses.

**Prasyarat:** M1 lulus untuk data dummy; M4 lulus untuk wiring penuh.

**Pecah menjadi empat sub-milestone.**

#### M5A — Bootstrap, login, dan kerangka utama

- [ ] `Main`: `FlatLightLaf.setup()` → `SwingUtilities.invokeLater` → cek
      `DatabaseConnection` → dialog error jelas bila gagal → `StaffDAO.ensureDefaultAdmin()` →
      `LoginFrame`.
- [ ] `LoginFrame`: layout `GridBagLayout`, Enter = Masuk, getter username/password, tombol
      terhubung ke `LoginController`.
- [ ] `MainFrame`: `BorderLayout`, `JMenuBar` (Data, Transaksi, Laporan, Akun), `CardLayout`
      dengan nama `VEHICLE`, `CUSTOMER`, `RENTAL`, `RETURN`, `REPORT`, `USERS`, status bar
      "Masuk sebagai {nama} ({role})", `setMinimumSize(1000, 650)`, `showCard(String)`.
- [ ] Sembunyikan menu "Akun" untuk role `STAFF`.

#### M5B — Panel master

- [ ] `VehiclePanel`: form di kiri, pencarian/filter di atas, tabel di tengah dalam
      `JScrollPane`; field khusus Car/Motorcycle/Bus muncul/hilang sesuai jenis.
- [ ] `CustomerPanel`: pola yang sama untuk nama, NIK, telepon, alamat, dan pencarian.
- [ ] Method standar panel: `setController`, `showError`, `showInfo`, `confirm`,
      `setTableData(List<T>)`, `getSelectedItem()`/`getSelectedIndex()`, dan getter field.
- [ ] Listener satu aksi satu method, tanpa logika bisnis dan tanpa SQL.
- [ ] Klik baris tabel memanggil `controller.selectRow(index)`.
- [ ] Untuk `STAFF`, tombol tambah/ubah/hapus kendaraan dinonaktifkan.

#### M5C — Panel transaksi dan laporan

- [ ] `RentalPanel`: combo customer dan kendaraan `AVAILABLE`, spinner tanggal, checkbox sopir
      yang otomatis dicentang dan dikunci untuk Bus, combo metode pembayaran, label total live,
      tabel riwayat, tombol Simpan/Ubah/Batalkan/Cetak Struk.
- [ ] Perubahan combo/tanggal/checkbox memanggil `controller.calculateTotal()`. Panel hanya
      menampilkan `setTotalCost(String)`, tidak menghitung biaya.
- [ ] `VehicleReturnPanel`: combo rental aktif, tanggal kembali aktual, kondisi kendaraan, label
      info sewa dan denda, `setLateFeeInfo(int lateDays, String lateFeeText)`.
- [ ] `ReportPanel`: combo periode, dua tabel, label total, `setRevenueData`,
      `setTopVehiclesData`, `getPeriod` (P1).
- [ ] `UserManagementPanel`: CRUD akun staff (P2).
- [ ] Struk ditampilkan lewat `JOptionPane` berisi `JTextArea` monospaced yang tidak editable.

#### M5D — Wiring, hak akses, dan UX

- [ ] Wiring setiap panel mengikuti pola PRD §5: buat panel → buat controller dengan panel + DAO
      → `panel.setController(controller)` → `controller.loadData()`.
- [ ] Saat kartu dibuka lewat menu, panggil loader yang sesuai agar data segar.
- [ ] `LoginController` membuka `MainFrame` setelah sukses; logout menutup `MainFrame` dan
      menampilkan `LoginFrame`.
- [ ] Batasi tombol dan menu sesuai role `Session.getStaff().isAdmin()`.
- [ ] Tidak ada layout `null` atau posisi absolut. Resize harus tetap rapi.
- [ ] Uang memakai `CurrencyFormatter`; tanggal tampil `dd-MM-yyyy`.
- [ ] Buat `test/rentgo/view/ViewPreview.java` dengan data dummy bila diperlukan untuk pratinjau
      tanpa database.
- [ ] Uji resize dan kedua role.

**Deliverable:** aplikasi dapat dijalankan dari login sampai navigasi semua panel P0.

**Exit criteria:** login berhasil; menu dan kartu berpindah; CRUD memakai controller; pembatasan
role terlihat; dialog error dan konfirmasi muncul; layout rapi saat resize; tidak ada SQL atau
logika bisnis di view.

**Gate:** E.

**Pemilik utama:** Anggota 3. Wiring akhir bersama Anggota 2.

**Referensi implementasi:** `.ai/DEVELOPMENT-REFERENCE.md` §5 dan §6.

**Prompt agent:**

```text
Baca .ai/prd/PRD.md §5, §7.5, §11 dan .ai/prd/DEV-GUIDE.md MILESTONE M5A. Implementasikan
hanya komponen view yang diminta, memakai layout manager dan controller. Jangan
menaruh SQL atau logika bisnis di view, jangan commit. Compile dan laporkan hasil
serta langkah uji manual.
```

Ganti `M5A` sesuai sub-milestone.

**Jebakan:** menghitung total di panel; mengisi combo dengan `String` alih-alih objek model;
layout `null`; memanggil DAO langsung; mengerjakan semua panel tanpa menjalankan; lupa membersihkan
`char[]` password; screenshot laporan diambil dari versi lama.

---

### M6 — Integrasi, Acceptance, dan Kesiapan Demo

**Tujuan:** membuktikan seluruh alur P0 bekerja, menutup P1/P2 yang sempat, dan menyiapkan demo.

**Prasyarat:** M5 lulus. Gate E terbuka.

**Pekerjaan:**

#### M6A — Acceptance P0

- [ ] Jalankan PRD §17 no. 1–12 secara end-to-end.
- [ ] Uji login salah, validasi tarif negatif, sewa kendaraan tidak tersedia, empat skenario
      tarif, denda keterlambatan, hapus dengan konfirmasi dan FK, resize, database terputus,
      atomisitas simpan rental, pembatasan role, dan struk.
- [ ] Catat bug dengan format: langkah → hasil aktual → hasil diharapkan → pemilik lapisan.
- [ ] Perbaiki bug di lapisan pemiliknya. Jangan menambal bug DAO di view.

#### M6B — Hardening

- [ ] Uji tanpa internet, password database salah, dan Neon cold start.
- [ ] Pastikan aplikasi tidak menampilkan stack trace dan tidak crash.
- [ ] Uji dua role: Admin dan Staff.
- [ ] Uji resize pada semua panel dan dialog.
- [ ] Pastikan transaksi rollback tidak meninggalkan status kendaraan yang salah.
- [ ] Pastikan tidak ada aturan bisnis di luar `model` (cari `* 0.9`, `1.5`, `150000`,
      `300000`, `0.85` di `controller/` dan `view/`).

#### M6C — P1

- [ ] Pencarian dan filter tabel.
- [ ] Ubah dan batalkan rental `ACTIVE`.
- [ ] Catat pembayaran dan tampilkan struk.
- [ ] `ReportPanel` dengan pendapatan dan kendaraan terlaris.
- [ ] Status bar.

#### M6D — P2 dan polish

- [ ] `UserManagementPanel` untuk Admin.
- [ ] Catat pembayaran denda bila waktu cukup.
- [ ] Polesan tampilan, tab order, mnemonic, dan konsistensi margin.
- [ ] Unit test JUnit opsional.

#### M6E — Demo

- [ ] Siapkan data demo yang bersih dan dapat diulang.
- [ ] Siapkan cadangan PostgreSQL lokal atau rekaman layar bila Neon/internet bermasalah.
- [ ] Latih alur demo: Login → CRUD → Sewa → Kembali → Laporan.
- [ ] Siapkan akun `admin/admin123` dan data kendaraan/pelanggan yang cukup.

**Deliverable:** aplikasi P0 stabil, skenario acceptance lulus, demo siap.

**Exit criteria:** PRD §17 no. 1–12 lulus; tidak ada pelanggaran batas lapisan; tidak ada stack
trace ke pengguna; data demo tersedia; demo dapat dilakukan dalam 10 menit.

**Gate:** F.

**Pemilik utama:** seluruh tim. Anggota 3 menjadi pemimpin demo; Anggota 1 dan 2 memperbaiki
bug sesuai lapisan.

**Jebakan:** mengerjakan P1 sebelum P0 selesai; menguji hanya jalur sukses; memakai data demo
kotor; tidak menguji role Staff; tidak menguji database terputus; menambal bug di lapisan salah.

---

## 8. Matriks Dependensi dan Kepemilikan

| Milestone | Dependensi | Pemilik utama | Boleh paralel dengan | Gate |
|---|---|---|---|---|
| M0 | — | Tim | — | A divalidasi |
| M1 | M0 | Anggota 1 | M2 | B |
| M2 | M0 | Anggota 2 | M1 | C |
| M3 | M1, M2 | Anggota 2 | — | D (DAO) |
| M4 | M3A, M3B | Anggota 2 | M5A | D selesai |
| M5 | M1, M4 | Anggota 3 | M4 untuk wiring akhir | E |
| M6 | M5 | Tim | — | F |

Kepemilikan paket tetap mengikuti PRD §14.1:

| Anggota | Paket/folder | Fokus |
|---|---|---|
| Anggota 1 | `model/`, `exception/`, `sql/`, setup repo | Domain, aturan bisnis, skema, seed |
| Anggota 2 | `dao/`, `controller/`, `util/`, `config/`, `test/` | Persistensi, orkestrasi, validasi, test |
| Anggota 3 | `view/`, `Main.java`, `test/rentgo/view/` | GUI, wiring, UX, demo |

Perubahan file milik anggota lain harus lewat permintaan atau PR kecil terpisah yang disetujui.

---

## 9. Matriks Acceptance

| # | Skenario PRD §17 | Dibuktikan di | Pemilik perbaikan |
|---|---|---|---|
| 1 | Login dengan password salah ditolak | M4, M5, M6A | Anggota 2/3 |
| 2 | Tarif negatif ditolak dengan pesan validasi | M1, M4, M5 | Anggota 1/2/3 |
| 3 | Sewa kendaraan `RENTED`/`MAINTENANCE` ditolak | M3, M4, M6A | Anggota 2 |
| 4 | Car 350.000, 7 hari = Rp2.205.000 | M1 | Anggota 1 |
| 4b | Car 350.000, 3 hari + sopir = Rp1.500.000 | M1 | Anggota 1 |
| 4c | Motorcycle 100.000, 7 hari = Rp595.000 | M1 | Anggota 1 |
| 5 | Bus 1.500.000, 2 hari = Rp3.600.000 | M1 | Anggota 1 |
| 6 | Terlambat 2 hari, denda Rp1.050.000 | M1, M3, M4, M6A | Anggota 1/2 |
| 7 | Hapus pelanggan memakai konfirmasi; FK ditolak ramah | M3, M4, M5, M6A | Anggota 2/3 |
| 8 | Resize tetap rapi | M5, M6B | Anggota 3 |
| 9 | Database terputus menghasilkan dialog, bukan crash | M2, M5, M6B | Anggota 2/3 |
| 10 | Simpan rental atomik; gagal berarti rollback total | M3, M6B | Anggota 2 |
| 11 | Role Staff: menu Akun sembunyi, tombol kendaraan nonaktif | M5, M6B | Anggota 3 |
| 12 | Struk rental tampil lengkap dengan format rupiah | M4, M5, M6A | Anggota 2/3 |

---

## 10. Definition of Done

### 10.1 Per Pull Request

- [ ] Perubahan hanya di paket/folder yang dimaksud.
- [ ] Compile bersih dengan perintah PRD §4.4.
- [ ] Test terkait lulus.
- [ ] Tidak ada SQL di luar `dao`, tidak ada aturan bisnis di luar `model`.
- [ ] Tidak ada TODO baru di jalur yang diklaim selesai.
- [ ] Tidak ada kredensial, artefak build, atau perubahan kontrak diam-diam.
- [ ] Diff sudah dibaca manusia sebelum merge.

### 10.2 Per Milestone

- [ ] Seluruh checklist milestone selesai.
- [ ] Exit criteria milestone terbukti, bukan diasumsikan.
- [ ] Gate terkait diumumkan ke tim.
- [ ] Risiko dan gap dicatat bila ada yang ditunda.

### 10.3 Proyek P0

- [ ] Minimal 8 kelas model dengan 2 hierarki pewarisan.
- [ ] Empat pilar OOP terlihat jelas di kode.
- [ ] Minimal 3 tampilan GUI dan 3 entitas dengan CRUD.
- [ ] `List<T>`/`Map<K,V>` generik dan `CrudDAO<T>` digunakan.
- [ ] Custom exception dan penanganan error di DAO/controller berjalan.
- [ ] Data permanen di PostgreSQL dan transaksi atomik terbukti.
- [ ] PRD §17 no. 1–12 lulus.
- [ ] Aplikasi tidak crash saat database tidak terjangkau atau input salah.
- [ ] Layout rapi saat resize.

---

## 11. Protokol Eksekusi Agent

### 11.1 Aturan prompt

- Satu prompt untuk satu milestone atau sub-milestone.
- Sertakan file yang harus dibaca, milestone, paket yang boleh diubah, dan perintah verifikasi.
- Jangan menempelkan kredensial Neon.
- Jangan meminta agent melakukan commit, push, atau membuat branch.
- Jika agent perlu mengubah kontrak, hentikan pekerjaan dan minta persetujuan manusia.

### 11.2 Template prompt

```text
Baca .ai/prd/PRD.md bagian <§...>, .ai/architecture/ARCHITECTURE.md bagian <§...>,
dan .ai/prd/DEV-GUIDE.md MILESTONE <M...>. Baca .ai/DEVELOPMENT-REFERENCE.md bagian
<§...> hanya sebagai referensi implementasi.

Kerjakan hanya <lingkup>. Hanya ubah file di <paket/folder>. Jangan menambah library,
jangan mengubah kontrak, jangan commit, dan jangan membuat branch.

Verifikasi dengan perintah PRD §4.4. Jalankan <test>. Laporkan file yang berubah,
hasil compile, hasil test, dan gap yang tersisa.
```

### 11.3 Checklist review kode

- [ ] Batas MVC tidak dilanggar.
- [ ] SQL hanya di `dao` dan memakai `PreparedStatement`.
- [ ] Aturan tarif/diskon/sopir/denda hanya di `model`.
- [ ] Tidak ada `instanceof` kendaraan di `Rental`, controller, atau view.
- [ ] Exception ditangkap di controller dan diterjemahkan menjadi dialog.
- [ ] `Connection` bersama tidak ditutup; statement/resultset memakai try-with-resources.
- [ ] `autoCommit` dipulihkan di `finally`.
- [ ] Tanggal dan angka dikonversi di controller.
- [ ] Hapus data selalu memakai konfirmasi.
- [ ] Role dibatasi di menu dan tombol.
- [ ] Teks UI Indonesia; identifier kode Inggris.
- [ ] Tidak ada magic number tarif/denda.
- [ ] Tidak ada stack trace atau kredensial yang tampil ke pengguna.

---

## 12. Git, PR, dan Perubahan Kontrak

### 12.1 Git

- Dilarang commit langsung ke `main`, kecuali commit awal repository oleh Anggota 1.
- Branch: `feature/model-oop`, `feature/dao-controller`, `feature/gui-view`. Sub-branch
  diperbolehkan untuk memecah PR.
- Commit kecil dan sering dengan pesan bahasa Inggris, misalnya `Add VehicleDAO.search`.
- Sebelum PR: `git fetch origin && git merge origin/main`, compile ulang, jalankan test.
- Setelah `git pull` atau perubahan agent: refresh Eclipse (F5).
- Jangan commit `config/db.properties`, `bin/`, `build/`, `*.class`, atau log.

### 12.2 Perubahan kontrak

Kontrak adalah signature method di PRD §6, §9, §10, §11. Jika perubahan diperlukan:

1. Diskusikan di grup.
2. Buat PR kecil yang mengubah PRD dan kode sekaligus.
3. Minta persetujuan dua anggota lain.
4. Baru merge.

Jangan mengubah signature diam-diam di branch fitur.

---

## 13. Risiko dan Mitigasi

| Risiko | Dampak | Mitigasi |
|---|---|---|
| Neon cold start atau internet putus saat demo | Aplikasi lambat/gagal terhubung | Retry satu kali, timeout ≥ 10 detik, cadangan PostgreSQL lokal, rekaman demo |
| Kredensial database tersebar | Penyalahgunaan database | Tidak masuk repo, bagikan lewat kanal pribadi, reset password setelah pengumpulan |
| Logika bisnis bocor ke view/controller | Penilaian MVC/OOP turun | Review mencari `* 0.9`, `1.5`, `150000`, `300000` di luar `model` |
| Perubahan kontrak diam-diam | Integrasi rusak | PR kontrak terpisah dan persetujuan tim |
| Race condition antar-instans | Dobel sewa | Cek status di dalam transaksi; peningkatan dengan `SELECT ... FOR UPDATE` atau update bersyarat |
| Waktu sempit | Fitur tidak selesai | Selesaikan P0 dulu; potong P1/P2, jangan potong pengujian |
| Perubahan lokal belum di-commit | Pekerjaan tercampur atau hilang | Review dan commit terpisah di M0 |
| README build tidak konsisten | Agent memakai perintah salah | Samakan dengan PRD §4.4 di M0 |
| Seed transaksi dijalankan sebelum admin ada | Seed gagal | Jalankan setelah `ensureDefaultAdmin` atau aplikasi start |
| Data demo kotor | Demo gagal | Siapkan data demo yang dapat diulang di M6E |

---

## 14. Aktivitas Dokumentasi, Laporan, dan Demo

Aktivitas non-kode tetap mengikuti panduan arsip. Kerjakan setelah bukti implementasi tersedia
dan jangan mengambil snippet dari versi kode yang sudah usang.

| Aktivitas | Pemilik | Referensi arsip |
|---|---|---|
| Cover dan Bab 1: 4 pilar OOP, class diagram, hierarki, aturan bisnis | Anggota 1 | `DEV-GUIDE-1-MODEL-DB.md` Langkah 6–7 |
| Bab 3: ERD, struktur tabel, DAO, transaksi, lampiran backend | Anggota 2 | `DEV-GUIDE-2-DAO-CONTROLLER.md` Langkah 10–11 |
| Bab 2: GUI, komponen Swing, layout, event handling | Anggota 3 | `DEV-GUIDE-3-VIEW.md` Langkah 7 |
| PPT 10–15 slide, format akhir laporan, skenario demo | Anggota 3 | `DEV-GUIDE-3-VIEW.md` Langkah 8 |

Deadline konkret dikelola tim di luar dokumen ini. Yang penting: dokumen dan demo memakai kode
final, nama kelas/method harus identik dengan implementasi, dan tidak menampilkan kredensial
database.

---

## 15. Referensi Silang

| Kebutuhan | Rujukan |
|---|---|
| Requirement, prioritas, aturan bisnis | PRD §1–§3, §7 |
| Kontrak model dan enum | PRD §6 |
| DDL, seed, akun awal | PRD §8 |
| Kontrak DAO | PRD §9 |
| Kontrak controller | PRD §10 |
| Spesifikasi view | PRD §11 |
| Exception dan util | PRD §12 |
| Skenario acceptance | PRD §17 |
| Keputusan dan asumsi | PRD §15 |
| Alur runtime dan transaksi | ARCHITECTURE §5, §7 |
| Penanganan error | ARCHITECTURE §8 |
| Keputusan arsitektur | ARCHITECTURE §10 |
| Risiko arsitektur | ARCHITECTURE §12 |
| Contoh DAO | DEVELOPMENT-REFERENCE §3.1–§3.7 |
| Contoh controller | DEVELOPMENT-REFERENCE §4.1–§4.7 |
| API view dan wiring | DEVELOPMENT-REFERENCE §5–§6 |
| Test DAO | DEVELOPMENT-REFERENCE §7 |
| Checklist dan jebakan | DEVELOPMENT-REFERENCE §8–§9 |
| Urutan kerja per anggota (arsip) | DEV-GUIDE 1–3 |

---

## Lampiran A — Urutan File yang Dikerjakan

Urutan ini membantu agent memilih file yang tepat tanpa melanggar dependensi.

| Urutan | File/kelompok | Milestone |
|---|---|---|
| 1 | `util/*`, `dao/DatabaseConnection.java` | M2 |
| 2 | `dao/StaffDAO.java`, `VehicleDAO.java`, `CustomerDAO.java` | M3A |
| 3 | `dao/RentalDAO.java`, `PaymentDAO.java`, `VehicleReturnDAO.java` | M3B |
| 4 | `dao/ReportDAO.java`, `test/rentgo/dao/DaoConsoleTest.java` | M3C |
| 5 | `controller/LoginController.java`, `VehicleController.java`, `CustomerController.java` | M4 |
| 6 | `controller/RentalController.java`, `VehicleReturnController.java` | M4 |
| 7 | `controller/ReportController.java`, `UserManagementController.java` | M4/M6 |
| 8 | `Main.java`, `view/LoginFrame.java`, `view/MainFrame.java` | M5A |
| 9 | `view/VehiclePanel.java`, `view/CustomerPanel.java` | M5B |
| 10 | `view/RentalPanel.java`, `view/VehicleReturnPanel.java`, `view/ReportPanel.java`, `view/UserManagementPanel.java` | M5C |
| 11 | Wiring `MainFrame`, role, resize, `test/rentgo/view/ViewPreview.java` | M5D |
| 12 | Perbaikan bug, P1/P2, data demo | M6 |

---

## Lampiran B — Checklist Cepat Sebelum Menyatakan Selesai

- [ ] Saya mengerjakan milestone yang dependensinya sudah lulus.
- [ ] Saya hanya mengubah file di paket yang disepakati.
- [ ] Saya tidak menambah library atau mengubah kontrak diam-diam.
- [ ] Saya sudah compile dengan perintah PRD §4.4.
- [ ] Saya sudah menjalankan test yang relevan.
- [ ] Saya sudah memeriksa batas MVC, transaksi, exception, dan role.
- [ ] Saya sudah mencatat gap yang belum selesai.
- [ ] Saya tidak melakukan commit atau membuat branch tanpa instruksi manusia.
