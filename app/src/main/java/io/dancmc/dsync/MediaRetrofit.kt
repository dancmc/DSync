package io.dancmc.dsync

import android.util.Log
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

class MediaRetrofit {

    companion object {
//        var domain = "dancmc.io"
        var domain = "192.168.1.47:8080"
        private var apiUrl = getApiUrl()

        private val httpclient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val originalRequest = chain.request()

                    val jwt = Prefs.instance!!.readString(Prefs.JWT,"")
                    val newRequest = originalRequest.newBuilder()
                            .addHeader("Authorization", jwt)
                            .build()

                    Log.d("API", "HTTP REQUEST :" + newRequest.url())
                    chain.proceed(newRequest)
                }.build()

        private var retrofit = getRetrofit()

        @JvmField
        var api = retrofit.create(PhotoApi::class.java)

        @JvmStatic
        fun rebuild(){
            apiUrl = getApiUrl()
            retrofit = getRetrofit()
        }

        fun getApiUrl():String{
            return "http://$domain/photobackup/v1/"
        }

        fun getRetrofit():Retrofit{
            return Retrofit.Builder()
                    .client(httpclient)
                    .baseUrl(apiUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
    }


    public interface PhotoApi {

        @POST("user/register")
        fun userRegister(@Body json: String): Call<String>

        @POST("user/login")
        fun userLogin(@Body json: String): Call<String>

        @GET("user/validate")
        fun validate(): Call<String>

        @GET("photo/complete")
        fun getComplete(@QueryMap queries:HashMap<String, String>): Call<String>

        @Multipart
        @POST("photo/upload")
        fun photoUpload(@Part("photo") file:RequestBody, @Part("json") json:RequestBody): Call<String>

    }
}