# Panduan Pengembangan — Anggota 3: GUI Designer & User Experience (View Layer)

> Dokumen kerja untuk **Anggota 3**. Dibaca manusia dan AI agent bersama `PRD.md` (PRD = spesifikasi; dokumen ini = urutan langkah).
> Jika dokumen ini bertentangan dengan PRD, **PRD yang benar**.

| Item | Nilai |
|---|---|
| Branch | `feature/gui-view` |
| Paket milik kamu | `rentgo.view`, `rentgo.Main`, `test/rentgo/view/ViewPreview.java` (peluncur pratinjau UI, opsional) |
| Kontrak yang kamu pegang | PRD §11 (view), §5 (wiring & alur start), §7.5 (hak akses di UI) |
| Dokumen yang kamu tulis | **Bab 2** laporan (rincian GUI, jenis frame/panel, komponen Swing, layout manager, event handling); **PPT 10–15 slide** (menyatukan bagian Anggota 1 & 2); **skenario demo**; **format akhir seluruh laporan** (font, margin, indentasi) |
| Kamu menunggu | **Gate A** (kerangka + model) dari Anggota 1; **Gate C** (`util`) dan **Gate D** (controller) dari Anggota 2 |
| Peran presentasi | **Presenter utama demo** |

Kamu bisa mulai bekerja tanpa database: bangun tampilan dengan **data dummy** dari model, lalu sambungkan ke controller ketika Gate D terbuka.

---

## Langkah 0 — Setup (menunggu Gate A)

- [ ] Clone repo, **File → Import → Existing Projects into Workspace** di Eclipse. JRE = JavaSE-21; jar di `lib/` terbaca.
- [ ] Buat `config/db.properties` dari `config/db.properties.example` (kredensial Neon dari Anggota 1).
- [ ] `git checkout -b feature/gui-view` setelah `main` berisi kerangka.
- [ ] Baca PRD §11 (spesifikasi view) dan wireframe pada proposal (Gambar 3–5).

---

## Langkah 1 — `Main`, `LoginFrame`, `MainFrame`

Prompt:
```
Baca PRD.md dan docs/DEV-GUIDE-3-VIEW.md. Kerjakan LANGKAH 1: Main (PRD §5 alur start),
LoginFrame dan MainFrame (PRD §11). Hanya ubah paket rentgo.view dan rentgo.Main.
Gunakan layout manager (tanpa layout null). Compile dengan PRD §4.4 dan laporkan hasilnya.
```

- [ ] `Main`: `FlatLightLaf.setup()` → `SwingUtilities.invokeLater` → cek DB (dialog error jelas bila gagal) → `StaffDAO.ensureDefaultAdmin()` → `LoginFrame`.
- [ ] `LoginFrame`: `GridBagLayout` di tengah; username, password (`JPasswordField`), tombol Masuk/Batal; **Enter = Masuk**; `getUsername()`, `getPassword()`.
- [ ] `MainFrame`: `BorderLayout` — `JMenuBar` (Data, Transaksi, Laporan, Akun), `CardLayout` di tengah dengan nama kartu `"VEHICLE"`, `"CUSTOMER"`, `"RENTAL"`, `"RETURN"`, `"REPORT"`, `"USERS"`, status bar "Masuk sebagai {nama} ({role})"; `setMinimumSize(1000×650)`; `showCard(String)`.
- [ ] Menu "Akun" disembunyikan untuk `STAFF` (PRD §7.5).

**Pratinjau tanpa database:** buat `test/rentgo/view/ViewPreview.java` berisi `main` yang menampilkan `MainFrame` dengan `Session.setStaff(...)` dummy dan panel diisi data dummy. Jalankan dari Eclipse (Run As → Java Application) atau terminal (PRD §4.4). File ini opsional untuk di-commit.

**Selesai bila:** login frame dan main frame tampil; ganti kartu lewat menu bekerja; resize jendela tetap rapi.

---

## Langkah 2 — `VehiclePanel` & `CustomerPanel` (P0)

Pola kedua panel sama; kerjakan `VehiclePanel` lebih dulu.

- [ ] `BorderLayout`: pencarian/filter di atas, form di kiri (`GridBagLayout`), `JTable` di tengah dalam `JScrollPane`.
- [ ] Komponen sesuai PRD §11.2; nama komponen/variabel Inggris (`plateNumberField`, `addButton`), **label yang tampil Indonesia**.
- [ ] `VehiclePanel`: field khusus jenis **muncul/hilang** sesuai `JComboBox<VehicleType>` (Car: penumpang + transmisi; Motorcycle: CC; Bus: kursi).
- [ ] Method standar: `setController`, `showError`, `showInfo`, `confirm`, `setTableData(List<T>)`, `getSelectedItem()`/`getSelectedIndex()`, getter per field bertipe `String`.
- [ ] Listener satu aksi satu method: `addButton.addActionListener(e -> controller.addVehicle());` — **tanpa logika bisnis dan tanpa SQL** di view.
- [ ] Klik baris tabel → `controller.selectRow(index)` mengisi form.
- [ ] Peran `STAFF`: tombol tambah/ubah/hapus di `VehiclePanel` dinonaktifkan (PRD §7.5).
- [ ] Hapus selalu memanggil `confirm(...)` (dialog konfirmasi) — controller yang memicu, view menyediakan method-nya.

