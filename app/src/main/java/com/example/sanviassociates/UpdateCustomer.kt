//package com.example.sanviassociates
//
//import android.content.ContentValues
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.LinearLayout
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.sanviassociates.databinding.ActivityUpdateCustomerBinding
//
//class UpdateCustomer : AppCompatActivity() {
//
//    private lateinit var binding: ActivityUpdateCustomerBinding
//    private var policyCounter = 1 // Counter for dynamically added policies
//    private lateinit var databaseHelper: DatabaseHelper // SQLite Database Helper
//    private var customerEntryId: Int = -1 // Customer Entry ID passed from HomePage
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityUpdateCustomerBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Initialize SQLite Database Helper
//        databaseHelper = DatabaseHelper(this)
//
//        // Get customer entry ID from intent
//        customerEntryId = intent.getIntExtra("UNIQUE_ID", -1)
//
//        // Set up click listeners for visibility toggling
//        setupSectionVisibilityListeners()
//
//        // Handle "Add More Policy" button
//        binding.btnAddMorePolicy.setOnClickListener {
//            addMorePolicy()
//        }
//
//        // Handle "View Policy" button
//        binding.btnViewPolicy.setOnClickListener {
//            fetchAndDisplayPolicies()
//        }
//
//        // Handle "Update" button
//        binding.updateButton.setOnClickListener {
//            updateCustomerData()
//        }
//
//        // Handle "Back" button
//        binding.ivUpdateBack.setOnClickListener {
//            onBackPressed()
//        }
//
//        // Fetch and populate customer data
//        fetchAndPopulateCustomerData()
//    }
//
//    // Section: Visibility Management
//    private fun setupSectionVisibilityListeners() {
//        binding.cardPersonalDetails.setOnClickListener {
//            toggleContainerVisibility(binding.containerPersonalDetails)
//        }
//        binding.cardFamilyDetails.setOnClickListener {
//            toggleContainerVisibility(binding.containerFamilyDetails)
//        }
//        binding.cardMeddicalDetails.setOnClickListener {
//            toggleContainerVisibility(binding.containerMeddicalDetails)
//        }
//        binding.cardOtherDetails.setOnClickListener {
//            toggleContainerVisibility(binding.containerOtherDetails)
//        }
//        binding.cardPreviousePolicyDetails.setOnClickListener {
//            toggleContainerVisibility(binding.containerPrevoiusPolicyDetails)
//        }
//    }
//
//    private fun toggleContainerVisibility(selectedContainer: View) {
//        val isVisible = selectedContainer.visibility == View.VISIBLE
//
//        // Hide all containers
//        binding.containerPersonalDetails.visibility = View.GONE
//        binding.containerFamilyDetails.visibility = View.GONE
//        binding.containerMeddicalDetails.visibility = View.GONE
//        binding.containerOtherDetails.visibility = View.GONE
//        binding.containerPrevoiusPolicyDetails.visibility = View.GONE
//        binding.btnAddMorePolicy.visibility = View.GONE
//        binding.btnViewPolicy.visibility = View.GONE
//
//        // Show the selected container if it was hidden
//        if (!isVisible) {
//            selectedContainer.visibility = View.VISIBLE
//            if (selectedContainer.id == R.id.containerPrevoiusPolicyDetails) {
//                binding.btnAddMorePolicy.visibility = View.VISIBLE
//                binding.btnViewPolicy.visibility = View.VISIBLE
//            }
//        }
//    }
//
//    // Section: Fetch and Populate Customer Data
//    private fun fetchAndPopulateCustomerData() {
//        if (customerEntryId == -1) {
//            Toast.makeText(this, "Invalid customer ID.", Toast.LENGTH_SHORT).show()
//            finish()
//            return
//        }
//
//        val cursor = databaseHelper.selectCustomerData(customerEntryId)
//        if (cursor.moveToFirst()) {
//            do {
//                populateEditText(binding.containerPersonalDetails, cursor)
//                populateEditText(binding.containerFamilyDetails, cursor)
//                populateEditText(binding.containerMeddicalDetails, cursor)
//                populateEditText(binding.containerOtherDetails, cursor)
//            } while (cursor.moveToNext())
//        }
//        cursor.close()
//    }
//
//    private fun populateEditText(view: View, cursor: android.database.Cursor) {
//        if (view is EditText) {
//            val fieldName = resources.getResourceEntryName(view.id)
//            view.setText(cursor.getString(cursor.getColumnIndexOrThrow(fieldName)))
//        } else if (view is ViewGroup) {
//            for (i in 0 until view.childCount) {
//                populateEditText(view.getChildAt(i), cursor)
//            }
//        }
//    }
//
//    // Section: Fetch and Display Policy Data
//    private fun fetchAndDisplayPolicies() {
//        val cursor = databaseHelper.selectPolicyData(customerEntryId)
//        val parentLayout = binding.containerPrevoiusPolicyDetails
//
//        parentLayout.removeAllViews() // Clear existing policies
//        policyCounter = 1
//
//        if (cursor.moveToFirst()) {
//            do {
//                val inflater = LayoutInflater.from(this)
//                val policyView = inflater.inflate(R.layout.container_prevoius_policy_details, null)
//                policyView.tag = "policy_${policyCounter++}"
//                populateEditText(policyView, cursor)
//                parentLayout.addView(policyView)
//            } while (cursor.moveToNext())
//        } else {
//            Toast.makeText(this, "No policies found for this customer.", Toast.LENGTH_SHORT).show()
//        }
//        cursor.close()
//    }
//
//    // Section: Add More Policy Button
//    private fun addMorePolicy() {
//        val inflater = LayoutInflater.from(this)
//        val newPolicyView = inflater.inflate(R.layout.container_prevoius_policy_details, null)
//        newPolicyView.tag = "policy_${policyCounter++}"
//
//        val parentLayout = binding.containerPrevoiusPolicyDetails
//        parentLayout.addView(newPolicyView)
//    }
//
//    // Section: Handle Update Button
//    private fun updateCustomerData() {
//        val customerData = ContentValues()
//        val policyDataList = mutableListOf<Pair<Int?, ContentValues>>() // Pair of policy_id and policy data
//
//        // Collect customer data
//        collectCustomerData(binding.containerPersonalDetails, customerData)
//        collectCustomerData(binding.containerFamilyDetails, customerData)
//        collectCustomerData(binding.containerMeddicalDetails, customerData)
//        collectCustomerData(binding.containerOtherDetails, customerData)
//
//        // Validate that customer name (etFullName) is present
//        val customerName = customerData.getAsString("etFullName")
//        if (customerName.isNullOrEmpty()) {
//            Toast.makeText(this, "Customer Name is required. Please enter it before updating.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Update customer data in the database
//        val customerUpdateResult = databaseHelper.updateCustomerData(customerEntryId, customerData)
//
//        if (customerUpdateResult == 0) {
//            Toast.makeText(this, "Error updating customer data. Please try again.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Collect policy data from dynamically added layouts
//        val parentLayout = binding.containerPrevoiusPolicyDetails
//        for (i in 0 until parentLayout.childCount) {
//            val child = parentLayout.getChildAt(i)
//            if (child.tag != null && child.tag.toString().startsWith("policy_")) {
//                val policyData = ContentValues()
//                collectPolicyData(child, policyData)
//
//                // Check if the policy has an existing ID (stored as a tag in the parent view)
//                val policyId = child.getTag(R.id.policy_id_tag) as? Int
//
//                // Add the customer ID to the policy data
//                policyData.put("customer_id", customerEntryId)
//
//                // Add to the list of policies to update/insert
//                policyDataList.add(Pair(policyId, policyData))
//            }
//        }
//
//        // Update or Insert Policy Data in the Database
//        for ((policyId, policyData) in policyDataList) {
//            if (policyId != null) {
//                // Update the existing policy
//                val policyUpdateResult = databaseHelper.updatePolicyData(policyId, policyData)
//                if (policyUpdateResult == 0) {
//                    Log.e("Database", "Failed to update policy with ID $policyId: $policyData")
//                }
//            } else {
//                // Insert a new policy
//                val policyInsertResult = databaseHelper.insertPolicyData(policyData)
//                if (policyInsertResult == -1L) {
//                    Log.e("Database", "Failed to insert new policy: $policyData")
//                }
//            }
//        }
//
//        Toast.makeText(this, "Customer and Policies successfully updated!", Toast.LENGTH_SHORT).show()
//        Log.d("Database", "Customer ID $customerEntryId and policies updated successfully.")
//        finish()
//    }
//    private fun collectCustomerData(view: View, customerData: ContentValues) {
//        if (view is EditText) {
//            val fieldName = resources.getResourceEntryName(view.id) // Get field name from resource ID
//            val fieldValue = view.text.toString().trim()
//            if (fieldValue.isNotEmpty()) {
//                customerData.put(fieldName, fieldValue)
//            }
//        } else if (view is ViewGroup) {
//            for (i in 0 until view.childCount) {
//                collectCustomerData(view.getChildAt(i), customerData)
//            }
//        }
//    }
//
//    private fun collectPolicyData(view: View, policyData: ContentValues) {
//        if (view is EditText) {
//            val fieldName = resources.getResourceEntryName(view.id) // Get field name from resource ID
//            val fieldValue = view.text.toString().trim()
//            if (fieldValue.isNotEmpty()) {
//                policyData.put(fieldName, fieldValue)
//            }
//        } else if (view is ViewGroup) {
//            for (i in 0 until view.childCount) {
//                collectPolicyData(view.getChildAt(i), policyData)
//            }
//        }
//    }
//}
package com.example.sanviassociates

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sanviassociates.databinding.ActivityUpdateCustomerBinding

