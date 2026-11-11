# Panduan Pengembangan — Anggota 1: Database Architect & Core OOP (Model Layer)

> Dokumen kerja untuk **Anggota 1**. Dibaca manusia dan AI agent bersama `PRD.md` (PRD = spesifikasi; dokumen ini = urutan langkah).
> Jika dokumen ini bertentangan dengan PRD, **PRD yang benar**.

| Item | Nilai |
|---|---|
| Branch | `feature/model-oop` |
| Paket milik kamu | `rentgo.model`, `rentgo.exception`, folder `sql/`, setup repo awal |
| Kontrak yang kamu pegang | PRD §6 (model), §7 (aturan bisnis), §8 (DDL), §12 (exception) |
| Dokumen yang kamu tulis | Cover; **Bab 1** laporan (4 pilar OOP, class diagram, hierarki, code snippet); slide PPT: konsep OOP, class diagram, aturan bisnis tarif/denda |
| Orang yang menunggumu | Anggota 2 & 3 menunggu **Gate A** (kerangka + model) dan Anggota 2 menunggu **Gate B** (skema Neon) |

**Kamu adalah jalur kritis hari pertama.** Prioritas: Gate A secepatnya, lalu Gate B, baru perbaikan dan dokumen.

---

## Langkah 0 — Setup Awal (manual, ±45 menit)

- [ ] **Neon:** buat project + database di Neon. Catat host, nama database, user, password. Buat `config/db.properties` lokal (format PRD §3). Bagikan kredensial ke Anggota 2 & 3 **lewat chat pribadi**, bukan lewat repo.
- [ ] **Eclipse:** buat project `RentGo` (JavaSE-21, source `src` + `test`, output `bin`, tanpa `module-info.java`), folder `lib/`, `config/`, `sql/`, dan package sesuai daftar di PRD §4. Salin `flatlaf-3.8.jar` dan `postgresql-42.7.14.jar` ke `lib/`, lalu tambahkan ke Build Path.
- [ ] **GitHub:** buat repo, tambahkan Anggota 2 & 3 sebagai collaborator. Aktifkan *branch protection* untuk `main` (wajib PR) **setelah** commit awal.
- [ ] **Commit awal langsung ke `main`** (satu-satunya commit langsung yang diizinkan): `.project`, `.classpath`, `lib/*.jar`, `.gitignore` (PRD §4.3), `PRD.md`, `docs/`, `.github/copilot-instructions.md`, `config/db.properties.example`.
- [ ] Pastikan `config/db.properties` **tidak** ikut ter-commit (`git status`).
- [ ] Buat branch: `git checkout -b feature/model-oop`.

**Selesai bila:** repo ada di GitHub, ketiga anggota bisa clone, `git status` bersih dari file rahasia.

---

## Langkah 1 — Jalankan Tahap 0 (PRD §16)

Prompt untuk agent:
```
Baca PRD.md dan docs/DEV-GUIDE-1-MODEL-DB.md. Kerjakan Tahap 0 (PRD §16) sepenuhnya.
Jangan mengubah .project/.classpath. Jangan commit dan jangan membuat branch.
Setelah selesai, compile dengan PRD §4.4, jalankan RentalCostTest, dan laporkan hasilnya.
```

- [ ] Struktur folder dan semua file di PRD §4 ada.
- [ ] `model/` dan `exception/` lengkap (bukan stub); `dao/`, `controller/`, `view/`, `util/` berupa kerangka dengan **signature persis kontrak** PRD §9–§12.
- [ ] Compile tanpa error dengan perintah PRD §4.4.
- [ ] `RentalCostTest` mencetak angka benar (lihat Langkah 3).

**Selesai bila:** compile bersih + `git diff` sudah kamu baca sekilas (tidak ada file aneh seperti `pom.xml`).

---

## Langkah 2 — PR #1 → Gate A (target: hari ini)

- [ ] Commit di `feature/model-oop`, push, buat PR ke `main` dengan judul `Scaffold project and core model`.
- [ ] Minta Anggota 2 dan 3 mereview **hanya satu hal**: *signature* di `dao/`, `controller/`, `view/` sudah cukup untuk mereka bekerja? (Review mendalam model menyusul.)
- [ ] Merge. Umumkan di grup: **"Gate A terbuka — silakan buat branch dari main."**

**Jangan menunggu model sempurna.** Model yang sudah compile dan benar untuk skenario inti cukup untuk membuka Gate A; penyempurnaan masuk PR berikutnya.

---

## Langkah 3 — Verifikasi & Perkuat Model

