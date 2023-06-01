package com.example.coding_study

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.coding_study.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class MainActivity : AppCompatActivity() {
    val binding by lazy {ActivityMainBinding.inflate(layoutInflater)}
    private lateinit var logButton: Button
    private lateinit var joinButton: Button

    fun saveToken(context: Context, token: String?) { // 토큰 저장 함수
        if (token == null) {
            Log.e("saveToken", "Token is null, failed to save token")
            return
        }
        val sharedPreferences = context.getSharedPreferences("MyToken", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("token", token)
        if (!editor.commit()) {
            Log.e("saveToken", "Failed to save token")
        }
    }
    // 토큰 만료시간 exp가 되면 서버에게 새 토큰 요청

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

    fun saveNickname(context: Context, nickname: String?) {
        val sharedPreferences = context.getSharedPreferences("MyNickname", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("nickname", nickname)
        if (!editor.commit()) {
            Log.e("saveNickname", "Failed to save nickname")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        logButton = binding.logButton
        joinButton = binding.joinButton

        val retrofit = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val loginService = retrofit.create(LoginService::class.java)

        //로그인 버튼 누를 때
        binding.logButton.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()

            val loginRequest = LoginRequest(email, password)

            Log.e("Login", "email: $email, password: $password") // 내가 보낸 data Log 출력

            //@GET 사용시 loginRequest 대신 email= email, password= password
            loginService.requestLogin(loginRequest).enqueue(object: Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) { // 통신에 성공했을 때
                    if (response.isSuccessful) {

                        val loginResponse = response.body() // 서버에서 받아온 응답 데이터
                        val code = response.code() // 서버 응답 코드
                        Log.e("login", "is : ${response.body()}") // 서버에서 받아온 응답 데이터 log 출력
                        Log.e("response code", "is : $code") // 서버 응답 코드 log 출력

                        if (loginResponse?.result == true && loginResponse.data != null) {
                            val receivedToken = loginResponse.data!!.token// 토큰 저장
                            saveToken(applicationContext, receivedToken) // receivedToken이 null이 아닌 경우 'let'블록 내부에서 savedToken 함수를 호출해 token 저장

                            val receivedAddress = loginResponse.data!!.address
                            saveAddress(applicationContext, receivedAddress)

                            val receivedFields = loginResponse.data!!.fieldList
                            saveFields(applicationContext, receivedFields)

                            val receivedNickname = loginResponse.data!!.nickname
                            saveNickname(applicationContext, receivedNickname)

                            val nextIntent = Intent(this@MainActivity, SecondActivity::class.java) // 스터디 게시글 화면으로 이동
                            startActivity(nextIntent)
                        }
                        val confirmDialog = loginResponse?.let { it1 -> ConfirmDialog(it1.message) }
                        if (confirmDialog != null) {
                            confirmDialog.isCancelable = false
                            confirmDialog.show(supportFragmentManager, "studyGuestFragment_guestButton")

                        }
                    } else {
                         //서버로부터 응답이 실패한 경우
                        val confirmDialog = ConfirmDialog("아이디, 비밀번호를 확인해주세요")
                        confirmDialog.isCancelable = false
                        confirmDialog.show(supportFragmentManager, "studyGuestFragment_guestButton")                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) { // 통신에 실패했을 때
                    val confirmDialog = ConfirmDialog("서버 연결 실패")
                    confirmDialog.isCancelable = false
                    confirmDialog.show(supportFragmentManager, "studyGuestFragment_guestButton")
                }
            })
        }

        //회원가입 버튼을 누를 때
        binding.joinButton.setOnClickListener{
            val joinFragment = JoinFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.mainLayout, joinFragment)
                .addToBackStack(null)
                .commit()
        }
    }
    fun showButton() {
        logButton.visibility = View.VISIBLE
        joinButton.visibility = View.VISIBLE
    }

    fun hideButton() {
        logButton.visibility = View.GONE
        joinButton.visibility = View.GONE
    }

    override fun onBackPressed() { // AddressFragment에서 뒤로가기 버튼 처리 (JoinFragment로 이동)
        val joinFragment = supportFragmentManager.findFragmentByTag("JOIN_FRAGMENT") as? JoinFragment
        if (joinFragment != null) {
            supportFragmentManager.popBackStack("JOIN_FRAGMENT", 0)
        } else {
            super.onBackPressed()
        }
    }

}