package com.example.spendlyze.data.backup

import android.app.Application
import android.util.Log
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.data.repository.TransactionRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class BackupManager @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val application: Application
) {
    companion object {
        private const val TAG = "BackupManager"
        private const val BACKUP_FILE_NAME = "transactions_backup.json"
        private val json = Json { ignoreUnknownKeys = true }
    }

    suspend fun createBackup(): Boolean {
        return try {
            val transactions = transactionRepository.getAllTransactions()
            val jsonString = json.encodeToString(transactions)
            val backupFile = File(application.filesDir, BACKUP_FILE_NAME)
            backupFile.writeText(jsonString)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating backup", e)
            false
        }
    }

    suspend fun restoreBackup(): Boolean {
        return try {
            val backupFile = File(application.filesDir, BACKUP_FILE_NAME)
            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file not found")
                return false
            }
            restoreTransactions(backupFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring backup", e)
            false
        }
    }

    private suspend fun restoreTransactions(backupFile: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = backupFile.readText()
                val transactions = json.decodeFromString<List<Transaction>>(jsonString)
                
                // Clear existing transactions
                transactionRepository.deleteAllTransactions()
                
                // Add restored transactions
                transactions.forEach { transaction ->
                    transactionRepository.insertTransaction(transaction)
                }
                
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring transactions", e)
                false
            }
        }
    }
} 