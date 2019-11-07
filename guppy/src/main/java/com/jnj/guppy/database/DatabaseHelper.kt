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

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.jnj.guppy.BuildConfig
import com.jnj.guppy.R
import com.jnj.guppy.database.NetworkRequestContract.NetworkRequestEntry
import com.jnj.guppy.interceptor.Logger
import com.jnj.guppy.models.GuppyData

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
        context, context.getString(R.string.databaseName), null,
        BuildConfig.databaseVersion
) {

    companion object {
        private const val SQL_CREATE_TABLE =
                "CREATE TABLE ${NetworkRequestEntry.TABLE_NAME} (" +
                        "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                        "${NetworkRequestEntry.REQUEST_TYPE} TEXT," +
                        "${NetworkRequestEntry.HOST} TEXT," +
                        "${NetworkRequestEntry.REQUEST_CONTENT_TYPE} TEXT," +
                        "${NetworkRequestEntry.REQUEST_CONTENT_LENGTH} TEXT," +
                        "${NetworkRequestEntry.REQUEST_BODY} TEXT," +
                        "${NetworkRequestEntry.REQUEST_HEADERS} TEXT," +
                        "${NetworkRequestEntry.RESPONSE_RESULT} TEXT," +
                        "${NetworkRequestEntry.RESPONSE_CONTENT_TYPE} TEXT," +
                        "${NetworkRequestEntry.RESPONSE_CONTENT_LENGTH} TEXT," +
                        "${NetworkRequestEntry.RESPONSE_HEADERS} TEXT," +
                        "${NetworkRequestEntry.RESPONSE_BODY} TEXT," +
                        "${NetworkRequestEntry.STATUS_MSG} TEXT," +
                        "${NetworkRequestEntry.STATUS_CODE} UNSIGNED SMALLINT," +
                        "${NetworkRequestEntry.TIMESTAMP} LONG)"

        private const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS ${NetworkRequestEntry.TABLE_NAME}"

        private const val SQL_DELETE_ENTRIES = "DELETE FROM ${NetworkRequestEntry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(SQL_CREATE_TABLE)
        } catch (err: SQLException) {
            Log.e("Guppy", err.message, err)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL(SQL_DROP_TABLE)
            onCreate(db)
        } catch (err: SQLException) {
            Log.e("Guppy", err.message, err)
        }
    }

    fun clearGuppyData() {
        try {
            val db = this.writableDatabase
            db.execSQL(SQL_DELETE_ENTRIES)
        } catch (err: SQLException) {
            Log.e("Guppy", err.message, err)
        }
    }

    fun saveGuppyData(logger: Logger.InterceptedData) {
        try {
            val db = this.writableDatabase
            val requestHeaders = logger.requestHeaders.joinToString("\n")
            val responseHeaders = logger.responseHeaders.joinToString("\n")
            val values = ContentValues().apply {
                put(NetworkRequestEntry.REQUEST_TYPE, logger.requestType)
                put(NetworkRequestEntry.HOST, logger.host)
                put(NetworkRequestEntry.REQUEST_CONTENT_TYPE, logger.requestContentType)
                put(NetworkRequestEntry.REQUEST_CONTENT_LENGTH, logger.requestContentLength)
                put(NetworkRequestEntry.REQUEST_BODY, logger.requestBody)
                put(NetworkRequestEntry.REQUEST_HEADERS, requestHeaders)
                put(NetworkRequestEntry.RESPONSE_RESULT, logger.responseResult)
                put(NetworkRequestEntry.RESPONSE_CONTENT_TYPE, logger.responseContentType)
                put(NetworkRequestEntry.RESPONSE_CONTENT_LENGTH, logger.responseContentLength)
                put(NetworkRequestEntry.RESPONSE_BODY, logger.responseBody)
                put(NetworkRequestEntry.RESPONSE_HEADERS, responseHeaders)
                put(NetworkRequestEntry.STATUS_MSG, logger.statusMessage)
                put(NetworkRequestEntry.STATUS_CODE, logger.statusCode)
                put(NetworkRequestEntry.TIMESTAMP, logger.timestamp)
            }

            db.insert(NetworkRequestEntry.TABLE_NAME, null, values)
            db.close()
        } catch (err: IllegalStateException) {
            Log.e("Guppy", err.message, err)
        } catch (err: SQLException) {
            Log.e("Guppy", err.message, err)
        }
    }

    fun getGuppyData(): List<GuppyData> {
        val guppyDataList = mutableListOf<GuppyData>()
        try {
            val db = this.writableDatabase
            val cursor = db.query(NetworkRequestEntry.TABLE_NAME, null, null, null, null, null, null)
            with(cursor) {
                while (moveToNext()) {
                    guppyDataList.add(
                            GuppyData(
                                    getString(getColumnIndex(NetworkRequestEntry.REQUEST_TYPE)),
                                    getString(getColumnIndex(NetworkRequestEntry.HOST)),
                                    getString(getColumnIndex(NetworkRequestEntry.REQUEST_CONTENT_TYPE)),
                                    getString(getColumnIndex(NetworkRequestEntry.REQUEST_CONTENT_LENGTH)),
                                    getString(getColumnIndex(NetworkRequestEntry.REQUEST_HEADERS)),
                                    getString(getColumnIndex(NetworkRequestEntry.REQUEST_BODY)),
                                    getString(getColumnIndex(NetworkRequestEntry.RESPONSE_CONTENT_TYPE)),
                                    getString(getColumnIndex(NetworkRequestEntry.RESPONSE_CONTENT_LENGTH)),
                                    getString(getColumnIndex(NetworkRequestEntry.RESPONSE_RESULT)),
                                    getString(getColumnIndex(NetworkRequestEntry.RESPONSE_HEADERS)),
                                    getString(getColumnIndex(NetworkRequestEntry.RESPONSE_BODY)),
                                    getString(getColumnIndex(NetworkRequestEntry.STATUS_MSG)),
                                    getInt(getColumnIndex(NetworkRequestEntry.STATUS_CODE)),
                                    getLong(getColumnIndex(NetworkRequestEntry.TIMESTAMP))
                            )
                    )
                }
                close()
            }
            db.close()
        } catch (err: SQLException) {
            Log.e("Guppy", err.message, err)
        }
        return guppyDataList
    }
}