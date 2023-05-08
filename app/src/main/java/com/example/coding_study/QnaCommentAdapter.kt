package com.example.coding_study

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.QnaCommentGuestBinding
import com.example.coding_study.databinding.QnaCommentHostBinding

data class QnaComment (
    var nickname: String,
    var comment: String,
    var currentDateTime: String,
    var commentId: Long
        )

class QnaCommentAdapter(private val commentHostList: List<QnaComment>, private val commentGuestList: List<QnaComment>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_COMMENT_GUEST = 0
        private const val VIEW_TYPE_COMMENT_HOST = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < commentHostList.size) VIEW_TYPE_COMMENT_HOST else VIEW_TYPE_COMMENT_GUEST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_COMMENT_HOST -> {
                val binding = QnaCommentHostBinding.inflate(inflater, parent, false)
                CommentHostViewHolder(binding)
            }
            VIEW_TYPE_COMMENT_GUEST -> {
                val binding = QnaCommentGuestBinding.inflate(inflater, parent, false)
                CommentGuestViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CommentHostViewHolder -> holder.bind(commentHostList[position])
            is CommentGuestViewHolder -> holder.bind(commentGuestList[position - commentHostList.size])
        }
    }

    override fun getItemCount(): Int {
        return commentHostList.size + commentGuestList.size
    }


    inner class CommentGuestViewHolder(private val binding: QnaCommentGuestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: QnaComment) {
            binding.guestCommentNickname.text = comment.nickname
            binding.guestCommentTextView.text = comment.comment
            binding.guestCommentCurrentDateTime.text = comment.currentDateTime

            binding.cgReplyButton.setOnClickListener {

            }
        }
    }

    inner class CommentHostViewHolder(private val binding: QnaCommentHostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: QnaComment) {
            binding.hostCommentNickname.text = comment.nickname
            binding.hostCommentTextView.text = comment.comment
            binding.hostCommentCurrentDateTime.text = comment.currentDateTime
        }
    }

}

