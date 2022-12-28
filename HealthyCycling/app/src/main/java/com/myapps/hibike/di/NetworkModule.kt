package com.myapps.hibike.di

import com.myapps.hibike.data.network.AccessTokenInterface
import com.myapps.hibike.data.network.NotifMessageInterface
import com.myapps.hibike.utils.Constants.BASE_URL_ACCESS_TOKEN
import com.myapps.hibike.utils.Constants.BASE_URL_NOTIFICATION
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    companion object {
        private const val TIMEOUT: Long = 500000
    }

    @Singleton
    @Provides
    fun provideOkHttpInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.MICROSECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()

    @Singleton
    @Provides
    fun provideAccessTokenClientInstance(
        okHttpClient: OkHttpClient
    ): AccessTokenInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_ACCESS_TOKEN).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(AccessTokenInterface::class.java)
    }

    @Singleton
    @Provides
    fun provideNotificationClientInstance(
        okHttpClient: OkHttpClient
    ): NotifMessageInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_NOTIFICATION).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(NotifMessageInterface::class.java)
    }
}