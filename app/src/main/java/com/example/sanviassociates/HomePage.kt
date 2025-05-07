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
                generateLicFormPdf(this@HomePage, entryData.entryId, dbHelper)
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



    fun generateLicFormPdf(context: Context, entryId: Int, dbHelper: DatabaseHelper) {
        val (customerCursor, policyCursor) = dbHelper.getCustomerWithPolicies(entryId)

        if (customerCursor == null || !customerCursor.moveToFirst()) {
            Toast.makeText(context, "No customer found", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch customer details
        val fullName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFullName")) ?: ""
        val fatherName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFatherName")) ?: ""
        val address = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAddress")) ?: ""
        val birthPlace = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBirthPlace")) ?: ""
        val birthDate = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etBirthDate")) ?: ""
        val occupation = customerCursor.getString(customerCursor.getColumnIndexOrThrow("Occuption")) ?: ""
        val income = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etAnnualIncome")) ?: ""
        val officeName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etCompanyName")) ?: ""
        val mobile = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMobileNumber")) ?: ""
        val ifsc = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etIfsc")) ?: ""
        val micr = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etMicr")) ?: ""

        // Set up file for PDF output
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val licFolder = File(downloadsDir, "Sanvi_Associates")
        if (!licFolder.exists()) licFolder.mkdirs()
        val filePath = File(licFolder, "${fullName}_LIC_Form.pdf")

        val pdfWriter = PdfWriter(filePath)
        val pdfDoc = PdfDocument(pdfWriter)
        val document = Document(pdfDoc, PageSize.A4)

//        try {
//            // Add LIC logo
//            val logoStream = context.resources.openRawResource(R.raw.lic_logo) // Add LIC logo to res/raw/ as lic_logo.png
//            val logoImage = Image(ImageDataFactory.create(logoStream.readBytes()))
//            logoImage.scaleToFit(100f, 50f)
//            document.add(logoImage)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(context, "Failed to load LIC logo.", Toast.LENGTH_SHORT).show()
//        }

        // Add Header
        document.add(Paragraph("Shree Jaychandrasinh Parmar").setFontSize(14f).setBold().setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("Insurance Agent - Code No. 1986").setFontSize(10f).setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("Mobile: 9824500867").setFontSize(10f).setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("\n"))

        // Personal Details Section
        val personalDetailsTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f, 1f, 2f))).useAllAvailableWidth()
        personalDetailsTable.addCell(Cell().add(Paragraph("Son's Name").setFontSize(10f)))
        personalDetailsTable.addCell(Cell().add(Paragraph(if (fullName.isNotBlank()) fullName else "N/A")))
        personalDetailsTable.addCell(Cell().add(Paragraph("Father's Name").setFontSize(10f)))
        personalDetailsTable.addCell(Cell().add(Paragraph(if (fatherName.isNotBlank()) fatherName else "N/A")))
        personalDetailsTable.addCell(Cell().add(Paragraph("Address").setFontSize(10f)))
        personalDetailsTable.addCell(Cell(1, 3).add(Paragraph(if (address.isNotBlank()) address else "N/A")))
        personalDetailsTable.addCell(Cell().add(Paragraph("Place of Birth").setFontSize(10f)))
        personalDetailsTable.addCell(Cell().add(Paragraph(if (birthPlace.isNotBlank()) birthPlace else "N/A")))
        personalDetailsTable.addCell(Cell().add(Paragraph("Date of Birth").setFontSize(10f)))
        personalDetailsTable.addCell(Cell().add(Paragraph(if (birthDate.isNotBlank()) birthDate else "N/A")))
        personalDetailsTable.addCell(Cell().add(Paragraph("Mobile No.").setFontSize(10f)))
        personalDetailsTable.addCell(Cell().add(Paragraph(if (mobile.isNotBlank()) mobile else "N/A")))
        personalDetailsTable.addCell(Cell().add(Paragraph("Occupation").setFontSize(10f)))
        personalDetailsTable.addCell(Cell().add(Paragraph(if (occupation.isNotBlank()) occupation else "N/A")))
        personalDetailsTable.addCell(Cell().add(Paragraph("Annual Income").setFontSize(10f)))
        personalDetailsTable.addCell(Cell().add(Paragraph(if (income.isNotBlank()) income else "N/A")))
        personalDetailsTable.addCell(Cell().add(Paragraph("Office/Company Name").setFontSize(10f)))
        personalDetailsTable.addCell(Cell(1, 3).add(Paragraph(if (officeName.isNotBlank()) officeName else "N/A")))
        document.add(personalDetailsTable)
        document.add(Paragraph("\n"))

        // Policy Details Section
        document.add(Paragraph("Details of Previous Policies (Self/Father/Husband)").setBold().setFontSize(12f))
        val policyTable = Table(UnitValue.createPercentArray(floatArrayOf(1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f))).useAllAvailableWidth()
        listOf("Branch", "Policy Number", "Start Date", "Sum Assured", "Plan/Term", "Premium").forEach {
            policyTable.addHeaderCell(Cell().add(Paragraph(it).setFontSize(10f).setBold()))
        }

        if (policyCursor != null && policyCursor.moveToFirst()) {
            do {
                val branchName = policyCursor.getString(policyCursor.getColumnIndexOrThrow("etBranchName")) ?: "N/A"
                val policyNumber = policyCursor.getString(policyCursor.getColumnIndexOrThrow("etPolicyNumber")) ?: "N/A"
                val startDate = policyCursor.getString(policyCursor.getColumnIndexOrThrow("etStartDate")) ?: "N/A"
                val sumAssured = policyCursor.getString(policyCursor.getColumnIndexOrThrow("etSumAssured")) ?: "N/A"
                val plan = policyCursor.getString(policyCursor.getColumnIndexOrThrow("etPlan")) ?: "N/A"
                val policyPremium = policyCursor.getString(policyCursor.getColumnIndexOrThrow("etPolicyPremium")) ?: "N/A"

                listOf(branchName, policyNumber, startDate, sumAssured, plan, policyPremium).forEach {
                    policyTable.addCell(Cell().add(Paragraph(it).setFontSize(10f)))
                }
            } while (policyCursor.moveToNext())
        } else {
            repeat(3) { // Add empty rows to maintain structure
                repeat(6) {
                    policyTable.addCell(Cell().add(Paragraph("N/A").setFontSize(10f)))
                }
            }
        }
        document.add(policyTable)

        // Footer Section
        document.add(Paragraph("\n"))
        val footerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 3f))).useAllAvailableWidth()
        footerTable.addCell(Cell().add(Paragraph("IFSC Code:").setFontSize(10f)))
        footerTable.addCell(Cell().add(Paragraph(if (ifsc.isNotBlank()) ifsc else "N/A")))
        footerTable.addCell(Cell().add(Paragraph("MICR Code:").setFontSize(10f)))
        footerTable.addCell(Cell().add(Paragraph(if (micr.isNotBlank()) micr else "N/A")))
        footerTable.addCell(Cell().add(Paragraph("Signature:").setFontSize(10f)))
        footerTable.addCell(Cell().add(Paragraph(""))) // Signature placeholder
        document.add(footerTable)

        // Close the document
        document.close()
        customerCursor.close()
        policyCursor?.close()

        Toast.makeText(context, "LIC form PDF saved at: ${filePath.absolutePath}", Toast.LENGTH_LONG).show()
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