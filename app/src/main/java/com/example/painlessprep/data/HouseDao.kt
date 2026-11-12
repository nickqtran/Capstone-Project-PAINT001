package com.example.painlessprep.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseDao {
    //House operations
    @Insert suspend fun insertHouse(house: House): Long
    @Update suspend fun updateHouse(house: House)
    @Delete suspend fun deleteHouse(house: House)

    @Query("SELECT * FROM houses") fun getAllHouses(): Flow<List<House>>

    @Transaction
    @Query("Select * FROM houses")
    fun getAllHousesWithRooms(): Flow<List<HouseWithRooms>>

    @Transaction
    @Query("SELECT * FROM houses WHERE house_id = :houseId")
    fun getHouseWithRooms(houseId: Int): Flow<HouseWithRooms>

    //Room operations
    @Insert suspend fun insertRoom(room: RoomEntity): Long
    @Update suspend fun updateRoom(room: RoomEntity)
    @Delete suspend fun deleteRoom(room: RoomEntity)

    @Transaction
    @Query("SELECT * FROM rooms WHERE room_id = :roomId")
    fun getRoomWithWindows(roomId: Int): Flow<RoomWithWindows?>

    @Query("UPDATE rooms SET number_of_windows = :count WHERE room_id = :roomId")
    suspend fun setWindowCount(roomId: Int, count: Int)

    //Window operations
    @Insert suspend fun insertWindow(window: Window): Long
    @Update suspend fun updateWindow(window: Window)
    @Delete suspend fun deleteWindow(window: Window)

    @Query("SELECT * FROM windows WHERE room_id = :roomId")
    fun getWindows(roomId: Int): Flow<List<Window>>

    @Query("SELECT COUNT(*) FROM windows WHERE room_id = :roomId")
    suspend fun countWindows(roomId: Int): Int

    //Remove query later, only for testing purposes
    @Query("DELETE FROM houses")
    suspend fun deleteAllHouses()

    @Query("DELETE FROM sqlite_sequence WHERE name IN ('houses', 'rooms', 'windows')")
    suspend fun resetIdCounters()
}