Review per kelas (PRD §6.2). Centang bila sudah dicek **dengan membaca kode dan menjalankan test**:

- [ ] `Person` abstrak; `getDisplayInfo()` abstrak; `Customer` & `Staff` memanggil `super(...)` dan meng-override-nya.
- [ ] `Vehicle` abstrak; `calculateRent(int)` abstrak; `calculateRent(int, boolean)` konkret default; `validateDays` menolak `days < 1`.
- [ ] `Car`, `Motorcycle`, `Bus` sesuai rumus PRD §7.1; konstanta `public static final` (tidak ada angka ajaib).
- [ ] `Rental.calculateTotalCost()` memanggil `vehicle.calculateRent(getRentalDays(), withDriver)` — **tanpa `instanceof`**.
- [ ] `Rental.getRentalDays()` = `max(1, DAYS.between(startDate, dueDate))`.
- [ ] `VehicleReturn.calculateLateDays()` tidak pernah negatif; `calculateLateFee()` = `lateDays × 1.5 × baseRate`.
- [ ] Semua setter memvalidasi dan melempar `InvalidDataException` dengan pesan **Indonesia** yang menyebut nama field.
- [ ] Semua field `private`; konstruktor memanggil setter.
- [ ] Enum punya `getLabel()`, `toString()` = label, `fromString(String)`.
- [ ] `printReceipt()` di `Rental`, `Payment`, `VehicleReturn` menghasilkan teks rapi (pelanggan, kendaraan, tanggal, lama sewa, total dalam format rupiah).
- [ ] `toString()` ramah tampilan untuk `Customer`, `Vehicle`, `Rental` (dipakai `JComboBox`).

**Tambahkan test tepi** ke `RentalCostTest` (prompt agent: *"Tambahkan test tepi ke RentalCostTest sesuai docs/DEV-GUIDE-1-MODEL-DB.md Langkah 3"*):

| Kasus | Hasil benar |
|---|---|
| Car 350.000, 7 hari, tanpa sopir | 2.205.000 |
| Car 350.000, 3 hari, dengan sopir | 1.500.000 |
| Car 6 hari vs 7 hari | 6 hari tanpa diskon; 7 hari diskon 10% |
| Motorcycle 100.000, 7 hari | 595.000 |
| Bus 1.500.000, 2 hari (`withDriver` true **atau** false) | 3.600.000 |
| `calculateRent(0)` / `(-1)` | `InvalidDataException` |
| `setBaseRate(-1)`, plat kosong, tahun 1900, NIK bukan 16 digit | `InvalidDataException` |
| `dueDate` sebelum `startDate` | `InvalidDataException` |
| `startDate == dueDate` | `getRentalDays() == 1` |
| Late fee Car 350.000 terlambat 2 hari | 1.050.000 |
| Kembali tepat waktu / lebih awal | late fee 0 |
| `List<Vehicle>` isi Car+Motorcycle+Bus, `calculateRent(5)` | tiap jenis menghasilkan nilai berbeda tanpa `if` |

Jalankan: lihat PRD §4.4 (`java -cp "build/classes:lib/*" rentgo.model.RentalCostTest`).

**Selesai bila:** semua baris di atas lulus dan semua centang review terisi.

---

## Langkah 4 — Skema Neon → Gate B (target: besok pagi)

- [ ] Buka SQL Editor Neon → jalankan isi `sql/schema.sql` → harus sukses tanpa error.
- [ ] Verifikasi: enam tabel ada (`staff`, `customer`, `vehicle`, `rental`, `payment`, `vehicle_return`).
- [ ] Jalankan `sql/seed.sql`. Cek: `SELECT vehicle_type, count(*) FROM vehicle GROUP BY 1;` → ada Car, Motorcycle, Bus; ada satu `MAINTENANCE`.
- [ ] **Uji constraint:** `INSERT` kendaraan dengan `base_rate = -1` → harus ditolak; `INSERT` rental dengan `due_date < start_date` → ditolak; `INSERT` dua kendaraan dengan plat sama → ditolak.
- [ ] Jika skema perlu diubah, **jangan edit tabel langsung di Neon**; ubah `sql/schema.sql`, kabari Anggota 2, lalu jalankan ulang (hapus tabel lama dulu: `DROP TABLE ... CASCADE` — hanya aman sebelum ada data berharga).
- [ ] PR kecil `Add and verify database schema` bila ada perbaikan SQL. Umumkan: **"Gate B terbuka."**

