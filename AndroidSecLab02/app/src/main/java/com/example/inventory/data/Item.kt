package com.example.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
@Entity(tableName = "item")
data class Item (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    val itemName: String,
    @ColumnInfo(name = "price")
    val itemPrice: Double,
    @ColumnInfo(name = "quantity")
    val quantityInStock: Int,
    val providerName: String,
    val providerEmail: String,
    val providerPhoneNumber: String,
    var source: String = "manual"
)

fun Item.getFormattedPrice() : String = NumberFormat.getCurrencyInstance().format(itemPrice)