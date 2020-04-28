package io.bluetrace.opentrace.api

import io.bluetrace.opentrace.models.Data
import retrofit2.http.*
import io.reactivex.Observable;


interface TracerApi {
    
    @POST("user-data/")
    fun sendData(@Body data: Data): Observable<String> // body data
}