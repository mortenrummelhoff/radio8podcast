package dk.mhr.radio8podcast.data

import androidx.room.*

@Dao
interface PodcastDao {

    @Query("SELECT * FROM podcastEntity")
    fun getAll(): List<PodcastEntity>

    @Query("SELECT * FROM podcastEntity WHERE url = :url")
    fun findByUrl(url: String): PodcastEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPodcast(podcastEntity: PodcastEntity)

    @Update
    fun updatePodcast(podcastEntity: PodcastEntity)
}