package com.example.aplikasisetoranmahasiswa.model

// Generic data class untuk struktur response umum dari API
// T adalah placeholder untuk tipe data di dalam field 'data'
data class ApiResponse<T>(
    val response: Boolean,
    val message: String,
    val data: T? = null // Data bisa berupa tipe apapun, tergantung endpoint. Bisa null jika tidak ada data.
)