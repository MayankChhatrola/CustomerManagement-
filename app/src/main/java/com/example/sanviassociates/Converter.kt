package com.example.sanviassociates

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sanviassociates.databinding.ActivityConverterBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Converter : AppCompatActivity() {

    private lateinit var binding: ActivityConverterBinding

    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private var fromDate: LocalDate? = null
    private var toDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConverterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button functionality
        binding.ivConverterBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize with today's date
        val today = LocalDate.now()
        val todayString = today.format(dateFormatter)

        fromDate = today
        toDate = today

        binding.etFrom.setText(todayString)
        binding.etTo.setText(todayString)
        binding.tvSelecctedFrom.text = todayString
        binding.tvSelectedTo.text = todayString

        calculateDifference()

        // Set up date pickers
        binding.etFrom.setOnClickListener {
            showDatePicker { selectedDate ->
                fromDate = selectedDate
                val formattedDate = selectedDate.format(dateFormatter)
                binding.etFrom.setText(formattedDate)
                binding.tvSelecctedFrom.text = formattedDate
                calculateDifference()
            }
        }

        binding.etTo.setOnClickListener {
            showDatePicker { selectedDate ->
                toDate = selectedDate
                val formattedDate = selectedDate.format(dateFormatter)
                binding.etTo.setText(formattedDate)
                binding.tvSelectedTo.text = formattedDate
                calculateDifference()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
  //          .setTheme(R.style.CustomMaterialDatePicker)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraintsBuilder)
            .build()

        datePicker.show(supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val localDate = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            onDateSelected(localDate)
        }
    }

    private fun calculateDifference() {
        val from = fromDate
        val to = toDate
        if (from != null && to != null) {
            val period = if (!from.isAfter(to)) {
                Period.between(from, to)
            } else {
                Period.between(to, from)
            }

            binding.tvYear.text = period.years.toString()
            binding.tvMonths.text = period.months.toString().padStart(2, '0')
            binding.tvDays.text = period.days.toString().padStart(2, '0')
        }
    }
}
