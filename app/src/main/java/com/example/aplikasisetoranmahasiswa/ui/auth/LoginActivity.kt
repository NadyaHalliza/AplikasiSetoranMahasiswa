package com.example.aplikasisetoranmahasiswa.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.aplikasisetoranmahasiswa.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import com.example.aplikasisetoranmahasiswa.ui.main.MainActivity
import com.example.setoranhafalanapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonLogin.setOnClickListener {
            val username = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                performLogin(username, password)
            } else {
                Toast.makeText(this, "Username dan Password tidak boleh kosong", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.kcAuthService.login(
                        clientId = "setoran-mobile-dev",
                        clientSecret = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                        grantType = "password",
                        username = username,
                        password = password,
                        scope = "openid profile email"
                    )
                    if (response.isSuccessful) {
                        val loginResponse = response.body()

                        if (loginResponse != null) {
                            // 🔁 Panggil userinfo di sini (masih dalam Dispatchers.IO)
                            val userInfoResponse =
                                RetrofitClient.kcAuthService.getUserInfo("Bearer ${loginResponse.accessToken}")
                            val userInfo = userInfoResponse.body()
                            val nimFromToken = userInfo?.preferredUsername ?: username

                            // Pindah ke Main thread untuk UI operations
                            withContext(Dispatchers.Main) {
                                // Simpan data ke SharedPreferences
                                val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("access_token", loginResponse.accessToken)
                                    putString("refresh_token", loginResponse.refreshToken)
                                    putString("user_nim", nimFromToken)
                                    apply()
                                }

                                Log.d("LoginActivity", "NIM dari token: $nimFromToken")

                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Berhasil!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent =
                                    Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Log.e("LoginActivity", "Login response body is null")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Gagal: Respons tidak valid",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(
                            "LoginActivity",
                            "Login Gagal: ${response.code()} - ${response.message()} - Error Body: $errorBody"
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Login Gagal: Cek username/password Anda",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Error selama login", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Terjadi kesalahan jaringan: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }
    }
}