Catatan: akun `admin/admin123` **tidak** ada di SQL — dibuat oleh `StaffDAO.ensureDefaultAdmin()` (Anggota 2).

---

## Langkah 5 — Dukungan & Review (berjalan terus)

- [ ] Jawab permintaan perubahan model dari Anggota 2/3 (getter tambahan, `toString`, dsb.) — ikuti aturan *Mengubah Kontrak* (Bagian E).
- [ ] Review PR Anggota 2 (DAO, controller) dan Anggota 3 (view). Fokus review untukmu: apakah mereka **memakai model dengan benar** (tidak menduplikasi aturan tarif di controller/view; tidak memakai `instanceof` Vehicle).
- [ ] Pastikan tidak ada aturan bisnis bocor ke luar `model` (cari `* 0.9`, `1.5`, `150000` di `controller/` dan `view/`).

---

## Langkah 6 — Dokumen: Cover & Bab 1 Laporan (mulai Jum 13 Nov)

Bab 1 (menurut Pembagian Tugas): karakteristik 4 pilar OOP, class diagram, hierarki, code snippet inheritance/polimorfisme. Usulan susunan:

| Sub-bab | Isi | Bahan |
|---|---|---|
| 1.1 Gambaran Singkat Kelas Model | 10 kelas + interface + enum + exception | PRD §6.2 |
| 1.2 Pemetaan Istilah | Tabel istilah proposal ↔ nama di kode | PRD §15 (A2) |
| 1.3 Hierarki Pewarisan | `Person` → `Customer`/`Staff`; `Vehicle` → `Car`/`Motorcycle`/`Bus` | PRD §6.1 |
| 1.4 Class Diagram | Gambar diagram (hierarki + relasi transaksi) | Mermaid PRD §6.1; gambar ulang rapi di draw.io, **sesuaikan dengan nama kode final** |
| 1.5 Empat Pilar OOP | Per pilar: penjelasan + **potongan kode asli** dari proyek + letak file/baris | PRD §6.3; ambil snippet dari kode final |
| 1.6 Aturan Bisnis | Tabel tarif/diskon/sopir + rumus + contoh hitung | PRD §7.1–7.2, §17 |
| 1.7 Exception Handling pada Model | `InvalidDataException`, `VehicleUnavailableException` dan kapan dilempar | PRD §12 |

Catatan: ambil snippet dari **kode final** (bukan dari PRD) agar sama persis dengan lampiran source code Anggota 2. Cover memuat judul, nama kelompok, anggota + NIM, Departemen Teknik Komputer UNDIP.

**Selesai bila:** Bab 1 + cover tersusun; semua nama kelas/method di dokumen identik dengan kode.

---

## Langkah 7 — Slide PPT Bagian Anggota 1 (Minggu IV)

3 kelompok slide (total ±3–4 slide dari 10–15 slide keseluruhan, digabung oleh Anggota 3):
1. **Konsep OOP**: 4 pilar, masing-masing satu contoh kode pendek dari proyek.
2. **Class diagram**: hierarki + relasi; satu slide, visual bersih.
3. **Aturan bisnis**: tabel tarif + satu contoh hitung berjalan (Car 7 hari = Rp2.205.000) + aturan denda.

Kirim ke Anggota 3 dalam bentuk teks + gambar terpisah (bukan slide jadi) agar mudah disatukan satu tema.

---

## Definition of Done — Anggota 1

- [ ] PR #1 (scaffold + model) ter-merge; Gate A & B terbuka.
- [ ] `RentalCostTest` lulus seluruh baris tabel Langkah 3.
- [ ] Skema + seed berjalan di Neon; constraint terbukti menolak data salah.
- [ ] Tidak ada aturan tarif/denda di luar `model`.
- [ ] Bab 1 + cover selesai dan konsisten dengan kode final.
- [ ] Slide konsep OOP, class diagram, aturan bisnis diserahkan ke Anggota 3.
- [ ] Ikut uji alur penuh (PRD §17) dan memperbaiki bug yang berasal dari model.
- [ ] Setelah laporan dikumpulkan: **reset password database Neon** (kredensial sempat beredar di chat).

## Jebakan Umum
- Menunda Gate A demi "menyempurnakan" model → Anggota 2 & 3 menganggur.
- Mengedit tabel langsung di Neon tanpa memperbarui `sql/schema.sql` → skema tidak reproduktif.
- Setter diisi langsung di konstruktor (`this.x = x`) sehingga validasi terlewat.
- Membiarkan agent menambah `record`, Lombok, atau `pom.xml`.
- Snippet laporan berbeda dengan kode final.

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
