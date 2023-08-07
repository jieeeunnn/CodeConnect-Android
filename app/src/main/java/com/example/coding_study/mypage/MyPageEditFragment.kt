package com.example.coding_study.mypage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.coding_study.dialog.ConfirmDialog
import com.example.coding_study.common.LoadImageTask
import com.example.coding_study.R
import com.example.coding_study.common.TokenManager
import com.example.coding_study.databinding.MypageEditBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream

class MyPageEditFragment:Fragment(R.layout.mypage_edit) {
    private lateinit var binding: MypageEditBinding
    private var selectedFields = mutableListOf<String>() // selectedFields 리스트 정의
    private lateinit var addressViewModel: MyPageAddressViewModel
    private val tokenManager: TokenManager by lazy { TokenManager(requireContext()) }

    @SuppressLint("CheckResult")
    private fun displaySelectedImage(selectedImageUri: Uri?) {
        val imageView = binding.myPageProfileImage
        if (selectedImageUri != null) {
            val glideRequest = Glide.with(this)
                .asBitmap()
                .load(selectedImageUri)

            glideRequest.addListener(object : RequestListener<Bitmap?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // 이미지 로딩 실패 시 처리
                    imageView.setImageDrawable(null)
                    imageView.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let { bitmap ->
                        val croppedBitmap = cropToSquare(bitmap)
                        setCircularImage(imageView, croppedBitmap)
                        imageView.visibility = View.VISIBLE
                    }
                    return true
                }
            })

            // 이미지를 원하는 크기로 제한
            val targetWidth = 300 // 원하는 가로 크기
            val targetHeight = 300 // 원하는 세로 크기
            glideRequest.override(targetWidth, targetHeight)

