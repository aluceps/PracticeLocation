package me.aluceps.practicelocation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.aluceps.practicelocation.databinding.ActivityFirstBinding

class FirstActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFirstBinding

    private val permissionHelper = LocationPermissionHelper(this, object : LocationPermissionHelper.Listener {
        override fun updated(state: LocationPermissionHelper.PermissionState) {
            binding.status.text = state.toString()
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    private fun setupView() {
        binding.request.setOnClickListener {
            permissionHelper.requestPermission()
        }
    }
}

