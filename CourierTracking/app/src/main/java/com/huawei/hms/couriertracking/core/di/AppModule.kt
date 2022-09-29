package com.huawei.hms.couriertracking.core.di

import android.content.Context
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.couriertracking.core.utils.Constants.MAP_ROUTE_URL
import com.huawei.hms.couriertracking.data.network.CloudDBDatasource
import com.huawei.hms.couriertracking.data.network.NetworkDatasource
import com.huawei.hms.couriertracking.data.network.cloud.CloudDBDatasourceImpl
import com.huawei.hms.couriertracking.data.network.retrofit.NetworkDatasourceImpl
import com.huawei.hms.couriertracking.data.network.retrofit.RetrofitCourierTrackingService
import com.huawei.hms.couriertracking.data.repository.OrderRepositoryImpl
import com.huawei.hms.couriertracking.domain.repository.OrderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("APP_ID")
    fun provideAppID(
        @ApplicationContext context: Context
    ):String {
        return AGConnectServicesConfig.fromContext(context)
            .getString("client/app_id")
    }

    @Provides
    @Singleton
    @Named("API_KEY")
    fun provideApiKey(
        @ApplicationContext context: Context
    ):String {
        return AGConnectServicesConfig.fromContext(context)
            .getString("client/api_key")
    }

    @Provides
    @Singleton
    fun provideCloudDBDatasource(
        @ApplicationContext context: Context
    ): CloudDBDatasource {
        return CloudDBDatasourceImpl(context)
    }

    @Provides
    @Singleton
    fun provideNetworkDatasource(
        apiService: RetrofitCourierTrackingService,
        @Named("API_KEY") apiKey:String
    ): NetworkDatasource{
        return NetworkDatasourceImpl(apiService,apiKey)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        cloudDBDatasource: CloudDBDatasource,
        networkDatasource: NetworkDatasource
    ): OrderRepository {
        return OrderRepositoryImpl(
            cloudDBDatasource,
            networkDatasource
        )
    }

    @Provides
    fun provideLoggingInterceptor() =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    @Provides
    fun provideOkHttpClient(logger: HttpLoggingInterceptor): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
        okHttpClient.addInterceptor(logger)
        return okHttpClient.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(MAP_ROUTE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit) =
        retrofit.create(RetrofitCourierTrackingService::class.java)
}