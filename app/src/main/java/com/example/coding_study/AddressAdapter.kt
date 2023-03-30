package com.example.coding_study

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class AddressAdapter(private var addressList: List<AddressItem>) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressTextView: TextView = itemView.findViewById(R.id.addressView)

        fun bind(addressItem: AddressItem) {
            addressTextView.text = addressItem.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val addressItem = addressList[position]
        holder.bind(addressItem)
    }

    override fun getItemCount() = addressList.size

    fun submitList(newList: List<AddressItem>?) {
        addressList = newList ?: emptyList()
        notifyDataSetChanged()
    }

}
