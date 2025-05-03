package com.example.sanviassociates

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SanviAssociates.db"
        private const val DATABASE_VERSION = 1

        // Table Name
        const val TABLE_NAME = "CustomerData"

        // Column Names
        const val COLUMN_ID = "id" // Primary key for each row
        const val COLUMN_ENTRY_ID = "entry_id" // Unique ID for each submission
        const val COLUMN_FIELD_NAME = "fieldName"
        const val COLUMN_FIELD_VALUE = "fieldValue"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ENTRY_ID INTEGER,
                $COLUMN_FIELD_NAME TEXT,
                $COLUMN_FIELD_VALUE TEXT
            )
        """
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert Data with Unique Entry ID
    fun insertData(entryId: Int, fieldName: String, fieldValue: String): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_ENTRY_ID, entryId)
            put(COLUMN_FIELD_NAME, fieldName)
            put(COLUMN_FIELD_VALUE, fieldValue)
        }
        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L // Return true if insertion was successful, false otherwise
    }

    // Get the Last Entry ID (Auto-Incremented)
    fun getLastEntryId(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT MAX($COLUMN_ENTRY_ID) FROM $TABLE_NAME", null)
        var lastId = 0
        if (cursor.moveToFirst()) {
            lastId = cursor.getInt(0)
        }
        cursor.close()
        return lastId
    }

    // Delete Data by Entry ID
    fun deleteDataByEntryId(entryId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ENTRY_ID = ?", arrayOf(entryId.toString()))
    }

    // Update Data Based on Entry ID and Field Name
    fun updateData(entryId: Int, fieldName: String, newFieldValue: String): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_FIELD_VALUE, newFieldValue)
        }
        return db.update(
            TABLE_NAME,
            contentValues,
            "$COLUMN_ENTRY_ID = ? AND $COLUMN_FIELD_NAME = ?",
            arrayOf(entryId.toString(), fieldName)
        )
    }

    // Update All Data for an Entry (Optional: For Bulk Updates)
    fun updateAllDataByEntryId(entryId: Int, updatedValues: Map<String, String>): Int {
        var rowsUpdated = 0
        val db = writableDatabase
        db.beginTransaction()
        try {
            updatedValues.forEach { (fieldName, newFieldValue) ->
                val contentValues = ContentValues().apply {
                    put(COLUMN_FIELD_VALUE, newFieldValue)
                }
                rowsUpdated += db.update(
                    TABLE_NAME,
                    contentValues,
                    "$COLUMN_ENTRY_ID = ? AND $COLUMN_FIELD_NAME = ?",
                    arrayOf(entryId.toString(), fieldName)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return rowsUpdated
    }

    fun getPoliciesByEntryId(entryId: Int): List<Map<String, String>> {
        val policies = mutableListOf<Map<String, String>>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ENTRY_ID = ? AND $COLUMN_FIELD_NAME LIKE 'Policy%'",
            arrayOf(entryId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val policyData = mutableMapOf<String, String>()
                policyData["Branch"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIELD_VALUE))
                // Add other fields such as PolicyNumber, StartDate, etc.
                policies.add(policyData)
            } while (cursor.moveToNext())
        }
        cursor.close()

        return policies
    }
}