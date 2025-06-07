package com.example.aplikasisetoranmahasiswa.model

import com.google.gson.annotations.SerializedName

// Data class untuk response dari endpoint login Keycloak
data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("id_token") val idToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("refresh_expires_in") val refreshExpiresIn: Int,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("session_state") val sessionState: String,
    val scope: String
)