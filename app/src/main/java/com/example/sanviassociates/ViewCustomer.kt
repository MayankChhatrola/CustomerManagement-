package com.example.sanviassociates

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sanviassociates.databinding.ActivityHomePageBinding
import com.example.sanviassociates.databinding.ActivityViewCustomerBinding

class ViewCustomer : AppCompatActivity() {

    private lateinit var viewCustomerBinding: ActivityViewCustomerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewCustomerBinding = ActivityViewCustomerBinding.inflate(layoutInflater)
        setContentView(viewCustomerBinding.root)

    }
}