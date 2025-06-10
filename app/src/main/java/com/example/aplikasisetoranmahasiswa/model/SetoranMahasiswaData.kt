package com.example.aplikasisetoranmahasiswa.model

import com.google.gson.annotations.SerializedName

// Data class untuk response dari endpoint GET /mahasiswa/setoran/:nim
data class SetoranMahasiswaData(
    val info: InfoMahasiswa,
    val setoran: DetailSetoran
    // ... tambahkan field lain jika ada di response JSON
)

data class InfoMahasiswa(
    val nama: String,
    val nim: String,
    val email: String,
    val angkatan: String,
    val semester: Int,
    @SerializedName("dosen_pa") val dosenPa: DosenPA // Menggunakan SerializedName jika nama JSON berbeda
)

data class DosenPA(
    val nip: String,
    val nama: String,
    val email: String
)

data class DetailSetoran(
    val log: List<LogSetoran>,
    @SerializedName("info_dasar") val infoDasar: InfoDasarSetoran,
    val ringkasan: List<RingkasanSetoran>,
    val detail: List<DetailItemSetoran>
)

data class LogSetoran(
    val id: Int,
    val keterangan: String,
    val aksi: String,
    val ip: String,
    @SerializedName("user_agent") val userAgent: String,
    val timestamp: String,
    val nim: String,
    @SerializedName("dosen_yang_mengesahkan") val dosenYangMengesahkan: DosenMengesahkan? // Bisa null
)

data class InfoDasarSetoran(
    @SerializedName("total_wajib_setor") val totalWajibSetor: Int,
    @SerializedName("total_sudah_setor") val totalSudahSetor: Int,
    @SerializedName("total_belum_setor") val totalBelumSetor: Int,
    @SerializedName("persentase_progres_setor") val persentaseProgresSetor: Double,
    @SerializedName("tgl_terakhir_setor") val tglTerakhirSetor: String?, // Bisa null
    @SerializedName("terakhir_setor") val terakhirSetor: String
)

data class RingkasanSetoran(
    val label: String,
    @SerializedName("total_wajib_setor") val totalWajibSetor: Int,
    @SerializedName("total_sudah_setor") val totalSudahSetor: Int,
    @SerializedName("total_belum_setor") val totalBelumSetor: Int,
    @SerializedName("persentase_progres_setor") val persentaseProgresSetor: Double
)

data class InfoSetoran(
    val id: String,
    val tgl_setoran: String,
    val tgl_validasi: String,
    @SerializedName("dosen_yang_mengesahkan") val dosenYangMengesahkan: DosenMengesahkan
)

data class DosenMengesahkan(
    val nip: String,
    val nama: String,
    val email: String
)
