package com.yalantis.ucrop

import androidx.annotation.NonNull
import okhttp3.OkHttpClient

class OkHttpClientStore private constructor() {
    companion object {
        @JvmField
        val INSTANCE = OkHttpClientStore()
    }

    @JvmField
    internal var client: OkHttpClient? = null

    /**
     * @return stored OkHttpClient if it was already set,
     *         or just an instance created via empty constructor
     *         and store it
     */
    @NonNull
    fun getClient(): OkHttpClient {
        if (client == null) {
            client = OkHttpClient()
        }
        return client!!
    }

    /**
     * @param client OkHttpClient for downloading bitmap form remote Uri,
     *               it may contain any preferences you need
     */
    internal fun setClient(@NonNull client: OkHttpClient) {
        this.client = client
    }
}

