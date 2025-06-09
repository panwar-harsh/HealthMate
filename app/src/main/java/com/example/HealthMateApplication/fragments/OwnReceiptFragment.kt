package com.example.HealthMateApplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.HealthMateApplication.adapters.ReceiptAdapter
import com.example.HealthMateApplication.databinding.FragmentOwnReceiptBinding
import com.example.HealthMateApplication.models.Report
import com.example.HealthMateApplication.utils.FirebaseHelper

class OwnReceiptFragment : Fragment() {

    private var _binding: FragmentOwnReceiptBinding? = null
    private val binding get() = _binding!!
    private lateinit var receiptAdapter: ReceiptAdapter
    private val reportList = mutableListOf<Report>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOwnReceiptBinding.inflate(inflater, container, false)

        // Set up RecyclerView
        setupRecyclerView()


        fetchUserEmail { userEmail ->
            fetchUserAadhaar(userEmail) { userAadhaar ->
                fetchReports(userAadhaar)
            }
        }

        return binding.root
    }

    // Set up RecyclerView with the adapter
    private fun setupRecyclerView() {
        receiptAdapter = ReceiptAdapter(reportList, isParentalReport = false) // User report, set false
        binding.recyclerViewReceipts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = receiptAdapter
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

    private fun fetchUserAadhaar(userEmail: String, callback: (String) -> Unit) {
        val userRef = FirebaseHelper.database.getReference("users")
        userRef.orderByChild("user_email").equalTo(userEmail).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (userSnapshot in snapshot.children) {
                    val userAadhaar = userSnapshot.child("user_aadhaar").getValue(String::class.java)
                    if (!userAadhaar.isNullOrEmpty()) {
                        callback(userAadhaar)
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
        FirebaseHelper.reportsRef.orderByChild("user_report_aadhaar").equalTo(userAadhaar)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    reportList.clear()
                    var totalPrice = 0.0
                    for (reportSnapshot in snapshot.children) {
                        val report = reportSnapshot.getValue(Report::class.java)
                        report?.let {
                            reportList.add(it)
                            val price = it.user_report_price?.toString()?.toDoubleOrNull() ?: 0.0
                            totalPrice += price
                        }
                    }
                    receiptAdapter.notifyDataSetChanged()
                    binding.textViewTotalPrice.text = "Total Price: Rs%.2f".format(totalPrice)
                } else {
                    Toast.makeText(requireContext(), "No reports found for this user", Toast.LENGTH_SHORT).show()
                    reportList.clear()
                    receiptAdapter.notifyDataSetChanged()
                    binding.textViewTotalPrice.text = "Total Price: Rs0.00"
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching reports: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


