package dk.mhr.radio8podcast.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class PodcastEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "url") val url: String?,
    @ColumnInfo(name = "start_position") val startPosition: Long? = 0,
    @ColumnInfo(name= "current_playing") val currentPlaying: Boolean? = false,
    @ColumnInfo(name= "has_been_played") val hasBeenPlayed: Boolean? = false
)
