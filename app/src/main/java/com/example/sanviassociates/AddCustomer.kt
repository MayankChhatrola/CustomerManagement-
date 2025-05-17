package com.example.sanviassociates

import android.content.ContentValues
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.sanviassociates.databinding.ActivityAddCustomerBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date
import java.util.Locale

class AddCustomer : AppCompatActivity() {

    private lateinit var binding: ActivityAddCustomerBinding
    private var policyCounter = 1 // Counter for dynamically added policies
    private lateinit var databaseHelper: DatabaseHelper // SQLite Database Helper
    private val yearToAgeMapping = mutableMapOf<EditText, EditText>()
    private val dynamicPolicyViews = mutableListOf<View>() // List to track dynamically added policy views

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityAddCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SQLite Database Helper
        databaseHelper = DatabaseHelper(this)

        // Set up click listeners for visibility toggling
        setupSectionVisibilityListeners()

        setupMaterialDatePicker()

        // Map year fields to their corresponding age fields
        yearToAgeMapping[binding.etFatherYear] = binding.etFatherAge
        yearToAgeMapping[binding.etMotherYear] = binding.etMotherAge
        yearToAgeMapping[binding.etBrotherYear] = binding.etBrotherAge
        yearToAgeMapping[binding.etSisterYear] = binding.etSisterAge
        yearToAgeMapping[binding.etHusbandYear] = binding.etHusbandAge
        yearToAgeMapping[binding.etChildrenYear] = binding.etChildrenAge

        setYearClickListeners()

        // Handle "Add More Policy" button
        binding.btnAddMorePolicy.setOnClickListener {
            addMorePolicy()
        }

        // Handle "Submit" button
        binding.submitButton.setOnClickListener {
            fetchSaveAndClearFields()
        }

        // Back
        binding.ivAddBack.setOnClickListener {
            onBackPressed()
        }

