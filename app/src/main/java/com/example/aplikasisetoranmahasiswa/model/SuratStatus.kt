package com.example.aplikasisetoranmahasiswa.model

import com.google.gson.annotations.SerializedName

data class SuratStatus(
    @SerializedName("id") val id: String?,
    @SerializedName("nama") val nama: String?,
    @SerializedName("label") val label: String?,
    @SerializedName("sudah_setor") val sudahSetor: Boolean
)