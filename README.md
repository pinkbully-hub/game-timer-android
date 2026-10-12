# Game Timer

Aplikasi Android sederhana berbahasa Indonesia untuk mengatur timer game sampai **9.999 jam**.

## Fitur

- Masukkan jam, menit, dan detik; batas durasi 9.999:59:59.
- Preset 1 jam, 24 jam, dan 1.000 jam.
- Kaitkan timer dengan game: isi nama secara manual atau pilih aplikasi terpasang untuk memakai nama dan ikonnya.
- Mulai, jeda, lanjutkan, dan reset.
- Waktu tetap akurat setelah aplikasi dibuka kembali karena sisa waktu disimpan sebagai batas waktu absolut.
- Tampilan gelap dengan indikator progres.

## Membuka proyek

1. Buka folder ini di Android Studio.
2. Tunggu Gradle menyinkronkan plugin dan dependensi.
3. Jalankan konfigurasi `app` pada emulator atau perangkat Android (Android 6.0/API 23 atau lebih baru).

Proyek memakai Kotlin dan Jetpack Compose. Jika ingin membuat APK dari terminal, jalankan `gradle assembleDebug` dari folder proyek setelah Gradle dan Android SDK tersedia.

