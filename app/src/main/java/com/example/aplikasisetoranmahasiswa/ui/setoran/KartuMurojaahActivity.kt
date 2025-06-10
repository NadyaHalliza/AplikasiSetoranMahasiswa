package com.example.aplikasisetoranmahasiswa.ui.setoran

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.aplikasisetoranmahasiswa.R
import com.example.aplikasisetoranmahasiswa.network.RetrofitClient
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import okhttp3.ResponseBody
import java.io.File

class KartuMurojaahActivity : AppCompatActivity() {

    private val authToken: String
        get() = "Bearer " + getSharedPreferences("auth", MODE_PRIVATE)
            .getString("access_token", "")!!  // Pastikan kamu simpan token saat login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kartu_murojaah)

        downloadAndOpenKartuMurojaah()
    }

    private fun downloadAndOpenKartuMurojaah() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.downloadKartuMurojaah(authToken)
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val pdfFile = savePdfToFile(body)
                        openPdfFile(pdfFile)
                    }
                } else {
                    Toast.makeText(this@KartuMurojaahActivity, "Gagal download: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@KartuMurojaahActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePdfToFile(body: ResponseBody): File {
        val file = File(getExternalFilesDir(null), "kartu_murojaah.pdf")
        val sink = file.sink().buffer()
        sink.writeAll(body.source())
        sink.close()
        return file
    }

    private fun openPdfFile(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",  // Pastikan authority di AndroidManifest sesuai
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Tidak ada aplikasi pembuka PDF", Toast.LENGTH_LONG).show()
        }
    }
}