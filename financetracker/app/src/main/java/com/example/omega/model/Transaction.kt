package com.example.omega.model

import java.util.Date
import java.util.UUID

data class Transaction(
    var id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var amount: Double = 0.0,
    var category: String = "",
    var date: Date = Date(),
    var isExpense: Boolean = true
) {
    companion object {
        fun fromMap(map: Map<String, Any>): Transaction {
            return Transaction(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                title = map["title"] as? String ?: "",
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                category = map["category"] as? String ?: "Other",
                date = map["date"] as? Date ?: Date(),
                isExpense = map["isExpense"] as? Boolean ?: true
            )
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "amount" to amount,
            "category" to category,
            "date" to date,
            "isExpense" to isExpense
        )
    }
}
