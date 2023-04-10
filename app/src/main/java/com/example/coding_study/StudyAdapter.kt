package com.example.coding_study

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.StudyUploadLayoutBinding

/*
class StudyAdapter(private val viewModel: StudyViewModel) : RecyclerView.Adapter<StudyAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: StudyUploadLayoutBinding) : RecyclerView.ViewHolder(binding.root) { // ViewHolder는 inner class이기 때문에 CustomAdapter 객체가 먼저 만들어져야 그 이후에 만들어 질 수 있음
                                                                                                                    // 대신 ViewHolder 내에서 StudyAdapter의 속성들(viewModel)에 접근할 수 있음
        fun setContents(pos: Int) { // 실제로 데이터를 집어넣는 부분
            with(viewModel.items[pos]) { // viewModel에서 위치(pos)에 해당하는 데이터를 가져옴
                binding.idTextView.text = nickname
                binding.titleTextView.text = title
                binding.contentTextView.text = content
                binding.fieldTextView.text = field
                binding.numberTextView.text = num
            }
            binding.root.setOnClickListener{ // itemClickEvent는 MultableLiveData
                viewModel.itemClickEvent.value = pos // itemClickEvent는 옵저버에게 항목 번호와 클릭되었음을 알림
            }
        }
    }

    // ViewHolder 생성, ViewHolder는 View를 담는 상자
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context) // ViewHolder를 만들기 위해서 view를 먼저 만듬 (layoutInflater 이용)
        val binding = StudyUploadLayoutBinding.inflate(layoutInflater, parent, false) // 아이템을 표현하기 위한 레이아웃 바인딩
        return ViewHolder(binding)
    }

    // ViewHolder에 데이터 쓰기 (인자로 넘어온 ViewHolder에 해당하는 데이터를 넣어서 리턴)
    // 리스트의 항목 내용이 변경되어야 할 때 항목에 해당하는 ViewHolder에 데이터를 채우도록 Adapter에게 요청 -> onBindViewHolder() 호출
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { // recyclerView가 ViewHolder에 데이터를 넣어달라고 onBindViewHolder를 호출
        holder.setContents(position) // ViewHolder를 어떻게 채울지는 viewHolder에서 메소드를 만들어서 사용
    }

    override fun getItemCount(): Int { // 지금 현재 이 StudyAdapter가 관리하는 데이터의 갯수
        return viewModel.items.size
    }

    fun onItemClick(position: Int) {
        viewModel.itemClickEvent.value = position // 클릭한 게시글의 인덱스 정보를 LiveData에 전달
    }
}
 */
class StudyAdapter(private var viewModel: StudyViewModel) : RecyclerView.Adapter<StudyAdapter.StudyUploadViewHolder>() {

    private var posts: List<Post> = emptyList()

    init { // 생성자에서 받아온 viewModel 객체의 postList 속성을 관찰, postList 속성에 변화가 생길 때마다 posts 변수를 업데이트 하고, notifyDataSetChanged() 함수를 호출하여 UI를 업데이트

        /*
        viewModel.postList.observeForever { newPosts ->
            if (newPosts != null) {
                posts = newPosts
            }
            notifyDataSetChanged()
        }

         */
    }
    //뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyUploadViewHolder {
        val binding = StudyUploadLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudyUploadViewHolder(binding)
    }

    //뷰 홀더에 데이터 바인딩
    override fun onBindViewHolder(holder: StudyUploadViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    //게시글 목록 갱신
    fun updatePosts(posts: List<Post>) {
        this.posts = posts
        notifyDataSetChanged()
    }
/*
    fun addStudy(posts: MutableList<Post>, post: Post) {
        posts.add(post)
        notifyItemInserted(posts.size - 1)
    }
 */
    fun addPost(post: Post) {
        //viewModel.addPost(post)
        posts = viewModel.postList.value ?: emptyList()
        notifyDataSetChanged()
    }

    // 뷰 홀더 클래스
    inner class StudyUploadViewHolder(private val binding: StudyUploadLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        //뷰 홀더에 데이터 바인딩
        fun bind(post: Post) {
            binding.idTextView.text = post.nickname
            binding.titleTextView.text = post.title
            binding.contentTextView.text = post.content
            binding.numberTextView.text = post.num.toString()
            binding.fieldTextView.text = post.field
            binding.currentTextView.text = post.currentTime
        }
    }
}