/**
 * Copyright © 2019 Johnson & Johnson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package com.jnj.guppysample

import android.content.Context
import com.google.gson.GsonBuilder
import com.jnj.guppy.database.DatabaseHelper
import com.jnj.guppy.interceptor.GuppyInterceptor
import com.jnj.guppy.interceptor.Logger
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIClient(context: Context) {

    private val client: OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(
                    GuppyInterceptor(
                            DatabaseHelper(context),
                            GuppyInterceptor.Level.BODY,
                            Logger()
                    )
            ).build()

    private val service: BackendService = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .callFactory(client)
            .build().create(BackendService::class.java)

    fun getService(): BackendService {
        return service
    }
}