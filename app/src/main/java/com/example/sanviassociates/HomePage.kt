//package com.example.sanviassociates
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.SearchView
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_ADDRESS
//import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_ENTRY_ID
//import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_FULL_NAME
//import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_TABLE
//import com.example.sanviassociates.databinding.ActivityHomePageBinding
//
//class HomePage : AppCompatActivity() {
//
//    private lateinit var homepageBinding: ActivityHomePageBinding
//    private lateinit var databaseHelper: DatabaseHelper
//    private lateinit var adapter: HomePageAdapter
//    private var fullEntryList: List<EntryData> = listOf() // Full list of entries for search functionality
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        homepageBinding = ActivityHomePageBinding.inflate(layoutInflater)
//        setContentView(homepageBinding.root)
//
//        // Initialize database helper
//        databaseHelper = DatabaseHelper(this)
//
//        // Set up RecyclerView
//        setupRecyclerView()
//
//        // Add customer button
//        homepageBinding.mcvAddCustomer.setOnClickListener {
//            startActivity(Intent(this, AddCustomer::class.java))
//        }
//
//        // Set up SearchView functionality
//        setupSearchView()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        setupRecyclerView() // Refresh RecyclerView on resume
//    }
//
//    private fun setupSearchView() {
//        homepageBinding.searchView.queryHint = "Search customer by name"
//        homepageBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                query?.let {
//                    filterEntries(it)
//                }
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                newText?.let {
//                    filterEntries(it)
//                }
//                return true
//            }
//        })
//    }
//
//    private fun filterEntries(query: String) {
//        val filteredList = fullEntryList.filter { it.customerName.contains(query, ignoreCase = true) }
//        adapter.updateData(filteredList)
//    }
//
//    private fun fetchEntriesFromDatabase(): List<EntryData> {
//        val groupedData: MutableList<EntryData> = mutableListOf()
//        val db = databaseHelper.readableDatabase
//
//        // Corrected query
//        val query = """
//        SELECT $CUSTOMER_COLUMN_ENTRY_ID AS entry_id,
//               $CUSTOMER_COLUMN_FULL_NAME AS customerName,
//               $CUSTOMER_COLUMN_ADDRESS AS address
//        FROM $CUSTOMER_TABLE
//        ORDER BY $CUSTOMER_COLUMN_ENTRY_ID DESC
//    """
//        val cursor = db.rawQuery(query, null)
//
//        if (cursor.moveToFirst()) {
//            do {
//                val entryId = cursor.getInt(cursor.getColumnIndexOrThrow("entry_id"))
//                val customerName = cursor.getString(cursor.getColumnIndexOrThrow("customerName")) ?: "Unknown"
//                val address = cursor.getString(cursor.getColumnIndexOrThrow("address")) ?: "No details available"
//
//                // Log values to debug
//                Log.d("Database", "Entry ID: $entryId, Customer Name: $customerName, Address: $address")
//
//                // Add to list
//                groupedData.add(EntryData(entryId, customerName, address))
//            } while (cursor.moveToNext())
//        } else {
//            Log.d("Database", "No data found in CustomerDetails table.")
//        }
//        cursor.close()
//
//        return groupedData
//    }
//
//    private fun setupRecyclerView() {
//        fullEntryList = fetchEntriesFromDatabase() // Fetch all entries from the database
//        adapter = HomePageAdapter(
//            dataList = fullEntryList,
//            onViewClick = { entryData ->
//                // Handle View action
//
//            },
//            onEditClick = { entryData ->
//                // Handle Edit action
//                val intent = Intent(this, UpdateCustomer::class.java)
//                intent.putExtra("UNIQUE_ID", entryData.entryId)
//                startActivity(intent)
//            },
//            onDeleteClick = { entryData ->
//                // Handle Delete action
//                deleteCustomer(entryData)
//            }
//        )
//        homepageBinding.recyclerView.layoutManager = LinearLayoutManager(this)
//        homepageBinding.recyclerView.adapter = adapter
//    }
//
//    private fun deleteCustomer(entryData: EntryData) {
//        val entryId = entryData.entryId // Assuming `entryId` exists in EntryData class
//        val customerDeleted = databaseHelper.deleteCustomerData(entryId)
//        val policiesDeleted = databaseHelper.deletePolicyData(entryId)
//
//        if (customerDeleted > 0) {
//            Toast.makeText(this, "Customer and associated policies deleted successfully!", Toast.LENGTH_SHORT).show()
//            // Refresh RecyclerView
//            setupRecyclerView()
//        } else {
//            Toast.makeText(this, "Failed to delete customer. Please try again.", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
package com.example.sanviassociates

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_ADDRESS
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_ENTRY_ID
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_FULL_NAME
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_FATHER_NAME
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_MOBILE_NUMBER
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_MOTHER_NAME
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_PAN_CARD
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_COLUMN_ADHAR_CARD
import com.example.sanviassociates.DatabaseHelper.Companion.CUSTOMER_TABLE
import com.example.sanviassociates.databinding.ActivityHomePageBinding
import com.example.sanviassociates.helpermethod.PermissionUtil
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
                generateCustomerPdf(entryData.entryId)
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

    /*private fun generateCustomerPdf(entryId: Int) {
        // Check All Files Access permission (Android 11+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Toast.makeText(this, "Please grant 'All Files Access' permission to save PDF.", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            return
        }

        // Fetch customer data
        val cursor = databaseHelper.selectCustomerData(entryId)
        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Customer data not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_FULL_NAME))
        val fatherName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_FATHER_NAME))
        val motherName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_MOTHER_NAME))
        val address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_ADDRESS))
        val mobileNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_MOBILE_NUMBER))
        val panCard = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_PAN_CARD))
        val aadharNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_ADHAR_CARD))
        cursor.close()

        // Define PDF file path in public Downloads/Sanvi_Associates
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val folder = File(downloadsDir, "Sanvi_Associates")
        if (!folder.exists() && !folder.mkdirs()) {
            Toast.makeText(this, "Failed to create PDF folder!", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfFile = File(folder, "$fullName.pdf")

        // Optional: show progress bar
        homepageBinding.progressBar.visibility = View.VISIBLE

        Thread {
            try {
                val pdfWriter = PdfWriter(pdfFile)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                // Add title
                document.add(Paragraph("Sanvi Associates").setFontSize(18f).setBold().setMarginBottom(20f))

                // Create and populate the table
                val table = Table(UnitValue.createPercentArray(2)).setWidth(UnitValue.createPercentValue(100f))
                table.addCell("Full Name:");      table.addCell(fullName ?: "")
                table.addCell("Father's Name:");  table.addCell(fatherName ?: "")
                table.addCell("Mother's Name:");  table.addCell(motherName ?: "")
                table.addCell("Address:");        table.addCell(address ?: "")
                table.addCell("Mobile Number:");  table.addCell(mobileNumber ?: "")
                table.addCell("PAN Card:");       table.addCell(panCard ?: "")
                table.addCell("Aadhaar Number:"); table.addCell(aadharNumber ?: "")
                document.add(table)

                document.close()

                // Notify media scanner so file is visible
                MediaScannerConnection.scanFile(this, arrayOf(pdfFile.absolutePath), null, null)

                runOnUiThread {
                    homepageBinding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "PDF saved to ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("PDF_ERROR", "PDF creation failed", e)
                runOnUiThread {
                    homepageBinding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to create PDF!", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }*/

    private fun generateCustomerPdf(entryId: Int) {
        if (!PermissionUtil.hasNecessaryPermissions(this)) {
            PermissionUtil.requestNecessaryPermissions(this)
            return
        }

        homepageBinding.progressBar.visibility = View.VISIBLE

        Thread {
            try {
                val cursor = databaseHelper.selectCustomerData(entryId)
                if (!cursor.moveToFirst()) {
                    runOnUiThread {
                        homepageBinding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Customer data not found!", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_FULL_NAME))
                val fatherName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_FATHER_NAME))
                val motherName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_MOTHER_NAME))
                val address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_ADDRESS))
                val mobileNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_MOBILE_NUMBER))
                val panCard = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_PAN_CARD))
                val aadharNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUSTOMER_COLUMN_ADHAR_CARD))
                cursor.close()

                val baseDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (baseDir == null) {
                    runOnUiThread {
                        homepageBinding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Storage not available!", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val folder = File(baseDir, "Sanvi_Associates")
                if (!folder.exists() && !folder.mkdirs()) {
                    runOnUiThread {
                        homepageBinding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Failed to create folder!", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val pdfFile = File(folder, "$fullName.pdf")
                val pdfWriter = PdfWriter(pdfFile)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                // Header
                document.add(Paragraph("Sanvi Associates").setBold().setFontSize(20f).setMarginBottom(15f).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))

                // Personal Details Section Header
                document.add(Paragraph("Personal Details").setBold().setFontSize(14f).setMarginTop(10f).setUnderline())

                // Create a 2-column table for details
                val table = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f))).setWidth(UnitValue.createPercentValue(100f))
                table.addCell("Full Name:");     table.addCell(fullName)
                table.addCell("Father's Name:"); table.addCell(fatherName)
                table.addCell("Mother's Name:"); table.addCell(motherName)
                table.addCell("Mobile Number:"); table.addCell(mobileNumber)
                table.addCell("Address:");       table.addCell(address)
                table.addCell("PAN Card:");      table.addCell(panCard)
                table.addCell("Aadhaar Number:");table.addCell(aadharNumber)

                document.add(table)
                document.close()

                runOnUiThread {
                    homepageBinding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "PDF saved to ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("PDF_ERROR", "PDF creation failed", e)
                runOnUiThread {
                    homepageBinding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to generate PDF!", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
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
