package com.example.aplikasisetoranmahasiswa.network
// ==============================================================
// Tahap 5: Import semua model data class yang akan digunakan
import com.example.aplikasisetoranmahasiswa.model.*
import okhttp3.ResponseBody
// ==============================================================

import retrofit2.Response
import retrofit2.http.*

// Interface yang mendefinisikan semua endpoint API
interface ApiService {

    // Endpoint untuk mendapatkan data setoran mahasiswa (GET)
    @GET("/setoran-dev/v1/mahasiswa/setoran/{nim}")
    suspend fun getSetoranMahasiswa(
        @Path("nim") nim: String,
        @Query("apikey") apiKey: String,
        @Header("Authorization") authToken: String
    ): Response<ApiResponse<SetoranMahasiswaData>> // Response dengan data SetoranMahasiswaData

    // Endpoint untuk mendapatkan data setoran mahasiswa tanpa API key tapi dengan Authorization (GET)
    // Berdasarkan error 401: endpoint ini memerlukan JWT token tapi tidak perlu API key
    @GET("/setoran-dev/v1/mahasiswa/setoran/{nim}")
    suspend fun getSetoranMahasiswaTanpaApiKey(
        @Path("nim") nim: String,
        @Header("Authorization") authToken: String
    ): Response<ApiResponse<SetoranMahasiswaData>> // Response dengan data SetoranMahasiswaData

    // Endpoint untuk download data setoran (kartu murojaah saya)
    @GET("/setoran-dev/v1/mahasiswa/kartu-murojaah-saya")
    @Streaming
    suspend fun downloadKartuMurojaah(
        @Query("apikey") apiKey: String,
        @Header("Authorization") authToken: String
    ): Response<ResponseBody> // Ganti KartuMurojaahData dengan data class yang sesuai

    // Endpoint untuk mendapatkan data setoran mahasiswa tanpa parameter sama sekali (GET)
    // Untuk debugging - coba tanpa authentication sama sekali
    @GET("/setoran-dev/v1/mahasiswa/setoran/{nim}")
    suspend fun getSetoranMahasiswaNoAuth(
        @Path("nim") nim: String
    ): Response<ApiResponse<SetoranMahasiswaData>> // Response dengan data SetoranMahasiswaData

    // Endpoint untuk mendapatkan daftar mahasiswa PA (GET, untuk role dosen)
    @GET("/setoran-dev/v1/dosen/pa-saya")
    suspend fun getMahasiswaPA(
        @Query("apikey") apiKey: String,
        @Header("Authorization") authToken: String
    ): Response<ApiResponse<Any>> // Gunakan Any jika struktur datanya belum jelas atau tidak digunakan

    // --- Endpoint Autentikasi Keycloak (KC_URL) ---

    // Endpoint untuk login (POST dengan FormUrlEncoded)
    @FormUrlEncoded // Gunakan ini karena body request adalah form-urlencoded
    @POST("realms/dev/protocol/openid-connect/token") // Perhatikan: ini menggunakan BASE_URL_KC, bukan BASE_URL_API
    suspend fun login(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String
    ): Response<LoginResponse> // Response berupa data class LoginResponse

    // Endpoint untuk logout (POST dengan FormUrlEncoded)
    @FormUrlEncoded
    @POST("realms/dev/protocol/openid-connect/logout")
    suspend fun logout(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("id_token_hint") idTokenHint: String
    ): Response<Void> // Response kosong untuk logout

    // Endpoint untuk refresh token (POST dengan FormUrlEncoded)
    @FormUrlEncoded
    @POST("realms/dev/protocol/openid-connect/token") // Perhatikan: ini menggunakan BASE_URL_KC
    suspend fun refreshToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): Response<LoginResponse> // Response berupa LoginResponse baru

    // Endpoint untuk mendapatkan info user (GET)
    @GET("realms/dev/protocol/openid-connect/userinfo") // Perhatikan: ini menggunakan BASE_URL_KC
    suspend fun getUserInfo(
        @Header("Authorization") authToken: String // Membutuhkan Bearer Token
    ): Response<UserInfoData> // Response berupa data class UserInfoData

    // --- Endpoint Lainnya (BASE_URL_API) ---

    // Endpoint untuk cek status API (GET)
    @GET("/setoran-dev/v1/") // Perhatikan path root BASE_URL
    suspend fun getStatus(): Response<ApiResponse<Any>> // Sesuaikan jika ada data spesifik di response status

    // Endpoint untuk cek health API (GET)
    @GET("/setoran-dev/v1/health")
    suspend fun getHealth(): Response<ApiResponse<Any>> // Sesuaikan jika ada data spesifik di response health
}