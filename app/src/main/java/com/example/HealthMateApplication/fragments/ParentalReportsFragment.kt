package com.example.HealthMateApplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.HealthMateApplication.adapters.ReportAdapter
import com.example.HealthMateApplication.databinding.FragmentParentalReportsBinding
import com.example.HealthMateApplication.models.Report
import com.example.HealthMateApplication.utils.FirebaseHelper

class ParentalReportsFragment : Fragment() {
    private var _binding: FragmentParentalReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var reportAdapter: ReportAdapter
    private val reportList = mutableListOf<Report>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentParentalReportsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        // Fetch reports when the fragment is opened
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

    // Set up RecyclerView with the adapter
    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter(reportList)
        binding.recyclerViewParentalReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportAdapter
        }
    }

    // Fetch the current user's email from Firebase Auth
    private fun fetchUserEmail(callback: (String) -> Unit) {
        val user = FirebaseHelper.getAuth().currentUser
        user?.let {
            val userEmail = it.email
            if (!userEmail.isNullOrEmpty()) {
                callback(userEmail)
            } else {
                Toast.makeText(requireContext(), "User email not found", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch the Aadhaar of the user whose email matches the authenticated email
    private fun fetchUserAadhaar(userEmail: String, callback: (String) -> Unit) {
        FirebaseHelper.usersRef.orderByChild("user_email").equalTo(userEmail).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val userAadhaar = userSnapshot.child("user_aadhaar").value as String?
                        userAadhaar?.let {
                            callback(it) // Pass the Aadhaar back to the callback
                            return@addOnSuccessListener
                        }
                    }
                    Toast.makeText(requireContext(), "Aadhaar not found for the user", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No matching user found for this email", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching user Aadhaar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch the parental reports based on the Aadhaar
    private fun fetchReports(userAadhaar: String) {
        val queryField = "user_report_parent_aadhaar"

        FirebaseHelper.reportsRef.orderByChild(queryField).equalTo(userAadhaar)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    reportList.clear()
                    for (reportSnapshot in snapshot.children) {
                        val report = reportSnapshot.getValue(Report::class.java)
                        report?.let {
                            reportList.add(it)
                        }
                    }
                    reportAdapter.notifyDataSetChanged()
                } else {
                    showNoReportsFound()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching reports: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Show "No reports found" message
    private fun showNoReportsFound() {
        binding.recyclerViewParentalReports.visibility = View.GONE
        binding.noReportsTextView.visibility = View.VISIBLE
    }
}
