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
import java.util.Locale
import android.util.Log
import com.example.omega.model.Category

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var notificationManager: NotificationManager
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionRepository = TransactionRepository(this)
        notificationManager = NotificationManager(this)

        setupToolbar()
        setupCategorySpinner()
        setupDatePicker()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_transaction)

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
        Log.d("AddTransactionActivity", "Categories loaded: ${categories.joinToString()}")
    }

    private fun setupDatePicker() {
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

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
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

    private fun saveTransaction() {
        try {
            val title = binding.etTitle.text.toString().trim()
            val amount = binding.etAmount.text.toString().toDouble()
            val category = binding.spinnerCategory.selectedItem.toString()
            val date = calendar.time
            val isExpense = binding.radioExpense.isChecked

            val transaction = Transaction(
                title = title,
                amount = amount,
                category = category,
                date = date,
                isExpense = isExpense
            )

            transactionRepository.saveTransaction(transaction)
            notificationManager.checkBudgetAndNotify()

            Toast.makeText(
                this,
                getString(R.string.transaction_added),
                Toast.LENGTH_SHORT
            ).show()

            finish()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.error_adding_transaction),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
