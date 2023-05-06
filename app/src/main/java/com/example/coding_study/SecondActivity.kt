package com.example.coding_study

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.coding_study.databinding.ActivitySecondBinding


class SecondActivity : AppCompatActivity() {
    private lateinit var appbarc : AppBarConfiguration
    private val binding by lazy {
        ActivitySecondBinding.inflate(layoutInflater)
    }


    //private val viewModel by viewModels<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //val toolbar = binding.toolBar // 툴바 선언
        //setSupportActionBar(toolbar)

        val nhf = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment

        //val appbarc = AppBarConfiguration(nhf.navController.graph)
        //setupActionBarWithNavController(nhf.navController,appbarc)

        binding.bottomNavigationView.setupWithNavController(nhf.navController)

        //val adapter = StudyAdapter(viewModel) // viewModel을 StudyAdapter에 주고

    }


    /*
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment).navigateUp(appbarc) || super.onSupportNavigateUp()
    }
     */
}