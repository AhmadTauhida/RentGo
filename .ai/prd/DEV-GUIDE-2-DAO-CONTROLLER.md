# Panduan Pengembangan — Anggota 2: Data Access Layer & Integration (DAO & Controller)

> Dokumen kerja untuk **Anggota 2**. Dibaca manusia dan AI agent bersama `PRD.md` (PRD = spesifikasi; dokumen ini = urutan langkah).
> Jika dokumen ini bertentangan dengan PRD, **PRD yang benar**.

| Item | Nilai |
|---|---|
| Branch | `feature/dao-controller` |
| Paket milik kamu | `rentgo.dao`, `rentgo.controller`, `rentgo.util`, folder `config/`, `test/rentgo/dao/` |
| Kontrak yang kamu pegang | PRD §9 (DAO), §10 (controller), §12 (util), §7.4 (transaksi DB) |
| Dokumen yang kamu tulis | **Bab 3** laporan (skema ERD, struktur relasi tabel, penjelasan DAO), **lampiran source code backend**; slide PPT: arsitektur data, alur transaksi backend, integrasi database |
| Kamu menunggu | **Gate A** (kerangka + model) dan **Gate B** (skema Neon) dari Anggota 1 |
| Yang menunggumu | Anggota 3 menunggu **Gate C** (`util`) dan **Gate D** (DAO + controller) |

Kamu pemilik lapisan penghubung: semua alur data lewat kodemu. Kualitas transaksi database (atomik, rollback) ada di tanganmu.

---

## Langkah 0 — Setup (menunggu Gate A)

- [ ] Clone repo, **File → Import → Existing Projects into Workspace** di Eclipse. Pastikan JRE-nya JavaSE-21 dan jar di `lib/` terbaca (tidak ada tanda silang merah).
- [ ] Buat `config/db.properties` dari `config/db.properties.example` dengan kredensial Neon dari Anggota 1.
- [ ] `git checkout -b feature/dao-controller` setelah `main` berisi kerangka (Gate A).
- [ ] Sambil menunggu Gate A, kamu sudah bisa membaca PRD §9, §10, §12 dan memeriksa model yang akan dipakai.

---

## Langkah 1 — `util` Lebih Dulu → Gate C (Kam 12 Nov)

Anggota 3 butuh `CurrencyFormatter` dan `Session`; semua orang butuh `Validator`. Selesaikan dan PR **kecil dan cepat**.

Prompt:
```
Baca PRD.md dan docs/DEV-GUIDE-2-DAO-CONTROLLER.md. Kerjakan LANGKAH 1:
implementasi util: Validator, CurrencyFormatter, PasswordHasher, Session sesuai PRD §12.
Hanya ubah paket rentgo.util. Tambahkan pengujian console untuk util di test/rentgo/dao/DaoConsoleTest (bagian util).
Compile dengan PRD §4.4 dan laporkan hasilnya.
```

- [ ] `Validator`: semua method di PRD §12; pesan error Indonesia **menyebut nama field**; `NumberFormatException` dikonversi menjadi `InvalidDataException`.
- [ ] `CurrencyFormatter.formatRupiah(350000)` → `"Rp350.000"`; `parseRupiah("Rp350.000")` → `350000.0`.
- [ ] `PasswordHasher.hash(...)` menghasilkan string berbeda tiap panggilan (salt acak) tetapi `verify` benar untuk password yang sama dan `false` untuk yang salah.
- [ ] `Session` set/get/clear.
- [ ] PR → merge → umumkan **Gate C**.

---

## Langkah 2 — `DatabaseConnection` (Singleton)

- [ ] Memuat `config/db.properties`; `Class.forName("org.postgresql.Driver")`.
- [ ] `getConnection()` membuat ulang koneksi bila `null`/tertutup/`!isValid(2)`; **retry satu kali** (Neon cold start).
- [ ] Pesan `DatabaseException` yang berguna bila gagal: sebut cek internet, URL/kredensial di `config/db.properties`, status database Neon.
- [ ] **Uji:** koneksi sukses; ganti password salah sengaja → `DatabaseException` berpesan jelas (tidak crash, tidak mencetak password); matikan wifi → pesan jelas.

