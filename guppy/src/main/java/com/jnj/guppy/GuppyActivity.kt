/**
 * Copyright © 2020 Johnson & Johnson
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

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.jnj.guppy.database.DatabaseHelper
import com.jnj.guppy.models.GuppyData
import com.jnj.guppy.ui.GuppyDialogFragment
import com.jnj.guppy.ui.GuppyRecyclerAdapter

abstract class GuppyActivity : AppCompatActivity(), ShakeAction {

    // The following are used for the shake detection
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var shakeDetector: ShakeDetector? = null
    private var dbHelper: DatabaseHelper? = null


    @VisibleForTesting
    var shakeListener: ShakeDetector.OnShakeListener? = null

    @VisibleForTesting
    var adapter: GuppyRecyclerAdapter? = null

    @VisibleForTesting
    var dialogFragment: GuppyDialogFragment? = null

    override fun setSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager?
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector().apply {
            setOnShakeListener(shakeListener)
        }
    }

    override fun onShakeReceived(dialogFragment: GuppyDialogFragment?) {
        dialogFragment?.let {
            if (!it.isAdded) {
                it.show(supportFragmentManager, GuppyDialogFragment::class.java.simpleName)
            }
        }
    }

    override fun getGuppyData(): List<GuppyData> {
        val dataList: ArrayList<GuppyData> = ArrayList()
        dbHelper?.getGuppyData()?.let {
            dataList.addAll(it)
        }
        return dataList
    }

    override fun register() {
        sensorManager?.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun unregister() {
        sensorManager?.unregisterListener(shakeDetector)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            shakeListener = object : ShakeDetector.OnShakeListener {
                override fun onShake(count: Int) {
                    onShakeReceived(buildGuppyDialogFragment())
                }
            }
            dbHelper = DatabaseHelper(applicationContext)
            setSensors()
        }
    }

    override fun buildGuppyDialogFragment(): GuppyDialogFragment? {
        adapter?.let { adapter ->
            adapter.updateData(getGuppyData())
            dialogFragment?.let {
                return it
            } ?: run {
                dialogFragment = GuppyDialogFragment().newInstance(adapter, dbHelper)
            }
        } ?: run {
            adapter = GuppyRecyclerAdapter(this, getGuppyData())
            adapter?.let {
                dialogFragment = GuppyDialogFragment().newInstance(it, dbHelper)
            }
        }
        return dialogFragment
    }

    public override fun onResume() {
        super.onResume()
        if (BuildConfig.DEBUG) {
            register()
        }
    }

    public override fun onPause() {
        if (BuildConfig.DEBUG) {
            unregister()
        }
        super.onPause()
    }
}