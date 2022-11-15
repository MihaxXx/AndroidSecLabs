package com.example.inventory

import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.*
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*


class InventoryViewModel(private val itemDao: ItemDao) : ViewModel() {
    val allItems: LiveData<List<Item>> = itemDao.getItems().asLiveData()


    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }
    private fun getNewItemEntry(itemName: String, itemPrice: String, itemCount: String, providerName: String, providerEmail: String, providerPhone: String): Item {
        return Item(
            itemName = itemName,
            itemPrice = itemPrice.toDouble(),
            quantityInStock = itemCount.toInt(),
            providerName = providerName,
            providerEmail = providerEmail,
            providerPhoneNumber = providerPhone
        )
    }
    fun addNewItem(itemName: String, itemPrice: String, itemCount: String, providerName: String, providerEmail: String, providerPhone: String) {
        val newItem = getNewItemEntry(itemName, itemPrice, itemCount, providerName, providerEmail, providerPhone)
        insertItem(newItem)
    }

    fun isEntryValid(itemName: String, itemPrice: String, itemCount: String, providerName: String, providerEmail: String, providerPhone: String): Boolean {
        if (itemName.isBlank() || itemPrice.isBlank() || itemCount.isBlank() || providerName.isBlank() || !isValidEmail(providerEmail) || !isValidPhone(providerPhone)) {
            return false
        }
        return true
    }
    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun isValidPhone(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.PHONE.matcher(target).matches()
    }

    fun retrieveItem(id: Int): LiveData<Item> {
        return itemDao.getItem(id).asLiveData()
    }

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }

    fun sellItem(item: Item) {
        if (item.quantityInStock > 0) {
            val newItem = item.copy(quantityInStock = item.quantityInStock - 1)
            updateItem(newItem)
        }
    }

    fun isStockAvailable(item: Item): Boolean {
        return (item.quantityInStock > 0)
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    private fun getUpdatedItemEntry(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String,
        providerName: String,
        providerEmail: String,
        providerPhone: String
    ): Item {
        val nf = NumberFormat.getInstance()
        return Item(
            id = itemId,
            itemName = itemName,
            itemPrice = nf.parse(itemPrice)!!.toDouble(),
            quantityInStock = itemCount.toInt(),
            providerName = providerName,
            providerEmail = providerEmail,
            providerPhoneNumber = providerPhone
        )
    }

    fun updateItem(
        itemId: Int,
        itemName: String,
        itemPrice: String,
        itemCount: String,
        providerName: String,
        providerEmail: String,
        providerPhone: String
    ) {
        val updatedItem = getUpdatedItemEntry(itemId, itemName, itemPrice, itemCount, providerName, providerEmail, providerPhone)
        updateItem(updatedItem)
    }
}


class InventoryViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
