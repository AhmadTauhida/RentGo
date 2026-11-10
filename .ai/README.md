# AI Project Context

Folder ini menyimpan konteks proyek RentGo yang perlu dibaca AI agent sebelum melakukan analisis, desain, atau implementasi.

## Struktur

```text
.ai/
├── README.md
├── prd/
│   └── PRD.md
└── architecture/
    └── ARCHITECTURE.md
```

## Cara Menggunakan

- `../PRD.md` adalah sumber kebutuhan produk utama: masalah, tujuan, scope, user story, requirement, kontrak teknis, dan acceptance criteria.
- `../.github/copilot-instructions.md` berisi instruksi ringkas yang mengarahkan agent ke PRD dan aturan inti proyek.
- `architecture/ARCHITECTURE.md` adalah sumber keputusan teknis: komponen, alur data, kontrak, keamanan, deployment, dan keputusan arsitektur.
- Isi bagian `Open Questions` sebelum menganggap dokumen lengkap.
- Gunakan heading yang stabil dan tabel ringkas agar mudah dicari oleh agent.
- Setelah perubahan penting, perbarui `Last Updated` dan `Changelog`.

## Status Dokumen

| Dokumen | Status | Pemilik | Terakhir diperbarui |
|---|---|---|---|
| PRD | Sumber kebenaran | Tim RentGo | 2026-11-10 |
| Arsitektur | Draft | Tim RentGo | 2026-11-10 |
