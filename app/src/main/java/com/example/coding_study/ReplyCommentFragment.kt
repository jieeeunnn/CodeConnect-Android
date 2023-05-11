package com.example.coding_study

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.QnaHostBinding
import com.google.gson.Gson

class ReplyCommentFragment : Fragment() {
    private lateinit var replyRecyclerView: RecyclerView
    private lateinit var binding: QnaHostBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QnaHostBinding.inflate(inflater, container, false)

        val commentGson = Gson()
        val commentJson = arguments?.getString("commentHostJson")
        val commentHost = commentGson.fromJson(commentJson, Comment::class.java)

        Log.e("ReplyCommentFragment commentHost", "$commentHost")
        //val commentRecyclerView = binding.replyCommentRecyclerView


        return binding.root
    }
}