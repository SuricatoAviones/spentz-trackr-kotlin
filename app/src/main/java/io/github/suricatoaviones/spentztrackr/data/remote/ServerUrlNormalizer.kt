package io.github.suricatoaviones.spentztrackr.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Normaliza la URL que escribe el usuario en el onboarding a la forma
 * canónica `scheme://host[:puerto]` (sin `/api/v1`: el cliente la compone).
 *
 * Reglas (docs/04, rama remota):
 * - sin esquema → se asume `https://`;
 * - se descarta cualquier ruta pegada del navegador (`/public`, `/api/v1`, …)
 *   porque el API vive colgado de la raíz del dominio;
 * - `http` se permite (ADR-A11) y la UI advierte que va sin cifrar.
 */
object ServerUrlNormalizer {
    fun normalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val withScheme =
            if (trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true)
            ) {
                trimmed
            } else {
                "https://$trimmed"
            }

        val url = withScheme.toHttpUrlOrNull() ?: return null
        if (url.host.isBlank()) return null

        return url
            .newBuilder()
            .encodedPath("/")
            .query(null)
            .fragment(null)
            .build()
            .toString()
            .removeSuffix("/")
    }
}
