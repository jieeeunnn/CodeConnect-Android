package com.example.coding_study

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.StudyUploadLayoutBinding


class StudyAdapter(var postList: List<Post>, private var onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<StudyAdapter.StudyUploadViewHolder>() {

    interface OnItemClickListener { // 아이템 클릭 리스너를 정의
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) { // 클릭 리스너를 설정하는 메서드
        this.onItemClickListener = listener
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
                binding.numberTextView.text = "${post.currentCount} / ${post.num}"
            } else {
                binding.numberTextView.text = "모집 완료"
            }
            binding.fieldTextView.text = "# ${post.field}"
            binding.currentTextView.text = post.currentTime

            val imageUrl: String? = "http://112.154.249.74:8080/"+ "${post.profileImagePath}"
            val imageView: ImageView = binding.studyImageView
            val loadImageTask = LoadImageTask(imageView)
            loadImageTask.execute(imageUrl)
        }

        fun onClick(v: View?) {
            onItemClickListener.onItemClick(adapterPosition) // 클릭된 아이템의 포지션 전달
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
