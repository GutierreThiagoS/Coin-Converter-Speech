package com.example.coinconverterspeech.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.coinconverterspeech.R
import com.example.coinconverterspeech.databinding.ActivityMainBinding
import com.example.coinconverterspeech.ui.history.HistoryActivity

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var host: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        host = NavHostFragment.create(R.navigation.navigation)
        setNavControler()
        setBottomNavigationMenu()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_history) {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setBottomNavigationMenu() {
        binding.navigationBottom.setupWithNavController(findNavController(R.id.fragment_main))
    }

    fun setNavControler(){
        navController = findNavController(R.id.fragment_main)
    }

}