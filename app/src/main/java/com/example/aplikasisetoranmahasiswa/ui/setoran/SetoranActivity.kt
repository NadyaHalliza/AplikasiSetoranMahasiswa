package com.example.aplikasisetoranmahasiswa.ui.setoran

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasisetoranmahasiswa.R
import com.example.aplikasisetoranmahasiswa.model.DetailItemSetoran
import com.example.aplikasisetoranmahasiswa.model.SuratStatus
import com.example.aplikasisetoranmahasiswa.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SetoranActivity : AppCompatActivity() {

    private lateinit var setoranAdapter: DetailSetoranAdapter
    private var currentSetoranList: List<DetailItemSetoran> = emptyList()

    private lateinit var recyclerViewSetoran: RecyclerView
    private lateinit var textViewProgress: TextView
    private lateinit var buttonDownloadLaporan: Button

    // Deklarasikan variabel untuk data user di level kelas
    private var nimMahasiswa: String? = null
    private var authToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setoran)

        recyclerViewSetoran = findViewById(R.id.recyclerViewSetoran)
        textViewProgress = findViewById(R.id.textViewProgress)
        buttonDownloadLaporan = findViewById(R.id.buttonDownloadLaporan)

        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        authToken = sharedPref.getString("access_token", null)
        nimMahasiswa = sharedPref.getString("user_nim", null)
        // namaMahasiswa tidak perlu lagi untuk fungsi download dari server

        setupRecyclerView()

        fetchStatusHafalan(authToken)

        // ✅ PERUBAHAN 1: OnClickListener sekarang memanggil fungsi download dari server
        buttonDownloadLaporan.setOnClickListener {
            downloadPdfFromServer()
        }
    }

    // ✅ PERUBAHAN 2: Fungsi baru untuk download PDF dari Server
    private fun downloadPdfFromServer() {
        if (authToken == null || nimMahasiswa == null) {
            Toast.makeText(this, "Sesi tidak valid, silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "📄 Memulai download Kartu Muroja'ah...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.downloadKartuMurojaah("Bearer $authToken")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val fileName = "kartu_murojaah_${nimMahasiswa}_${timestamp}.pdf"

                        withContext(Dispatchers.IO) {
                            savePdfToDownloads(this@SetoranActivity, responseBody.bytes(), fileName)
                        }
                    } else {
                        Toast.makeText(this@SetoranActivity, "Gagal: Respons server kosong.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DownloadPDF", "Gagal download PDF: $errorBody")
                    Toast.makeText(this@SetoranActivity, "Gagal download: Server mengembalikan error.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("DownloadPDF", "Error saat download: ${e.message}", e)
                Toast.makeText(this@SetoranActivity, "Terjadi kesalahan jaringan.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ❌ PERUBAHAN 3: Fungsi lama untuk membuat PDF di HP sudah tidak diperlukan dan dihapus
    // private fun downloadLaporan(...) { ... }
    // private fun generateSimplePdfReport(...) { ... }


    // --- Fungsi lainnya tetap ada dan tidak berubah ---

    private fun fetchStatusHafalan(token: String?) {
        // ... (kode fungsi ini tetap sama seperti sebelumnya)
        if (token == null) {
            Toast.makeText(this, "Sesi tidak valid, silakan login ulang", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getStatusHafalan("Bearer $token")

                if (response.isSuccessful) {
                    val suratStatusList = response.body()?.data?.setoran?.detail

                    if (suratStatusList != null) {
                        currentSetoranList = suratStatusList.map { surat ->
                            DetailItemSetoran(
                                id = surat.id ?: "",
                                nama = surat.nama ?: "Tanpa Nama",
                                label = surat.label ?: "",
                                sudahSetor = surat.sudahSetor,
                                infoSetoran = null
                            )
                        }
                        updateRecyclerView(currentSetoranList)
                        updateProgress()
                        // Menghapus toast sukses agar tidak terlalu ramai
                        // Toast.makeText(this@SetoranActivity, "Data berhasil dimuat!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SetoranActivity, "List 'detail' tidak ditemukan di respons.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SetoranActivity", "Gagal mengambil data: $errorBody")
                    Toast.makeText(this@SetoranActivity, "Gagal mengambil data dari server.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SetoranActivity", "Error: ${e.message}", e)
                Toast.makeText(this@SetoranActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProgress() {
        val sudahSetor = currentSetoranList.count { it.sudahSetor }
        val total = currentSetoranList.size
        val percentage = if (total > 0) (sudahSetor * 100) / total else 0
        textViewProgress.text = "Progress Hafalan: $sudahSetor/$total Surat ($percentage%)"
    }

    private fun savePdfToDownloads(context: Context, pdfBytes: ByteArray, fileName: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        var stream: java.io.OutputStream? = null
        try {
            val uri = resolver.insert(collection, contentValues) ?: throw Exception("MediaStore insert failed")
            stream = resolver.openOutputStream(uri) ?: throw Exception("Gagal membuka output stream.")
            stream.write(pdfBytes)
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "✅ Laporan berhasil disimpan di Downloads!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("FileSaveError", "Error saving PDF: ${e.message}", e)
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Gagal menyimpan laporan: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            stream?.close()
        }
    }

    private fun setupRecyclerView() {
        setoranAdapter = DetailSetoranAdapter(emptyList())
        recyclerViewSetoran.apply {
            layoutManager = LinearLayoutManager(this@SetoranActivity)
            adapter = setoranAdapter
        }
    }

    private fun updateRecyclerView(setoranList: List<DetailItemSetoran>) {
        setoranAdapter.updateData(setoranList)
    }
}

// ... Kode untuk DetailSetoranAdapter tetap sama ...
class DetailSetoranAdapter(private var setoranList: List<DetailItemSetoran>) :
    RecyclerView.Adapter<DetailSetoranAdapter.DetailSetoranViewHolder>() {

    class DetailSetoranViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNama: TextView = itemView.findViewById(R.id.textViewNamaKomponen)
        val textViewId: TextView = itemView.findViewById(R.id.textViewIdKomponen)
        val textViewIcon: TextView = itemView.findViewById(R.id.textViewIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailSetoranViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setoran, parent, false)
        return DetailSetoranViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailSetoranViewHolder, position: Int) {
        val setoran = setoranList[position]
        holder.textViewNama.text = setoran.nama ?: "Nama tidak tersedia"

        if (setoran.sudahSetor) {
            holder.textViewId.text = "${setoran.label} - ✅ Sudah Setor"
            holder.textViewIcon.text = "✅"
            holder.textViewId.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green_700))
        } else {
            holder.textViewId.text = "${setoran.label} - ⏳ Belum Setor"
            holder.textViewIcon.text = "📖"
            holder.textViewId.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green_500))
        }
    }

    override fun getItemCount(): Int = setoranList.size

    fun updateData(newList: List<DetailItemSetoran>) {
        setoranList = newList
        notifyDataSetChanged()
    }
}