package com.example.aplikasisetoranmahasiswa.network

import com.example.aplikasisetoranmahasiswa.model.SetoranSayaResponse
import com.example.aplikasisetoranmahasiswa.model.SuratStatus
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @GET("setoran-dev/v1/mahasiswa/kartu-murojaah-saya")
    suspend fun downloadKartuMurojaah(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @GET("setoran-dev/v1/mahasiswa/setoran-saya")
    suspend fun getStatusHafalan(
        @Header("Authorization") token: String
    ): Response<SetoranSayaResponse>

    @POST("setoran-dev/v1/mahasiswa/simpan-setoran")
    suspend fun simpanSetoran(
        @Header("Authorization") token: String,
        @Body daftar: List<SuratStatus>
    ): Response<ResponseBody>

}