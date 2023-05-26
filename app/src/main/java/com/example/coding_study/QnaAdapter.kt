package com.example.coding_study

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.QnaUploadLayoutBinding


// 게시글의 정보를 담는 데이터 클래스 QnaPost
data class QnaPost (
    var nickname: String,
    var title: String,
    var content: String,
    var currentTime: String,
    var commentCount: Int,
    var profileImagePath: String
)


class QnaAdapter(var qnaPostList: List<QnaPost>, private var onQnaClickListener: OnQnaClickListener) : RecyclerView.Adapter<QnaAdapter.QnaUploadViewHolder>() {

    interface OnQnaClickListener {
        fun onQnaClick(position: Int)
    }

    fun setOnQnaClickListener(listener: OnQnaClickListener) {
        this.onQnaClickListener = listener
    }

    inner class QnaUploadViewHolder(private val binding: QnaUploadLayoutBinding) : RecyclerView.ViewHolder(binding.root) { // 각 게시글 뷰의 textView를 참조
        fun bind(qnapost: QnaPost) {
            binding.quaUploadId.text = qnapost.nickname
            binding.qnaUploadTitle.text = qnapost.title
            binding.qnaUploadContent.text = qnapost.content
            binding.qnaUploadTime.text = qnapost.currentTime
            binding.qnaCommentCountTextView.text = qnapost.commentCount.toString()

            val imageUrl: String? = "http://112.154.249.74:8080/"+ qnapost.profileImagePath
            val imageView: ImageView = binding.qnaProfileImage
            val loadImageTask = LoadImageTask(imageView)
            loadImageTask.execute(imageUrl)
        }

        fun onClick(v: View?) {
            onQnaClickListener.onQnaClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QnaUploadViewHolder {
        val binding = QnaUploadLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QnaUploadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QnaUploadViewHolder, position: Int) {
        holder.bind(qnaPostList[position])
        holder.itemView.setOnClickListener{
            onQnaClickListener?.onQnaClick(position)
        }
    }

    override fun getItemCount(): Int = qnaPostList.size
}