---

## Langkah 3 — `StaffDAO` (menunggu Gate B)

- [ ] `CrudDAO<Staff>` + `findByUsername` + `ensureDefaultAdmin` (membuat `admin/admin123` hanya bila tabel `staff` kosong, hash via `PasswordHasher`).
- [ ] Uji console: panggil `ensureDefaultAdmin()` dua kali → hanya satu baris `admin`. `findByUsername("admin")` + `PasswordHasher.verify` benar; username tak dikenal → `null`.

---

## Langkah 4 — `VehicleDAO`

- [ ] Satu *factory method* privat memetakan baris → `Car`/`Motorcycle`/`Bus` dari `vehicle_type`.
- [ ] `add` mengisi `id` hasil `RETURN_GENERATED_KEYS`/`RETURNING`; kolom khusus subclass diisi sesuai jenis, sisanya `NULL`.
- [ ] `search(keyword, type, status)` — parameter `null` = tidak difilter; **semua query `PreparedStatement`**.
- [ ] `findAvailable()`, `updateStatus(id, status)`.
- [ ] Uji console: tambah Car/Motorcycle/Bus → `findAll` mengembalikan **tipe dinamis yang benar** (`instanceof` hanya di test) → ubah → hapus. Plat duplikat → `DatabaseException` "Plat nomor sudah terdaftar" (via SQLState `23505`).

---

## Langkah 5 — `CustomerDAO`

- [ ] CRUD + `search(nameOrNationalId)` + `mapByNationalId()` (`Map<String, Customer>`).
- [ ] Uji: NIK duplikat → pesan ramah; hapus pelanggan yang punya riwayat rental → pesan "Data masih dipakai transaksi lain" (SQLState `23503`), **bukan stack trace**.

---

## Langkah 6 — `RentalDAO` + `PaymentDAO` (bagian terpenting)

- [ ] `saveRental(Rental, Payment)`: **satu transaksi JDBC** — `setAutoCommit(false)` → cek status kendaraan **di dalam transaksi** (`AVAILABLE`, kalau tidak → `VehicleUnavailableException`) → `INSERT rental` → `UPDATE vehicle SET status='RENTED'` → `INSERT payment` → `commit`; `rollback` di `catch`; `setAutoCommit(true)` di `finally`.
- [ ] `cancel(rentalId)`: hanya status `ACTIVE`; rental → `CANCELLED`, vehicle → `AVAILABLE`, atomik.
- [ ] `findActive()`, `findHistory()`: objek `Rental` terisi lengkap `Customer`, `Vehicle`, `Staff` (JOIN atau query lanjutan).
- [ ] `update` hanya untuk `ACTIVE`; `add`/`delete` boleh `UnsupportedOperationException`.
- [ ] `PaymentDAO.findByRentalId`.
- [ ] **Uji wajib (skenario PRD §17 no. 10):**
  1. Simpan rental normal → baris `rental` + `payment` ada **dan** vehicle `RENTED`.
  2. Sewa kendaraan yang sudah `RENTED` → `VehicleUnavailableException`; jumlah baris `rental`/`payment` **tidak bertambah**.
  3. Simulasi kegagalan di tengah transaksi (mis. paksa `payment.amount` melanggar constraint) → **rollback total**: status vehicle tetap `AVAILABLE`.
  4. `cancel` → vehicle kembali `AVAILABLE`.

---

## Langkah 7 — `VehicleReturnDAO` + `ReportDAO`

- [ ] `saveReturn`: transaksi `INSERT vehicle_return` + rental → `COMPLETED` + vehicle → `AVAILABLE`. Pengembalian kedua untuk rental yang sama ditolak (UNIQUE `rental_id`) dengan pesan ramah.
- [ ] `ReportDAO.revenue(from, to)` → `{date, transactionCount, totalRevenue}`; `topVehicles(limit)` → `{plateNumber, brand, vehicleType, rentalCount}`. Uji dengan data seed.

