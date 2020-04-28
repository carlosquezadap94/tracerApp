package io.bluetrace.opentrace.api

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object ApiService {
    private val TAG = "--ApiService"

    fun apiCall() = Retrofit.Builder()
        .baseUrl("https://rc0bjr9m6l.execute-api.us-east-1.amazonaws.com/PROD/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ApiWorker.gsonConverter)
        .client(ApiWorker.client)
        .build()
        .create(TracerApi::class.java)
}