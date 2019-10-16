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

package com.jnj.guppy.interceptor

import com.jnj.guppy.database.DatabaseHelper
import okhttp3.*
import okio.Buffer
import okio.GzipSource
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlin.text.Charsets.UTF_8

/**
 * Due to the nature of logging, this class is a replica of OkHttp's [okhttp3.logging.HttpLoggingInterceptor]
 * https://github.com/square/okhttp/blob/master/okhttp-logging-interceptor/src/main/java/okhttp3/logging/HttpLoggingInterceptor.kt
 *
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * [application interceptor][OkHttpClient.interceptors] or as a [OkHttpClient.networkInterceptors].
 *
 * Add an instance of this class to your OkHttp instance to enable logging with Guppy.
 */
class GuppyInterceptor
constructor(
        private val dbHelper: DatabaseHelper,
        val level: Level = Level.NONE,
        private val logger: Logger = Logger()
) : Interceptor {
    enum class Level {
        /** No logs. */
        NONE,
        /**
         * Logs request and response lines and their respective headers.
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * ```
         */
        HEADERS,

        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * ```
         */
        BODY
    }

    /**
     * This method logs the Request Headers and the Content Type/Length
     */
    private fun logRequestHeaders(requestBody: RequestBody?, request: Request) {
        // Request body headers are only present when installed as a network interceptor. Force
        // them to be included (when available) so there values are known.
        requestBody?.contentType()?.let {
            logger.logRequestContentType("Content-Type: $it")
        }
        if (requestBody?.contentLength() != -1L) {
            logger.logRequestContentLength("Content-Length: ${requestBody?.contentLength()}")
        }

        val headers = request.headers
        for (i in 0 until headers.size) {
            logHeader(0, headers, i)
        }
    }

    /**
     * This method logs the Body of the Request if it is set.
     */
    private fun logRequestBody(requestBody: RequestBody?, request: Request, shouldLogBody: Boolean) {
        if (!shouldLogBody || requestBody == null) {
            logger.logRequestBody("--> END ${request.method}")
        } else if (bodyHasUnknownEncoding(request.headers)) {
            logger.logRequestBody("--> END ${request.method} (encoded body omitted)")
        } else if (requestBody.isDuplex()) {
            logger.logRequestBody("--> END ${request.method} (duplex request body omitted)")
        } else {
            val buffer = Buffer()
            requestBody.writeTo(buffer)

            val contentType = requestBody.contentType()
            val charset: Charset = contentType?.charset(UTF_8) ?: UTF_8

            if (charset == UTF_8) {
                logger.logRequestBody("${buffer.clone().readString(charset)} --> END ${request.method} (${requestBody.contentLength()}-byte body)")
            } else {
                logger.logRequestBody(
                        "--> END ${request.method} (binary ${requestBody.contentLength()}-byte body omitted)"
                )
            }
        }
    }

    /**
     * This method logs the Response Headers.
     */
    private fun logResponseHeaders(response: Response): Headers {
        val headers = response.headers
        for (i in 0 until headers.size) {
            logHeader(1, headers, i)
        }
        return headers
    }

    /**
     * This method logs the response result
     */
    private fun logResponseResult(
            contentLength: Long?,
            shouldLogHeaders: Boolean,
            response: Response
    ) {
        val startNs = System.nanoTime()
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        logger.logResponseResult(
                "<-- ${response.code}${if (response.message.isEmpty()) "" else ' ' + response.message} " +
                        "${response.request.url} (${tookMs}ms${if (!shouldLogHeaders) ", $bodySize body" else ""})"
        )
    }

    /**
     * This method will properly log the Response Body and Metadata.
     */
    private fun logResponseBody(
            shouldLogBody: Boolean, response: Response, contentLength: Long?, headers: Headers
    ): Response {
        if (!shouldLogBody || response.body == null) {
            logger.logResponseBody("<-- END HTTP")
        } else if (bodyHasUnknownEncoding(response.headers)) {
            logger.logResponseBody("<-- END HTTP (encoded body omitted)")
        } else {
            logResponseMetaData(response, headers, contentLength)
        }
        dbHelper.saveGuppyData(logger.interceptedData)
        return response
    }

    /**
     * This method will log the Response Body, Content Type, and Content Length if they're set.
     */
    private fun logResponseMetaData(response: Response, headers: Headers, contentLength: Long?) {
        val source = response.body?.source()
        source?.request(Long.MAX_VALUE) // Buffer the entire body.
        var buffer = source?.buffer

        var gzippedLength: Long? = null
        if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
            gzippedLength = buffer?.size
            if (buffer == null) {
                buffer = Buffer()
            }
            GzipSource(buffer.clone()).use { gzippedResponseBody ->
                buffer = Buffer()
                buffer?.writeAll(gzippedResponseBody)
            }
        }

        val contentType = response.body?.contentType()
        val charset: Charset = contentType?.charset(UTF_8) ?: UTF_8

        if (gzippedLength != null) {
            logger.logResponseBody("${buffer?.clone()?.readString(charset)} <-- END HTTP (${buffer?.size}-byte, $gzippedLength-gzipped-byte body)")
        } else {
            logger.logResponseBody("<-- END HTTP (${buffer?.size}-byte body)")
        }

        if (charset != UTF_8) {
            logger.logResponseContentType("<-- END HTTP (binary ${buffer?.size}-byte body omitted)")
        }

        if (contentLength != 0L) {
            logger.logResponseContentLength(buffer?.clone()?.readString(charset))
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val level = this.level
        logger.interceptedData = Logger.InterceptedData()
        val request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        val shouldLogBody = level == Level.BODY
        val shouldLogHeaders = shouldLogBody || level == Level.HEADERS

        val requestBody = request.body

        val connection = chain.connection()
        var requestStartMessage =
                ("--> ${request.method} ${request.url}${if (connection != null) " " + connection.protocol() else ""}")
        if (!shouldLogHeaders && requestBody != null) {
            requestStartMessage += " (${requestBody.contentLength()}-byte body)"
        }
        logger.logHost(request.url.toString())
        logger.logRequestType(requestStartMessage)

        if (shouldLogHeaders) {
            logRequestHeaders(requestBody, request)
            logRequestBody(requestBody, request, shouldLogBody)
        }

        val response: Response
        try {
            response = chain.proceed(request)
            logger.logStatus(response.code)
        } catch (e: Exception) {
            logger.logResponseResult("<-- HTTP FAILED: $e")
            throw e
        }

        val responseBody = response.body
        val contentLength = responseBody?.contentLength()
        logResponseResult(contentLength, shouldLogHeaders, response)

        if (shouldLogHeaders) {
            val headers = logResponseHeaders(response)
            return logResponseBody(shouldLogBody, response, contentLength, headers)
        }
        dbHelper.saveGuppyData(logger.interceptedData)
        return response
    }

    private fun logHeader(reqRes: Int, headers: Headers, i: Int) {
        val value = headers.value(i)
        if (reqRes == 0) {
            logger.logRequestHeaders(headers.name(i) + ": " + value)
        } else {
            logger.logResponseHeaders(headers.name(i) + ": " + value)
        }
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }
}