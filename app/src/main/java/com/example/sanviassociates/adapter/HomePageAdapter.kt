/*
package com.example.sanviassociates

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class HomePageAdapter(
    private var dataList: List<EntryData>,
    private val onViewClick: (EntryData) -> Unit,
    private val onEditClick: (EntryData) -> Unit,
    private val onDeleteClick: (EntryData) -> Unit
) : RecyclerView.Adapter<HomePageAdapter.HomePageViewHolder>() {

    // ViewHolder for RecyclerView
    class HomePageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val mvcView: MaterialCardView = itemView.findViewById(R.id.mvcView)
        val mvcEdit: MaterialCardView = itemView.findViewById(R.id.mvcEdit)
        val mvcDelete: MaterialCardView = itemView.findViewById(R.id.mvcDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
        return HomePageViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomePageViewHolder, position: Int) {
        val entryData = dataList[position]

        // Set customer name
        holder.tvCustomerName.text = entryData.customerName

        // Handle button clicks
       // holder.mvcView.setOnClickListener { onViewClick(entryData) }
        holder.mvcView.setOnClickListener {
            onViewClick(entryData)
        }
        holder.mvcEdit.setOnClickListener {
            // Trigger the onEditClick callback
            onEditClick(entryData)
        }
        holder.mvcDelete.setOnClickListener { onDeleteClick(entryData) }
    }

    override fun getItemCount(): Int = dataList.size

    // Update data dynamically
    fun updateData(newDataList: List<EntryData>) {
        dataList = newDataList
        notifyDataSetChanged()
    }
}
*/
package com.example.sanviassociates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sanviassociates.databinding.RecyclerItemBinding

class HomePageAdapter(
    private var dataList: List<EntryData>,
    private val onViewClick: (EntryData) -> Unit,
    private val onEditClick: (EntryData) -> Unit,
    private val onDeleteClick: (EntryData) -> Unit
) : RecyclerView.Adapter<HomePageAdapter.HomePageViewHolder>() {

    inner class HomePageViewHolder(private val binding: RecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entryData: EntryData) {
            binding.tvCustomerName.text = entryData.customerName

            // Handle button clicks
            binding.mvcView.setOnClickListener { onViewClick(entryData) }
            binding.mvcEdit.setOnClickListener { onEditClick(entryData) }
            binding.mvcDelete.setOnClickListener { onDeleteClick(entryData) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePageViewHolder {
        val binding = RecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomePageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomePageViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(newDataList: List<EntryData>) {
        dataList = newDataList
        notifyDataSetChanged()
    }
}