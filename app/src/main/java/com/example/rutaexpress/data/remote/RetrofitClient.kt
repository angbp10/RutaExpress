package com.example.rutaexpress.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente singleton de Retrofit configurado con la URL base de json.pe.
 */
object RetrofitClient {
    private const val BASE_URL = "https://api.json.pe/"

    val dniApiService: DniApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DniApiService::class.java)
    }
}
