package com.example.aplikasisetoranmahasiswa.ui.setoran

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
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
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SetoranActivity : AppCompatActivity() {

    private lateinit var setoranAdapter: DetailSetoranAdapter
    private var currentSetoranList: List<DetailItemSetoran> = emptyList()

    private lateinit var recyclerViewSetoran: RecyclerView
    private lateinit var textViewProgress: TextView
    private lateinit var buttonDownloadLaporan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setoran)

        recyclerViewSetoran = findViewById(R.id.recyclerViewSetoran)
        textViewProgress = findViewById(R.id.textViewProgress)
        buttonDownloadLaporan = findViewById(R.id.buttonDownloadLaporan)

        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val authToken = sharedPref.getString("access_token", null)
        val nimMahasiswa = sharedPref.getString("user_nim", "NIM Tidak Ada")
        val namaMahasiswa = sharedPref.getString("user_name", "Nama Tidak Ada")

        setupRecyclerView()

        fetchStatusHafalan(authToken)

        buttonDownloadLaporan.setOnClickListener {
            if (currentSetoranList.isNotEmpty()) {
                // SESUDAH
                downloadLaporan(nimMahasiswa ?: "0000", namaMahasiswa ?: "Mahasiswa")
            } else {
                Toast.makeText(this, "Data setoran belum dimuat.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchStatusHafalan(token: String?) {
        if (token == null) {
            Toast.makeText(this, "Sesi tidak valid, silakan login ulang", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getStatusHafalan("Bearer $token")

                if (response.isSuccessful) {
                    // ✅ PERBAIKAN: Mengambil list dari .data.setoran.detail
                    val suratStatusList = response.body()?.data?.setoran?.detail

                    if (suratStatusList != null) {

                        // ✅ PERBAIKAN: Mapping menggunakan field yang benar dari SuratStatus baru
                        currentSetoranList = suratStatusList.map { surat ->
                            DetailItemSetoran(
                                id = surat.id ?: "", // Menggunakan id asli dari server
                                nama = surat.nama ?: "Tanpa Nama",
                                label = surat.label ?: "", // Menggunakan label asli dari server
                                sudahSetor = surat.sudahSetor,
                                infoSetoran = null
                            )
                        }

                        updateRecyclerView(currentSetoranList)
                        updateProgress()
                        Toast.makeText(this@SetoranActivity, "Data berhasil dimuat!", Toast.LENGTH_SHORT).show()
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

    private fun downloadLaporan(nim: String, nama: String) {
        Toast.makeText(this, "📄 Mempersiapkan laporan...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            try {
                val pdfBytes = withContext(Dispatchers.IO) { generateSimplePdfReport(nim, nama, currentSetoranList) }
                withContext(Dispatchers.IO) {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "laporan_setoran_${nim}_${timestamp}.pdf"
                    savePdfToDownloads(this@SetoranActivity, pdfBytes, fileName)
                }
                Log.d("DownloadLaporan", "Laporan berhasil dibuat dan disimpan.")
            } catch (e: Exception) {
                Log.e("DownloadLaporan", "Gagal membuat atau menyimpan laporan", e)
                Toast.makeText(this@SetoranActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun generateSimplePdfReport(nim: String, nama: String, data: List<DetailItemSetoran>): ByteArray {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var yPosition = 40f

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Laporan Setoran Hafalan", 40f, yPosition, paint)
        yPosition += 40f

        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Nama: $nama", 40f, yPosition, paint)
        yPosition += 20f
        canvas.drawText("NIM: $nim", 40f, yPosition, paint)
        yPosition += 40f

        paint.isFakeBoldText = true
        canvas.drawText("No.", 40f, yPosition, paint)
        canvas.drawText("Nama Surat", 80f, yPosition, paint)
        canvas.drawText("Status", 450f, yPosition, paint)
        yPosition += 25f
        canvas.drawLine(40f, yPosition - 15, 555f, yPosition - 15, paint)

        paint.isFakeBoldText = false
        data.forEachIndexed { index, item ->
            canvas.drawText("${index + 1}.", 40f, yPosition, paint)
            canvas.drawText(item.nama ?: "N/A", 80f, yPosition, paint)
            val status = if (item.sudahSetor) "✅ Sudah Setor" else "⏳ Belum Setor"
            canvas.drawText(status, 450f, yPosition, paint)
            yPosition += 20f

            if (yPosition > 800) {
                document.finishPage(page)
                val newPage = document.startPage(pageInfo)
                canvas = newPage.canvas
                yPosition = 40f
            }
        }

        document.finishPage(page)
        val outputStream = ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()
        return outputStream.toByteArray()
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