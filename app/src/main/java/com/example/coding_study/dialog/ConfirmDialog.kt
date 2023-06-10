package com.example.coding_study.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.coding_study.databinding.ConfirmDialogBinding

class ConfirmDialog(text: String) : DialogFragment() {
    private lateinit var binding: ConfirmDialogBinding
    private var text: String

    init {
        this.text = text
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConfirmDialogBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명
        binding.confirmTextView.text = text

        binding.cofirmButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }
}