        // Add functionality for etNommineeDate to open MaterialDatePicker
        setupNommineeDatePicker()
    }

    private fun setupNommineeDatePicker() {
        val etNommineeDate: EditText = binding.etNomineeDate

        etNommineeDate.setOnClickListener {
            // Create constraints to prevent selecting future dates
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now()) // Prevent future dates

            // Build the MaterialDatePicker
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Nominee Date of Birth")
                .setTheme(R.style.CustomMaterialDatePicker)
                .setCalendarConstraints(constraintsBuilder.build()) // Add constraints
                .build()

            // Show the MaterialDatePicker
            datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")

            // Handle the date selection
            datePicker.addOnPositiveButtonClickListener { selection ->
                // Format the selected date to DD/MM/YYYY
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDate = sdf.format(Date(selection))
                etNommineeDate.setText(selectedDate)
            }
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

        // Hide all dynamically added policy sections
        dynamicPolicyViews.forEach { it.visibility = View.GONE }

        // Show the selected container if it was hidden
        if (!isVisible) {
            selectedContainer.visibility = View.VISIBLE
            if (selectedContainer.id == R.id.containerPrevoiusPolicyDetails) {
                binding.btnAddMorePolicy.visibility = View.VISIBLE
            }
        }
    }

    private fun toggleDynamicPolicyVisibility(policyView: View) {
        val isVisible = policyView.visibility == View.VISIBLE

        // Hide all dynamic policy sections
        dynamicPolicyViews.forEach { it.visibility = View.GONE }

        // Show the selected policy view if it was hidden
        if (!isVisible) {
            policyView.visibility = View.VISIBLE
        }
    }

    // Section: Dynamic Policy Management
    private fun addMorePolicy() {
        val inflater = LayoutInflater.from(this)
        val newPolicyView = inflater.inflate(R.layout.container_prevoius_policy_details, null)
        newPolicyView.tag = "policy_${policyCounter++}"

        // Add visibility toggle for the new policy view
        newPolicyView.setOnClickListener {
            toggleDynamicPolicyVisibility(newPolicyView)
        }

        // Track the dynamically added policy view
        dynamicPolicyViews.add(newPolicyView)

        val parentLayout = binding.containerPrevoiusPolicyDetails.parent as LinearLayout
        val addMoreButtonIndex = parentLayout.indexOfChild(binding.btnAddMorePolicy)
        parentLayout.addView(newPolicyView, addMoreButtonIndex)
    }

    // Section: Handle Submit Button
    private fun fetchSaveAndClearFields() {

        if (!validateImportantFields()) {
            return
        }

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

        // **CHECK FOR DUPLICATE NAME HERE**
        if (databaseHelper.isFullNameExists(customerName)) {
            Toast.makeText(this, "Full Name already registered. Please use a different name.", Toast.LENGTH_SHORT).show()
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
        dynamicPolicyViews.forEach { clearEditTextFields(it) }
        dynamicPolicyViews.clear()
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

        // Collapse all dynamically added policy sections
        dynamicPolicyViews.forEach { it.visibility = View.GONE }
    }

    private fun setupMaterialDatePicker() {
        val etBirthDate: EditText = binding.etBirthDate

        etBirthDate.setOnClickListener {
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now()) // Prevent future dates

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Birth Date")
                .setTheme(R.style.CustomMaterialDatePicker)
                .setCalendarConstraints(constraintsBuilder.build()) // Add constraints
                .build()

            datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDate = sdf.format(Date(selection))
                etBirthDate.setText(selectedDate)
            }
        }
    }

    private fun setYearClickListeners() {
        for ((yearField, _) in yearToAgeMapping) {
            yearField.setOnClickListener { openMaterialDatePicker(yearField) }
        }
    }

    private fun openMaterialDatePicker(yearField: EditText) {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now()) // Prevent future dates

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date of Birth")
            .setTheme(R.style.CustomMaterialDatePicker)
            .setCalendarConstraints(constraintsBuilder.build()) // Apply constraints
            .build()

        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDate = Date(selection)
            val formattedDate = sdf.format(selectedDate)

            yearField.setText(formattedDate)

            yearToAgeMapping[yearField]?.let { ageField ->
                val calendar = Calendar.getInstance()
                val selectedYear = Calendar.getInstance().apply { time = selectedDate }.get(Calendar.YEAR)
                val currentYear = calendar.get(Calendar.YEAR)
                val age = currentYear - selectedYear

                ageField.setText(age.toString())
            }
        }
    }

    private fun validateImportantFields(): Boolean {
        val aadhar = binding.etAadharNumber.text.toString().trim()
        val pan = binding.etPancard.text.toString().trim()
        val email = binding.etEmailId.text.toString().trim()
        val mobile = binding.etMobileNumber.text.toString().trim()
        val ifsc = binding.etIfsc.text.toString().trim()
        val micr = binding.etMicr.text.toString().trim()
        val pincode = binding.etPincode.text.toString().trim()
        val accountHolderName = binding.etAccountHolderName.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()

        if (!accountHolderName.equals(fullName, ignoreCase = true)) {
            Toast.makeText(this, "Full name must match account holder name", Toast.LENGTH_SHORT).show()
            return false
        }

        val mobileRegex = Regex("^[6-9]\\d{9}$")
        if (!mobileRegex.matches(mobile)) {
            Toast.makeText(this, "Invalid Mobile Number. Should start with 6-9 and be 10 digits.", Toast.LENGTH_SHORT).show()
            return false
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        if (!emailRegex.matches(email)) {
            Toast.makeText(this, "Invalid Email ID format.", Toast.LENGTH_SHORT).show()
            return false
        }

        val pincodeRegex = Regex("^[1-9][0-9]{5}$")
        if (!pincodeRegex.matches(pincode)) {
            Toast.makeText(this, "Invalid Pincode. Should be 6 digits and not start with 0.", Toast.LENGTH_SHORT).show()
            return false
        }

        val micrRegex = Regex("^\\d{9}$")
        if (!micrRegex.matches(micr)) {
            Toast.makeText(this, "Invalid MICR Code. Should be 9 digits.", Toast.LENGTH_SHORT).show()
            return false
        }

        val ifscRegex = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")
        if (!ifscRegex.matches(ifsc)) {
            Toast.makeText(this, "Invalid IFSC Code. Format: ABCD0XXXXXX", Toast.LENGTH_SHORT).show()
            return false
        }

        val panRegex = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
        if (!panRegex.matches(pan)) {
            Toast.makeText(this, "Invalid PAN number. Format: ABCDE1234F", Toast.LENGTH_SHORT).show()
            return false
        }

        val aadhaarRegex = Regex("^\\d{12}$")
        if (!aadhaarRegex.matches(aadhar)) {
            Toast.makeText(this, "Invalid Aadhaar number. It should be 12 digits.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}