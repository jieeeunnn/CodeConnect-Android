package com.example.coding_study

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.StudyUploadLayoutBinding

class StudyAdapter(var postList: List<Post>) :
    RecyclerView.Adapter<StudyAdapter.StudyUploadViewHolder>() {

    inner class StudyUploadViewHolder(private val binding: StudyUploadLayoutBinding) : RecyclerView.ViewHolder(binding.root) { // 각 게시글 뷰의 textView를 참조
        fun bind(post: Post) { // bind 메서드를 통해 해당 뷰의 텍스트를 게시글 데이터로 설정
            binding.idTextView.text = post.nickname
            binding.titleTextView.text = post.title
            binding.contentTextView.text = post.content
            binding.numberTextView.text = post.num.toString()
            binding.fieldTextView.text = post.field
            binding.currentTextView.text = post.currentTime
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyUploadViewHolder { //게시글 뷰의 레이아웃을 inflate하고 StudyUploadViewHolder 객체를 생성하여 반환
        val binding = StudyUploadLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudyUploadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyUploadViewHolder, position: Int) { //각 StudyUploadViewHolder 객체를 생성할 때 받은 게시글 데이터를 bind() 메서드에 전달하여 게시글 뷰를 업데이트
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int = postList.size
}
