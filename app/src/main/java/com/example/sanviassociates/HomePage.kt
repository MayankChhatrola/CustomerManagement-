package com.example.sanviassociates

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.media3.exoplayer.source.chunk.Chunk
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_ADDRESS
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_ENTRY_ID
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_FULL_NAME
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_TABLE
import com.example.sanviassociates.databinding.ActivityHomePageBinding
import com.example.sanviassociates.helpermethod.PermissionUtil
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import kotlinx.coroutines.launch
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Image
import com.itextpdf.kernel.pdf.canvas.PdfCanvas


class HomePage : AppCompatActivity() {

    private lateinit var homepageBinding: ActivityHomePageBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var adapter: HomePageAdapter
    private var fullEntryList: List<EntryData> = listOf() // Full list of entries for search functionality

    companion object {
        private const val STORAGE_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        homepageBinding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(homepageBinding.root)

        // Initialize database helper
        databaseHelper = DatabaseHelper(this)

        // Check permissions when the activity is created
        if (!PermissionUtil.hasNecessaryPermissions(this)) {
            PermissionUtil.requestNecessaryPermissions(this)
        }

        // Set up RecyclerView
        setupRecyclerView()

        // Add customer button
        homepageBinding.mcvAddCustomer.setOnClickListener {
            startActivity(Intent(this, AddCustomer::class.java))
        }
        homepageBinding.ivConverter.setOnClickListener {
            startActivity(Intent(this, Converter::class.java))
        }

        // Set up SearchView functionality
        setupSearchView()
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView() // Refresh RecyclerView on resume
    }

    private fun setupSearchView() {
        homepageBinding.searchView.queryHint = "Search customer by name"
        homepageBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    filterEntries(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    filterEntries(it)
                }
                return true
            }
        })
    }

    private fun filterEntries(query: String) {
        val filteredList = fullEntryList.filter { it.customerName.contains(query, ignoreCase = true) }
        adapter.updateData(filteredList)
    }

    private fun fetchEntriesFromDatabase(): List<EntryData> {
        val groupedData: MutableList<EntryData> = mutableListOf()
        val db = databaseHelper.readableDatabase

        val query = """
        SELECT $CUSTOMER_COLUMN_ENTRY_ID AS entry_id, 
               $CUSTOMER_COLUMN_FULL_NAME AS customerName,
               $CUSTOMER_COLUMN_ADDRESS AS address
        FROM $CUSTOMER_TABLE
        ORDER BY $CUSTOMER_COLUMN_ENTRY_ID DESC
    """
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val entryId = cursor.getInt(cursor.getColumnIndexOrThrow("entry_id"))
                val customerName = cursor.getString(cursor.getColumnIndexOrThrow("customerName")) ?: "Unknown"
                val address = cursor.getString(cursor.getColumnIndexOrThrow("address")) ?: "No details available"

                Log.d("Database", "Entry ID: $entryId, Customer Name: $customerName, Address: $address")
                groupedData.add(EntryData(entryId, customerName, address))
            } while (cursor.moveToNext())
        } else {
            Log.d("Database", "No data found in CustomerDetails table.")
        }
        cursor.close()

        return groupedData
    }

    private fun setupRecyclerView() {
        fullEntryList = fetchEntriesFromDatabase()
        adapter = HomePageAdapter(
            dataList = fullEntryList,
            onViewClick = { entryData ->
                Log.d("HomePage", "View clicked for: ${entryData.customerName}")
                Toast.makeText(this, "Generating PDF for ${entryData.customerName}", Toast.LENGTH_SHORT).show()
                val dbHelper = DatabaseHelper(this@HomePage) // Create an instance of DatabaseHelper
                generateCustomerPdf(this@HomePage, entryData.entryId, dbHelper)
            },
            onEditClick = { entryData ->
                val intent = Intent(this, UpdateCustomer::class.java)
                intent.putExtra("UNIQUE_ID", entryData.entryId)
                startActivity(intent)
            },
            onDeleteClick = { entryData ->
                deleteCustomer(entryData)
            }
        )
        homepageBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        homepageBinding.recyclerView.adapter = adapter
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
        document.add(Paragraph("Sanvi Associates").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(22f).setFontColor(DeviceRgb(245, 145, 78)))
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

      //  Toast.makeText(context, "PDF saved to: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
        Toast.makeText(context, "PDF saved Successfully.", Toast.LENGTH_LONG).show()
    }


    fun getSafeColumnValue(cursor: Cursor, columnName: String): String {
        val columnIndex = cursor.getColumnIndex(columnName)
        return if (columnIndex != -1) cursor.getString(columnIndex) ?: "-" else "-"
    }


    private fun deleteCustomer(entryData: EntryData) {
        val entryId = entryData.entryId
        val customerDeleted = databaseHelper.deleteCustomerData(entryId)
        val policiesDeleted = databaseHelper.deletePolicyData(entryId)

        if (customerDeleted > 0) {
            Toast.makeText(this, "Customer and associated policies deleted successfully!", Toast.LENGTH_SHORT).show()
            setupRecyclerView()
        } else {
            Toast.makeText(this, "Failed to delete customer. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}