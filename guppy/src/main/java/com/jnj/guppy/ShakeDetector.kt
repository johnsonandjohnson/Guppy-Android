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

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


class ShakeDetector : SensorEventListener {

    private var listener: OnShakeListener? = null
    private var shakeTimestamp: Long = 0
    private var shakeCount: Int = 0

    fun setOnShakeListener(listener: OnShakeListener?) {
        this.listener = listener
    }

    interface OnShakeListener {
        fun onShake(count: Int)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        //no-op
    }

    override fun onSensorChanged(event: SensorEvent) {
        var x = 0F
        var y = 0F
        var z = 0F

        event.values.forEachIndexed() { index, value ->
            when (index) {
                xIndex -> x = value
                yIndex -> y = value
                zIndex -> z = value
                else -> return
            }
        }

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // gForce will be close to 1 when there is no movement.
        val gForce = kotlin.math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble())

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()
            // ignore shake events too close to each other (500ms)
            if (shakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return
            }

            // reset the shake count after 3 seconds of no shakes
            if (shakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                shakeCount = 0
            }

            shakeTimestamp = now
            shakeCount++

            listener?.onShake(shakeCount)
        }
    }

    companion object {
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        private const val SHAKE_SLOP_TIME_MS = 500
        private const val SHAKE_COUNT_RESET_TIME_MS = 3000
        private const val xIndex = 0
        private const val yIndex = 1
        private const val zIndex = 2
    }
}