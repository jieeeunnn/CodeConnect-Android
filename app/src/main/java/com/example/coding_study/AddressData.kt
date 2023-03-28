package com.example.coding_study
/*
import com.beust.klaxon.*
import com.squareup.moshi.Json

private val klaxon = Klaxon()

data class Welcome7 (
    val response: Response
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Welcome7>(json)
    }
}

data class Response (
    val service: Service,
    val status: String,
    val record: Record,
    val page: Page,
    val result: Result
)

data class Page (
    val total: String,
    val current: String,
    val size: String
)

data class Record (
    val total: String,
    val current: String
)

data class Result (
    val featureCollection: FeatureCollection
)

data class FeatureCollection (
    val type: String,
    val bbox: List<Double>,
    val features: List<Feature>
)

data class Feature (
    val type: String,
    val properties: Properties,
    val id: String
)

data class Properties (
    @Json(name = "full_nm")
    val fullNm: String,

    @Json(name = "emd_kor_nm")
    val emdKorNm: String,

    @Json(name = "emd_eng_nm")
    val emdEngNm: String,

    @Json(name = "emd_cd")
    val emdCD: String
)

data class Service (
    val name: String,
    val version: String,
    val operation: String,
    val time: String
)


 */