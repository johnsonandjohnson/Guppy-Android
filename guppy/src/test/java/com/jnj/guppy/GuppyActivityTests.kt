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

package com.jnj.guppy

import android.hardware.SensorEvent
import com.jnj.guppy.database.DatabaseHelper
import com.jnj.guppy.interceptor.HttpStatus
import com.jnj.guppy.models.GuppyData
import com.jnj.guppy.testclasses.TestActivity
import com.jnj.guppy.ui.GuppyDialogFragment
import com.jnj.guppy.ui.GuppyRecyclerAdapter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GuppyActivityTests {

    private lateinit var activity: GuppyActivity

    private lateinit var detector: ShakeDetector

    private lateinit var shakeListener: ShakeDetector.OnShakeListener

    @Mock
    private lateinit var mockDatabaseHelper: DatabaseHelper


    @Throws(Exception::class)
    fun mockSensorEvent(values: FloatArray): SensorEvent {
        val sensorEvent = mock(SensorEvent::class.java)
        val valuesField = SensorEvent::class.java.getField("values")
        valuesField.isAccessible = true
        valuesField.set(sensorEvent, values)

        return sensorEvent
    }

    @Before
    fun setUpTests() {
        shakeListener = mock(ShakeDetector.OnShakeListener::class.java)
        activity = TestActivity()
        activity.setSensors()
        activity.shakeListener = shakeListener
        detector = ShakeDetector()
    }

    @Test
    fun testActivity() {
        assertNotNull(activity)
        assertNotNull(activity.shakeListener)
    }

    @Test
    fun testActivityShake() {
        activity.shakeListener?.onShake(1)
        verify(shakeListener, times(1)).onShake(1)
    }

    @Test
    fun testNoShake() {
        val shakeArray = FloatArray(3)
        shakeArray[0] = 0F
        shakeArray[1] = 0F
        shakeArray[2] = 0F

        assertNotNull(detector)

        detector.setOnShakeListener(shakeListener)
        detector.onSensorChanged(mockSensorEvent(shakeArray))

        verify(shakeListener, times(0)).onShake(0)
    }

    @Test
    fun testShake() {
        val shakeArray = FloatArray(3)
        shakeArray[0] = 142F
        shakeArray[1] = 22F
        shakeArray[2] = 124F

        assertNotNull(detector)

        detector.setOnShakeListener(shakeListener)
        detector.onSensorChanged(mockSensorEvent(shakeArray))

        verify(shakeListener, times(1)).onShake(1)
    }

    @Test
    fun testGuppyData() {
        val data = GuppyData(
                "requestType", "host",
                "requestContentType", "requestContentLength",
                "requestHeaders", "requestBody",
                "responseContentType", "responseContentLength",
                "responseResult", "responseHeaders",
                "responseBody", "statusMessage", 200, 1L
        )
        assertNotNull(data)
        assertEquals("host", data.host)
        assertEquals("requestType", data.requestType)
        assertEquals("requestContentType", data.requestContentType)
        assertEquals("responseContentLength", data.responseContentLength)
        assertEquals("requestHeaders", data.requestHeaders)
        assertEquals("requestBody", data.requestBody)
        assertEquals("responseContentType", data.responseContentType)
        assertEquals("responseContentLength", data.responseContentLength)
        assertEquals("responseResult", data.responseResult)
        assertEquals("responseHeaders", data.responseHeaders)
        assertEquals("responseBody", data.responseBody)
        assertEquals("statusMessage", data.statusMessage)
        assertEquals(200, data.statusCode)
        assertEquals(1L, data.timestamp)
    }

    @Test
    fun testShakeReceived() {
        assertNotNull(activity)
        assertNull(activity.adapter)
        assertNull(activity.dialogFragment)

        activity.shakeListener = object : ShakeDetector.OnShakeListener {
            override fun onShake(count: Int) {
                activity.buildGuppyDialogFragment()
            }
        }

        activity.shakeListener?.onShake(1)

        assertEquals(0, activity.adapter?.itemCount)

        assertNotNull(activity.adapter)
        assertNotNull(activity.dialogFragment)
    }

    @Test
    fun testAdapterWasNull() {
        assertNotNull(activity)
        assertNull(activity.adapter)
        assertNull(activity.dialogFragment)

        activity.shakeListener = object : ShakeDetector.OnShakeListener {
            override fun onShake(count: Int) {
                activity.buildGuppyDialogFragment()
            }
        }

        activity.adapter = null

        activity.shakeListener?.onShake(1)

        assertEquals(0, activity.adapter?.itemCount)

        assertNotNull(activity.adapter)
        assertNotNull(activity.dialogFragment)
    }

    @Test
    fun testMultipleShakesReceived() {
        assertNotNull(activity)
        assertNull(activity.adapter)
        assertNull(activity.dialogFragment)

        activity.shakeListener = object : ShakeDetector.OnShakeListener {
            override fun onShake(count: Int) {
                activity.buildGuppyDialogFragment()
            }
        }

        activity.shakeListener?.onShake(1)

        assertEquals(0, activity.adapter?.itemCount)
        assertNotNull(activity.adapter)
        assertNotNull(activity.dialogFragment)

        val adapter: GuppyRecyclerAdapter = mock(GuppyRecyclerAdapter::class.java)
        activity.adapter = adapter

        activity.shakeListener?.onShake(1)

        assertNotNull(activity.adapter)
        assertNotNull(activity.dialogFragment)

        verify(adapter, times(1)).updateData(activity.getGuppyData())

        assertEquals(activity.getGuppyData(), activity.adapter?.data)
    }

    @Test
    fun assertDialogFragmentBuilds() {
        val dialogFragment = GuppyDialogFragment().newInstance(
                GuppyRecyclerAdapter(activity, activity.getGuppyData()), mockDatabaseHelper
        )
        dialogFragment.onCreateDialog(null)
        assertNotNull(dialogFragment)
    }
}