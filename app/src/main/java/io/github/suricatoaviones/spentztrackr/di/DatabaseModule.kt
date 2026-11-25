package io.github.suricatoaviones.spentztrackr.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.suricatoaviones.spentztrackr.data.local.SpentzDatabase
import io.github.suricatoaviones.spentztrackr.data.local.dao.CategoryDao
import io.github.suricatoaviones.spentztrackr.data.local.dao.PaymentSourceDao
import io.github.suricatoaviones.spentztrackr.data.local.dao.UserPrefsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): SpentzDatabase =
        Room
            .databaseBuilder(context, SpentzDatabase::class.java, "spentz.db")
            // Pre-v1.0.0 (sin usuarios reales) las migraciones son destructivas
            // (nota en SpentzDatabase); a partir del primer release se
            // escriben migraciones reales.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideUserPrefsDao(database: SpentzDatabase): UserPrefsDao = database.userPrefsDao()

    @Provides
    fun provideCategoryDao(database: SpentzDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun providePaymentSourceDao(database: SpentzDatabase): PaymentSourceDao = database.paymentSourceDao()
}
