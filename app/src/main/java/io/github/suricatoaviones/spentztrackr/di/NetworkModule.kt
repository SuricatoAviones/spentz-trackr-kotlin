package io.github.suricatoaviones.spentztrackr.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.suricatoaviones.spentztrackr.data.remote.AuthInterceptor
import io.github.suricatoaviones.spentztrackr.data.remote.DynamicBaseUrlInterceptor
import io.github.suricatoaviones.spentztrackr.data.remote.SpentzApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        dynamicBaseUrl: DynamicBaseUrlInterceptor,
        auth: AuthInterceptor,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(dynamicBaseUrl)
            .addInterceptor(auth)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideSpentzApi(
        client: OkHttpClient,
        json: Json,
    ): SpentzApi =
        Retrofit
            .Builder()
            // Placeholder: DynamicBaseUrlInterceptor reescribe scheme/host/puerto
            // con la URL guardada en settings (docs/05).
            .baseUrl("http://spentz.invalid/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SpentzApi::class.java)
}
