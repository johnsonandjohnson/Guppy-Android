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

package com.jnj.guppy.database

import android.provider.BaseColumns

object NetworkRequestContract {
    object NetworkRequestEntry : BaseColumns {
        const val TABLE_NAME = "network_request"
        const val REQUEST_TYPE = "request_type"
        const val HOST = "host"
        const val REQUEST_CONTENT_TYPE = "request_content_type"
        const val REQUEST_CONTENT_LENGTH = "request_content_length"
        const val REQUEST_BODY = "request_body"
        const val REQUEST_HEADERS = "request_headers"
        const val RESPONSE_RESULT = "response_result"
        const val RESPONSE_CONTENT_TYPE = "response_content_type"
        const val RESPONSE_CONTENT_LENGTH = "response_content_length"
        const val RESPONSE_HEADERS = "response_headers"
        const val RESPONSE_BODY = "response_body"
    }
}