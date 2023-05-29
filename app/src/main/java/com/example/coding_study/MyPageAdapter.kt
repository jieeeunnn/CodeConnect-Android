package com.example.coding_study

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.MypageListBinding

class MyPageAdapter(private val textList: List<String>, private var onItemClickListener: OnItemClickListener): RecyclerView.Adapter<MyPageAdapter.MyPageListViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnMyPageClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    inner class MyPageListViewHolder(private val binding: MypageListBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(position)
                }
            }
        }
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