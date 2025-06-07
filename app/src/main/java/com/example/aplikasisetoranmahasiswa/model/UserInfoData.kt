package com.example.aplikasisetoranmahasiswa.model

import com.google.gson.annotations.SerializedName

data class UserInfoData(
    @SerializedName("email_verified")
    val emailVerified: Boolean,

    @SerializedName("name")
    val name: String,

    @SerializedName("preferred_username")
    val preferredUsername: String,

    @SerializedName("given_name")
    val givenName: String,

    @SerializedName("family_name")
    val familyName: String,

    @SerializedName("email")
    val email: String
)