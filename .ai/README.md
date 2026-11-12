# AI Project Context

Folder ini menyimpan konteks proyek RentGo yang perlu dibaca AI agent sebelum melakukan analisis, desain, atau implementasi.

## Struktur

```text
.ai/
├── README.md
├── DEVELOPMENT-REFERENCE.md      referensi implementasi kode
├── prd/
│   ├── PRD.md                    sumber kebutuhan utama
│   ├── DEV-GUIDE.md              panduan eksekusi terpadu (utama)
│   ├── DEV-GUIDE-1-MODEL-DB.md   arsip panduan Anggota 1
│   ├── DEV-GUIDE-2-DAO-CONTROLLER.md arsip panduan Anggota 2
│   └── DEV-GUIDE-3-VIEW.md       arsip panduan Anggota 3
└── architecture/
    └── ARCHITECTURE.md
```

## Cara Menggunakan

- `prd/PRD.md` adalah sumber kebutuhan produk utama: masalah, tujuan, scope, user story, requirement, kontrak teknis, dan acceptance criteria.
- `../.github/copilot-instructions.md` berisi instruksi ringkas yang mengarahkan agent ke PRD dan aturan inti proyek.
- `architecture/ARCHITECTURE.md` adalah sumber keputusan teknis: komponen, alur data, kontrak, keamanan, deployment, dan keputusan arsitektur.
- `prd/DEV-GUIDE.md` adalah panduan eksekusi utama: urutan milestone, dependency, gate, dan acceptance. Tiga `DEV-GUIDE-*.md` lain disimpan sebagai arsip pembagian tugas per anggota.
- `DEVELOPMENT-REFERENCE.md` berisi contoh implementasi dan checklist; sumber kebenaran tetap PRD.
- Isi bagian `Open Questions` sebelum menganggap dokumen lengkap.
- Gunakan heading yang stabil dan tabel ringkas agar mudah dicari oleh agent.
- Setelah perubahan penting, perbarui `Last Updated` dan `Changelog`.

## Status Dokumen

| Dokumen | Status | Pemilik | Terakhir diperbarui |
|---|---|---|---|
| PRD | Sumber kebenaran | Tim RentGo | 2026-11-10 |
| Arsitektur | Draft | Tim RentGo | 2026-11-10 |
| DEV-GUIDE | Panduan eksekusi utama | Tim RentGo | 2026-11-12 |
| DEVELOPMENT-REFERENCE | Referensi implementasi | Tim RentGo | 2026-11-12 |
