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

package com.jnj.guppy.models

/**
 *
 * Model Class to hold Logged data from {GuppyInterceptor}
 *
 * Modeling after:
 *
 * <pre>`--> POST /greeting http/1.1
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
 **/
data class GuppyData(
        val requestType: String?, val host: String?, val requestContentType: String?,
        val requestContentLength: String?, val requestHeaders: String,
        val requestBody: String?, val responseContentType: String?,
        val responseContentLength: String?, val responseResult: String?,
        val responseHeaders: String, val responseBody: String?, var statusMessage : String?, var statusCode : Int?,
        val timestamp : Long
)