            glideRequest.into(imageView)
        } else {
            // 이미지가 없는 경우, ImageView를 빈 상태로 남겨둠
            imageView.setImageDrawable(null)
            imageView.visibility = View.GONE
        }
    }


    // 이미지를 정사각형으로 크롭하는 함수
    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = bitmap.width.coerceAtMost(bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    // 이미지를 정사각형으로 크롭한 후 동그라미 형태로 보여주는 함수
    private fun setCircularImage(imageView: ImageView, bitmap: Bitmap) {
        val croppedBitmap = cropToSquare(bitmap)
        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(imageView.resources, croppedBitmap)
        roundedBitmapDrawable.isCircular = true
        imageView.setImageDrawable(roundedBitmapDrawable)
    }


    private fun updateSelectedFields(field: String) { // 클릭된 버튼을 selectedFields 리스트에 추가하는 함수 (2개 선택)
        if (selectedFields.contains(field)) { // 이미 선택된 상태였을 경우
            selectedFields.remove(field) // 필드값 삭제
        } else {
            if (selectedFields.size < 2) { // 선택되지 않은 상태였을 경우, selectedFields 리스트에 필드값이 2개 미만이면 리스트에 필드값 추가
                selectedFields.add(field)
            }
        }
    }

    private fun updateButtonAppearance(button: Button, isSelected: Boolean) {  // field 버튼이 눌리면 색상 변경하는 함수
        if (isSelected) {
            button.setBackgroundResource(R.drawable.round_rect_stroke2)
        } else {
            button.setBackgroundResource(R.drawable.round_rect_mainblue)
        }
    }

    fun saveNickname(context: Context, nickname: String?) {
        val sharedPreferences = context.getSharedPreferences("MyNickname", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("nickname", nickname)
        if (!editor.commit()) {
            Log.e("saveNickname_MyPageEditFragment", "Failed to save nickname")
        }
    }
    fun saveAddress(context: Context, address: String?) {
        val sharedPreferences = context.getSharedPreferences("MyAddress", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("address", address)
        if (!editor.commit()) {
            Log.e("saveAddress", "Failed to save address")
        }
    }
    // saveFields 함수 (로컬 저장소에 필드값 저장하는 함수)
    fun saveFields(context: Context, postFields: List<String>) {
        val sharedPreferencesFields = context.getSharedPreferences("MyFields", Context.MODE_PRIVATE)
        val editor = sharedPreferencesFields.edit()
        val postFieldsString = postFields.joinToString(",")
        editor.putString("fields", postFieldsString)
        if (!editor.commit()) {
            Log.e("saveFields", "Failed to save Fields")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MypageEditBinding.inflate(inflater, container, false)
        val myPageViewModel: MyPageViewModel by viewModels({ requireParentFragment() })
        val myProfile: MyProfile? = myPageViewModel.getMyProfile().value

        addressViewModel = ViewModelProvider(requireActivity()).get(MyPageAddressViewModel::class.java)


        if (myProfile != null) {
            binding.myPageNewId.setText(myProfile.nickname)
            binding.myPageNewAddress.text = myProfile.address

            val imageUrl: String? = "http://52.79.53.62:8080/"+ "${myProfile.profileImagePath}"
            val imageView: ImageView = binding.myPageProfileImage
            val loadImageTask = LoadImageTask(imageView)
            loadImageTask.execute(imageUrl)

            binding.myPageNewAddress.setOnClickListener {
                val addressFragment = MyPageAddressFragment()

                childFragmentManager.beginTransaction()
                    .replace(R.id.myPageEdit_layout, addressFragment)
                    .addToBackStack(null)
                    .commit()
            }

            addressViewModel.getSelectedAddress().observe(viewLifecycleOwner) {
                address ->
                if (address == "") {
                    binding.myPageNewAddress.text = myProfile.address
                } else {
                    binding.myPageNewAddress.text = address

                }

                Log.e("MyPageEditFragment", "selected address: $address")
            }

            val fieldList: List<String> = myProfile.fieldList

            // 필드와 버튼 ID 간의 매핑을 위한 HashMap 생성
            val fieldButtonMap = hashMapOf(
                "안드로이드" to binding.myPageAndroidBtn,
                "ios" to binding.myPageIosBtn,
                "알고리즘" to binding.myPageAlgorithmBtn,
                "데이터베이스" to binding.myPageDatabaseBtn,
                "운영체제" to binding.myPageOsBtn,
                "서버" to binding.myPageServerBtn,
                "웹" to binding.myPageWebBtn,
                "머신러닝" to binding.myPageMachinBtn,
                "기타" to binding.myPageEtcBtn
            )

            // 필드 리스트에 있는 모든 필드에 대해 버튼 색상 업데이트
            for (field in fieldList) {
                if (fieldButtonMap.containsKey(field)) {
                    val button = fieldButtonMap[field]
                    if (button != null) {
                        updateSelectedFields(field)
                        updateButtonAppearance(button, true)
                    }
                }
            }
        }

        binding.myPageAndroidBtn.setOnClickListener {
            updateSelectedFields("안드로이드")
            updateButtonAppearance(binding.myPageAndroidBtn, selectedFields.contains("안드로이드"))
        }

        binding.myPageIosBtn.setOnClickListener {
            updateSelectedFields("ios")
            updateButtonAppearance(binding.myPageIosBtn, selectedFields.contains("ios"))
        }

        binding.myPageAlgorithmBtn.setOnClickListener {
            updateSelectedFields("알고리즘")
            updateButtonAppearance(binding.myPageAlgorithmBtn, selectedFields.contains("알고리즘"))
        }

        binding.myPageDatabaseBtn.setOnClickListener {
            updateSelectedFields("데이터베이스")
            updateButtonAppearance(binding.myPageDatabaseBtn, selectedFields.contains("데이터베이스"))
        }

        binding.myPageOsBtn.setOnClickListener {
            updateSelectedFields("운영체제")
            updateButtonAppearance(binding.myPageOsBtn, selectedFields.contains("운영체제"))
        }

        binding.myPageServerBtn.setOnClickListener {
            updateSelectedFields("서버")
            updateButtonAppearance(binding.myPageServerBtn, selectedFields.contains("서버"))
        }

        binding.myPageWebBtn.setOnClickListener {
            updateSelectedFields("웹")
            updateButtonAppearance(binding.myPageWebBtn, selectedFields.contains("웹"))
        }

        binding.myPageMachinBtn.setOnClickListener {
            updateSelectedFields("머신러닝")
            updateButtonAppearance(binding.myPageMachinBtn, selectedFields.contains("머신러닝"))
        }

        binding.myPageEtcBtn.setOnClickListener {
            updateSelectedFields("기타")
            updateButtonAppearance(binding.myPageEtcBtn, selectedFields.contains("기타"))
        }


        binding.myPageUploadButton.setOnClickListener {// 사진은 선택 안했을 경우 사용되는 수정 버튼
            val nickname = binding.myPageNewId.text.toString()
            val address = binding.myPageNewAddress.text.toString()
            val fieldList = selectedFields.toList()
            var myPageEditRequest = MyPageEditRequest(nickname, address, fieldList, null)

            editMyPage(myPageEditRequest)
        }



        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data // 선택된 이미지를 여기서 처리
                val selectedImageUri = intent?.data // 선택된 이미지 URI를 필요에 따라 처리

                displaySelectedImage(selectedImageUri)

                val contentResolver: ContentResolver = requireContext().contentResolver
                val inputStream: InputStream? = selectedImageUri?.let { contentResolver.openInputStream(it) }
                val imageBytes: ByteArray = inputStream?.readBytes() ?: ByteArray(0)

                val base64Image: String? = if (imageBytes.isNotEmpty()) {
                    Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                } else {
                    null
                }

                binding.myPageUploadButton.setOnClickListener {//프로필 이미지를 선택했을 경우 사용되는 수정 버튼
                    val nickname = binding.myPageNewId.text.toString()
                    val address = binding.myPageNewAddress.text.toString()
                    val fieldList = selectedFields.toList()

                    if (base64Image != null) {
                        Log.e("myPageEditFragment encoding image", base64Image)
                    }

                    var myPageEditRequest = MyPageEditRequest(nickname, address, fieldList, base64Image)

                    editMyPage(myPageEditRequest)
                }

            }
        }

        binding.myPageProfileImage.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(galleryIntent)
        }

        return binding.root
    }

    fun editMyPage(MyPageEditRequest: MyPageEditRequest) {
        if (selectedFields.size < 2) { // 필드를 2개 선택하지 않았을 경우 Toast 메세지 띄우기
            val confirmDialog = ConfirmDialog("관심 언어 2개를 선택해 주세요")
            confirmDialog.isCancelable = false
            confirmDialog.show(childFragmentManager, "joinFragment joinButton2_관심사 선택")
        } else {
            val nickname = binding.myPageNewId.text.toString()
            val address = binding.myPageNewAddress.text.toString()
            val fieldList = selectedFields.toList()

            val token = tokenManager.getAccessToken()
            tokenManager.checkAccessTokenExpiration()

            val retrofitBearer = Retrofit.Builder()
                .baseUrl("http://52.79.53.62:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer $token")
                                .build()
                            Log.d("TokenInterceptor_StudyFragment", "Token: $token")
                            chain.proceed(request)
                        }
                        .build()
                )
                .build()

            val myPageEditService = retrofitBearer.create(MyPageEditService::class.java)

            myPageEditService.myPageEditPost(MyPageEditRequest)
                .enqueue(object : Callback<MyPageEditResponse> {
                    override fun onResponse(
                        call: Call<MyPageEditResponse>,
                        response: Response<MyPageEditResponse>
                    ) {
                        if (response.isSuccessful) {
                            Log.e("MyPageEditFragment response code is", "${response.code()}")
                            Log.e("MyPageEditFragment response body is", "${response.body()}")

                            context?.let { it1 -> saveNickname(it1, nickname) } // 수정된 nickname 저장
                            context?.let { it1 -> saveAddress(it1, address) }
                            context?.let { it1 -> saveFields(it1, fieldList) }

                            val parentFragment = parentFragment
                            if (parentFragment is MyPageFragment) {
                                parentFragment.onResume()
                            }

                            val parentFragmentManager = requireActivity().supportFragmentManager
                            parentFragmentManager.popBackStack()
                        } else {
                            Log.e("MyPageEditFragment_onResponse", "But not success")

                        }
                    }

                    override fun onFailure(call: Call<MyPageEditResponse>, t: Throwable) {
                        Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                    }

                })
        }
    }
}