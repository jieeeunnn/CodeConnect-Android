package com.example.coding_study.study

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.common.LoadImageTask
import com.example.coding_study.R
import com.example.coding_study.databinding.StudyUploadLayoutBinding

class StudyAdapter(var postList: List<Post>, private var onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<StudyAdapter.StudyUploadViewHolder>() {

    interface OnItemClickListener { // 아이템 클릭 리스너를 정의
        fun onItemClick(position: Int)
    }

    fun setData(data: List<Post>) {
        postList = data // 데이터를 설정합니다.
        notifyDataSetChanged() // RecyclerView를 다시 그립니다.
    }

    inner class StudyUploadViewHolder(private val binding: StudyUploadLayoutBinding) : RecyclerView.ViewHolder(binding.root) { // 각 게시글 뷰의 textView를 참조
        @SuppressLint("SetTextI18n") // 다국어 지원 기능 (currentCount와 num을 함께 문자열에 담기 위해 사용)
        fun bind(post: Post) { // bind 메서드를 통해 해당 뷰의 텍스트를 게시글 데이터로 설정
            binding.idTextView.text = post.nickname
            binding.titleTextView.text = post.title
            binding.contentTextView.text = post.content

            if (post.num > post.currentCount) {
                binding.numberTextView.text = "모집 중 [ ${post.currentCount} / ${post.num} ]"
                binding.numberTextView.setBackgroundResource(R.drawable.round_rect_skyblue)
            } else {
                binding.numberTextView.text = "모집 완료"
                binding.numberTextView.setBackgroundResource(R.drawable.round_rect_grey_clear)
            }


            binding.fieldTextView.text = "# ${post.field}"
            val fieldText = binding.fieldTextView
            when (post.field) {
                "안드로이드" -> {
                    fieldText.setTextColor(ContextCompat.getColor(itemView.context, R.color.android)) // Android 필드일 경우 글자색 변경
                    fieldText.background = ContextCompat.getDrawable(itemView.context,
                        R.drawable.android_background
                    ) // Android 필드일 경우 배경 변경
                }                "IOS" -> fieldText.setTextColor(ContextCompat.getColor(itemView.context,
                R.color.IOS
            )) // iOS 필드일 경우 회색으로 설정
                "알고리즘" -> {
                    fieldText.setTextColor(ContextCompat.getColor(itemView.context,
                        R.color.algorithm
                    )) // Android 필드일 경우 글자색 변경
                    fieldText.background = ContextCompat.getDrawable(itemView.context,
                        R.drawable.algorithm_background
                    ) // Android 필드일 경우 배경 변경
                }
                "데이터베이스" -> {
                fieldText.setTextColor(ContextCompat.getColor(itemView.context, R.color.database)) // Android 필드일 경우 글자색 변경
                fieldText.background = ContextCompat.getDrawable(itemView.context,
                    R.drawable.database_background
                ) // Android 필드일 경우 배경 변경
                }
                "운영체제" -> {
                    fieldText.setTextColor(ContextCompat.getColor(itemView.context, R.color.os)) // Android 필드일 경우 글자색 변경
                    fieldText.background = ContextCompat.getDrawable(itemView.context,
                        R.drawable.os_background
                    ) // Android 필드일 경우 배경 변경
                }
                "서버" -> {
                fieldText.setTextColor(ContextCompat.getColor(itemView.context, R.color.server)) // Android 필드일 경우 글자색 변경
                fieldText.background = ContextCompat.getDrawable(itemView.context,
                    R.drawable.server_background
                ) // Android 필드일 경우 배경 변경
                }
                "웹" -> {
                    fieldText.setTextColor(ContextCompat.getColor(itemView.context, R.color.web)) // Android 필드일 경우 글자색 변경
                    fieldText.background = ContextCompat.getDrawable(itemView.context,
                        R.drawable.web_background
                    ) // Android 필드일 경우 배경 변경
                }
                "머신러닝" -> {
                    fieldText.setTextColor(ContextCompat.getColor(itemView.context,
                        R.color.machine_learning
                    )) // Android 필드일 경우 글자색 변경
                    fieldText.background = ContextCompat.getDrawable(itemView.context,
                        R.drawable.machine_learning_background
                    ) // Android 필드일 경우 배경 변경
                }
                "기타" -> {
                    fieldText.setTextColor(ContextCompat.getColor(itemView.context, R.color.other)) // Android 필드일 경우 글자색 변경
                    fieldText.background = ContextCompat.getDrawable(itemView.context,
                        R.drawable.other_background
                    ) // Android 필드일 경우 배경 변경
                }
            }

            binding.currentTextView.text = post.currentTime

            val imageUrl: String? = "http://13.124.68.20:8080/"+ "${post.profileImagePath}"
            val imageView: ImageView = binding.studyImageView
            val loadImageTask = LoadImageTask(imageView)
            loadImageTask.execute(imageUrl)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyUploadViewHolder { //게시글 뷰의 레이아웃을 inflate하고 StudyUploadViewHolder 객체를 생성하여 반환
        val binding = StudyUploadLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudyUploadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyUploadViewHolder, position: Int) { //각 StudyUploadViewHolder 객체를 생성할 때 받은 게시글 데이터를 bind() 메서드에 전달하여 게시글 뷰를 업데이트
        holder.bind(postList[position])
        holder.itemView.setOnClickListener { // 게시글 클릭 시
            //게시글 상세화면으로 이동하는 코드
            onItemClickListener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int = postList.size
}