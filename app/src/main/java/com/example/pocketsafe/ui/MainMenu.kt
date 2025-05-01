package com.example.pocketsafe.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.pocketsafe.R
import com.example.pocketsafe.data.SavingsGoal
import com.example.pocketsafe.data.dao.SavingsGoalDao
import com.example.pocketsafe.databinding.ActivityMainMenuBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainMenu : AppCompatActivity() {

    @Inject
    lateinit var savingsGoalDao: SavingsGoalDao

    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var cvBankingDetails: CardView
    private lateinit var cvAddExpense: CardView
    private lateinit var cvEditGoal: CardView
    private lateinit var cvSubscriptions: CardView
    private lateinit var cvViewCategories: CardView
    private lateinit var cvViewUsers: CardView

    private lateinit var progressAnimation: ImageView
    private lateinit var tvGoalStatus: TextView
    private lateinit var tvMonthsRemaining: TextView
    private lateinit var tvProgressPercentage: TextView

    // Navigation bar items
    private lateinit var bottomNavigation: com.google.android.material.bottomnavigation.BottomNavigationView

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        try {
            // Linking CardViews
            cvBankingDetails = binding.btnBankingDetails
            cvAddExpense = binding.btnAddExpense
            cvEditGoal = binding.btnEditGoal
            cvSubscriptions = binding.btnSubscriptions
            cvViewCategories = binding.btnViewCategories
            cvViewUsers = binding.btnViewUsers

            // Linking Navigation Bar
            bottomNavigation = binding.navigationBar.bottomNavigation

            progressAnimation = binding.progressAnimation
            tvGoalStatus = binding.tvGoalStatus
            tvMonthsRemaining = binding.tvMonthsRemaining
            tvProgressPercentage = binding.tvProgressPercentage

            // Load and display current savings goal
            loadSavingsGoal()

            // Button listeners
            cvAddExpense.setOnClickListener {
                val intent = Intent(this, ExpenseEntryActivity::class.java)
                startActivity(intent)
            }

            cvEditGoal.setOnClickListener {
                val intent = Intent(this, SetupActivity::class.java)
                startActivity(intent)
            }

            cvBankingDetails.setOnClickListener {
                // TODO: Open Banking Details screen
                Toast.makeText(this, "Banking Details feature coming soon!", Toast.LENGTH_SHORT).show()
            }

            cvSubscriptions.setOnClickListener {
                // TODO: Open Subscription Tracker screen
                Toast.makeText(this, "Subscriptions feature coming soon!", Toast.LENGTH_SHORT).show()
            }

            cvViewCategories.setOnClickListener {
                val intent = Intent(this, CategoryActivity::class.java)
                startActivity(intent)
            }

            cvViewUsers.setOnClickListener {
                val intent = Intent(this, UserListActivity::class.java)
                startActivity(intent)
            }

            // Navigation Bar listeners
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        // Already on home screen
                        Toast.makeText(this, "You're already on the home screen", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.navigation_expenses -> {
                        // TODO: Navigate to Expenses screen
                        Toast.makeText(this, "Expenses feature coming soon!", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.navigation_categories -> {
                        val intent = Intent(this, CategoryActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.navigation_profile -> {
                        // TODO: Navigate to Profile screen
                        Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }

            // Show setup prompt only if there's no savings goal
            checkAndShowSetupPrompt()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing UI: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadSavingsGoal() {
        lifecycleScope.launch {
            try {
                val goal = withContext(Dispatchers.IO) {
                    try {
                        savingsGoalDao.getCurrentGoal()
                    } catch (e: Exception) {
                        null
                    }
                }

                goal?.let {
                    updateProgressUI(it)
                } ?: run {
                    // If no goal exists, show a message or handle accordingly
                    Toast.makeText(this@MainMenu, "No savings goal set", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainMenu, "Error loading savings goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProgressUI(goal: SavingsGoal) {
        val progress = if (goal.target_amount > 0) {
            ((goal.current_amount / goal.target_amount) * 100).toInt()
        } else {
            0
        }
        val remaining = goal.target_amount - goal.current_amount

        try {
            // Update progress bar with error handling
            val progressDrawable = when {
                progress < 12 -> R.drawable.progress_frame_0
                progress < 25 -> R.drawable.progress_frame_1
                progress < 37 -> R.drawable.progress_frame_2
                progress < 50 -> R.drawable.progress_frame_3
                progress < 62 -> R.drawable.progress_frame_4
                progress < 75 -> R.drawable.progress_frame_5
                progress < 87 -> R.drawable.progress_frame_6
                else -> R.drawable.progress_frame_7
            }
            progressAnimation.setImageResource(progressDrawable)
        } catch (e: Exception) {
            // If drawables are missing, just show a default image
            progressAnimation.setImageResource(android.R.drawable.ic_menu_compass)
        }

        // Update progress percentage text
        tvProgressPercentage.text = "$progress% COMPLETE"

        // Update text views
        tvGoalStatus.text = "GOAL: ${currencyFormat.format(goal.target_amount)} | SAVED: ${currencyFormat.format(goal.current_amount)}"
        
        // Calculate months remaining
        val monthsRemaining = if (goal.target_date > System.currentTimeMillis()) {
            val diff = goal.target_date - System.currentTimeMillis()
            (diff / (1000L * 60 * 60 * 24 * 30)).toInt() + 1
        } else {
            0
        }
        tvMonthsRemaining.text = "$monthsRemaining MONTHS REMAINING"
    }

    private fun checkAndShowSetupPrompt() {
        lifecycleScope.launch {
            try {
                val goal = withContext(Dispatchers.IO) {
                    savingsGoalDao.getCurrentGoal()
                }

                if (goal == null) {
                    showBudgetGoalPrompt()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainMenu, "Error checking setup status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBudgetGoalPrompt() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Setup Your PocketSafe")

        val message = """
            🎯 What is your monthly budget goal?
            💸 What are your regular expenses?
            📂 What categories do they fall under?
            🏦 Banking Details?
        """.trimIndent()

        builder.setMessage(message)

        builder.setPositiveButton("Setup") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton("Skip") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
} 