package com.example.sanviassociates

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sanviassociates.databinding.ActivityViewCustomerBinding
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File

class ViewCustomer : AppCompatActivity() {

    private lateinit var viewCustomerBinding: ActivityViewCustomerBinding
    private lateinit var databaseHelper: DatabaseHelper // SQLite Database Helper
    private var entryId: Int = -1
    private val dynamicPolicyViews = mutableListOf<View>() // List to track dynamically added policy views

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewCustomerBinding = ActivityViewCustomerBinding.inflate(layoutInflater)
        setContentView(viewCustomerBinding.root)

        // Initialize database helper
        databaseHelper = DatabaseHelper(this)

        // Get the entry ID from the intent
        entryId = intent.getIntExtra("UNIQUE_ID", -1)

        if (entryId == -1) {
            Toast.makeText(this, "Invalid customer ID.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch and populate data
        fetchDataAndPopulate()

        // Set up section visibility toggling
        setupSectionVisibilityListeners()

        // Handle download button click
        viewCustomerBinding.downloadButton.setOnClickListener {
            generateCustomerPdf(this, entryId, databaseHelper)
        }

        // Handle "View Policy" button
        viewCustomerBinding.btnViewPolicy.setOnClickListener {
            fetchAndDisplayPolicies()
        }

        //back
        viewCustomerBinding.ivAddBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun fetchDataAndPopulate() {
        val (customerCursor, policyCursor, familyCursor) = databaseHelper.getCustomerWithPoliciesAndFamily(entryId)

        if (customerCursor == null || !customerCursor.moveToFirst()) {
            Toast.makeText(this, "Customer data not found.", Toast.LENGTH_SHORT).show()
            return
        }

        // Personal Details
        viewCustomerBinding.tvFullName.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_FULL_NAME))
        viewCustomerBinding.tvFatherName.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_FATHER_NAME))
        viewCustomerBinding.tvMotherName.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_MOTHER_NAME))
        viewCustomerBinding.tvBirthPlace.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_BIRTH_PLACE))
        viewCustomerBinding.tvAddress.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_ADDRESS))
        viewCustomerBinding.tvMobileNumber.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_MOBILE_NUMBER))
        viewCustomerBinding.tvEmailId.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_EMAIL_ID))
        viewCustomerBinding.tvNomineeName.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_NOMINEE_NAME))
        viewCustomerBinding.tvBirthDate.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_BIRTH_DATE))
        viewCustomerBinding.tvPincode.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_PINCODE))
        viewCustomerBinding.tvNomineeDate.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_NOMINEE_DOB))

        // Medical Details
        viewCustomerBinding.tvHeight.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_HEIGHT))
        viewCustomerBinding.tvWeight.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_WEIGHT))
        viewCustomerBinding.tvWaist.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_WAIST))
        viewCustomerBinding.tvChest.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_CHEST))
        viewCustomerBinding.tvIllness.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_ILLNESS))

        // Family Details
        if (familyCursor != null && familyCursor.moveToFirst()) {
            viewCustomerBinding.tvFatherYear.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_FATHER_YEAR))
            viewCustomerBinding.tvFatherAge.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_FATHER_AGE))
            viewCustomerBinding.tvFatherCondition.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_FATHER_CONDITION))

            viewCustomerBinding.tvMotherYear.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_MOTHER_YEAR))
            viewCustomerBinding.tvMotherAge.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_MOTHER_AGE))
            viewCustomerBinding.tvMotherCondition.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_MOTHER_CONDITION))

            viewCustomerBinding.tvBrotherYear.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_BROTHER_YEAR))
            viewCustomerBinding.tvBrotherAge.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_BROTHER_AGE))
            viewCustomerBinding.tvBrotherCondition.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_BROTHER_CONDITION))

            viewCustomerBinding.tvSisterYear.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_SISTER_YEAR))
            viewCustomerBinding.tvSisterAge.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_SISTER_AGE))
            viewCustomerBinding.tvSisterCondition.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_SISTER_CONDITION))

            viewCustomerBinding.tvHusbandYear.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_HUSBAND_YEAR))
            viewCustomerBinding.tvHusbandAge.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_HUSBAND_AGE))
            viewCustomerBinding.tvHusbandCondition.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_HUSBAND_CONDITION))

            viewCustomerBinding.tvChildrenYear.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_CHILDREN_YEAR))
            viewCustomerBinding.tvChildrenAge.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_CHILDREN_AGE))
            viewCustomerBinding.tvChildrenCondition.text = familyCursor.getString(familyCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_CHILDREN_CONDITION))

        } else {
            // Handle the case where familyCursor is null or empty
            viewCustomerBinding.tvFatherYear.text = "-"
            viewCustomerBinding.tvFatherAge.text = "-"
            viewCustomerBinding.tvFatherCondition.text = "-"

            viewCustomerBinding.tvMotherYear.text = "-"
            viewCustomerBinding.tvMotherAge.text = "-"
            viewCustomerBinding.tvMotherCondition.text = "-"

            viewCustomerBinding.tvBrotherAge.text = "-"
            viewCustomerBinding.tvBrotherYear.text = "-"
            viewCustomerBinding.tvBrotherCondition.text = "-"

            viewCustomerBinding.tvSisterYear.text = "-"
            viewCustomerBinding.tvSisterAge.text = "-"
            viewCustomerBinding.tvSisterCondition.text = "-"

            viewCustomerBinding.tvHusbandYear.text = "-"
            viewCustomerBinding.tvHusbandAge.text = "-"
            viewCustomerBinding.tvHusbandCondition.text = "-"

            viewCustomerBinding.tvChildrenAge.text = "-"
            viewCustomerBinding.tvChildrenYear.text = "-"
            viewCustomerBinding.tvChildrenCondition.text = "-"
        }

        // Other Details
        viewCustomerBinding.tvAccountHolderName.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_ACCOUNT_HOLDER_NAME))
        viewCustomerBinding.tvAccountNumber.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_ACCOUNT_NUMBER))
        viewCustomerBinding.tvBankName.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_BANK_NAME))
        viewCustomerBinding.tvMicr.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_MICR))
        viewCustomerBinding.tvIfsc.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_IFSC))
        viewCustomerBinding.tvPancard.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_PAN_CARD))
        viewCustomerBinding.tvAadharNumber.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_ADHAR_CARD))
        viewCustomerBinding.tvEducation.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_EDUCATION))
        viewCustomerBinding.tvOccupation.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_OCCUPATION))
        viewCustomerBinding.tvAnnualIncome.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_ANNUAL_INCOME))
        viewCustomerBinding.tvCompanyName.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_COMPANY_NAME))
        viewCustomerBinding.tvSinceWhen.text = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_SINCE_WHEN))

        // Policy Details
        if (policyCursor != null && policyCursor.moveToFirst()) {
            viewCustomerBinding.containerPrevoiusPolicyDetails.visibility = View.VISIBLE
            do {
                val branchName = policyCursor.getString(policyCursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_BRANCH_NAME))
                val policyNumber = policyCursor.getString(policyCursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_POLICY_NUMBER))
                val startDate = policyCursor.getString(policyCursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_START_DATE))
                val sumAssured = policyCursor.getString(policyCursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_SUM_ASSURED))
                val plan = policyCursor.getString(policyCursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_PLAN))
                val premium = policyCursor.getString(policyCursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_POLICY_PREMIUM))

                // Dynamically add policy details to the layout (e.g., using a custom view or TextViews)
            } while (policyCursor.moveToNext())
        } else {
            viewCustomerBinding.containerPrevoiusPolicyDetails.visibility = View.GONE
        }

        customerCursor.close()
        policyCursor?.close()
        familyCursor?.close()
    }

    // Section: Visibility Management
    private fun setupSectionVisibilityListeners() {
        viewCustomerBinding.cardPersonalDetails.setOnClickListener {
            toggleContainerVisibility(viewCustomerBinding.containerPersonalDetails)
        }
        viewCustomerBinding.cardFamilyDetails.setOnClickListener {
            toggleContainerVisibility(viewCustomerBinding.containerFamilyDetails)
        }
        viewCustomerBinding.cardMeddicalDetails.setOnClickListener {
            toggleContainerVisibility(viewCustomerBinding.containerMeddicalDetails)
        }
        viewCustomerBinding.cardOtherDetails.setOnClickListener {
            toggleContainerVisibility(viewCustomerBinding.containerOtherDetails)
        }
        viewCustomerBinding.cardPreviousePolicyDetails.setOnClickListener {
            toggleContainerVisibility(viewCustomerBinding.containerPrevoiusPolicyDetails)
        }
    }


    private fun toggleContainerVisibility(selectedContainer: View) {
        val isVisible = selectedContainer.visibility == View.VISIBLE

        // Hide all containers
        viewCustomerBinding.containerPersonalDetails.visibility = View.GONE
        viewCustomerBinding.containerFamilyDetails.visibility = View.GONE
        viewCustomerBinding.containerMeddicalDetails.visibility = View.GONE
        viewCustomerBinding.containerOtherDetails.visibility = View.GONE
        viewCustomerBinding.containerPrevoiusPolicyDetails.visibility = View.GONE
        viewCustomerBinding.btnViewPolicy.visibility = View.GONE

        // Show the selected container if it was hidden
        if (!isVisible) {
            selectedContainer.visibility = View.VISIBLE
            if (selectedContainer.id == R.id.containerPrevoiusPolicyDetails) {
                viewCustomerBinding.btnViewPolicy.visibility = View.VISIBLE
            }
        }
    }

    private fun fetchAndDisplayPolicies() {
        val cursor = databaseHelper.selectPolicyData(entryId)
        val parentLayout = viewCustomerBinding.containerPrevoiusPolicyDetails

        parentLayout.removeAllViews() // Clear existing policies
        dynamicPolicyViews.clear() // Clear the tracking list

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val inflater = LayoutInflater.from(this)
                val policyView = inflater.inflate(R.layout.policy_item, null)

                // Populate policy fields
                val branchName = policyView.findViewById<TextView>(R.id.tvBranchName)
                val policyNumber = policyView.findViewById<TextView>(R.id.tvPolicyNumber)
                val startDate = policyView.findViewById<TextView>(R.id.tvStartDate)
                val sumAssured = policyView.findViewById<TextView>(R.id.tvSumAssured)
                val planTerm = policyView.findViewById<TextView>(R.id.tvPlan)
                val premium = policyView.findViewById<TextView>(R.id.tvPolicyPremium)

                branchName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_BRANCH_NAME)))
                policyNumber.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_POLICY_NUMBER)))
                startDate.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_START_DATE)))
                sumAssured.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_SUM_ASSURED)))
                planTerm.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_PLAN)))
                premium.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.POLICY_COLUMN_POLICY_PREMIUM)))

                // Add the policy view to the parent layout
                parentLayout.addView(policyView)
                dynamicPolicyViews.add(policyView)

            } while (cursor.moveToNext())
        } else {
            Toast.makeText(this, "No policies found for this customer.", Toast.LENGTH_SHORT).show()
        }
        cursor?.close()
    }
    //Updated Version of PDF

    fun generateCustomerPdf(context: Context, entryId: Int, dbHelper: DatabaseHelper) {
        val (customerCursor, policyCursor, familyCursor) = dbHelper.getCustomerWithPoliciesAndFamily(entryId)

        if (customerCursor == null || !customerCursor.moveToFirst()) {
            Toast.makeText(context, "No customer found", Toast.LENGTH_SHORT).show()
            return
        }

        // Extracting Personal Details
        val personalDetails = mapOf(
            "Full Name" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFullName")) ?: "-"),
            "Father's Name" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFatherName")) ?: "-"),
            "Mother's Name" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMotherName")) ?: "-"),
            "Birth Place" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBirthPlace")) ?: "-"),
            "Birth Date" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBirthDate")) ?: "-"),
            "Mobile Number" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMobileNumber")) ?: "-"),
            "Email Id" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etEmailId")) ?: "-"),
            "Address" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAddress")) ?: "-"),
            "Pincode" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etPincode")) ?: "-"),
            "Nominee Name" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etNomineeName")) ?: "-"),
            "Nominee Date of Birth" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etNomineeDate")) ?: "-")
        )

        // Extracting Medical Details
        val medicalDetails = mapOf(
            "Height (cm)" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etHeight")) ?: "-"),
            "Weight (kg)" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etWeight")) ?: "-"),
            "Waist (cm)" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etWaist")) ?: "-"),
            "Chest (cm)" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etChest")) ?: "-"),
            "Any Illness" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etIllness")) ?: "-")
        )

        // Extracting Other Details
        val otherDetails = mapOf(
            "Bank Account Holder Name" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAccountHolderName")) ?: "-"),
            "Bank Account Number" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAccountNumber")) ?: "-"),
            "Bank Name" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBankName")) ?: "-"),
            "MICR" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMicr")) ?: "-"),
            "IFSC Code" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etIfsc")) ?: "-"),
            "Pancard" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etPancard")) ?: "-"),
            "Aadhar Number" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAadharNumber")) ?: "-"),
            "Education" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etEducation")) ?: "-"),
            "Occupation" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etOccupation")) ?: "-"),
            "Annual Income" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAnnualIncome")) ?: "-"),
            "Organization/Company Name" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etCompanyName")) ?: "-"),
            "Since When" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etSinceWhen")) ?: "-")
        )

        // File setup
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val pdfFolder = File(downloadsDir, "Sanvi_Associates")
        if (!pdfFolder.exists()) pdfFolder.mkdirs()
        val pdfFile = File(pdfFolder, "${personalDetails["Full Name"]}_Details.pdf")

        val pdfWriter = PdfWriter(pdfFile)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4)

        // Define Colors
        val headerColor = DeviceRgb(245, 145, 78)
        val underlineColor = DeviceRgb(245, 145, 78) // Example: Blue color
        val white = DeviceRgb(255, 255, 255)

        // Function to add section titles
        fun addSectionTitle(title: String) {
            val cell = Cell(1, 4).add(Paragraph(title).setBold().setFontSize(14f).setFontColor(white))
                .setBackgroundColor(headerColor)
                .setTextAlignment(TextAlignment.CENTER)
            val table = Table(UnitValue.createPercentArray(4)).useAllAvailableWidth()
            table.addCell(cell)
            document.add(table)
        }

        // Function to add rows to tables
        fun addRow(table: Table, fieldName: String, value: String) {
            table.addCell(Cell().add(Paragraph(fieldName).setBold()))
            table.addCell(Cell().add(Paragraph(value)))
        }

        // Title
        document.add(
            Paragraph("Sanvi Associates").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(22f).setFontColor(
                DeviceRgb(245, 145, 78)
            ))
        document.add(Paragraph("Customer Details").setTextAlignment(TextAlignment.CENTER).setFontSize(14f))
        document.add(Paragraph("\n"))

        // Personal Details Section
        addSectionTitle("Personal Details")
        document.add(Paragraph("\n"))
        val personalTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 2f, 4f))).useAllAvailableWidth()
        for ((field, value) in personalDetails) {
            addRow(personalTable, field, value)
        }
        document.add(personalTable)
        document.add(Paragraph("\n"))

        // Family Details Section
        addSectionTitle("Family Details")
        document.add(Paragraph("\n"))
        val familyTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 2f, 2f, 2f))).useAllAvailableWidth()
        familyTable.addCell(Cell().add(Paragraph("Relation").setBold()))
        familyTable.addCell(Cell().add(Paragraph("Year").setBold()))
        familyTable.addCell(Cell().add(Paragraph("Age").setBold()))
        familyTable.addCell(Cell().add(Paragraph("Condition").setBold()))

        // Add Father Details
        familyTable.addCell(Cell().add(Paragraph("Father")))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etFatherYear"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etFatherAge"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etFatherCondition"))))

        // Add Mother Details
        familyTable.addCell(Cell().add(Paragraph("Mother")))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etMotherYear"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etMotherAge"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etMotherCondition"))))

        // Add Brother Details
        familyTable.addCell(Cell().add(Paragraph("Brother")))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etBrotherYear"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etBrotherAge"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etBrotherCondition"))))

        // Add Sister Details
        familyTable.addCell(Cell().add(Paragraph("Sister")))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etSisterYear"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etSisterAge"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etSisterCondition"))))

        // Add Husband Details
        familyTable.addCell(Cell().add(Paragraph("Husband / Wife")))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etHusbandYear"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etHusbandAge"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etHusbandCondition"))))

        // Add Children Details
        familyTable.addCell(Cell().add(Paragraph("Children")))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etChildrenYear"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etChildrenAge"))))
        familyTable.addCell(Cell().add(Paragraph(getSafeColumnValue(customerCursor, "etChildrenCondition"))))

        document.add(familyTable)
        document.add(Paragraph("\n"))

        // Medical Details Section
        addSectionTitle("Medical Details")
        document.add(Paragraph("\n"))
        val medicalTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 2f, 4f))).useAllAvailableWidth()
        for ((field, value) in medicalDetails) {
            addRow(medicalTable, field, value)
        }
        document.add(medicalTable)
        document.add(Paragraph("\n"))
        document.add(Paragraph("\n"))

        // Other Details Section
        addSectionTitle("Other Details")
        document.add(Paragraph("\n"))
        val otherTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 2f, 4f))).useAllAvailableWidth()
        for ((field, value) in otherDetails) {
            addRow(otherTable, field, value)
        }
        document.add(otherTable)
        document.add(Paragraph("\n"))

        // Previous Policy Details Section
        addSectionTitle("Previous Policy Details")
        document.add(Paragraph("\n"))
        if (policyCursor != null && policyCursor.moveToFirst()) {
            val policyTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 3f, 2f, 2f, 2f, 2f))).useAllAvailableWidth()
            policyTable.addCell(Cell().add(Paragraph("Branch").setBold()))
            policyTable.addCell(Cell().add(Paragraph("Policy Number").setBold()))
            policyTable.addCell(Cell().add(Paragraph("Start Date").setBold()))
            policyTable.addCell(Cell().add(Paragraph("Coverage Amount").setBold()))
            policyTable.addCell(Cell().add(Paragraph("Plan/Term").setBold()))
            policyTable.addCell(Cell().add(Paragraph("Premium Amount").setBold()))

            do {
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etBranchName")) ?: "-")))
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etPolicyNumber")) ?: "-")))
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etStartDate")) ?: "-")))
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etSumAssured")) ?: "-")))
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etPlan")) ?: "-")))
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etPolicyPremium")) ?: "-")))
            } while (policyCursor.moveToNext())

            document.add(policyTable)
        } else {
            document.add(Paragraph("No Previous Policy Details Available").setItalic())
        }

        // Close and save document
        document.close()
        customerCursor.close()
        familyCursor?.close()
        policyCursor?.close()

        //  Toast.makeText(context, "PDF saved to: ${NNNNpdfFile.absolutePath}", Toast.LENGTH_LONG).show()
        Toast.makeText(context, "PDF saved Successfully.", Toast.LENGTH_LONG).show()
    }

    fun getSafeColumnValue(cursor: Cursor, columnName: String): String {
        val columnIndex = cursor.getColumnIndex(columnName)
        return if (columnIndex != -1) cursor.getString(columnIndex) ?: "-" else "-"
    }
}
