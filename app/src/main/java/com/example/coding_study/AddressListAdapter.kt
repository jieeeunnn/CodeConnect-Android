package com.example.coding_study
/*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AddressListAdapter(
    private val addressList: List<AddressSearchHelper.AddressItem>,
    private val onItemClick: (AddressSearchHelper.AddressItem) -> Unit
) : RecyclerView.Adapter<AddressListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(addressList[position])
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressView: TextView = itemView.findViewById(R.id.addressView)

        fun bind(address: AddressSearchHelper.AddressItem) {
            addressView.text = address.toString()

            itemView.setOnClickListener {
                onItemClick(address)
            }
        }
    }
}

 */