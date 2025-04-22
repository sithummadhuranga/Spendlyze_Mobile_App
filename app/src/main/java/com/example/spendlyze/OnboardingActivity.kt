package com.example.spendlyze

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import android.content.Context

class OnboardingActivity : AppCompatActivity() {

    private lateinit var onboardingItemsAdapter: OnboardingItemsAdapter
    private lateinit var indicatorsContainer: LinearLayout
    private lateinit var buttonNext: MaterialButton
    private lateinit var buttonSkip: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Check if onboarding is completed
        if (isOnboardingCompleted()) {
            startMainActivity()
            finish()
            return
        }

        setOnboardingItems()
        setupIndicators()
        setCurrentIndicator(0)
    }

    private fun setOnboardingItems() {
        onboardingItemsAdapter = OnboardingItemsAdapter(
            listOf(
                OnboardingItem(
                    "Track Your Expenses",
                    "Keep track of your daily expenses and income in one place",
                    R.drawable.onboarding_track
                ),
                OnboardingItem(
                    "Set Monthly Budget",
                    "Set your monthly budget and get alerts when you're close to the limit",
                    R.drawable.onboarding_budget
                ),
                OnboardingItem(
                    "Analyze Spending",
                    "Get insights into your spending habits with detailed charts",
                    R.drawable.onboarding_analyze
                )
            )
        )

        val onboardingViewPager = findViewById<ViewPager2>(R.id.onboardingViewPager)
        onboardingViewPager.adapter = onboardingItemsAdapter

        onboardingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        // Initialize Next button
        buttonNext = findViewById(R.id.buttonNext)
        buttonNext.setOnClickListener {
            if (onboardingViewPager.currentItem + 1 < onboardingItemsAdapter.itemCount) {
                onboardingViewPager.currentItem += 1
            } else {
                startMainActivity()
            }
        }

        // Initialize Skip button
        buttonSkip = findViewById(R.id.buttonSkip)
        buttonSkip.setOnClickListener {
            startMainActivity()
        }
    }

    private fun setupIndicators() {
        indicatorsContainer = findViewById(R.id.indicatorsContainer)
        val indicators = arrayOfNulls<ImageView>(onboardingItemsAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive_background
                    )
                )
                it.layoutParams = layoutParams
                indicatorsContainer.addView(it)
            }
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = indicatorsContainer.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorsContainer.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active_background
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive_background
                    )
                )
            }
        }
    }

    private fun startMainActivity() {
        // Save that onboarding is completed
        getSharedPreferences("onboarding", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("isCompleted", true)
            .apply()

        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

    private fun isOnboardingCompleted(): Boolean {
        return getSharedPreferences("onboarding", Context.MODE_PRIVATE)
            .getBoolean("isCompleted", false)
    }
}

data class OnboardingItem(
    val title: String,
    val description: String,
    val image: Int
) 