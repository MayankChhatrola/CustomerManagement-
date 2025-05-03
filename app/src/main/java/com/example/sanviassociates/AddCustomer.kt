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
import com.example.sanviassociates.databinding.ActivityAddCustomerBinding

class AddCustomer : AppCompatActivity() {

    private lateinit var binding: ActivityAddCustomerBinding
    private var policyCounter = 1 // Counter for dynamically added policies
    private lateinit var databaseHelper: DatabaseHelper // SQLite Database Helper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SQLite Database Helper
        databaseHelper = DatabaseHelper(this)

        // Set up click listeners for visibility toggling
        setupSectionVisibilityListeners()

        // Handle "Add More Policy" button
        binding.btnAddMorePolicy.setOnClickListener {
            addMorePolicy()
        }

        // Handle "Submit" button
        binding.submitButton.setOnClickListener {
            fetchSaveAndClearFields()
        }

        //back
        binding.ivAddBack.setOnClickListener {
            onBackPressed()
        }
    }

    // Section: Visibility Management
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

        // Hide all containers
        binding.containerPersonalDetails.visibility = View.GONE
        binding.containerFamilyDetails.visibility = View.GONE
        binding.containerMeddicalDetails.visibility = View.GONE
        binding.containerOtherDetails.visibility = View.GONE
        binding.containerPrevoiusPolicyDetails.visibility = View.GONE
        binding.btnAddMorePolicy.visibility = View.GONE

        // Show the selected container if it was hidden
        if (!isVisible) {
            selectedContainer.visibility = View.VISIBLE
            if (selectedContainer.id == R.id.containerPrevoiusPolicyDetails) {
                binding.btnAddMorePolicy.visibility = View.VISIBLE
            }
        }
    }

    // Section: Dynamic Policy Management
    private fun addMorePolicy() {
        val inflater = LayoutInflater.from(this)
        val newPolicyView = inflater.inflate(R.layout.container_prevoius_policy_details, null)
        newPolicyView.tag = "policy_${policyCounter++}"

        val parentLayout = binding.containerPrevoiusPolicyDetails.parent as LinearLayout
        val addMoreButtonIndex = parentLayout.indexOfChild(binding.btnAddMorePolicy)
        parentLayout.addView(newPolicyView, addMoreButtonIndex)
    }

    // Section: Handle Submit Button
    private fun fetchSaveAndClearFields() {
        val customerData = ContentValues()
        val policyDataList = mutableListOf<ContentValues>()

        // Collect customer data
        collectCustomerData(binding.containerPersonalDetails, customerData)
        collectCustomerData(binding.containerFamilyDetails, customerData)
        collectCustomerData(binding.containerMeddicalDetails, customerData)
        collectCustomerData(binding.containerOtherDetails, customerData)

        // Validate that customer name (etFullName) is present
        val customerName = customerData.getAsString("etFullName")
        if (customerName.isNullOrEmpty()) {
            Toast.makeText(this, "Customer Name is required. Please enter it before submitting.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the last customer entry ID and increment it
        val lastEntryId = databaseHelper.getLastCustomerEntryId()
        val newCustomerId = lastEntryId + 1
        customerData.put("entry_id", newCustomerId)

        // Insert customer data into the database
        val customerInsertResult = databaseHelper.insertCustomerData(customerData)

        if (customerInsertResult == -1L) {
            Toast.makeText(this, "Error saving customer data. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Collect policy data from dynamically added layouts
        val parentLayout = binding.containerPrevoiusPolicyDetails.parent as LinearLayout
        for (i in 0 until parentLayout.childCount) {
            val child = parentLayout.getChildAt(i)
            if (child.tag != null && child.tag.toString().startsWith("policy_")) {
                val policyData = ContentValues()
                collectPolicyData(child, policyData)

                // Add the customer ID to the policy data
                policyData.put("customer_id", newCustomerId)

                // Add to the list of policies to insert
                policyDataList.add(policyData)
            }
        }

        // Insert policy data into the database
        var policiesInserted = true
        for (policyData in policyDataList) {
            val policyInsertResult = databaseHelper.insertPolicyData(policyData)
            if (policyInsertResult == -1L) {
                policiesInserted = false
                Log.e("Database", "Failed to insert policy data: $policyData")
            }
        }

        if (policiesInserted) {
            Toast.makeText(this, "Customer and Policies successfully saved!", Toast.LENGTH_SHORT).show()
            Log.d("Database", "Customer ID $newCustomerId and policies saved successfully.")
        } else {
            Toast.makeText(this, "Error saving some policies. Please try again.", Toast.LENGTH_SHORT).show()
        }

        // Clear all fields
        clearAllFields()

        // Collapse all sections after submission
        collapseAllSections()
    }

    private fun collectCustomerData(view: View, customerData: ContentValues) {
        if (view is EditText) {
            val fieldName = resources.getResourceEntryName(view.id) // Get field name from resource ID
            val fieldValue = view.text.toString().trim()
            if (fieldValue.isNotEmpty()) {
                customerData.put(fieldName, fieldValue)
            }
        } else if (view is ViewGroup) {
            // Recursively check child views
            for (i in 0 until view.childCount) {
                collectCustomerData(view.getChildAt(i), customerData)
            }
        }
    }

    private fun collectPolicyData(view: View, policyData: ContentValues) {
        if (view is EditText) {
            val fieldName = resources.getResourceEntryName(view.id) // Get field name from resource ID
            val fieldValue = view.text.toString().trim()
            if (fieldValue.isNotEmpty()) {
                policyData.put(fieldName, fieldValue)
            }
        } else if (view is ViewGroup) {
            // Recursively check child views
            for (i in 0 until view.childCount) {
                collectPolicyData(view.getChildAt(i), policyData)
            }
        }
    }

    private fun clearAllFields() {
        clearEditTextFields(binding.containerPersonalDetails)
        clearEditTextFields(binding.containerFamilyDetails)
        clearEditTextFields(binding.containerMeddicalDetails)
        clearEditTextFields(binding.containerOtherDetails)

        // Clear dynamically added layouts
        val parentLayout = binding.containerPrevoiusPolicyDetails.parent as LinearLayout
        for (i in 0 until parentLayout.childCount) {
            val child = parentLayout.getChildAt(i)
            if (child.tag != null && child.tag.toString().startsWith("policy_")) {
                clearEditTextFields(child)
            }
        }
    }

    private fun clearEditTextFields(view: View) {
        if (view is EditText) {
            view.text.clear() // Clear the text
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                clearEditTextFields(view.getChildAt(i))
            }
        }
    }

    private fun collapseAllSections() {
        binding.containerPersonalDetails.visibility = View.GONE
        binding.containerFamilyDetails.visibility = View.GONE
        binding.containerMeddicalDetails.visibility = View.GONE
        binding.containerOtherDetails.visibility = View.GONE
        binding.containerPrevoiusPolicyDetails.visibility = View.GONE
        binding.btnAddMorePolicy.visibility = View.GONE
    }
}