**PR → Gate D bagian DAO**: kamu bisa merge DAO dulu (PR "DAO layer") sebelum controller selesai.

---

## Langkah 8 — Controller (PRD §10)

Urutan: `LoginController` → `VehicleController` → `CustomerController` → `RentalController` → `VehicleReturnController` → `ReportController` (P1).

Aturan setiap controller:
- [ ] Konstruktor sesuai kontrak PRD §10; method publik **persis** nama di PRD.
- [ ] Ambil input dari view dalam bentuk `String`/objek komponen; validasi dengan `Validator`; **konversi** teks → angka dan `Date` → `LocalDate` terjadi di controller.
- [ ] Tangkap `InvalidDataException`, `VehicleUnavailableException`, `DatabaseException`, `NumberFormatException` → `view.showError(...)`; sukses → `view.showInfo(...)`. **Tidak ada exception yang lolos ke Swing.**
- [ ] Hapus selalu didahului `view.confirm(...)`.
- [ ] **Tidak ada SQL** dan **tidak ada aturan tarif** di controller (panggil `rental.calculateTotalCost()`, `vehicleReturn.calculateLateFee()`).
- [ ] `RentalController.calculateTotal()`/`VehicleReturnController.calculateLateFee()` hanya pratinjau (tidak menyimpan).
- [ ] `RentalController.saveRental()`: Bus → `withDriver` dipaksa `true`; `Session.getStaff()` menjadi `staff`; pembayaran penuh sebesar `totalCost`.
- [ ] `LoginController.login()`: ambil `Staff` via `StaffDAO`, `PasswordHasher.verify`, set `Session`, buka `MainFrame`; salah → pesan error, tidak membocorkan apakah username atau password yang salah.

Prompt contoh per controller:
```
Baca PRD.md §10 dan docs/DEV-GUIDE-2-DAO-CONTROLLER.md. Kerjakan LANGKAH 8 untuk VehicleController saja.
Controller tidak boleh berisi SQL maupun aturan tarif. Compile dengan PRD §4.4.
```

Karena `view/` baru berupa kerangka dari Gate A, controller dikompilasi terhadap **signature panel di PRD §11**. Bila Anggota 3 perlu mengubah signature, ikuti Bagian E.

**PR → Gate D** (DAO + controller P0 ter-merge). Umumkan ke Anggota 3.

---

## Langkah 9 — Integrasi Bersama Anggota 3 (Sab–Min)

- [ ] Sesi berdua (atau tiga): jalankan `rentgo.Main`, uji skenario **PRD §17 no. 1–12**.
- [ ] Bug di DAO/controller kamu perbaiki; bug di view serahkan ke Anggota 3; bug di model ke Anggota 1.
- [ ] Uji kondisi gagal: matikan wifi / password salah → dialog error, aplikasi tidak crash (skenario 9).

---

## Langkah 10 — Dokumen: Bab 3 Laporan + Lampiran (Sen 16 Nov)

Bab 3 (menurut Pembagian Tugas): skema database JDBC, arsitektur DAO, lampiran source code.

| Sub-bab | Isi | Bahan |
|---|---|---|
| 3.1 Basis Data | Alasan PostgreSQL di Neon (database bersama tim), konfigurasi koneksi tanpa membuka kredensial | PRD §3, §15 (A16) |
| 3.2 ERD | Gambar ERD 6 tabel + relasi + kardinalitas | Gambar ulang dari PRD §8 (draw.io / dbdiagram.io) |
| 3.3 Struktur Tabel | Tabel kolom/tipe/constraint; pemetaan pewarisan `Vehicle` ke satu tabel | PRD §8 |
| 3.4 Lapisan DAO | `CrudDAO<T>` (generics), `DatabaseConnection` (Singleton), `PreparedStatement` (anti SQL injection), pemetaan `ResultSet` → objek | PRD §9 |
| 3.5 Transaksi | Alur atomik simpan rental & pengembalian (commit/rollback) | PRD §7.4 |
| 3.6 Controller & Validasi | Peran controller, `Validator`, penanganan exception | PRD §10, §12 |
| 3.7 Pengujian Backend | Hasil `DaoConsoleTest` (tangkapan layar/teks) | Output test |
| Lampiran | Source code lengkap backend: `dao/`, `controller/`, `util/` | Salin dari kode final |

