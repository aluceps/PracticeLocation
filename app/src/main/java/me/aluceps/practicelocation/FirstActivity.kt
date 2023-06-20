package me.aluceps.practicelocation

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import me.aluceps.practicelocation.databinding.ActivityFirstBinding

class FirstActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFirstBinding

    private val permissionHelper = LocationPermissionHelper(this, object : LocationPermissionHelper.Listener {
        override fun state(state: LocationPermissionHelper.PermissionState) {
            binding.status.text = state.toString()
        }

        override fun deny() {
            AlertDialog.Builder(this@FirstActivity)
                .setTitle("位置情報の許可")
                .setMessage("この機能を使うために必要なので有効にしてください")
                .setPositiveButton("はい", null)
                .show()
        }

        override fun showRequestReason(action: () -> Unit) {
            AlertDialog.Builder(this@FirstActivity)
                .setTitle("位置情報の許可")
                .setMessage("より便利にするため位置情報を常に許可するよう設定してください")
                .setPositiveButton("はい") { _, _ -> action.invoke() }
                .setNegativeButton("いいえ", null)
                .show()
        }

        override fun ready() {
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

