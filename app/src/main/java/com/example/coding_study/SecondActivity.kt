package com.example.coding_study

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.coding_study.databinding.ActivityMainBinding
import com.example.coding_study.databinding.ActivitySecondBinding
import com.example.coding_study.databinding.StudyUploadLayoutBinding
import com.example.coding_study.databinding.WriteStudyBinding



class SecondActivity : AppCompatActivity() {
    private lateinit var appbarc : AppBarConfiguration
    private val binding by lazy {
        ActivitySecondBinding.inflate(layoutInflater)
    }

    //private val viewModel by viewModels<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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