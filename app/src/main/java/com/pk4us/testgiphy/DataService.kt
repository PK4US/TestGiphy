package com.pk4us.testgiphy

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DataService {
    @GET("gifs/trending?api_key=Ke8WRRGTENw3XCtrnYgFqyct2xEK6NEP")
    fun getGifs(): Call<DataResult>

    @GET("gifs/search?api_key=Ke8WRRGTENw3XCtrnYgFqyct2xEK6NEP")
    fun searchGifs(@Query("q") query: String): Call<DataResult>
}