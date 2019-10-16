package com.jnj.guppy.ui

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jnj.guppy.R
import com.jnj.guppy.interceptor.HttpStatus
import com.jnj.guppy.models.GuppyData
import java.util.*

private const val TIME_FORMAT = "hh:mm a"

class GuppyRecyclerAdapter(private val activity: AppCompatActivity, var data: List<GuppyData>) :
        RecyclerView.Adapter<GuppyRecyclerAdapter.GuppyViewHolder>() {

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: GuppyViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.request_type)?.text = data[position].requestType
        holder.itemView.findViewById<TextView>(R.id.request_status)?.let {
            it.text = data[position].statusMessage
            it.setTextColor(ContextCompat.getColor(holder.itemView.context, getTextColor(data[position].statusCode)))
        }
        holder.itemView.findViewById<TextView>(R.id.request_timestamp)?.text = formatTime(data[position].timestamp)
        holder.itemView.setOnClickListener {
            val detailFragment = RequestDetailDialogFragment().newInstance(data[position])
            detailFragment.show(activity.supportFragmentManager, "RequestDetailDialogFragment")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuppyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.card_view_guppy_item,
                parent, false
        )
        return GuppyViewHolder(itemView)
    }

    fun updateData(data: List<GuppyData>) {
        this.data = data
        notifyDataSetChanged()
    }

    @VisibleForTesting
    fun getTextColor(statusCode: Int?): Int {
         return if (statusCode != null && HttpStatus.isSuccessful(statusCode)) {
            R.color.successful
        } else {
            R.color.unsuccessful
        }
    }

    private fun formatTime(timestampMs: Long): String {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = timestampMs

        val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), TIME_FORMAT)
        return DateFormat.format(pattern, cal).toString()
    }

    inner class GuppyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}