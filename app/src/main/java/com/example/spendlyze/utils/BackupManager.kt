package com.example.spendlyze.utils

import android.content.Context
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val context: Context,
    private val repository: TransactionRepository
) {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    data class BackupData(
        val transactions: List<Transaction>,
        val settings: Map<String, Any>
    )

    suspend fun createBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Get all transactions
            val transactions = repository.getAllTransactions().first()

            // Get settings
            val settings = mapOf(
                "currency" to repository.getCurrency(),
                "monthlyBudget" to repository.getMonthlyBudget(),
                "theme" to repository.getTheme()
            )

            // Create backup data
            val backupData = BackupData(transactions, settings)
            val json = gson.toJson(backupData)

            // Create backup file
            val timestamp = dateFormat.format(Date())
            val fileName = "spendlyze_backup_$timestamp.json"
            val file = File(context.filesDir, fileName)

            // Write backup to file
            FileOutputStream(file).use { output ->
                output.write(json.toByteArray())
            }

            Result.success(fileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromBackup(fileName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                return@withContext Result.failure(IOException("Backup file not found"))
            }

            // Read backup file
            val json = FileInputStream(file).use { input ->
                input.bufferedReader().use { it.readText() }
            }

            // Parse backup data
            val type = object : TypeToken<BackupData>() {}.type
            val backupData = gson.fromJson<BackupData>(json, type)

            // Restore transactions
            repository.deleteAllTransactions()
            backupData.transactions.forEach { transaction ->
                repository.insertTransaction(transaction)
            }

            // Restore settings
            backupData.settings.forEach { (key, value) ->
                when (key) {
                    "currency" -> repository.updateCurrency(value as String)
                    "monthlyBudget" -> repository.updateMonthlyBudget(value as Double)
                    "theme" -> repository.updateTheme(value as String)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBackupFiles(): List<String> {
        return context.filesDir.listFiles { file ->
            file.name.startsWith("spendlyze_backup_") && file.name.endsWith(".json")
        }?.map { it.name } ?: emptyList()
    }

    suspend fun restoreBackup(): Result<Unit> {
        val backupFiles = getBackupFiles()
        if (backupFiles.isEmpty()) {
            return Result.failure(IOException("No backup files found"))
        }
        return restoreFromBackup(backupFiles.last())
    }
} 