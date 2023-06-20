package me.aluceps.practicelocation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.aluceps.practicelocation.databinding.ActivityFirstBinding

class FirstActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFirstBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    private fun setupView() {
        binding.request.setOnClickListener {
        }
    }
}

