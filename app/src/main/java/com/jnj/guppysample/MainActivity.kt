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

package com.jnj.guppysample

import android.os.Bundle
import android.util.Log
import com.jnj.guppy.GuppyActivity
import com.jnj.guppysample.databinding.ActivityMainBinding
import com.jnj.guppysample.models.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.security.SecureRandom

class MainActivity : GuppyActivity() {

    private var client: APIClient? = null
    private val job: Job = Job()
    private lateinit var binding: ActivityMainBinding

    private fun setClient() {
        client = APIClient(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        val view = binding.root
        setContentView(view)
        setClient()

        binding.buttonGetAll.setOnClickListener {
            getAllPosts()
        }

        binding.buttonGetOne.setOnClickListener {
            getOnePost()
        }

        binding.buttonCreate.setOnClickListener {
            createRandomPost()
        }

        binding.tvOpenGuppy.setOnClickListener {
            shakeListener?.onShake(0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun getAllPosts() {
        CoroutineScope(Dispatchers.IO + job).launch {
            try {
                client?.getService()?.getAllPosts()
            } catch (err: Exception) {
                Log.e(MainActivity::class.java.simpleName, err.message, err)
            }
        }
    }

    private fun getOnePost() {
        val randomId = SecureRandom().nextInt(101)
        CoroutineScope(Dispatchers.IO + job).launch {
            try {
                client?.getService()?.getOnePost(randomId)
            } catch (err: Exception) {
                Log.e(MainActivity::class.java.simpleName, err.message, err)
            }
        }
    }

    private fun createRandomPost() {
        val randomId = SecureRandom().nextInt(1000)
        CoroutineScope(Dispatchers.IO + job).launch {
            try {
                client?.getService()?.createPost(
                        Post(
                                userId = 50,
                                id = randomId,
                                title = "This is a sample title with ID: $randomId",
                                body = "This is a sample body with ID: $randomId"
                        )
                )
            } catch (err: Exception) {
                Log.e(MainActivity::class.java.simpleName, err.message, err)
            }
        }
    }
}
