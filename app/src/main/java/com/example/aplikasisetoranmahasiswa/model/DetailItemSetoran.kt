package com.example.aplikasisetoranmahasiswa.model

data class DetailItemSetoran(
    val id: String,
    val nama: String?,
    val label: String?,
    val sudahSetor: Boolean,
    val infoSetoran: String?
)