**Uji tanpa DB:** isi tabel dengan `Car`/`Motorcycle`/`Bus` dummy lewat `ViewPreview`. Cek tampilan saat jendela diperbesar/diperkecil (skenario PRD §17 no. 8).

---

## Langkah 3 — `RentalPanel`, `VehicleReturnPanel` (P0), `ReportPanel` (P1)

- [ ] `RentalPanel`: `BorderLayout` (form atas, tabel riwayat bawah); `JComboBox<Customer>`, `JComboBox<Vehicle>` (hanya yang `AVAILABLE`), dua `JSpinner` dengan `SpinnerDateModel` (format `dd-MM-yyyy`), `JCheckBox` "Dengan sopir" (**dicentang dan dikunci** bila kendaraan terpilih adalah `Bus`), `JComboBox<PaymentMethod>`, `JLabel` total biaya, `JTable` riwayat, tombol Simpan / Ubah / Batalkan / Cetak Struk.
- [ ] Setiap perubahan combo/tanggal/checkbox memanggil `controller.calculateTotal()`; panel menyediakan `setTotalCost(String)`. **Panel tidak menghitung biaya sendiri.**
- [ ] `VehicleReturnPanel`: `GridBagLayout`; `JComboBox<Rental>` (transaksi aktif), `JSpinner` tanggal kembali aktual, `JComboBox<VehicleCondition>`, label info sewa, label denda; `setLateFeeInfo(int lateDays, String lateFeeText)`.
- [ ] `ReportPanel` (P1): `JComboBox` periode, dua `JTable` (pendapatan, kendaraan terlaris), label total; `setRevenueData`, `setTopVehiclesData`, `getPeriod`.
- [ ] Struk: dialog `JOptionPane` berisi `JTextArea` (monospaced, tidak editable) dari `rental.printReceipt()`.
- [ ] Uang ditampilkan dengan `CurrencyFormatter.formatRupiah(...)` (dari Anggota 2, Gate C).

---

## Langkah 4 — Sambungkan ke Controller (menunggu Gate D)

- [ ] Wiring di `MainFrame` sesuai PRD §5:
  ```java
  VehiclePanel panel = new VehiclePanel();
  VehicleController controller = new VehicleController(panel, new VehicleDAO());
  panel.setController(controller);
  controller.loadData();
  ```
  Lakukan untuk semua panel; `RentalController` butuh DAO tambahan (PRD §10).
- [ ] Saat kartu dibuka via menu, panggil `loadData()`/`loadOptions()`/`loadActiveRentals()`/`loadReport()` agar data segar.
- [ ] `LoginController` membuka `MainFrame` setelah sukses; `logout()` menutup `MainFrame` dan menampilkan ulang `LoginFrame`.
- [ ] Jika signature controller ternyata kurang cocok dengan kebutuhan view: ikuti Bagian E (jangan ubah diam-diam).

**PR → Gate E** (view P0 + wiring). Umumkan ke tim.

---

## Langkah 5 — Polesan UX

- [ ] Tab order logis; tombol default (Enter) pada form; mnemonic menu (`Alt+...`) bila sempat.
- [ ] Konsistensi: margin/padding sama, judul panel, jarak antar komponen, font bawaan FlatLaf.
- [ ] Tabel: lebar kolom wajar, kolom uang rata kanan, tanggal `dd-MM-yyyy`.
- [ ] Pesan dialog Indonesia, jelas menyebut field yang salah (pesan datang dari `InvalidDataException`).
- [ ] **Tes resize** (PRD §17 no. 8): perbesar, perkecil, maksimalkan — tidak ada komponen terpotong/menumpuk.
- [ ] Tes dengan peran `STAFF` dan `ADMIN` (PRD §17 no. 11).

---

## Langkah 6 — Uji Integrasi Bersama (Min 15 Nov)

Uji skenario PRD §17 no. 1–12 bersama Anggota 1 & 2. Bagian yang kamu pegang: **1, 7, 8, 11, 12** dan semua yang berkaitan dengan dialog/tata letak. Catat bug dengan format: *langkah → hasil aktual → hasil diharapkan → pemilik (model/dao/controller/view)*.

---

## Langkah 7 — Dokumen: Bab 2 Laporan (Sen 16 Nov)

Bab 2 (menurut Pembagian Tugas): rincian GUI, jenis frame/panel, komponen Swing, layout manager.

