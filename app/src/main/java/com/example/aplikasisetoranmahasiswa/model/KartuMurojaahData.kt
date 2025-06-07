package com.example.aplikasisetoranmahasiswa.model

class KartuMurojaahData {
    data class KartuMurojaahData(
        val nim: String,
        val nama: String,
        val penasehatAkademik: String,
        val daftarSetoran: List<SetoranItem>
    )

    data class SetoranItem(
        val tanggal: String,
        val surah: String,
        val ayat: String,
        val status: String
    )
}