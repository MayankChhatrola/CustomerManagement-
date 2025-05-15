package com.example.sanviassociates.utils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.sanviassociates.DatabaseHelper
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File

fun generateCustomerPdf(context: Context, entryId: Int, dbHelper: DatabaseHelper): File? {
    val (customerCursor, policyCursor, familyCursor) = dbHelper.getCustomerWithPoliciesAndFamily(entryId)

    if (customerCursor == null || !customerCursor.moveToFirst()) {
        Toast.makeText(context, "No customer found", Toast.LENGTH_SHORT).show()
        return null
    }

    // Extracting Personal Details
    val fullName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("etFullName")) ?: "Unknown"

    // File setup
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val pdfFolder = File(downloadsDir, "Sanvi_Associates")
    if (!pdfFolder.exists()) pdfFolder.mkdirs()
    val pdfFile = File(pdfFolder, "${fullName}_Details.pdf")

    val pdfWriter = PdfWriter(pdfFile)
    val pdfDocument = PdfDocument(pdfWriter)
    val document = Document(pdfDocument)

    // Add content to the PDF
    document.add(Paragraph("Sanvi Associates"))
    document.add(Paragraph("Customer Details"))
    document.close()

    customerCursor.close()
    policyCursor?.close()
    familyCursor?.close()

    return pdfFile
}