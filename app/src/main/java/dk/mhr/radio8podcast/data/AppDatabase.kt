package dk.mhr.radio8podcast.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PodcastEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun podcastDao(): PodcastDao
}