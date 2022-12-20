package com.example.inventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.inventory.SQLCipherUtils
import net.sqlcipher.database.SupportFactory

@Database(entities = [Item::class], version = 3, exportSchema = false)
abstract class ItemRoomDatabase : RoomDatabase() {
    abstract fun  itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: ItemRoomDatabase? = null

        fun getDatabase(context: Context, passphrase: ByteArray): ItemRoomDatabase {
            val factory = SupportFactory(passphrase)
            return INSTANCE ?: synchronized(this) {
                val dbFile = context.getDatabasePath("item_database")
                val state = SQLCipherUtils.getDatabaseState(context, "item_database")

                if (state == SQLCipherUtils.State.UNENCRYPTED) {
                    SQLCipherUtils.encrypt(context, dbFile, passphrase)
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItemRoomDatabase::class.java,
                    "item_database"
                )
                    .fallbackToDestructiveMigration()
                    .openHelperFactory(factory)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

}