Catatan: sertakan juga tabel pemetaan istilah (PRD A2) jika Bab 1 belum memuatnya. Jangan menampilkan kredensial Neon di laporan.

## Langkah 11 — Slide PPT Bagian Anggota 2 (Minggu IV)

1. **Arsitektur data**: diagram MVC + DAO + Neon (PRD §5), ERD ringkas.
2. **Alur transaksi backend**: diagram urutan "simpan rental" (validasi → cek ketersediaan → insert → update status → insert payment → commit/rollback).
3. **Integrasi database**: Singleton, PreparedStatement, penanganan error koneksi (cold start Neon).

Kirim sebagai teks + gambar ke Anggota 3 untuk disatukan.

---

## Definition of Done — Anggota 2

- [ ] `util` lulus uji; Gate C terbuka.
- [ ] Semua DAO P0 teruji di `DaoConsoleTest` (CRUD, filter, UNIQUE/FK ramah, transaksi + rollback).
- [ ] Semua controller P0 (Login, Vehicle, Customer, Rental, VehicleReturn) bekerja dengan view; Gate D terbuka.
- [ ] Tidak ada SQL di luar `dao`; tidak ada string SQL hasil penyambungan input.
- [ ] Tidak ada aturan tarif/denda di `controller`/`dao`.
- [ ] Skenario PRD §17 no. 1, 3, 6, 7, 9, 10, 11 lulus dalam uji integrasi.
- [ ] Bab 3 + lampiran source backend selesai; slide arsitektur data/alur/integrasi diserahkan.

## Jebakan Umum
- Lupa `setAutoCommit(true)` di `finally` → operasi berikutnya ikut transaksi menggantung.
- Cek ketersediaan **di luar** transaksi → dua petugas bisa menyewa kendaraan yang sama.
- Memakai `Statement` + penyambungan string → rentan SQL injection, dan dosen menandainya.
- Koneksi/`ResultSet` tidak ditutup → gunakan try-with-resources.
- Membaca `DATE`/`TIMESTAMP` dengan `getDate` lalu salah zona → pakai `getObject(col, LocalDate.class)`.
- Menelan `SQLException` kosong, atau membiarkan stack trace sampai ke pengguna.
- Mencetak password atau URL lengkap ke log.
- Koneksi pertama lambat (Neon *autosuspend*) dianggap error → timeout ≥ 10 detik + retry.

---
## Bagian Bersama (sama di ketiga panduan)

### A. Aturan Git
- Dilarang commit langsung ke `main` (kecuali commit awal repo oleh Anggota 1). Semua lewat Pull Request yang direview anggota lain.
- Branch: `feature/model-oop` (Anggota 1), `feature/dao-controller` (Anggota 2), `feature/gui-view` (Anggota 3). Boleh membuat sub-branch (`feature/dao-controller-rental`) bila PR dipecah.
- Kerjakan **hanya di paket milikmu** (PRD §14.1). Perlu mengubah file orang lain → minta orangnya, atau PR kecil terpisah yang disetujui.
- Commit kecil dan sering, pesan singkat dalam Bahasa Inggris (`Add VehicleDAO.search`).
- Sebelum membuat PR: `git fetch origin && git merge origin/main` (atau `git rebase origin/main`), compile ulang, jalankan test console.
- Setelah `git pull` atau agent mengubah file: **F5** pada project di Eclipse.
- Jangan pernah commit `config/db.properties`, `bin/`, `build/`, `*.class`.

