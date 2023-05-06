package com.example.coding_study

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.QnaCommentHostBinding

data class QnaComment (
    var nickname: String,
    var comment: String,
    var currentDateTime: String
        )


class QnaCommentAdapter(var qnaCommentList: List<QnaComment>): RecyclerView.Adapter<QnaCommentAdapter.QnaCommentViewHolder>() {

    inner class QnaCommentViewHolder(private val binding: QnaCommentHostBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(qnaComment: QnaComment) {
            binding.hostCommentNickname.text = qnaComment.nickname
            binding.hostCommentTextView.text = qnaComment.comment
            binding.hostCommentCurrentDateTime.text = qnaComment.currentDateTime
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QnaCommentViewHolder {
        val binding = QnaCommentHostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QnaCommentViewHolder(binding)
    }

    override fun getItemCount(): Int = qnaCommentList.size

    override fun onBindViewHolder(holder: QnaCommentViewHolder, position: Int) {
        holder.bind(qnaCommentList[position])
    }
}
