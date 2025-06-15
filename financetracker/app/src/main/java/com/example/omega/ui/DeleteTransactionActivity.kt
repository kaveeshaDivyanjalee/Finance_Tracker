package com.example.omega.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.omega.R
import com.example.omega.data.TransactionRepository
import com.example.omega.databinding.ActivityDeleteTransactionBinding
import com.example.omega.notification.NotificationManager

class DeleteTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteTransactionBinding
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var notificationManager: NotificationManager

    private var transactionId: String = ""
    private var transactionTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionRepository = TransactionRepository(this)
        notificationManager = NotificationManager(this)

        // Get transaction data from intent
        intent.extras?.let {
            transactionId = it.getString("id", "")
            transactionTitle = it.getString("title", "")
        }

        setupToolbar()
        setupDeleteConfirmation()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.delete_transaction)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupDeleteConfirmation() {
        binding.tvDeleteConfirmation.text = getString(
            R.string.delete_transaction_confirmation, transactionTitle
        )
    }

    private fun setupButtons() {
        binding.btnDelete.setOnClickListener {
            deleteTransaction()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun deleteTransaction() {
        transactionRepository.deleteTransaction(transactionId)
        notificationManager.checkBudgetAndNotify()

        Toast.makeText(
            this,
            getString(R.string.transaction_deleted),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }
}
