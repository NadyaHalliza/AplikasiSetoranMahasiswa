package com.example.aplikasisetoranmahasiswa.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasisetoranmahasiswa.R
import com.example.aplikasisetoranmahasiswa.model.DetailItemSetoran

class DetailSetoranAdapter(private var setoranList: List<DetailItemSetoran>) :
    RecyclerView.Adapter<DetailSetoranAdapter.DetailSetoranViewHolder>() {

    // ViewHolder class
    class DetailSetoranViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNama: TextView = itemView.findViewById(R.id.textViewNamaKomponen)
        val textViewId: TextView = itemView.findViewById(R.id.textViewIdKomponen)
        val textViewIcon: TextView = itemView.findViewById(R.id.textViewIcon)
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailSetoranViewHolder {
        // Inflate the item layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setoran, parent, false)
        return DetailSetoranViewHolder(view)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: DetailSetoranViewHolder, position: Int) {
        val setoran = setoranList[position]

        // Set nama surat
        holder.textViewNama.text = setoran.nama ?: "Nama tidak tersedia"

        if (setoran.sudahSetor) {
            // Sudah setor state
            holder.textViewId.text = "${setoran.label} - ✅ Sudah Setor"
            holder.textViewIcon.text = "✅"
            holder.textViewId.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.success_green)
            )
        } else {
            // Belum setor state
            holder.textViewId.text = "${setoran.label} - ⏳ Belum Setor"
            holder.textViewIcon.text = "📖"
            holder.textViewId.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.warning_orange)
            )
        }
    }

    // Return the size of your dataset
    override fun getItemCount(): Int = setoranList.size

    // Update adapter data
    fun updateData(newList: List<DetailItemSetoran>) {
        setoranList = newList
        notifyDataSetChanged()
    }
}