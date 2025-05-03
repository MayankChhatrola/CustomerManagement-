/*
package com.example.sanviassociates

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sanviassociates.databinding.ActivityUpdateCustomerBinding

class UpdateCustomer : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateCustomerBinding
    private lateinit var databaseHelper: DatabaseHelper // SQLite Database Helper
    private var policyCounter = 1 // Counter for dynamically added policies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database helper
        databaseHelper = DatabaseHelper(this)

        // Set up click listeners for visibility toggling
        setupSectionVisibilityListeners()

        // Handle "Add More Policy" button
        binding.btnAddMorePolicy.setOnClickListener {
            addMorePolicy()
        }


        // Get unique ID from intent
        val customerId = intent.getIntExtra("UNIQUE_ID", -1)
        if (customerId == -1) {
            Log.e("UpdateCustomer", "Invalid customer ID passed to the activity.")
            Toast.makeText(this, "Invalid Customer ID", Toast.LENGTH_SHORT).show()
            finish() // Exit the activity if the ID is invalid
        } else {
            // Load and display customer data based on the customerId
            fetchAndSetCustomerData(customerId)
        }



        // Handle update button click
        binding.updateButton.setOnClickListener {
            if (isSingleFieldUpdate()) {
                // Single field update logic
                updateSingleField(customerId)
            } else {
                // Multiple field update logic
                updateMultipleFields(customerId)
            }
        }

        // Handle back button click
        binding.ivUpdateBack.setOnClickListener {
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

    */
/**
     * Fetch customer data based on the unique ID and display it in the EditText fields.
     *//*

    private fun fetchAndSetCustomerData(customerId: Int) {
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_ENTRY_ID} = ?",
            arrayOf(customerId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val fieldName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIELD_NAME))
                val fieldValue = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIELD_VALUE))
                setEditTextValue(fieldName, fieldValue)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    */
/**
     * Set the value of an EditText based on the field name from the database.
     *//*

    private fun setEditTextValue(fieldName: String, fieldValue: String) {
        when (fieldName) {
            "etFullName" -> binding.etFullName.setText(fieldValue)
            "etFatherName" -> binding.etFatherName.setText(fieldValue)
            "etMotherName" -> binding.etMotherName.setText(fieldValue)
            "etAddress" -> binding.etAddress.setText(fieldValue)
            "etPincode" -> binding.etPincode.setText(fieldValue)
            "etBirthPlace" -> binding.etBirthPlace.setText(fieldValue)
            "etBirthDate" -> binding.etBirthDate.setText(fieldValue)
            "etMobileNumber" -> binding.etMobileNumber.setText(fieldValue)
            "etEmailId" -> binding.etEmailId.setText(fieldValue)
            "etFatherAge" -> binding.etFatherAge.setText(fieldValue)
            "etFatherYear" -> binding.etFatherYear.setText(fieldValue)
            "etFatherCondition" -> binding.etFatherCondition.setText(fieldValue)
            "etMotherAge" -> binding.etMotherAge.setText(fieldValue)
            "etMotherYear" -> binding.etMotherYear.setText(fieldValue)
            "etMotherCondition" -> binding.etMotherCondition.setText(fieldValue)
            "etHeight" -> binding.etHeight.setText(fieldValue)
            "etWeight" -> binding.etWeight.setText(fieldValue)
            "etWaist" -> binding.etWaist.setText(fieldValue)
            "etChest" -> binding.etChest.setText(fieldValue)
            "etIlleness" -> binding.etIlleness.setText(fieldValue)
            "etPregencyDate" -> binding.etPregencyDate.setText(fieldValue)
            "etAccountHolderName" -> binding.etAccountHolderName.setText(fieldValue)
            "etAccountNumber" -> binding.etAccountNumber.setText(fieldValue)
            "etBanmeName" -> binding.etBanmeName.setText(fieldValue)
            "etMicr" -> binding.etMicr.setText(fieldValue)
            "etIfsc" -> binding.etIfsc.setText(fieldValue)
            "etEducation" -> binding.etEducation.setText(fieldValue)
            "Occuption" -> binding.Occuption.setText(fieldValue)
            "etAnnualIncome" -> binding.etAnnualIncome.setText(fieldValue)
            "etCompanyName" -> binding.etCompanyName.setText(fieldValue)
            "etsinceWhen" -> binding.etsinceWhen.setText(fieldValue)
            else -> Log.w("UpdateCustomer", "Field not recognized: $fieldName")
        }
    }

    */
