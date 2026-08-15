package com.example.onlinementor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CertificateAdapter(
    private val certificateTitles: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertificateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_certificate, parent, false)
        return CertificateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CertificateViewHolder, position: Int) {
        val title = certificateTitles[position]
        holder.titleTextView.text = title
        holder.itemView.setOnClickListener { onItemClick(title) }
    }

    override fun getItemCount(): Int = certificateTitles.size

    class CertificateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.certificateTitle)
    }
}
