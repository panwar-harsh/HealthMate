package com.example.HealthMateApplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.HealthMateApplication.R
import com.example.HealthMateApplication.models.Appointment

class AppointmentAdapter(private val appointmentsList: List<Appointment>) :
    RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val testAppointment: TextView? = itemView.findViewById(R.id.tvTestAppointment)
        val dateAppointment: TextView? = itemView.findViewById(R.id.tvDateAppointment)
        val timeAppointment: TextView? = itemView.findViewById(R.id.tvTimeAppointment)
        val addressAppointment: TextView? = itemView.findViewById(R.id.tvAddressAppointment)
        val statusAppointment: TextView? = itemView.findViewById(R.id.textViewAppointmentStatus)
        val acceptedStatus:View?=itemView.findViewById(R.id.statusSubmittedLine)
        val rejectedStatus:View?=itemView.findViewById(R.id.statusShortlistedLine)
        val completedStatus:View?=itemView.findViewById(R.id.statusOfferLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointmentsList[position]

        holder.testAppointment?.text = appointment.user_test_appointment.ifBlank { "N/A" }
        holder.dateAppointment?.text = appointment.user_date_appointment.ifBlank { "N/A" }
        holder.timeAppointment?.text = appointment.user_time_appointment.ifBlank { "N/A" }
        holder.addressAppointment?.text = appointment.user_address_appointment.ifBlank { "N/A" }

        holder.statusAppointment?.text = when (appointment.user_status_appointment) {
            "P" -> "Status: Pending"
            "T" -> "Status: Confirmed"
            "R" -> "Status: Rejected"
            "C" -> "Status: Completed"
            else -> "Status: Unknown"
        }

        val context = holder.itemView.context

        holder.acceptedStatus?.setBackgroundColor(
            ContextCompat.getColor(context,
                if (appointment.user_status_appointment == "T") R.color.Darkblue else R.color.gray)
        )

        holder.rejectedStatus?.setBackgroundColor(
            ContextCompat.getColor(context,
                if (appointment.user_status_appointment == "R") R.color.red else R.color.gray)
        )

        holder.completedStatus?.setBackgroundColor(
            ContextCompat.getColor(context,
                if (appointment.user_status_appointment == "C") R.color.Darkblue else R.color.gray)
        )
    }


    override fun getItemCount(): Int {
        return appointmentsList.size
    }
}