### B. Urutan Merge (Gate)
| Gate | Isi | Pemilik | Membuka blokir untuk |
|---|---|---|---|
| **A** | PR #1: kerangka proyek + model + exception lengkap, semua compile | Anggota 1 | Anggota 2 & 3 mulai coding |
| **B** | Skema Neon sudah dijalankan + data seed ada | Anggota 1 | `StaffDAO`, `VehicleDAO`, dst. bisa diuji ke DB |
| **C** | PR `util` (`Validator`, `CurrencyFormatter`, `PasswordHasher`, `Session`) | Anggota 2 | Anggota 3 memakai format rupiah & sesi |
| **D** | PR DAO + controller P0 | Anggota 2 | Anggota 3 menyambungkan view ke controller |
| **E** | PR view P0 + wiring `MainFrame` | Anggota 3 | Uji integrasi alur penuh |

### C. Jadwal Usulan (mengejar kumpul laporan 18 Nov 2026)
| Tanggal | Anggota 1 | Anggota 2 | Anggota 3 |
|---|---|---|---|
| Rab 11 Nov | Neon + repo awal + Tahap 0 → **Gate A** | clone, setup, mulai `util` | clone, setup, `Main` + `LoginFrame` + `MainFrame` (data dummy) |
| Kam 12 Nov | Perkuat model + test, **Gate B** (skema Neon) | **Gate C**, `DatabaseConnection`, `StaffDAO`, `VehicleDAO`, `CustomerDAO` | `VehiclePanel`, `CustomerPanel` |
| Jum 13 Nov | Review PR, mulai Bab 1 | `RentalDAO`, `PaymentDAO`, `VehicleReturnDAO`; controller Login/Vehicle/Customer | `RentalPanel`, `VehicleReturnPanel` |
| Sab 14 Nov | Bab 1 + cover | Controller Rental/Return → **Gate D**; `ReportDAO` | `ReportPanel`; sambungkan ke controller → **Gate E** |
| Min 15 Nov | **Uji alur penuh bersama** (PRD §17), perbaiki bug | idem | idem |
| Sen 16 Nov | Finalisasi Bab 1, bantu uji | Bab 3 + lampiran source | Bab 2 + format dokumen |
| Sel 17 Nov | Gabung laporan, cek konsistensi | idem | Format akhir (font, margin, indentasi) |
| **Rab 18 Nov** | **Kumpul laporan** | | |
| Minggu IV Nov | PPT bagian masing-masing, latihan demo 10 menit | | Presenter utama demo |

Jadwal ini usulan; geser sesuai kondisi, tetapi **urutan Gate jangan dibalik**. Jika terlambat, potong P1/P2 (PRD §2), jangan potong pengujian.

### D. Cara Memakai AI Agent di Terminal
Jalankan agent dari **root proyek** (folder `RentGo`). Pola prompt:

```
Baca PRD.md dan docs/<panduan-saya>.md. Kerjakan LANGKAH <n> saja.
Hanya ubah file di paket milik saya. Ikuti kontrak method di PRD §<x>.
Setelah selesai: compile dengan perintah di PRD §4.4 dan laporkan hasilnya.
Jangan commit, jangan membuat branch.
```

Kebiasaan yang disarankan:
1. **Satu langkah per prompt.** Prompt "kerjakan semuanya" menghasilkan kode yang sulit direview.
2. Setelah agent selesai: baca diff (`git diff`), jalankan test, baru commit sendiri.
3. Bila agent menyimpang dari PRD (mengganti nama method, menambah library, memindahkan logika ke lapisan lain), minta perbaiki dengan mengutip bagian PRD yang dilanggar.
4. Jangan menempel kredensial Neon ke prompt.
5. Kamu harus **memahami** kode yang masuk ke repo — dosen akan bertanya.

### E. Mengubah Kontrak
Kontrak = signature method di PRD §6, §9, §10, §11. Jika perlu berubah (misalnya DAO butuh getter baru di model): bicarakan di grup, buat PR kecil terpisah yang mengubah **PRD dan kode sekaligus**, minta persetujuan dua anggota lain, baru merge. Jangan diam-diam mengubah signature.
