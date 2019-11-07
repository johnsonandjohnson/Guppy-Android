package com.jnj.guppy.ui

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jnj.guppy.BuildConfig
import com.jnj.guppy.R
import com.jnj.guppy.database.DatabaseHelper

class GuppyDialogFragment : DialogFragment() {

    private var adapter: GuppyRecyclerAdapter? = null

    private var dbHelper: DatabaseHelper? = null

    fun newInstance(adapter: GuppyRecyclerAdapter, dbHelper: DatabaseHelper?): GuppyDialogFragment {
        val dialogFragment = GuppyDialogFragment()
        dialogFragment.adapter = adapter
        dialogFragment.dbHelper = dbHelper
        return dialogFragment
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(context)

        activity?.layoutInflater?.inflate(R.layout.guppy_dialog, null)?.let { view ->
            dialog.setContentView(view)

            // needs to be called after you set the content view
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            onViewCreated(view, savedInstanceState)

            dialog.findViewById<TextView>(R.id.guppy_version).text = getString(
                    R.string.guppy_version_string,
                    BuildConfig.VERSION_NAME + "+" + BuildConfig.VERSION_CODE
            )

            dialog.findViewById<ImageView>(R.id.clear_all_guppies).setOnClickListener {
                dbHelper?.clearGuppyData()
                this.adapter?.updateData(ArrayList())
            }

            configureRecyclerView(dialog.findViewById(R.id.guppy_recycler_view))
        }
        return dialog
    }

    private fun configureRecyclerView(guppyRv: RecyclerView) {
        context?.let {
            val divider = DividerItemDecoration(it, DividerItemDecoration.VERTICAL).apply {
                setDrawable(it.getDrawable(R.drawable.divider) as Drawable)
            }

            guppyRv.layoutManager = LinearLayoutManager(activity)
            guppyRv.adapter = this.adapter
            guppyRv.addItemDecoration(divider)
            guppyRv.setHasFixedSize(true)
        }
    }
}