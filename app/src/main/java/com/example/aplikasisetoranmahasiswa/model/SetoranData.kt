package com.example.aplikasisetoranmahasiswa.model

// Data class untuk item setoran di dalam request/response
data class SetoranData(
    val id: String? = null, // Untuk delete request, bisa null jika baru
    val id_komponen_setoran: String,
    val nama_komponen_setoran: String
)