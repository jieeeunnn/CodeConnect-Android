package com.example.coding_study.qna

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.common.LoadImageTask
import com.example.coding_study.common.TokenManager
import com.example.coding_study.databinding.QnaPopularLayoutBinding
import com.example.coding_study.databinding.QnaUploadLayoutBinding


// 게시글의 정보를 담는 데이터 클래스 QnaPost
data class QnaPost (
    var nickname: String,
    var title: String,
    var content: String,
    var currentTime: String,
    var commentCount: Int,
    var profileImagePath: String,
    var likeCount: Int,
    var isPopular: Boolean
)


class QnaAdapter(var qnaPostList: List<QnaPost>, private var onQnaClickListener: OnQnaClickListener, private var onPopularClickListener: OnPopularClickListener,
                 private var tokenManager: TokenManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_QNA_POST = 1
    private val VIEW_TYPE_POPULAR_POST = 2
    val token = tokenManager.getAccessToken()

    override fun getItemViewType(position: Int): Int {
        // position에 따라서 뷰 유형을 식별하고 반환
        val qnaPost = qnaPostList[position]
        return if (qnaPost.isPopular) {
            VIEW_TYPE_POPULAR_POST
        } else {
            VIEW_TYPE_QNA_POST
        }
    }

    interface OnQnaClickListener {
        fun onQnaClick(position: Int)
    }

    interface OnPopularClickListener {
        fun onPopularClick(position: Int)
    }

    inner class QnaUploadViewHolder(private val binding: QnaUploadLayoutBinding) : RecyclerView.ViewHolder(binding.root) { // 각 게시글 뷰의 textView를 참조
        fun bind(qnapost: QnaPost) {
            binding.quaUploadId.text = qnapost.nickname
            binding.qnaUploadTitle.text = qnapost.title
            binding.qnaUploadTime.text = qnapost.currentTime
            binding.qnaCommentCountTextView.text = qnapost.commentCount.toString()
            binding.qnaLikeCount.text = qnapost.likeCount.toString()

            val imageUrl: String? = "http://52.79.53.62:8080/"+ qnapost.profileImagePath
            val imageView: ImageView = binding.qnaProfileImage
            val loadImageTask = LoadImageTask(imageView, token)
            loadImageTask.execute(imageUrl)

        }
    }

    inner class QnaPopularViewHolder(private val binding: QnaPopularLayoutBinding) : RecyclerView.ViewHolder(binding.root) { // 각 게시글 뷰의 textView를 참조
        fun bind(qnapost: QnaPost) {
            binding.qnaPopularNickname.text = qnapost.nickname
            binding.qnaPopularTitle.text = qnapost.title
            binding.qnaPopularCurrentTime.text = qnapost.currentTime
            binding.qnaPopularCommentCount.text = qnapost.commentCount.toString()
            binding.qnaPopularLikeCount.text = qnapost.likeCount.toString()

            val imageUrl: String? = "http://52.79.53.62:8080/"+ qnapost.profileImagePath
            val imageView: ImageView = binding.qnaPopularProfileImage
            val loadImageTask = LoadImageTask(imageView,token)
            loadImageTask.execute(imageUrl)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_POPULAR_POST -> {
                val binding = QnaPopularLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                QnaPopularViewHolder(binding)
            }
            else -> {
                val binding = QnaUploadLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                QnaUploadViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val qnaPost = qnaPostList[position]
        when (holder) {
            is QnaPopularViewHolder -> {
                holder.bind(qnaPost)
                holder.itemView.setOnClickListener {
                    onPopularClickListener.onPopularClick(position)
                }
            }
            is QnaUploadViewHolder -> {
                holder.bind(qnaPost)
                holder.itemView.setOnClickListener {
                    onQnaClickListener.onQnaClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int = qnaPostList.size
}