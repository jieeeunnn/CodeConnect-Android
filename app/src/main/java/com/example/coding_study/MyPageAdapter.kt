package com.example.coding_study

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.MypageListBinding

class MyPageAdapter(private val textList: List<String>, private var itemClickListener: ItemClickListener): RecyclerView.Adapter<MyPageAdapter.MyPageListViewHolder>() {

    interface ItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnMyPageClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    inner class MyPageListViewHolder(private val binding: MypageListBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(text: String){
            binding.textView.text = text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPageListViewHolder {
        val binding = MypageListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyPageListViewHolder(binding)
    }

    override fun getItemCount(): Int = textList.size

    override fun onBindViewHolder(holder: MyPageListViewHolder, position: Int) {
        val text = textList[position]
        holder.bind(text)
    }

}