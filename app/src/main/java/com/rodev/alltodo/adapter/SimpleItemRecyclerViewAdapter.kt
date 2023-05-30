package com.rodev.alltodo.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.rodev.alltodo.ItemEditActivity.Companion.putTask
import com.rodev.alltodo.R
import com.rodev.alltodo.data.entity.Task
import com.rodev.alltodo.databinding.ItemListContentBinding
import com.rodev.alltodo.util.formatToString
import com.rodev.alltodo.util.getImageFromUri
import com.rodev.alltodo.util.replaceArg


class SimpleItemRecyclerViewAdapter(
    private val itemDetailFragmentContainer: View?
) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

    private val values = ArrayList<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding =
            ItemListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setValues(values: Collection<Task>) {
        setValuesSilently(values)

        notifyDataSetChanged()
    }

    fun setValuesSilently(values: Collection<Task>) {
        this.values.clear()
        this.values.addAll(values)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        with(holder.binding) {
            itemTitle.text = item.title
            itemCreatedDate.text = holder.replaceArg(R.string.date_textview, item.createdDate.formatToString())
            itemTime.text = holder.replaceArg(R.string.time_textview, item.time.toString())
            taskImage.setImageBitmap(
                holder.itemView.context.getImageFromUri(Uri.parse(item.imageUri.toString()))
            )
        }

        with(holder.itemView) {
            tag = item
            setOnClickListener { itemView ->
                val item = itemView.tag as Task
                val bundle = Bundle()
                bundle.putTask(item)
                if (itemDetailFragmentContainer != null) {
                    itemDetailFragmentContainer.findNavController()
                        .navigate(R.id.fragment_item_detail, bundle)
                } else {
                    itemView.findNavController().navigate(R.id.show_item_detail, bundle)
                }
            }
        }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(val binding: ItemListContentBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun replaceArg(msg: Int, arg: String): String {
                return binding.root.context.replaceArg(msg, arg)
            }

        }

}