| Sub-bab | Isi | Bahan |
|---|---|---|
| 2.1 Gambaran Antarmuka | Daftar 2 frame + 5 panel (+ 1 opsional), fungsi masing-masing, alur navigasi | PRD §11.2 |
| 2.2 Tangkapan Layar | Satu gambar per tampilan, dengan keterangan | Screenshot dari aplikasi final |
| 2.3 Komponen Swing | Tabel per tampilan: komponen yang dipakai dan fungsinya | PRD §11.2 + kode final |
| 2.4 Layout Manager | `GridBagLayout`, `BorderLayout`, `CardLayout`; alasan pemilihan + bukti tata letak responsif (screenshot resize) | PRD §11.1 |
| 2.5 Event Handling | `ActionListener` dengan lambda; satu aksi satu method; contoh potongan kode asli | Kode final |
| 2.6 Dialog & Validasi di UI | `JOptionPane` konfirmasi hapus dan pesan error | Kode final |
| 2.7 Tema FlatLaf | Alasan, cara memasang (`FlatLightLaf.setup()`) | PRD §3 |

Ambil snippet dan screenshot dari **versi final** aplikasi.

---

## Langkah 8 — PPT, Skenario Demo, Format Laporan

### PPT (10–15 slide, modern & visual)
Kumpulkan teks dan gambar dari Anggota 1 (konsep OOP, class diagram, aturan bisnis) dan Anggota 2 (arsitektur data, alur transaksi, integrasi DB). Usulan susunan:

1. Judul + anggota · 2. Latar belakang & tujuan · 3. Fitur & peran pengguna · 4. Arsitektur MVC + DAO · 5. Konsep OOP (4 pilar) · 6. Class diagram · 7. Aturan bisnis tarif/denda · 8. Basis data & ERD · 9. Alur transaksi backend · 10. Desain GUI (wireframe → hasil) · 11. Demo langsung · 12. Pengujian · 13. Kendala & pembelajaran · 14. Penutup & tanya jawab.

### Skenario demo (10 menit)
Alur menurut Pembagian Tugas: **Login → CRUD → Sewa → Kembali → Laporan**.

| Menit | Aksi | Data yang disiapkan |
|---|---|---|
| 0–1 | Login sebagai Admin | akun `admin/admin123` |
| 1–3 | CRUD Kendaraan: tambah Motorcycle baru, ubah tarif, coba tarif negatif (error validasi), hapus (dialog konfirmasi) | — |
| 3–4 | CRUD Customer singkat | — |
| 4–6 | Sewa: pilih Car, 7 hari → total tampil diskon; tambah "dengan sopir"; simpan; tunjukkan status kendaraan jadi Disewa; coba sewa kendaraan yang sama → ditolak | Car tersedia |
| 6–8 | Pengembalian terlambat 2 hari → denda 150%; status kembali Tersedia | sewa aktif yang sudah disiapkan |
| 8–9 | Laporan: pendapatan + kendaraan terlaris | data seed/transaksi |
| 9–10 | Resize jendela + ringkas penutup | — |

Siapkan **cadangan**: rekaman layar demo dan PostgreSQL lokal (ganti `db.url`) bila internet bermasalah. Latihan minimal dua kali; waktu di hari demo adalah Minggu IV Nov.

### Format akhir laporan (Sel 17 Nov)
Setelah Bab 1 (Anggota 1) dan Bab 3 (Anggota 2) masuk: seragamkan font, ukuran, margin, spasi, indentasi, penomoran bab/gambar/tabel, daftar isi, dan kutipan istilah (nama kelas harus identik dengan kode).

---

## Definition of Done — Anggota 3

- [ ] `LoginFrame`, `MainFrame`, `VehiclePanel`, `CustomerPanel`, `RentalPanel`, `VehicleReturnPanel` (P0) berjalan; `ReportPanel` (P1) bila waktu cukup.
- [ ] Tidak ada logika bisnis atau SQL di `view`; semua aksi lewat `controller.method()`.
- [ ] Tidak ada layout `null`; tampilan rapi saat di-resize.
- [ ] Dialog konfirmasi hapus dan pesan error informatif muncul sesuai skenario.
- [ ] Pembatasan peran `STAFF` terlihat di UI.
- [ ] Skenario PRD §17 no. 1, 7, 8, 11, 12 lulus.
- [ ] Bab 2 selesai; format akhir seluruh laporan rapi.
- [ ] PPT 10–15 slide tersusun; skenario demo sudah dilatih.

## Jebakan Umum
- Menghitung total biaya di `RentalPanel` ("biar cepat") → melanggar MVC; panggil `controller.calculateTotal()`.
- Mengisi `JComboBox` dengan `String` → pakai objek model + `toString()` agar `getSelectedItem()` mengembalikan objek.
- Layout `null`/`setBounds` → rusak saat resize, dosen menandainya.
- Memanggil DAO langsung dari panel.
- Mengerjakan semua panel sekaligus tanpa menjalankan → bangun dan jalankan satu per satu.
- Screenshot laporan diambil dari versi lama yang berbeda dengan demo.
- Lupa membersihkan `char[]` password setelah dipakai.

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
