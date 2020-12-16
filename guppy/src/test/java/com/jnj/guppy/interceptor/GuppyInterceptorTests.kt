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

import com.google.gson.GsonBuilder
import com.jnj.guppy.database.DatabaseHelper
import junit.framework.TestCase
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.internal.TlsUtil.localhost
import okio.Buffer
import okio.ByteString.Companion.decodeBase64
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * This test strategy is pulled from the OkHTTP tests for HttpLoggingInterceptor
 * https://github.com/square/okhttp/blob/master/okhttp-logging-interceptor/src/test/java/okhttp3/logging/HttpLoggingInterceptorTest.java
 */
@RunWith(MockitoJUnitRunner::class)
class GuppyInterceptorTests : TestCase() {

    private val PLAIN = "text/plain; charset=utf-8".toMediaType()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private val server = MockWebServer()
    private val handshakeCertificates = localhost()
    private val logger: Logger = Logger()

    private lateinit var client: OkHttpClient
    private lateinit var host: String
    private lateinit var url: HttpUrl

    private lateinit var guppyInterceptor: GuppyInterceptor

    @Mock
    private lateinit var mockDatabaseHelper: DatabaseHelper


    private fun request(): Request.Builder {
        return Request.Builder().url(url)
    }

    @Before
    public override fun setUp() {
        guppyInterceptor = GuppyInterceptor(mockDatabaseHelper, GuppyInterceptor.Level.BODY, logger)
        client = OkHttpClient.Builder()
                .addNetworkInterceptor(guppyInterceptor)
                .sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager
                )
                .build()

