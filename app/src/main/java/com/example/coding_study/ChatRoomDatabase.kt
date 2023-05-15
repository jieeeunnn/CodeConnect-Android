package com.example.coding_study

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Entity
import androidx.room.RoomDatabase
import kotlin.coroutines.CoroutineContext


// 채팅방 데이터 클래스
@Entity(tableName = "chat_room")
data class ChatRoomEntity(
    @PrimaryKey val roomId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "host_nickname") val hostNickname: String,
    @ColumnInfo(name = "current_datetime") val currentDateTime: String,
    @ColumnInfo(name = "current_count") val currentCount: Int
)

@Dao
interface ChatRoomDao {
    @Query("SELECT * FROM chat_room")
    fun getAllChatRooms(): LiveData<List<ChatRoomEntity>>

    @Query("SELECT * FROM chat_room WHERE roomId = :roomId")
    fun getChatRoomById(roomId: Long): LiveData<ChatRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(chatRoomEntity: ChatRoomEntity)

    @Delete
    suspend fun deleteChatRoom(chatRoomEntity: ChatRoomEntity)
}


@Database(entities = [ChatRoomEntity::class], version = 1)
abstract class ChatRoomDatabase : RoomDatabase() {
    abstract fun chatRoomDao(): ChatRoomDao

    companion object {
        private var instance: ChatRoomDatabase? = null

        fun getInstance(context: Context): ChatRoomDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    ChatRoomDatabase::class.java,
                    "chatRoom_database"
                ).build()
            }
    }
}