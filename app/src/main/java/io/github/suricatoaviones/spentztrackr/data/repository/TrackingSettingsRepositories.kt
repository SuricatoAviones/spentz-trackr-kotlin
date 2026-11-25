package io.github.suricatoaviones.spentztrackr.data.repository

import io.github.suricatoaviones.spentztrackr.data.local.SpentzDatabase
import io.github.suricatoaviones.spentztrackr.data.remote.SpentzApi
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.di.LocalSource
import io.github.suricatoaviones.spentztrackr.di.RemoteSource
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.repository.TrackingMode
import io.github.suricatoaviones.spentztrackr.domain.repository.TrackingSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tipo de seguimiento — local/remoto/delegador.
 * Local: `user_prefs.tracking_type`; remoto: `auth/me` (UserDto.trackingType).
 */
@Singleton
class LocalTrackingSettingsRepository
    @Inject
    constructor(
        private val database: SpentzDatabase,
    ) : TrackingSettingsRepository {
        override suspend fun trackingMode(): TrackingMode = TrackingMode.fromValue(database.userPrefsDao().find()?.trackingType)
    }

@Singleton
class RemoteTrackingSettingsRepository
    @Inject
    constructor(
        private val api: SpentzApi,
    ) : TrackingSettingsRepository {
        override suspend fun trackingMode(): TrackingMode = TrackingMode.fromValue(api.me().data?.trackingType)
    }

@Singleton
class DelegatingTrackingSettingsRepository
    @Inject
    constructor(
        @LocalSource private val local: TrackingSettingsRepository,
        @RemoteSource private val remote: TrackingSettingsRepository,
        private val settings: SettingsStore,
    ) : TrackingSettingsRepository {
        override suspend fun trackingMode(): TrackingMode =
            if (settings.snapshot().mode == AppMode.REMOTE) remote.trackingMode() else local.trackingMode()
    }