class UpdateCustomer : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateCustomerBinding
    private var policyCounter = 1 // Counter for dynamically added policies
    private lateinit var databaseHelper: DatabaseHelper // SQLite Database Helper
    private var customerEntryId: Int = -1 // Customer Entry ID passed from HomePage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SQLite Database Helper
        databaseHelper = DatabaseHelper(this)

        // Get customer entry ID from intent
        customerEntryId = intent.getIntExtra("UNIQUE_ID", -1)

        // Set up click listeners for visibility toggling
        setupSectionVisibilityListeners()

        // Handle "Add More Policy" button
        binding.btnAddMorePolicy.setOnClickListener {
            addMorePolicy()
        }

        // Handle "View Policy" button
        binding.btnViewPolicy.setOnClickListener {
            fetchAndDisplayPolicies()
        }

        // Handle "Update" button
        binding.updateButton.setOnClickListener {
            updateCustomerData()
        }

        // Handle "Back" button
        binding.ivUpdateBack.setOnClickListener {
            onBackPressed()
        }

        // Fetch and populate customer data
        fetchAndPopulateCustomerData()
    }

    private fun setupSectionVisibilityListeners() {
        binding.cardPersonalDetails.setOnClickListener {
            toggleContainerVisibility(binding.containerPersonalDetails)
        }
        binding.cardFamilyDetails.setOnClickListener {
            toggleContainerVisibility(binding.containerFamilyDetails)
        }
        binding.cardMeddicalDetails.setOnClickListener {
            toggleContainerVisibility(binding.containerMeddicalDetails)
        }
        binding.cardOtherDetails.setOnClickListener {
            toggleContainerVisibility(binding.containerOtherDetails)
        }
        binding.cardPreviousePolicyDetails.setOnClickListener {
            toggleContainerVisibility(binding.containerPrevoiusPolicyDetails)
        }
    }

    private fun toggleContainerVisibility(selectedContainer: View) {
        val isVisible = selectedContainer.visibility == View.VISIBLE
        binding.containerPersonalDetails.visibility = View.GONE
        binding.containerFamilyDetails.visibility = View.GONE
        binding.containerMeddicalDetails.visibility = View.GONE
        binding.containerOtherDetails.visibility = View.GONE
        binding.containerPrevoiusPolicyDetails.visibility = View.GONE
        binding.btnAddMorePolicy.visibility = View.GONE
        binding.btnViewPolicy.visibility = View.GONE

        if (!isVisible) {
            selectedContainer.visibility = View.VISIBLE
            if (selectedContainer.id == R.id.containerPrevoiusPolicyDetails) {
                binding.btnAddMorePolicy.visibility = View.VISIBLE
                binding.btnViewPolicy.visibility = View.VISIBLE
            }
        }
    }

    private fun fetchAndPopulateCustomerData() {
        if (customerEntryId == -1) {
            Toast.makeText(this, "Invalid customer ID.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val cursor = databaseHelper.selectCustomerData(customerEntryId)
        if (cursor.moveToFirst()) {
            do {
                populateEditText(binding.containerPersonalDetails, cursor)
                populateEditText(binding.containerFamilyDetails, cursor)
                populateEditText(binding.containerMeddicalDetails, cursor)
                populateEditText(binding.containerOtherDetails, cursor)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private fun populateEditText(view: View, cursor: android.database.Cursor) {
        if (view is EditText) {
            val fieldName = resources.getResourceEntryName(view.id)
            view.setText(cursor.getString(cursor.getColumnIndexOrThrow(fieldName)))
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                populateEditText(view.getChildAt(i), cursor)
            }
        }
    }

    private fun fetchAndDisplayPolicies() {
        val cursor = databaseHelper.selectPolicyData(customerEntryId)
        val parentLayout = binding.containerPrevoiusPolicyDetails

        parentLayout.removeAllViews() // Clear existing policies
        policyCounter = 1

        if (cursor.moveToFirst()) {
            do {
                val inflater = LayoutInflater.from(this)
                val policyView = inflater.inflate(R.layout.container_prevoius_policy_details, null)

                // Set policy_id as a tag
                val policyId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                policyView.setTag(R.id.policy_id_tag, policyId)

                // Populate policy fields
                populateEditText(policyView, cursor)

                // Add the policy view to the parent layout
                parentLayout.addView(policyView)
            } while (cursor.moveToNext())
        } else {
            Toast.makeText(this, "No policies found for this customer.", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
    }

    private fun addMorePolicy() {
        val inflater = LayoutInflater.from(this)
        val newPolicyView = inflater.inflate(R.layout.container_prevoius_policy_details, null)
        newPolicyView.tag = "policy_${policyCounter++}"

        val parentLayout = binding.containerPrevoiusPolicyDetails
        parentLayout.addView(newPolicyView)
    }

    private fun updateCustomerData() {
        val customerData = ContentValues()
        val policyDataList = mutableListOf<Pair<Int?, ContentValues>>()

        collectCustomerData(binding.containerPersonalDetails, customerData)
        collectCustomerData(binding.containerFamilyDetails, customerData)
        collectCustomerData(binding.containerMeddicalDetails, customerData)
        collectCustomerData(binding.containerOtherDetails, customerData)

        val customerName = customerData.getAsString("etFullName")
        if (customerName.isNullOrEmpty()) {
            Toast.makeText(this, "Customer Name is required. Please enter it before updating.", Toast.LENGTH_SHORT).show()
            return
        }

        val customerUpdateResult = databaseHelper.updateCustomerData(customerEntryId, customerData)

        if (customerUpdateResult == 0) {
            Toast.makeText(this, "Error updating customer data. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val parentLayout = binding.containerPrevoiusPolicyDetails
        for (i in 0 until parentLayout.childCount) {
            val child = parentLayout.getChildAt(i)
            if (child.tag != null && child.tag.toString().startsWith("policy_")) {
                val policyData = ContentValues()
                collectPolicyData(child, policyData)

                val policyId = child.getTag(R.id.policy_id_tag) as? Int
                policyData.put("customer_id", customerEntryId)

                policyDataList.add(Pair(policyId, policyData))
            }
        }

        for ((policyId, policyData) in policyDataList) {
            if (policyId != null) {
                val policyUpdateResult = databaseHelper.updatePolicyData(policyId, policyData)
                if (policyUpdateResult == 0) {
                    Log.e("Database", "Failed to update policy with ID $policyId: $policyData")
                }
            } else {
                val policyInsertResult = databaseHelper.insertPolicyData(policyData)
                if (policyInsertResult == -1L) {
                    Log.e("Database", "Failed to insert new policy: $policyData")
                }
            }
        }

        Toast.makeText(this, "Customer and Policies successfully updated!", Toast.LENGTH_SHORT).show()
        Log.d("Database", "Customer ID $customerEntryId and policies updated successfully.")
        finish()
    }

    private fun collectCustomerData(view: View, customerData: ContentValues) {
        if (view is EditText) {
            val fieldName = resources.getResourceEntryName(view.id)
            val fieldValue = view.text.toString().trim()
            if (fieldValue.isNotEmpty()) {
                customerData.put(fieldName, fieldValue)
            }
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectCustomerData(view.getChildAt(i), customerData)
            }
        }
    }

    private fun collectPolicyData(view: View, policyData: ContentValues) {
        if (view is EditText) {
            val fieldName = resources.getResourceEntryName(view.id)
            val fieldValue = view.text.toString().trim()
            if (fieldValue.isNotEmpty()) {
                policyData.put(fieldName, fieldValue)
            }
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectPolicyData(view.getChildAt(i), policyData)
            }
        }
    }
}