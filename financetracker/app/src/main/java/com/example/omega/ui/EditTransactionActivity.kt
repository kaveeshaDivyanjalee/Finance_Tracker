package com.example.omega.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.omega.R
import com.example.omega.data.TransactionRepository
import com.example.omega.databinding.ActivityAddTransactionBinding
import com.example.omega.model.Transaction
import com.example.omega.notification.NotificationManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.util.Log
import com.example.omega.model.Category

class EditTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var notificationManager: NotificationManager
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private lateinit var transaction: Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionRepository = TransactionRepository(this)
        notificationManager = NotificationManager(this)

        // Get transaction data from intent
        intent.extras?.let {
            transaction = Transaction(
                id = it.getString("id", ""),
                title = it.getString("title", ""),
                amount = it.getDouble("amount", 0.0),
                category = it.getString("category", ""),
                date = Date(it.getLong("date")),
                isExpense = it.getBoolean("isExpense", true)
            )
        }

        setupToolbar()
        setupCategorySpinner()
        setupDatePicker()
        setupButtons()
        populateFields()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.edit_transaction)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupCategorySpinner() {
        val categories = Category.DEFAULT_CATEGORIES.toTypedArray()
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // Log the categories to verify they're being loaded
        Log.d("EditTransactionActivity", "Categories loaded: ${categories.joinToString()}")
    }

    private fun setupDatePicker() {
        calendar.time = transaction.date
        binding.etDate.setText(dateFormatter.format(calendar.time))

        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    binding.etDate.setText(dateFormatter.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun populateFields() {
        binding.etTitle.setText(transaction.title)
        binding.etAmount.setText(transaction.amount.toString())

        val categoryPosition = Category.DEFAULT_CATEGORIES.indexOf(transaction.category)
        if (categoryPosition >= 0) {
            binding.spinnerCategory.setSelection(categoryPosition)
            Log.d("EditTransactionActivity", "Selected category: ${transaction.category} at position $categoryPosition")
        } else {
            // If category not found in default list, add it to adapter and select it
            val adapter = binding.spinnerCategory.adapter as ArrayAdapter<String>
            adapter.add(transaction.category)
            binding.spinnerCategory.setSelection(adapter.count - 1)
            Log.d("EditTransactionActivity", "Added custom category: ${transaction.category}")
        }

        if (transaction.isExpense) {
            binding.radioExpense.isChecked = true
        } else {
            binding.radioIncome.isChecked = true
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                updateTransaction()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.etTitle.text.toString().trim().isEmpty()) {
            binding.etTitle.error = getString(R.string.field_required)
            isValid = false
        }

        if (binding.etAmount.text.toString().trim().isEmpty()) {
            binding.etAmount.error = getString(R.string.field_required)
            isValid = false
        }

        return isValid
    }

    private fun updateTransaction() {
        try {
            transaction.title = binding.etTitle.text.toString().trim()
            transaction.amount = binding.etAmount.text.toString().toDouble()
            transaction.category = binding.spinnerCategory.selectedItem.toString()
            transaction.date = calendar.time
            transaction.isExpense = binding.radioExpense.isChecked

            transactionRepository.saveTransaction(transaction)
            notificationManager.checkBudgetAndNotify()

            Toast.makeText(
                this,
                getString(R.string.transaction_updated),
                Toast.LENGTH_SHORT
            ).show()

            finish()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.error_updating_transaction),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
