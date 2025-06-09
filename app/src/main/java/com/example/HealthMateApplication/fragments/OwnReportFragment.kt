package com.example.HealthMateApplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.HealthMateApplication.adapters.ReportAdapter
import com.example.HealthMateApplication.databinding.FragmentOwnReportBinding
import com.example.HealthMateApplication.models.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class OwnReportFragment : Fragment() {

    private var _binding: FragmentOwnReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var reportAdapter: ReportAdapter
    private val reportList = mutableListOf<Report>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnReportBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupEmptyState(false)  // Hide empty state initially

        fetchUserEmail { userEmail ->
            fetchUserAadhaar(userEmail) { userAadhaar ->
                fetchReports(userAadhaar)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter(reportList)
        binding.recyclerViewReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportAdapter
        }
    }

    private fun fetchUserEmail(callback: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.email?.let { userEmail ->
            callback(userEmail)
        } ?: run {
            Toast.makeText(requireContext(), "No user logged in or email not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserAadhaar(userEmail: String, callback: (String) -> Unit) {
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        usersRef.orderByChild("user_email").equalTo(userEmail).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userAadhaar = snapshot.children.firstOrNull()?.child("user_aadhaar")?.getValue(String::class.java)
                    if (!userAadhaar.isNullOrEmpty()) {
                        callback(userAadhaar)
                    } else {
                        Toast.makeText(requireContext(), "Aadhaar not found for the user", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "No matching user found for this email", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching user Aadhaar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchReports(userAadhaar: String) {
        val reportsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("reports")
        reportsRef.orderByChild("user_report_aadhaar").equalTo(userAadhaar).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    reportList.clear()
                    for (reportSnapshot in snapshot.children) {
                        val report = reportSnapshot.getValue(Report::class.java)
                        report?.let {
                            reportList.add(it)
                        }
                    }
                    reportAdapter.notifyDataSetChanged()
                    setupEmptyState(reportList.isEmpty())
                } else {
                    setupEmptyState(true)
                    Toast.makeText(requireContext(), "No reports found for this user", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching reports: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupEmptyState(showEmpty: Boolean) {
        binding.recyclerViewReports.visibility = if (showEmpty) View.GONE else View.VISIBLE
    }
}
