package com.example.HealthMateApplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.HealthMateApplication.adapters.ReceiptAdapter
import com.example.HealthMateApplication.databinding.FragmentParentalReceiptBinding
import com.example.HealthMateApplication.models.Report
import com.example.HealthMateApplication.utils.FirebaseHelper

class ParentalReceiptFragment : Fragment() {

    private var _binding: FragmentParentalReceiptBinding? = null
    private val binding get() = _binding!!
    private lateinit var receiptAdapter: ReceiptAdapter
    private val reportList = mutableListOf<Report>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParentalReceiptBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupEmptyState(false)

        // Fetch parental reports after getting user email and Aadhaar
        fetchUserEmail { userEmail ->
            fetchUserAadhaar(userEmail) { userAadhaar ->
                fetchReports(userAadhaar)
            }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        receiptAdapter = ReceiptAdapter(reportList, isParentalReport = true)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = receiptAdapter
        }
    }

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

    private fun fetchUserAadhaar(userEmail: String, callback: (String) -> Unit) {
        FirebaseHelper.usersRef.orderByChild("user_email").equalTo(userEmail).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val userAadhaar = userSnapshot.child("user_aadhaar").getValue(String::class.java)
                        userAadhaar?.let {
                            callback(it)
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

    private fun fetchReports(userAadhaar: String) {
        FirebaseHelper.reportsRef.orderByChild("user_report_parent_aadhaar").equalTo(userAadhaar)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    reportList.clear()
                    for (reportSnapshot in snapshot.children) {
                        val report = reportSnapshot.getValue(Report::class.java)
                        report?.let {
                            reportList.add(it)
                        }
                    }
                    receiptAdapter.notifyDataSetChanged()
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
        binding.recyclerView.visibility = if (showEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
