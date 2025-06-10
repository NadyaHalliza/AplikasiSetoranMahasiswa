package com.example.aplikasisetoranmahasiswa.model

import com.google.gson.annotations.SerializedName

data class DataContainer(

    @SerializedName("setoran") val setoran: SetoranData?
)