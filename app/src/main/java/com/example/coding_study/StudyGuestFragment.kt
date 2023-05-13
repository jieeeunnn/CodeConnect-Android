package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.StudyGuestBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StudyGuestFragment : Fragment(R.layout.study_guest) {
    private lateinit var binding: StudyGuestBinding

    fun saveStudy(context: Context, study: ChatRoom) {
        val sharedPreferences = context.getSharedPreferences("MyStudy", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong("study_roomId", study.roomId)
        editor.putString("study_title", study.title)
        editor.putString("study_hostNickname", study.hostNickname)
        editor.putString("study_currentDateTime", study.currentDateTime)
        editor.putInt("study_currentCount", study.currentCount)
        if (!editor.commit()) {
            Log.e("saveStudy", "Failed to save study information")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StudyGuestBinding.inflate(inflater, container, false)

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 가져온 recruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val gson = Gson()
        val json = arguments?.getString("recruitmentJson")
        val recruitment = gson.fromJson(json, RecruitmentDto::class.java)
        val bundle = arguments
        val participantExist = bundle?.getBoolean("participateJson")

        val parentFragment = parentFragment
        if (parentFragment is StudyFragment) {
            parentFragment.hideFloatingButton()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { // secondActivity의 onBackPressed 함수 콜백
            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStack()

            val parentFragment = parentFragment
            if (parentFragment is StudyFragment) {
                parentFragment.showFloatingButton()
            }
        }

        Log.e("StudyGuest participantExist", "$participantExist")

        if (participantExist == true) {
            binding.guestButton.visibility = View.GONE
            binding.guestCancelButton.visibility = View.VISIBLE
        } else {
            binding.guestButton.visibility = View.VISIBLE
            binding.guestCancelButton.visibility = View.GONE
        }

        Log.e("StudyGuestFragment","$recruitment")

        //저장된 토큰값 가져오기
        val sharedPreferences = requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token.orEmpty())
                            .build()
                        Log.d("TokenInterceptor_StudyGuestFragment", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val postId = recruitment.recruitmentId
        val studyParticipateService = retrofitBearer.create(StudyParticipateService::class.java)


        binding.guestButton.setOnClickListener { // 참여하기 버튼을 누를 시
            studyParticipateService.participateStudy(postId, isParticipating = true).enqueue(object : Callback<StudyGuestCurrentCount>{
                override fun onResponse( call: Call<StudyGuestCurrentCount>, response: Response<StudyGuestCurrentCount>
                ) {
                    if (response.isSuccessful) {
                        val studyGuestParticipate = response.body()
                        Log.e("guestButton response code", "${response.code()}")
                        Log.e("guestButton response body", "$studyGuestParticipate")

                        if (studyGuestParticipate != null) {
                            if (studyGuestParticipate.data == -1.0) {
                                val cofirmDialog = ConfirmDialog("모집이 완료된 스터디입니다")
                                cofirmDialog.isCancelable = false
                                cofirmDialog.show(childFragmentManager, "studyGuestFragment_Recruitment Completed")
                            }else{
                                binding.guestButton.visibility = View.GONE
                                binding.guestCancelButton.visibility = View.VISIBLE

                                if (studyGuestParticipate.data is Double) {
                                    val studyCurrentCount = (studyGuestParticipate.data as? Double)?.toInt() ?: studyGuestParticipate.data as? Int ?: -5
                                    loadStudyGuest(studyCurrentCount)

                                    val cofirmDialog = ConfirmDialog("스터디 참여 신청이 완료되었습니다")
                                    cofirmDialog.isCancelable = false
                                    cofirmDialog.show(childFragmentManager, "studyGuestFragment_guestButton")
                                }

                                else if (studyGuestParticipate.data is ChatRoom) { // 마지막 참가자가 참여버튼 클릭 시 채팅방 정보 받음
                                    val chatRoom = studyGuestParticipate.data as ChatRoom
                                    val completeCurrentCount = chatRoom.currentCount
                                    loadStudyGuest(completeCurrentCount)

                                    context?.let { it1 -> saveStudy(it1, chatRoom) } // 서버에서 받은 chatRoom 정보 저장
                                    Log.e("StudyGuestFragment chatRoom response", "$chatRoom")

                                    val chatTitle = chatRoom.title
                                    val cofirmDialog = ConfirmDialog("$chatTitle 채팅방이 생성되었습니다!")
                                    cofirmDialog.isCancelable = false
                                    cofirmDialog.show(childFragmentManager, "studyGuestFragment_guestButton")
                                }
                            }
                        }
                    }else{
                        Log.e("StudyGuestFragment guestButton onResponse", "But not success")
                    }
                }

                override fun onFailure(call: Call<StudyGuestCurrentCount>, t: Throwable) {
                    Toast.makeText(context, "참여하기 버튼_서버 연결 실패", Toast.LENGTH_SHORT).show()
                    Log.e("studyHostFragment_", "$t")
                }
            })
        }

        binding.guestCancelButton.setOnClickListener { // 취소하기 버튼 누를 시
            studyParticipateService.participateStudy(postId, isParticipating = false).enqueue(object : Callback<StudyGuestCurrentCount>{
                override fun onResponse( call: Call<StudyGuestCurrentCount>, response: Response<StudyGuestCurrentCount>
                ) {
                    if (response.isSuccessful) {
                        val studyGuestCurrentCount = response.body()
                        Log.e("guestButton response code", "${response.code()}")
                        Log.e("guestButton response body", "$studyGuestCurrentCount")

                        if (studyGuestCurrentCount != null) {
                            if (studyGuestCurrentCount.data == -1.0) {
                                val cofirmDialog = ConfirmDialog("모집이 완료된 스터디는 취소할 수 없습니다")
                                cofirmDialog.isCancelable = false
                                cofirmDialog.show(childFragmentManager, "studyGuestFragment_Recruitment Completed")
                            } else{
                                binding.guestButton.visibility = View.VISIBLE
                                binding.guestCancelButton.visibility = View.GONE

                                val studyCurrentCount = (studyGuestCurrentCount.data as? Double)?.toInt() ?: studyGuestCurrentCount.data as? Int ?: -5
                                loadStudyGuest(studyCurrentCount)

                                val cofirmDialog = ConfirmDialog("스터디 신청이 취소되었습니다")
                                cofirmDialog.isCancelable = false
                                cofirmDialog.show(childFragmentManager, "studyGuestFragment_guestCancelButton")
                            }
                        }


                    } else {
                        Log.e("StudyGuestFragment guestCancelButton onResponse", "But not success")
                    }
                }
                override fun onFailure(call: Call<StudyGuestCurrentCount>, t: Throwable) {
                    Toast.makeText(context, "참여 취소하기 버튼_서버 연결 실패", Toast.LENGTH_SHORT).show()
                    Log.e("studyHostFragment_", "$t")
                }
            })
        }
    }
    override fun onResume() {
        super.onResume()
        val gson = Gson()
        val json = arguments?.getString("recruitmentJson")
        val recruitment = gson.fromJson(json, RecruitmentDto::class.java)
        val currentCountDouble = recruitment.currentCount
        loadStudyGuest(currentCountDouble)
    }

    @SuppressLint("SetTextI18n")
    private fun loadStudyGuest(currentCount: Int) {
        val gson = Gson()
        val json = arguments?.getString("recruitmentJson")
        val recruitment = gson.fromJson(json, RecruitmentDto::class.java)

        binding.guestNicknameText.text = recruitment.nickname
        binding.guestTitleText.text = recruitment.title
        binding.guestContentText.text = recruitment.content
        binding.guestFieldText.text = recruitment.field
        binding.guestCountText.text = "$currentCount / ${recruitment.count}"
        binding.guestCurrentText.text = recruitment.modifiedDateTime ?: recruitment.currentDateTime ?: ""

    }
}