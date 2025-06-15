package com.example.omega.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.omega.R
import com.example.omega.data.PreferencesManager
import com.example.omega.data.TransactionRepository
import com.example.omega.databinding.ActivityDashboardBinding
import com.example.omega.notification.NotificationManager
import com.example.omega.ui.adapters.RecentTransactionsAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.omega.model.Category

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var adapter: RecentTransactionsAdapter

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionRepository = TransactionRepository(this)
        preferencesManager = PreferencesManager(this)
        notificationManager = NotificationManager(this)

        setupBottomNavigation()
        setupMonthDisplay()
        setupSummaryCards()
        setupCategoryChart()
        setupRecentTransactions()

        binding.btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        binding.btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateDashboard()
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateDashboard()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupMonthDisplay() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvCurrentMonth.text = dateFormat.format(calendar.time)
    }

    private fun setupSummaryCards() {
        val currency = preferencesManager.getCurrency()
        val totalIncome = transactionRepository.getTotalIncomeForMonth(
            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)
        )
        val totalExpenses = transactionRepository.getTotalExpensesForMonth(
            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)
        )
        val balance = totalIncome - totalExpenses

        binding.tvIncomeAmount.text = String.format("%s %.2f", currency, totalIncome)
        binding.tvExpenseAmount.text = String.format("%s %.2f", currency, totalExpenses)
        binding.tvBalanceAmount.text = String.format("%s %.2f", currency, balance)

        // Budget progress
        val budget = preferencesManager.getBudget()
        if (budget.month == calendar.get(Calendar.MONTH) &&
            budget.year == calendar.get(Calendar.YEAR) &&
            budget.amount > 0) {

            val percentage = (totalExpenses / budget.amount) * 100
            binding.progressBudget.progress = percentage.toInt().coerceAtMost(100)
            binding.tvBudgetStatus.text = String.format(
                "%.1f%% of %s %.2f", percentage, currency, budget.amount
            )

            if (percentage >= 100) {
                binding.tvBudgetStatus.setTextColor(Color.RED)
            } else if (percentage >= 80) {
                binding.tvBudgetStatus.setTextColor(Color.parseColor("#FFA500")) // Orange
            } else {
                binding.tvBudgetStatus.setTextColor(Color.GREEN)
            }
        } else {
            binding.progressBudget.progress = 0
            binding.tvBudgetStatus.text = getString(R.string.no_budget_set)
            binding.tvBudgetStatus.setTextColor(Color.GRAY)
        }
    }

    private fun setupCategoryChart() {
        val expensesByCategory = transactionRepository.getExpensesByCategory(
            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)
        )

        if (expensesByCategory.isEmpty()) {
            binding.barChart.setNoDataText(getString(R.string.no_expenses_this_month))
            binding.barChart.invalidate()
            return
        }

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val colors = ArrayList<Int>()

        expensesByCategory.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
            colors.add(Category.getColorForCategory(entry.key))
        }

        val dataSet = BarDataSet(entries, "Categories")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        binding.barChart.data = barData
        binding.barChart.description.isEnabled = false
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChart.xAxis.granularity = 1f
        binding.barChart.xAxis.labelRotationAngle = 45f
        binding.barChart.legend.isEnabled = true
        binding.barChart.setDrawValueAboveBar(true)
        binding.barChart.animateY(1000)
        binding.barChart.invalidate()
    }

    private fun setupRecentTransactions() {
        val transactions = transactionRepository.getTransactionsForMonth(
            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)
        ).sortedByDescending { it.date }.take(5)

        adapter = RecentTransactionsAdapter(transactions, preferencesManager.getCurrency())
        binding.recyclerRecentTransactions.adapter = adapter

        binding.tvViewAllTransactions.setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun updateDashboard() {
        setupMonthDisplay()
        setupSummaryCards()
        setupCategoryChart()
        setupRecentTransactions()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
        notificationManager.checkBudgetAndNotify()
    }
}
