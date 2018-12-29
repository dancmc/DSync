package io.dancmc.dsync

import android.util.Log
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

class MediaRetrofit {

    companion object {
//        var domain = "dancmc.io"
        var domain = "https://dancmc.host"
        private var apiUrl = getApiUrl()

        private val httpclient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
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
            api = retrofit.create(PhotoApi::class.java)
        }

        fun getApiUrl():String{
            return "$domain/photobackup/v1/"
        }

        fun getRetrofit():Retrofit{
            return Retrofit.Builder()
                    .client(httpclient)
                    .baseUrl(apiUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }

        fun getPhotoThumbUrl(uuid:String):String{
            return getApiUrl()+"static/photos?id=$uuid&size=thumb"
        }
    }


    public interface PhotoApi {

        @POST("user/register")
        fun userRegister(@Body json: String): Call<String>

        @Multipart
        @POST("photo/upload")
        fun photoUpload(@Part("photo") file:RequestBody, @Part("json") json:RequestBody): Call<String>

        @GET("photo/complete")
        fun getComplete(@QueryMap queries:HashMap<String, String>): Call<String>

        @GET("static/photos")
        fun downloadPhoto(@QueryMap queries:HashMap<String, String>): Call<ResponseBody>

        @POST("photo/delete")
        fun deletePhoto(@Body json: String): Call<String>

        @POST("photo/edit")
        fun editMetadata(@Body json: String): Call<String>

        @POST("user/login")
        fun userLogin(@Body json: String): Call<String>

        @GET("photo/metadata")
        fun getMetadata(@QueryMap queries:HashMap<String, String>): Call<String>

        @GET("user/validate")
        fun validate(): Call<String>


    }
}