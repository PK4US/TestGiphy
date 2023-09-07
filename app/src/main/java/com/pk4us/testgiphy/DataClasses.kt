package com.pk4us.testgiphy

import com.google.gson.annotations.SerializedName

data class DataResult(
    @SerializedName("data") val res: List<DataObject>
)

data class DataObject(
    @SerializedName("images") val images: DataImage
)

data class DataImage(
    @SerializedName("original") val ogImage: OgImage
)

data class OgImage(
    val url: String
)