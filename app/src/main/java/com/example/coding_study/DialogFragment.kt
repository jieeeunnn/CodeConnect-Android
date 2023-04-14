package com.example.coding_study

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class LoginDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setTitle("로그인 실패")
            setMessage("아이디, 비밀번호를 확인하세요")
            setPositiveButton("확인") {dialog, id -> println("LoginDialogFragment 확인")}
        }.create()
    }
}

class ErrorDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setTitle("서버 연결 실패")
            setMessage("서버 연결을 확인하세요")
            setPositiveButton("확인") {dialog, id -> println("ErrorDialogFragment 확인")}
        }.create()
    }
}

class JoinDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setTitle("회원가입 실패")
            setMessage("모든 값을 입력했는지 확인하세요")
            setPositiveButton("확인") {dialog, id -> println("JoinDialogFragment 확인")}
        }.create()
    }
}

