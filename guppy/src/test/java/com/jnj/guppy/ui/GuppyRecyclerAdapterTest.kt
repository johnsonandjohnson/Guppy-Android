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

package com.jnj.guppy.ui

import com.jnj.guppy.GuppyActivity
import com.jnj.guppy.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import java.net.HttpURLConnection

@RunWith(MockitoJUnitRunner::class)
class GuppyRecyclerAdapterTest {

    private lateinit var adapter: GuppyRecyclerAdapter

    @Before
    fun setUp() {
        val activity = mock(GuppyActivity::class.java)
        adapter = GuppyRecyclerAdapter(activity, listOf())
    }

    @Test
    fun testGetSuccessfulResponseColor() {
        assertEquals(R.color.successful, adapter.getTextColor(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testGetErrorResponseColor() {
        assertEquals(R.color.unsuccessful, adapter.getTextColor(HttpURLConnection.HTTP_BAD_REQUEST))
    }
}