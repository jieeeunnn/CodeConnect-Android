package com.example.coding_study.qna

import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.dialog.CommentDeleteDialog
import com.example.coding_study.common.LoadImageTask
import com.example.coding_study.R
import com.example.coding_study.databinding.QnaCommentGuestBinding
import com.example.coding_study.databinding.QnaCommentHostBinding


class QnaCommentAdapter(private val fragmentManager: FragmentManager, var commentList: List<Comment>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_COMMENT_GUEST = 0
        private const val VIEW_TYPE_COMMENT_HOST = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (commentList[position].role) {
            "COMMENT_GUEST" -> VIEW_TYPE_COMMENT_GUEST
            "COMMENT_HOST" -> VIEW_TYPE_COMMENT_HOST
            else -> throw IllegalArgumentException("Invalid role")
        }
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
        val comment = commentList[position]
        when (holder) {
            is CommentHostViewHolder -> holder.bind(comment)
            is CommentGuestViewHolder -> holder.bind(comment)
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }


    inner class CommentGuestViewHolder(private val binding: QnaCommentGuestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.guestCommentNickname.text = comment.nickname
            binding.guestCommentTextView.text = comment.comment
            binding.guestCommentCurrentDateTime.text = comment.currentDateTime

            val imageUrl: String? = "http://52.79.53.62:8080/"+ comment.profileImagePath
            val imageView: ImageView = binding.commentGuestProfileImage
            val loadImageTask = LoadImageTask(imageView)
            loadImageTask.execute(imageUrl)
        }
    }

    inner class CommentHostViewHolder(private val binding: QnaCommentHostBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
                    // commentHostList가 null이 아니고, 현재 아이템이 리스트 범위 내에 있을 경우에만 바인딩을 진행합니다.
            binding.hostCommentNickname.text = comment.nickname
            binding.hostCommentTextView.text = comment.comment
            binding.hostCommentCurrentDateTime.text = comment.currentDateTime

            val imageUrl: String? = "http://52.79.53.62:8080/"+ comment.profileImagePath
            val imageView: ImageView = binding.commentHostProfileImage
            val loadImageTask = LoadImageTask(imageView)
            loadImageTask.execute(imageUrl)

                    binding.commentMenuButton.setOnClickListener { view ->
                        val popupMenu = PopupMenu(view.context, view)
                        popupMenu.menuInflater.inflate(R.menu.comment_menu, popupMenu.menu)

                        popupMenu.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.commentDeleteMenu -> {
                                    // 삭제 기능 구현
                                    val deleteDialog = CommentDeleteDialog(comment.commentId)
                                    deleteDialog.isCancelable = false
                                    deleteDialog.show(fragmentManager, "deleteDialog")
                                    true
                                }
                                else -> false
                            }
                        }
                        popupMenu.show()
                    }
                }
            }
    }

