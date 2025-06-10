package com.example.aplikasisetoranmahasiswa.network

import com.example.aplikasisetoranmahasiswa.model.LoginResponse
import com.example.aplikasisetoranmahasiswa.model.UserInfoData
import com.example.aplikasisetoranmahasiswa.model.SuratStatus
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AuthService {
    @FormUrlEncoded
    @POST("realms/dev/protocol/openid-connect/token")
    suspend fun login(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String
    ): Response<LoginResponse>

    @GET("realms/dev/protocol/openid-connect/userinfo")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): Response<UserInfoData>
}