/**
     * Check if a single field is being updated.
     *//*

    private fun isSingleFieldUpdate(): Boolean {
        // Logic to determine if only one field is being updated
        // For simplicity, we'll assume that if only one field is modified, it's a single field update
        // Otherwise, it's a multiple field update
        return false // Set to true if you want to test single field updates
    }

    */
/**
     * Update a single field in the database.
     *//*

    private fun updateSingleField(customerId: Int) {
        val fieldName = "etFullName" // Replace with the actual field name being updated
        val newValue = binding.etFullName.text.toString()

        val rowsAffected = databaseHelper.updateData(customerId, fieldName, newValue)
        if (rowsAffected > 0) {
            Toast.makeText(this, "Field updated successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Field update failed!", Toast.LENGTH_SHORT).show()
        }
    }

    */
/**
     * Update multiple fields in the database.
     *//*

    private fun updateMultipleFields(customerId: Int) {
        val updatedValues = mapOf(
            "etFullName" to binding.etFullName.text.toString(),
            "etFatherName" to binding.etFatherName.text.toString(),
            "etMotherName" to binding.etMotherName.text.toString(),
            "etAddress" to binding.etAddress.text.toString(),
            "etPincode" to binding.etPincode.text.toString(),
            "etBirthPlace" to binding.etBirthPlace.text.toString(),
            "etBirthDate" to binding.etBirthDate.text.toString(),
            "etMobileNumber" to binding.etMobileNumber.text.toString(),
            "etEmailId" to binding.etEmailId.text.toString(),
            "etFatherAge" to binding.etFatherAge.text.toString(),
            "etFatherYear" to binding.etFatherYear.text.toString(),
            "etFatherCondition" to binding.etFatherCondition.text.toString(),
            "etMotherAge" to binding.etMotherAge.text.toString(),
            "etMotherYear" to binding.etMotherYear.text.toString(),
            "etMotherCondition" to binding.etMotherCondition.text.toString(),
            "etHeight" to binding.etHeight.text.toString(),
            "etWeight" to binding.etWeight.text.toString(),
            "etWaist" to binding.etWaist.text.toString(),
            "etChest" to binding.etChest.text.toString(),
            "etIlleness" to binding.etIlleness.text.toString(),
            "etPregencyDate" to binding.etPregencyDate.text.toString(),
            "etAccountHolderName" to binding.etAccountHolderName.text.toString(),
            "etAccountNumber" to binding.etAccountNumber.text.toString(),
            "etBanmeName" to binding.etBanmeName.text.toString(),
            "etMicr" to binding.etMicr.text.toString(),
            "etIfsc" to binding.etIfsc.text.toString(),
            "etEducation" to binding.etEducation.text.toString(),
            "Occuption" to binding.Occuption.text.toString(),
            "etAnnualIncome" to binding.etAnnualIncome.text.toString(),
            "etCompanyName" to binding.etCompanyName.text.toString(),
            "etsinceWhen" to binding.etsinceWhen.text.toString()
        )

        val rowsAffected = databaseHelper.updateAllDataByEntryId(customerId, updatedValues)
        if (rowsAffected > 0) {
            Toast.makeText(this, "All fields updated successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Field update failed!", Toast.LENGTH_SHORT).show()
        }
    }
}*/
package com.example.sanviassociates

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sanviassociates.databinding.ActivityUpdateCustomerBinding

