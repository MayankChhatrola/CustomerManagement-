package com.example.sanviassociates

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object {
            private const val DATABASE_NAME = "SanviAssociates.db"
            private const val DATABASE_VERSION = 2 // Incremented version

            // Customer Table
            const val CUSTOMER_TABLE = "CustomerDetails"
            const val CUSTOMER_COLUMN_ID = "id" // Primary key
            const val CUSTOMER_COLUMN_ENTRY_ID = "entry_id" // Unique ID for each customer
            const val CUSTOMER_COLUMN_FULL_NAME = "etFullName"
            const val CUSTOMER_COLUMN_FATHER_NAME = "etFatherName"
            const val CUSTOMER_COLUMN_MOTHER_NAME = "etMotherName"
            const val CUSTOMER_COLUMN_ADDRESS = "etAddress"
            const val CUSTOMER_COLUMN_PINCODE = "etPincode"
            const val CUSTOMER_COLUMN_BIRTH_PLACE = "etBirthPlace"
            const val CUSTOMER_COLUMN_BIRTH_DATE = "etBirthDate"
            const val CUSTOMER_COLUMN_MOBILE_NUMBER = "etMobileNumber"
            const val CUSTOMER_COLUMN_EMAIL_ID = "etEmailId"
            const val CUSTOMER_COLUMN_PAN_CARD = "etPancard" // New Field
            const val CUSTOMER_COLUMN_ADHAR_CARD = "etAdharNumber" // Updated Field
            const val CUSTOMER_COLUMN_FATHER_AGE = "etFatherAge"
            const val CUSTOMER_COLUMN_FATHER_YEAR = "etFatherYear"
            const val CUSTOMER_COLUMN_FATHER_CONDITION = "etFatherCondition"
            const val CUSTOMER_COLUMN_MOTHER_AGE = "etMotherAge"
            const val CUSTOMER_COLUMN_MOTHER_YEAR = "etMotherYear"
            const val CUSTOMER_COLUMN_MOTHER_CONDITION = "etMotherCondition"
            const val CUSTOMER_COLUMN_BROTHER_AGE = "etBrotherAge"
            const val CUSTOMER_COLUMN_BROTHER_YEAR = "etBrotherYear"
            const val CUSTOMER_COLUMN_BROTHER_CONDITION = "eBrotherCondition"
            const val CUSTOMER_COLUMN_SISTER_AGE = "etSisterAge"
            const val CUSTOMER_COLUMN_SISTER_YEAR = "etSisterYear"
            const val CUSTOMER_COLUMN_SISTER_CONDITION = "etSisterCondition"
            const val CUSTOMER_COLUMN_HUSBAND_AGE = "etHusbandAge"
            const val CUSTOMER_COLUMN_HUSBAND_YEAR = "etHusbandYear"
            const val CUSTOMER_COLUMN_HUSBAND_CONDITION = "etHusbandCondition"
            const val CUSTOMER_COLUMN_CHILDREN_AGE = "etChildrenAge"
            const val CUSTOMER_COLUMN_CHILDREN_YEAR = "etChildrenYear"
            const val CUSTOMER_COLUMN_CHILDREN_CONDITION = "etChildrenCondition"
            const val CUSTOMER_COLUMN_HEIGHT = "etHeight"
            const val CUSTOMER_COLUMN_WEIGHT = "etWeight"
            const val CUSTOMER_COLUMN_WAIST = "etWaist"
            const val CUSTOMER_COLUMN_CHEST = "etChest"
            const val CUSTOMER_COLUMN_ILLNESS = "etIlleness"
            const val CUSTOMER_COLUMN_PREGNANCY_DATE = "etPregencyDate"
            const val CUSTOMER_COLUMN_ACCOUNT_HOLDER_NAME = "etAccountHolderName"
            const val CUSTOMER_COLUMN_ACCOUNT_NUMBER = "etAccountNumber"
            const val CUSTOMER_COLUMN_BANK_NAME = "etBanmeName"
            const val CUSTOMER_COLUMN_MICR = "etMicr"
            const val CUSTOMER_COLUMN_IFSC = "etIfsc"
            const val CUSTOMER_COLUMN_EDUCATION = "etEducation"
            const val CUSTOMER_COLUMN_OCCUPATION = "Occuption"
            const val CUSTOMER_COLUMN_ANNUAL_INCOME = "etAnnualIncome"
            const val CUSTOMER_COLUMN_COMPANY_NAME = "etCompanyName"
            const val CUSTOMER_COLUMN_SINCE_WHEN = "etsinceWhen"

            // Policy Table
            const val POLICY_TABLE = "PolicyDetails"
            const val POLICY_COLUMN_ID = "id" // Primary key
            const val POLICY_COLUMN_CUSTOMER_ID = "customer_id" // Foreign key referencing CustomerDetails table
            const val POLICY_COLUMN_BRANCH_NAME = "etBranchName"
            const val POLICY_COLUMN_POLICY_NUMBER = "etPolicyNumber"
            const val POLICY_COLUMN_START_DATE = "etStartDate"
            const val POLICY_COLUMN_SUM_ASSURED = "etSumAssured"
            const val POLICY_COLUMN_PLAN = "etPlan"
            const val POLICY_COLUMN_POLICY_PREMIUM = "etPolicyPremium"
        }

        override fun onCreate(db: SQLiteDatabase) {
            val createCustomerTableQuery = """
            CREATE TABLE $CUSTOMER_TABLE (
                $CUSTOMER_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $CUSTOMER_COLUMN_ENTRY_ID INTEGER,
                $CUSTOMER_COLUMN_FULL_NAME TEXT,
                $CUSTOMER_COLUMN_FATHER_NAME TEXT,
                $CUSTOMER_COLUMN_MOTHER_NAME TEXT,
                $CUSTOMER_COLUMN_ADDRESS TEXT,
                $CUSTOMER_COLUMN_PINCODE TEXT,
                $CUSTOMER_COLUMN_BIRTH_PLACE TEXT,
                $CUSTOMER_COLUMN_BIRTH_DATE TEXT,
                $CUSTOMER_COLUMN_MOBILE_NUMBER TEXT,
                $CUSTOMER_COLUMN_EMAIL_ID TEXT,
                $CUSTOMER_COLUMN_PAN_CARD TEXT,
                $CUSTOMER_COLUMN_ADHAR_CARD TEXT,
                $CUSTOMER_COLUMN_FATHER_AGE TEXT,
                $CUSTOMER_COLUMN_FATHER_YEAR TEXT,
                $CUSTOMER_COLUMN_FATHER_CONDITION TEXT,
                $CUSTOMER_COLUMN_MOTHER_AGE TEXT,
                $CUSTOMER_COLUMN_MOTHER_YEAR TEXT,
                $CUSTOMER_COLUMN_MOTHER_CONDITION TEXT,
                $CUSTOMER_COLUMN_BROTHER_AGE TEXT,
                $CUSTOMER_COLUMN_BROTHER_YEAR TEXT,
                $CUSTOMER_COLUMN_BROTHER_CONDITION TEXT,
                $CUSTOMER_COLUMN_SISTER_AGE TEXT,
                $CUSTOMER_COLUMN_SISTER_YEAR TEXT,
                $CUSTOMER_COLUMN_SISTER_CONDITION TEXT,
                $CUSTOMER_COLUMN_HUSBAND_AGE TEXT,
                $CUSTOMER_COLUMN_HUSBAND_YEAR TEXT,
                $CUSTOMER_COLUMN_HUSBAND_CONDITION TEXT,
                $CUSTOMER_COLUMN_CHILDREN_AGE TEXT,
                $CUSTOMER_COLUMN_CHILDREN_YEAR TEXT,
                $CUSTOMER_COLUMN_CHILDREN_CONDITION TEXT,
                $CUSTOMER_COLUMN_HEIGHT TEXT,
                $CUSTOMER_COLUMN_WEIGHT TEXT,
                $CUSTOMER_COLUMN_WAIST TEXT,
                $CUSTOMER_COLUMN_CHEST TEXT,
                $CUSTOMER_COLUMN_ILLNESS TEXT,
                $CUSTOMER_COLUMN_PREGNANCY_DATE TEXT,
                $CUSTOMER_COLUMN_ACCOUNT_HOLDER_NAME TEXT,
                $CUSTOMER_COLUMN_ACCOUNT_NUMBER TEXT,
                $CUSTOMER_COLUMN_BANK_NAME TEXT,
                $CUSTOMER_COLUMN_MICR TEXT,
                $CUSTOMER_COLUMN_IFSC TEXT,
                $CUSTOMER_COLUMN_EDUCATION TEXT,
                $CUSTOMER_COLUMN_OCCUPATION TEXT,
                $CUSTOMER_COLUMN_ANNUAL_INCOME TEXT,
                $CUSTOMER_COLUMN_COMPANY_NAME TEXT,
                $CUSTOMER_COLUMN_SINCE_WHEN TEXT
            )
        """
            db.execSQL(createCustomerTableQuery)

            val createPolicyTableQuery = """
            CREATE TABLE $POLICY_TABLE (
                $POLICY_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $POLICY_COLUMN_CUSTOMER_ID INTEGER,
                $POLICY_COLUMN_BRANCH_NAME TEXT,
                $POLICY_COLUMN_POLICY_NUMBER TEXT,
                $POLICY_COLUMN_START_DATE TEXT,
                $POLICY_COLUMN_SUM_ASSURED TEXT,
                $POLICY_COLUMN_PLAN TEXT,
                $POLICY_COLUMN_POLICY_PREMIUM TEXT,
                FOREIGN KEY($POLICY_COLUMN_CUSTOMER_ID) REFERENCES $CUSTOMER_TABLE($CUSTOMER_COLUMN_ENTRY_ID)
            )
        """
            db.execSQL(createPolicyTableQuery)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) { // Add new columns if upgrading from version 1
                db.execSQL("ALTER TABLE $CUSTOMER_TABLE ADD COLUMN $CUSTOMER_COLUMN_ADHAR_CARD TEXT")
                db.execSQL("ALTER TABLE $CUSTOMER_TABLE ADD COLUMN $CUSTOMER_COLUMN_PAN_CARD TEXT")
            }
        }


    // Insert Customer Data
    fun insertCustomerData(contentValues: ContentValues): Long {
        val db = writableDatabase
        return db.insert(CUSTOMER_TABLE, null, contentValues)
    }

    // Insert Policy Data
    fun insertPolicyData(contentValues: ContentValues): Long {
        val db = writableDatabase
        return db.insert(POLICY_TABLE, null, contentValues)
    }

    // Update Customer Data
    fun updateCustomerData(entryId: Int, contentValues: ContentValues): Int {
        val db = writableDatabase
        return db.update(
            CUSTOMER_TABLE,
            contentValues,
            "$CUSTOMER_COLUMN_ENTRY_ID = ?",
            arrayOf(entryId.toString())
        )
    }

    // Update Policy Data
    fun updatePolicyData(policyId: Int, contentValues: ContentValues): Int {
        val db = writableDatabase
        return db.update(
            POLICY_TABLE,
            contentValues,
            "$POLICY_COLUMN_ID = ?",
            arrayOf(policyId.toString())
        )
    }

    // Delete Customer Data
    fun deleteCustomerData(entryId: Int): Int {
        val db = writableDatabase
        return db.delete(CUSTOMER_TABLE, "$CUSTOMER_COLUMN_ENTRY_ID = ?", arrayOf(entryId.toString()))
    }

    // Delete Policy Data
    fun deletePolicyData(policyId: Int): Int {
        val db = writableDatabase
        return db.delete(POLICY_TABLE, "$POLICY_COLUMN_ID = ?", arrayOf(policyId.toString()))
    }

    // Select Customer Data
    fun selectCustomerData(entryId: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $CUSTOMER_TABLE WHERE $CUSTOMER_COLUMN_ENTRY_ID = ?", arrayOf(entryId.toString()))
    }

    // Select Policy Data
    fun selectPolicyData(customerId: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $POLICY_TABLE WHERE $POLICY_COLUMN_CUSTOMER_ID = ?", arrayOf(customerId.toString()))
    }

    fun getLastCustomerEntryId(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT MAX(entry_id) FROM CustomerDetails", null)
        var lastId = 0
        if (cursor.moveToFirst()) {
            lastId = cursor.getInt(0)
        }
        cursor.close()
        return lastId
    }

    fun getCustomerWithPolicies(entryId: Int): Pair<Cursor?, Cursor?> {
        val db = readableDatabase
        val customerCursor = db.rawQuery(
            "SELECT * FROM $CUSTOMER_TABLE WHERE $CUSTOMER_COLUMN_ENTRY_ID = ?",
            arrayOf(entryId.toString())
        )
        val policyCursor = db.rawQuery(
            "SELECT * FROM $POLICY_TABLE WHERE $POLICY_COLUMN_CUSTOMER_ID = ?",
            arrayOf(entryId.toString())
        )
        return Pair(customerCursor, policyCursor)
    }

}