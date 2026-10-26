package com.myapp.callrecorder

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myapp.callrecorder.data.CallRecording
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingAdapter(private val items: List<CallRecording>) :
    RecyclerView.Adapter<RecordingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.txtNumber)
        val dateTime: TextView = view.findViewById(R.id.txtDateTime)
        val duration: TextView = view.findViewById(R.id.txtDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recording, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        holder.number.text = "${item.direction}: ${item.number}"
        holder.dateTime.text = fmt.format(Date(item.timestamp))
        holder.duration.text = "${item.durationSec}s"

        holder.itemView.setOnClickListener {
            // Simple inline playback when tapped
            val player = MediaPlayer()
            try {
                player.setDataSource(item.filePath)
                player.prepare()
                player.start()
            } catch (_: Exception) {
                // File missing or unreadable - silently ignore for this simple version
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