        host = server.hostName + ":" + server.port
        url = server.url("/")
    }

    @Test
    fun testNoLogging() {
        guppyInterceptor = GuppyInterceptor(mockDatabaseHelper, GuppyInterceptor.Level.NONE, logger)
        client = OkHttpClient.Builder()
                .addNetworkInterceptor(guppyInterceptor)
                .sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager
                )
                .build()
        host = server.hostName + ":" + server.port
        url = server.url("/")

        server.enqueue(MockResponse())
        val request = request().post("Hi!".toRequestBody(PLAIN)).build()
        val response = client.newCall(request).execute()
        response.body?.close()

        assertEquals(ArrayList<String>(), logger.interceptedData.requestHeaders)
        assertEquals(ArrayList<String>(), logger.interceptedData.responseHeaders)

        assertNull(logger.interceptedData.host)

        assertNull(logger.interceptedData.requestBody)
        assertNull(logger.interceptedData.requestContentLength)
        assertNull(logger.interceptedData.requestContentType)
        assertNull(logger.interceptedData.requestType)

        assertNull(logger.interceptedData.responseBody)
        assertNull(logger.interceptedData.responseContentLength)
        assertNull(logger.interceptedData.responseContentType)
        assertNull(logger.interceptedData.responseResult)
    }

    @Test
    fun testHeaderLevel() {
        guppyInterceptor = GuppyInterceptor(mockDatabaseHelper, GuppyInterceptor.Level.HEADERS, logger)
        client = OkHttpClient.Builder()
                .addNetworkInterceptor(guppyInterceptor)
                .sslSocketFactory(
                        handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager
                )
                .build()
        host = server.hostName + ":" + server.port
        url = server.url("/")

        server.enqueue(MockResponse())
        val request = request().post("Hi!".toRequestBody(PLAIN)).build()
        val response = client.newCall(request).execute()
        response.body?.close()

        assertEquals(
            listOf(
                "Content-Type: text/plain; charset=utf-8",
                "Content-Length: 3",
                "Host: $host",
                "Connection: Keep-Alive",
                "Accept-Encoding: gzip",
                "User-Agent: okhttp/4.9.0"
            ), logger.interceptedData.requestHeaders
        )

        assertEquals(
                listOf("Content-Length: 0"), logger.interceptedData.responseHeaders
        )

        assertEquals("--> END POST", logger.interceptedData.requestBody)
        assertEquals("Content-Length: 3", logger.interceptedData.requestContentLength)
        assertEquals("Content-Type: text/plain; charset=utf-8", logger.interceptedData.requestContentType)
        assertEquals("--> POST http://$host/ http/1.1", logger.interceptedData.requestType)

        assertEquals("<-- END HTTP", logger.interceptedData.responseBody)
        assertNull(logger.interceptedData.responseContentLength)
        assertNull(logger.interceptedData.responseContentType)
        assertEquals(
                "<-- 200 OK http://$host/", logger.interceptedData.responseResult
                ?.subSequence(
                        0,
                        logger.interceptedData.responseResult?.indexOf("(")!! - 1
                )
        )
    }

    @Test
    fun testLogLevel() {
        assertEquals(GuppyInterceptor.Level.BODY, guppyInterceptor.level)
    }

    @Test
    fun testPostBody() {
        server.enqueue(MockResponse())
        val request = request().post("Hi!".toRequestBody(PLAIN)).build()
        val response = client.newCall(request).execute()
        response.body?.close()

        assertEquals(
                listOf(
                        "Content-Type: text/plain; charset=utf-8",
                        "Content-Length: 3",
                        "Host: $host",
                        "Connection: Keep-Alive",
                        "Accept-Encoding: gzip",
                    "User-Agent: okhttp/4.9.0"
                ), logger.interceptedData.requestHeaders
        )

        assertEquals(
                listOf("Content-Length: 0"), logger.interceptedData.responseHeaders
        )

        assertEquals("Hi! --> END POST (3-byte body)", logger.interceptedData.requestBody)
        assertEquals("Content-Length: 3", logger.interceptedData.requestContentLength)
        assertEquals("Content-Type: text/plain; charset=utf-8", logger.interceptedData.requestContentType)
        assertEquals("--> POST http://$host/ http/1.1", logger.interceptedData.requestType)

        assertEquals("<-- END HTTP (0-byte body)", logger.interceptedData.responseBody)
        assertNull(logger.interceptedData.responseContentLength)
        assertNull(logger.interceptedData.responseContentType)
        assertNotNull(logger.interceptedData.timestamp)
        assertEquals(200, logger.interceptedData.statusCode)
        assertEquals("200 OK", logger.interceptedData.statusMessage)
        assertEquals(
                "<-- 200 OK http://$host/", logger.interceptedData.responseResult
                ?.subSequence(
                        0,
                        logger.interceptedData.responseResult?.indexOf("(")!! - 1
                )
        )
    }

    @Test
    fun testNonNullHeadersAndNullBody() {
        server.enqueue(
                MockResponse()
                        // It's invalid to return this if not requested, but the server might anyway
                        .setHeader("Content-Encoding", "br")
                        .setHeader("Content-Type", PLAIN)
                        .setBody(
                                Buffer().write(
                                        "iwmASGVsbG8sIEhlbGxvLCBIZWxsbwoD".decodeBase64()!!
                                )
                        )
        )
        val response = client.newCall(request().build()).execute()
        response.body?.close()

        assertEquals(
                listOf(
                        "Host: $host",
                        "Connection: Keep-Alive",
                        "Accept-Encoding: gzip",
                    "User-Agent: okhttp/4.9.0"
                ), logger.interceptedData.requestHeaders
        )

        assertEquals(
                listOf(
                        "Content-Encoding: br",
                        "Content-Type: $PLAIN",
                        "Content-Length: 24"
                ), logger.interceptedData.responseHeaders
        )

        assertEquals("--> END GET", logger.interceptedData.requestBody)
        assertEquals("Content-Length: null", logger.interceptedData.requestContentLength)
        assertNull(logger.interceptedData.requestContentType)
        assertEquals("--> GET http://$host/ http/1.1", logger.interceptedData.requestType)

        assertEquals("<-- END HTTP (encoded body omitted)", logger.interceptedData.responseBody)
        assertNull(logger.interceptedData.responseContentLength)
        assertNull(logger.interceptedData.responseContentType)
        assertEquals(
                "<-- 200 OK http://$host/", logger.interceptedData.responseResult
                ?.subSequence(
                        0,
                        logger.interceptedData.responseResult?.indexOf("(")!! - 1
                )
        )
    }

    @Test
    fun testPostJsonBody() {
        server.enqueue(MockResponse())
        val json = GsonBuilder().create().toJson(
            SampleData(
                "This is a test!",
                true
            )
        )
        val request = request().post(json.toRequestBody(JSON)).build()
        val response = client.newCall(request().post(request.body!!).build()).execute()
        response.body?.close()

        assertEquals(
                listOf(
                        "Content-Type: application/json; charset=utf-8",
                        "Content-Length: 41",
                        "Host: $host",
                        "Connection: Keep-Alive",
                        "Accept-Encoding: gzip",
                    "User-Agent: okhttp/4.9.0"
                ), logger.interceptedData.requestHeaders
        )

        assertEquals(
                listOf("Content-Length: 0"), logger.interceptedData.responseHeaders
        )

        assertEquals(
                "{\"message\":\"This is a test!\",\"flag\":true} --> END POST (41-byte body)",
                logger.interceptedData.requestBody
        )
        assertEquals("Content-Length: 41", logger.interceptedData.requestContentLength)
        assertEquals("Content-Type: application/json; charset=utf-8", logger.interceptedData.requestContentType)
        assertEquals("--> POST http://$host/ http/1.1", logger.interceptedData.requestType)

        assertEquals("<-- END HTTP (0-byte body)", logger.interceptedData.responseBody)
        assertNull(logger.interceptedData.responseContentLength)
        assertNull(logger.interceptedData.responseContentType)
        assertEquals(
                "<-- 200 OK http://$host/", logger.interceptedData.responseResult
                ?.subSequence(
                        0,
                        logger.interceptedData.responseResult?.indexOf("(")!! - 1
                )
        )
        assertNotNull(logger.interceptedData.timestamp)
        assertEquals(200, logger.interceptedData.statusCode)
        assertEquals("200 OK", logger.interceptedData.statusMessage)
    }

    private data class SampleData(val message: String, val flag: Boolean)
}