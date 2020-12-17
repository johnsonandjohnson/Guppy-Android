package com.jnj.guppy.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.gson.GsonBuilder
import com.jnj.guppy.R
import com.jnj.guppy.models.GuppyData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class RequestDetailDialogFragment : DialogFragment() {

    companion object {
        private const val TAG = "RequestDetailDialog"
    }

    private var guppyData: GuppyData? = null

    fun newInstance(data: GuppyData): RequestDetailDialogFragment {
        val dialogFragment = RequestDetailDialogFragment()
        dialogFragment.guppyData = data
        return dialogFragment
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)

        activity?.let {
            val view = it.layoutInflater.inflate(R.layout.request_detail_dialog, null)
            dialog.setContentView(view)

            // needs to be called after you set the content view
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            guppyData?.let { data ->
                dialog.findViewById<TextView>(R.id.request_url).text = data.host
                dialog.findViewById<TextView>(R.id.request_body).text = data.requestBody

                dialog.findViewById<TextView>(R.id.request_headers).text = data.requestHeaders

                dialog.findViewById<TextView>(R.id.request_type).text = data.requestType
                dialog.findViewById<TextView>(R.id.response_result).text = data.responseResult
                dialog.findViewById<TextView>(R.id.response_body).text =
                    formatJson(data.responseBody)
            }
            onViewCreated(view, savedInstanceState)
        }
        return dialog
    }

    private fun formatJson(json: String?): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        json?.let { jsonString ->
            try {
                val jsonArr = JSONArray(jsonString)
                return gson.toJson(jsonArr)
            } catch (err: JSONException) {
                Log.e(TAG, err.message, err)
            }
            try {
                val jsonObj = JSONObject(jsonString)
                return gson.toJson(jsonObj)
            } catch (err: JSONException) {
                Log.e(TAG, err.message, err)
            }
        }
        return gson.toJson(json)
    }
}