package com.example.sanviassociates

import android.content.Context
import android.content.Intent
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

//    fun generateCustomerPdf(context: Context, entryId: Int, dbHelper: DatabaseHelper) {
//        val (customerCursor, policyCursor) = dbHelper.getCustomerWithPolicies(entryId)
//
//        if (customerCursor == null || !customerCursor.moveToFirst()) {
//            Toast.makeText(context, "No customer found", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Extract data
//        val fullName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFullName")) ?: ""
//        val fatherName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFatherName")) ?: ""
//        val motherName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMotherName")) ?: ""
//        val address = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAddress")) ?: ""
//        val birthPlace = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBirthPlace")) ?: ""
//        val birthDate = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBirthDate")) ?: ""
//        val occupation = customerCursor.getString(customerCursor.getColumnIndexOrThrow("Occuption")) ?: ""
//        val annualIncome = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAnnualIncome")) ?: ""
//        val mobileNumber = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMobileNumber")) ?: ""
//        val emailId = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etEmailId")) ?: ""
//        val bankName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBanmeName")) ?: ""
//        val accountNumber = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAccountNumber")) ?: ""
//        val ifsc = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etIfsc")) ?: ""
//        val micr = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMicr")) ?: ""
//
//        val fatherAge = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFatherAge")) ?: ""
//        val fatherYear = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFatherYear")) ?: ""
//        val fatherCondition = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFatherCondition")) ?: ""
//
//        val motherAge = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMotherAge")) ?: ""
//        val motherYear = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMotherYear")) ?: ""
//        val motherCondition = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMotherCondition")) ?: ""
//
//        val brotherAge = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBrotherAge")) ?: ""
//        val brotherYear = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBrotherYear")) ?: ""
//        val brotherCondition = customerCursor.getString(customerCursor.getColumnIndexOrThrow("eBrotherCondition")) ?: ""
//
//        val sisterAge = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etSisterAge")) ?: ""
//        val sisterYear = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etSisterYear")) ?: ""
//        val sisterCondition = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etSisterCondition")) ?: ""
//
//        val husbandAge = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etHusbandAge")) ?: ""
//        val husbandYear = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etHusbandYear")) ?: ""
//        val husbandCondition = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etHusbandCondition")) ?: ""
//
//        val childrenAge = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etChildrenAge")) ?: ""
//        val childrenYear = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etChildrenYear")) ?: ""
//        val childrenCondition = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etChildrenCondition")) ?: ""
//
//        // Create file
//        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val pdfFolder = File(downloadsDir, "Sanvi_Associates")
//        if (!pdfFolder.exists()) pdfFolder.mkdirs()
//        val pdfFile = File(pdfFolder, "${fullName}_Details.pdf")
//
//        // iText setup
//        val pdfWriter = PdfWriter(pdfFile)
//        val pdfDocument = PdfDocument(pdfWriter)
//        val document = Document(pdfDocument, PageSize.A4)
//        document.setMargins(20f, 20f, 20f, 20f)
//
//        val blue = DeviceRgb(63, 81, 181)
//        val gray = DeviceRgb(230, 230, 250)
//
//        // Title
//        document.add(
//            Paragraph("Sanvi Associates")
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(24f)
//                .setBold()
//                .setFontColor(blue)
//                .setMarginBottom(10f)
//        )
//        document.add(
//            Paragraph("Customer Details")
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(16f)
//                .setBold()
//                .setMarginBottom(20f)
//        )
//
//        fun sectionTitle(title: String): Paragraph {
//            return Paragraph(title)
//                .setBackgroundColor(blue)
//                .setFontColor(ColorConstants.WHITE)
//                .setBold()
//                .setPadding(5f)
//                .setTextAlignment(TextAlignment.CENTER)
//                .setMarginBottom(5f)
//        }
//
//        fun addDetailRow(table: Table, label: String, value: String) {
//            table.addCell(Cell().add(Paragraph(label).setBold()))
//            table.addCell(Cell().add(Paragraph(value)))
//        }
//
//        // Personal Details
//        document.add(sectionTitle("Personal Details"))
//        val personalTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 2f, 4f))).useAllAvailableWidth()
//        addDetailRow(personalTable, "Full Name", fullName)
//        addDetailRow(personalTable, "Father's Name", fatherName)
//        addDetailRow(personalTable, "Mother's Name", motherName)
//        addDetailRow(personalTable, "Address", address)
//        addDetailRow(personalTable, "Place of Birth", birthPlace)
//        addDetailRow(personalTable, "Date of Birth", birthDate)
//        addDetailRow(personalTable, "Mobile Number", mobileNumber)
//        addDetailRow(personalTable, "Email ID", emailId)
//        addDetailRow(personalTable, "Occupation", occupation)
//        addDetailRow(personalTable, "Annual Income", annualIncome)
//        document.add(personalTable)
//
//        // Family Details
//        document.add(sectionTitle("Family Details"))
//        val familyTable = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f, 2f, 3f))).useAllAvailableWidth()
//        val headers = listOf("Relation", "Age", "Year", "Condition")
//        headers.forEach {
//            familyTable.addCell(Cell().add(Paragraph(it).setBold()).setBackgroundColor(gray).setTextAlignment(TextAlignment.CENTER))
//        }
//
//        fun addFamilyRow(rel: String, age: String, year: String, cond: String) {
//            familyTable.addCell(Cell().add(Paragraph(rel)))
//            familyTable.addCell(Cell().add(Paragraph(age)).setTextAlignment(TextAlignment.CENTER))
//            familyTable.addCell(Cell().add(Paragraph(year)).setTextAlignment(TextAlignment.CENTER))
//            familyTable.addCell(Cell().add(Paragraph(cond)))
//        }
//
//        addFamilyRow("Father", fatherAge, fatherYear, fatherCondition)
//        addFamilyRow("Mother", motherAge, motherYear, motherCondition)
//        addFamilyRow("Brother", brotherAge, brotherYear, brotherCondition)
//        addFamilyRow("Sister", sisterAge, sisterYear, sisterCondition)
//        addFamilyRow("Husband/Wife", husbandAge, husbandYear, husbandCondition)
//        addFamilyRow("Children", childrenAge, childrenYear, childrenCondition)
//        document.add(familyTable)
//
//        // Bank Details
//        document.add(sectionTitle("Bank Details"))
//        val bankTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 2f, 4f))).useAllAvailableWidth()
//        addDetailRow(bankTable, "Bank Name", bankName)
//        addDetailRow(bankTable, "Account Number", accountNumber)
//        addDetailRow(bankTable, "IFSC Code", ifsc)
//        addDetailRow(bankTable, "MICR Code", micr)
//        document.add(bankTable)
//
//        document.close()
//        customerCursor.close()
//        policyCursor?.close()
//
//        Toast.makeText(context, "PDF saved to: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
//    }

    fun generateCustomerPdf(context: Context, entryId: Int, dbHelper: DatabaseHelper) {
        val (customerCursor, policyCursor) = dbHelper.getCustomerWithPolicies(entryId)

        if (customerCursor == null || !customerCursor.moveToFirst()) {
            Toast.makeText(context, "No customer found", Toast.LENGTH_SHORT).show()
            return
        }

        // Extract all fields with fallback for null values
        val fields = mapOf(
            "etFullName" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFullName")) ?: "-"),
            "etFatherName" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFatherName")) ?: "-"),
            "etMotherName" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMotherName")) ?: "-"),
            "etAddress" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAddress")) ?: "-"),
            "etPincode" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etPincode")) ?: "-"),
            "etBirthPlace" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBirthPlace")) ?: "-"),
            "etBirthDate" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBirthDate")) ?: "-"),
            "etNominneName" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etNominneName")) ?: "-"),
            "etNommineeDate" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etNommineeDate")) ?: "-"),
            "etMobileNumber" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMobileNumber")) ?: "-"),
            "etEmailId" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etEmailId")) ?: "-"),
            "etPancard" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etPancard")) ?: "-"),
            "etAdharNumber" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAdharNumber")) ?: "-"),
            "etHeight" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etHeight")) ?: "-"),
            "etWeight" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etWeight")) ?: "-"),
            "etWaist" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etWaist")) ?: "-"),
            "etChest" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etChest")) ?: "-"),
            "etIlleness" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etIlleness")) ?: "-"),
            "etAccountHolderName" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAccountHolderName")) ?: "-"),
            "etAccountNumber" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAccountNumber")) ?: "-"),
            "etBanmeName" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBanmeName")) ?: "-"),
            "etMicr" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMicr")) ?: "-"),
            "etIfsc" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etIfsc")) ?: "-"),
            "etEducation" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etEducation")) ?: "-"),
            "Occuption" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("Occuption")) ?: "-"),
            "etAnnualIncome" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAnnualIncome")) ?: "-"),
            "etCompanyName" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etCompanyName")) ?: "-"),
            "etsinceWhen" to (customerCursor.getString(customerCursor.getColumnIndexOrThrow("etsinceWhen")) ?: "-")
        )

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val pdfFolder = File(downloadsDir, "Sanvi_Associates")
        if (!pdfFolder.exists()) pdfFolder.mkdirs()
        val pdfFile = File(pdfFolder, "${fields["etFullName"]}_Details.pdf")

        val pdfWriter = PdfWriter(pdfFile)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4)

        // Define Colors
        val headerColor = DeviceRgb(245, 145, 78) // colorPrimary
        val white = DeviceRgb(255, 255, 255) // colorOnPrimary

        fun addSectionTitle(title: String) {
            val cell = Cell(1, 4).add(Paragraph(title).setBold().setFontSize(14f).setFontColor(white))
                .setBackgroundColor(headerColor)
                .setTextAlignment(TextAlignment.CENTER)
            val table = Table(UnitValue.createPercentArray(4)).useAllAvailableWidth()
            table.addCell(cell)
            document.add(table)
        }

        // Title
        document.add(Paragraph("Sanvi Associates").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(18f))
        document.add(Paragraph("Customer Details").setTextAlignment(TextAlignment.CENTER).setFontSize(14f))
        document.add(Paragraph("\n"))

        // Personal Info
        addSectionTitle("Personal Information")
        val personalTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 2f, 4f))).useAllAvailableWidth()
        fun addRow(key1: String, key2: String) {
            personalTable.addCell(Cell().add(Paragraph(key1.replace("et", "").capitalize())))
            personalTable.addCell(Cell().add(Paragraph(fields[key1] ?: "-")))
            personalTable.addCell(Cell().add(Paragraph(key2.replace("et", "").capitalize())))
            personalTable.addCell(Cell().add(Paragraph(fields[key2] ?: "-")))
        }
        addRow("etFullName", "etFatherName")
        addRow("etMotherName", "etBirthPlace")
        addRow("etBirthDate", "etMobileNumber")
        addRow("etEmailId", "etPincode")
        addRow("etAddress", "etPancard")
        addRow("etAdharNumber", "etEducation")
        addRow("Occuption", "etAnnualIncome")
        addRow("etCompanyName", "etsinceWhen")
        document.add(personalTable)
        document.add(Paragraph("\n"))

        // Bank Info
        addSectionTitle("Bank Details")
        val bankTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 2f, 4f))).useAllAvailableWidth()
        addRow("etAccountHolderName", "etAccountNumber")
        addRow("etBanmeName", "etIfsc")
        addRow("etMicr", "-")
        document.add(bankTable)
        document.add(Paragraph("\n"))

        // Medical Info
        addSectionTitle("Medical Details")
        val medicalTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 4f, 2f, 4f))).useAllAvailableWidth()
        addRow("etHeight", "etWeight")
        addRow("etWaist", "etChest")
        addRow("etIlleness", "-")
        document.add(medicalTable)
        document.add(Paragraph("\n"))

        // Previous Policy Details
        addSectionTitle("Previous Policy Details")
        if (policyCursor != null && policyCursor.moveToFirst()) {
            val policyTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 3f, 2f, 2f, 2f, 2f))).useAllAvailableWidth()
            policyTable.addCell(Cell().add(Paragraph("Policy #").setBold()))
            policyTable.addCell(Cell().add(Paragraph("Branch Name").setBold()))
            policyTable.addCell(Cell().add(Paragraph("Start Date").setBold()))
            policyTable.addCell(Cell().add(Paragraph("Sum Assured").setBold()))
            policyTable.addCell(Cell().add(Paragraph("Plan").setBold()))
            policyTable.addCell(Cell().add(Paragraph("Premium").setBold()))

            do {
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etPolicyNumber")) ?: "-")))
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etBranchName")) ?: "-")))
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etStartDate")) ?: "-")))
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etSumAssured")) ?: "-")))
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etPlan")) ?: "-")))
                policyTable.addCell(Cell().add(Paragraph(policyCursor.getString(policyCursor.getColumnIndexOrThrow("etPolicyPremium")) ?: "-")))
            } while (policyCursor.moveToNext())

            document.add(policyTable)
        } else {
            document.add(Paragraph("No Previous Policy Details Available").setItalic())
        }

        document.close()
        customerCursor.close()
        policyCursor?.close()

        Toast.makeText(context, "PDF saved to: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
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