class UpdateCustomer : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateCustomerBinding
    private lateinit var databaseHelper: DatabaseHelper
    private var policyCounter = 1 // Counter for dynamically added policies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database helper
        databaseHelper = DatabaseHelper(this)

        // Setup visibility toggling for sections
        setupSectionVisibilityListeners()

        // Handle "Add More Policy" button
        binding.btnAddMorePolicy.setOnClickListener {
            addMorePolicy()
        }

        // Get unique ID from intent
        val customerId = intent.getIntExtra("UNIQUE_ID", -1)
        if (customerId == -1) {
            Log.e("UpdateCustomer", "Invalid customer ID passed to the activity.")
            Toast.makeText(this, "Invalid Customer ID", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // Fetch and set customer data
            fetchAndSetCustomerData(customerId)
        }

        // Handle update button click
        binding.updateButton.setOnClickListener {
            updateCustomerData(customerId)
        }

        // Handle back button click
        binding.ivUpdateBack.setOnClickListener {
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

        val parentLayout = binding.containerPrevoiusPolicyDetails
        parentLayout.addView(newPolicyView)
    }

    /**
     * Fetch and set customer data, including policy details.
     */
    private fun fetchAndSetCustomerData(customerId: Int) {
        val db = databaseHelper.readableDatabase

        // Fetch general data
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_ENTRY_ID} = ?",
            arrayOf(customerId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val fieldName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIELD_NAME))
                val fieldValue = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIELD_VALUE))
                setEditTextValue(fieldName, fieldValue)
            } while (cursor.moveToNext())
        }
        cursor.close()

        // Fetch policy data
        val policies = databaseHelper.getPoliciesByEntryId(customerId)
        for (policy in policies) {
            addPolicyView(policy)
        }
    }

    /**
     * Add a policy view and populate it with data.
     */
    private fun addPolicyView(policy: Map<String, String>) {
        val inflater = LayoutInflater.from(this)
        val policyView = inflater.inflate(R.layout.container_prevoius_policy_details, null)

        policyView.findViewById<EditText>(R.id.etBranchName).setText(policy["Branch"])
        policyView.findViewById<EditText>(R.id.etPolicyNumber).setText(policy["PolicyNumber"])
        policyView.findViewById<EditText>(R.id.etStartDate).setText(policy["StartDate"])
        policyView.findViewById<EditText>(R.id.etSumAssured).setText(policy["SumAssured"])
        policyView.findViewById<EditText>(R.id.etPlan).setText(policy["Plan"])
        policyView.findViewById<EditText>(R.id.etPolicyPremium).setText(policy["Premium"])

        binding.containerPrevoiusPolicyDetails.addView(policyView)
    }

    /**
     * Set the value of an EditText based on the field name from the database.
     */
    private fun setEditTextValue(fieldName: String, fieldValue: String) {
        when (fieldName) {
            "etFullName" -> binding.etFullName.setText(fieldValue)
            "etFatherName" -> binding.etFatherName.setText(fieldValue)
            "etMotherName" -> binding.etMotherName.setText(fieldValue)
            "etAddress" -> binding.etAddress.setText(fieldValue)
            "etPincode" -> binding.etPincode.setText(fieldValue)
            // Add other fields as necessary
            else -> Log.w("UpdateCustomer", "Field not recognized: $fieldName")
        }
    }

    /**
     * Update customer data in the database.
     */
    private fun updateCustomerData(customerId: Int) {
        val generalData = mapOf(
            "etFullName" to binding.etFullName.text.toString(),
            "etFatherName" to binding.etFatherName.text.toString(),
            "etMotherName" to binding.etMotherName.text.toString(),
            "etAddress" to binding.etAddress.text.toString(),
            "etPincode" to binding.etPincode.text.toString()
            // Add other fields as necessary
        )
        val rowsUpdated = databaseHelper.updateAllDataByEntryId(customerId, generalData)

        if (rowsUpdated > 0) {
            Toast.makeText(this, "Customer data updated successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to update customer data!", Toast.LENGTH_SHORT).show()
        }
    }
}