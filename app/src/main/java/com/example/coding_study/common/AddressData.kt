package com.example.coding_study.common

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Welcome7 (
    val response: Response<Any?>
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Welcome7>(json)
    }
}

data class Response<T>(
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
    val full_nm: String,
    val emd_kor_nm: String,
    val emd_eng_nm: String,
    val emd_cd: String
)

data class Service (
    val name: String,
    val version: String,
    val operation: String,
    val time: String
)


