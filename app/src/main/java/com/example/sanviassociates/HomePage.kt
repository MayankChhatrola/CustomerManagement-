package com.example.sanviassociates

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.SearchView
import com.example.sanviassociates.databinding.ActivityHomePageBinding

class HomePage : AppCompatActivity() {

    private lateinit var homepageBinding: ActivityHomePageBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var adapter: HomePageAdapter
    private var fullEntryList: List<EntryData> = listOf() // Full list of entries for search functionality

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homepageBinding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(homepageBinding.root)

        // Initialize database helper
        databaseHelper = DatabaseHelper(this)

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
        setupRecyclerView()
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
        SELECT entry_id, 
               (SELECT fieldValue FROM ${DatabaseHelper.TABLE_NAME} 
                WHERE fieldName = 'etFullName' AND entry_id = e.entry_id LIMIT 1) AS customerName,
               GROUP_CONCAT(fieldName || ': ' || fieldValue, '\n') AS details
        FROM ${DatabaseHelper.TABLE_NAME} e
        GROUP BY entry_id
        ORDER BY entry_id DESC
    """
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val entryId = cursor.getInt(cursor.getColumnIndexOrThrow("entry_id"))
                val customerName =
                    cursor.getString(cursor.getColumnIndexOrThrow("customerName")) ?: "Unknown"
                val details =
                    cursor.getString(cursor.getColumnIndexOrThrow("details")) ?: "No details available"

                // Log values to debug
                Log.d("Database", "Entry ID: $entryId, Customer Name: $customerName, Details: $details")

                // Add to list
                groupedData.add(EntryData(entryId, customerName, details))
            } while (cursor.moveToNext())
        }
        cursor.close()

        return groupedData
    }

//    private fun setupRecyclerView() {
//        fullEntryList = fetchEntriesFromDatabase() // Fetch all entries from the database
//        adapter = HomePageAdapter(
//            dataList = fullEntryList,
//            onViewClick = { entryData ->
//                // Handle View action
//                showCustomerDetails(entryData)
//            },
//            onEditClick = { entryData ->
//                // Handle Edit action
//                editCustomer(entryData)
//            },
//            onDeleteClick = { entryData ->
//                // Handle Delete action
//                deleteCustomer(entryData)
//            }
//        )
//        homepageBinding.recyclerView.layoutManager = LinearLayoutManager(this)
//        homepageBinding.recyclerView.adapter = adapter
//    }

    private fun setupRecyclerView() {
        val entryList = fetchEntriesFromDatabase()
        val adapter = HomePageAdapter(
            dataList = entryList,
            onViewClick = { entryData ->
                // Handle View action
                showCustomerDetails(entryData)
            },
            onEditClick = { entryData ->
                // Handle Edit action
                val intent = Intent(this, UpdateCustomer::class.java)
                intent.putExtra("UNIQUE_ID", entryData.entryId) // Pass the unique ID to the UpdateCustomer activity
                startActivity(intent)
            },
            onDeleteClick = { entryData ->
                // Handle Delete action
                deleteCustomer(entryData)
            }
        )
        homepageBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        homepageBinding.recyclerView.adapter = adapter
    }
    private fun showCustomerDetails(entryData: EntryData) {
        // TODO: Implement functionality to show customer details
    }

    private fun editCustomer(entryData: EntryData) {
        // TODO: Implement functionality to edit customer details
    }

    private fun deleteCustomer(entryData: EntryData) {
        val entryId = entryData.entryId // Assuming `entryId` exists in EntryData class
        val rowsDeleted = databaseHelper.deleteDataByEntryId(entryId)

        if (rowsDeleted > 0) {
            Toast.makeText(this, "Customer deleted successfully!", Toast.LENGTH_SHORT).show()
            // Refresh RecyclerView
            setupRecyclerView()
        } else {
            Toast.makeText(this, "Failed to delete customer. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

}