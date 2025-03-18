package bav.petus.cache

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<PetsDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("petus.db")
    return Room.databaseBuilder<PetsDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}