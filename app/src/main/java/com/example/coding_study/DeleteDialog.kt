package com.example.coding_study

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.coding_study.databinding.DeleteDialogBinding

interface DeleteDialogInterface{
    fun onYesButtonClick(id: Long)
}

class DeleteDialog(deleteDialogInterface: DeleteDialogInterface, id: Long): DialogFragment() {
    private lateinit var binding: DeleteDialogBinding
    private var deleteDialogInterface: DeleteDialogInterface
    private var id : Long = 0

    init {
        this.id = id
        this.deleteDialogInterface = deleteDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DeleteDialogBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명
        binding.dialogTextView.text = "게시글을 삭제하시겠습니까?"

        binding.dialogNoButton.setOnClickListener {
            dismiss()
        }

        binding.dialogYesButton.setOnClickListener {
            this.deleteDialogInterface.onYesButtonClick(id)
            dismiss()
        }
        return binding.root
    }

}