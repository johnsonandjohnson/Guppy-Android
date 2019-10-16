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

class Logger {

    var interceptedData = InterceptedData()

    fun logRequestType(message: String) {
        interceptedData.requestType = message
    }

    fun logHost(message: String) {
        interceptedData.host = message
    }

    fun logRequestContentType(message: String) {
        interceptedData.requestContentType = message
    }

    fun logRequestContentLength(message: String) {
        interceptedData.requestContentLength = message
    }

    fun logRequestHeaders(message: String) {
        interceptedData.requestHeaders += message
    }

    fun logRequestBody(message: String) {
        interceptedData.requestBody = message
    }

    fun logResponseResult(message: String) {
        interceptedData.responseResult = message
    }

    fun logResponseContentType(message: String) {
        interceptedData.responseContentType = message
    }

    fun logResponseContentLength(message: String?) {
        interceptedData.responseContentLength = message
    }

    fun logResponseHeaders(message: String) {
        interceptedData.responseHeaders += message
    }

    fun logResponseBody(message: String) {
        interceptedData.responseBody = message
    }

    fun logStatus(code: Int) {
        interceptedData.apply {
            statusCode = code
            statusMessage = "$code ${HttpStatus.getDescription(code)}"
        }
    }

    class InterceptedData {
        var requestType: String? = null
        var host: String? = null
        var requestContentType: String? = null
        var requestContentLength: String? = null
        var requestBody: String? = null
        var requestHeaders: List<String> = ArrayList()
        var responseResult: String? = null
        var responseContentType: String? = null
        var responseContentLength: String? = null
        var responseHeaders: List<String> = ArrayList()
        var responseBody: String? = null
        var statusCode: Int? = null
        var statusMessage: String? = null
        val timestamp = System.currentTimeMillis()
    }
}
