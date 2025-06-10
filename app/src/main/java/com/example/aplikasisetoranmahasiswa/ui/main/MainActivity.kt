package com.example.aplikasisetoranmahasiswa.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.example.aplikasisetoranmahasiswa.ui.setoran.SetoranActivity // Import SetoranActivity
import com.example.aplikasisetoranmahasiswa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set root view dari binding object
        binding.buttonGoToSetoran.setOnClickListener {
            val intent = Intent(this, SetoranActivity::class.java)
            startActivity(intent)
        }
    }
}