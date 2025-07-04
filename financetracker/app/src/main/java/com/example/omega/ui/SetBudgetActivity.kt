package com.example.omega.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.omega.R
import com.example.omega.data.PreferencesManager
import com.example.omega.databinding.ActivitySetBudgetBinding
import com.example.omega.model.Budget
import com.example.omega.notification.NotificationManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SetBudgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetBudgetBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var notificationManager: NotificationManager

    private var month: Int = 0
    private var year: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        notificationManager = NotificationManager(this)

        // Get month and year from intent
        intent.extras?.let {
            month = it.getInt("month")
            year = it.getInt("year")
        }

        setupToolbar()
        setupMonthDisplay()
        setupCurrentBudget()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.set_budget)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupMonthDisplay() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)

        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonth.text = dateFormat.format(calendar.time)
    }

    private fun setupCurrentBudget() {
        val budget = preferencesManager.getBudget()

        if (budget.month == month && budget.year == year) {
            binding.etBudgetAmount.setText(budget.amount.toString())
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveBudget()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(): Boolean {
        if (binding.etBudgetAmount.text.toString().trim().isEmpty()) {
            binding.etBudgetAmount.error = getString(R.string.field_required)
            return false
        }

        return true
    }

    private fun saveBudget() {
        try {
            val amount = binding.etBudgetAmount.text.toString().toDouble()
            val budget = Budget(amount, month, year)

            preferencesManager.setBudget(budget)
            notificationManager.checkBudgetAndNotify()

            Toast.makeText(
                this,
                getString(R.string.budget_saved),
                Toast.LENGTH_SHORT
            ).show()

            finish()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.error_saving_budget),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
