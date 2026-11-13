package com.example.painlessprep.data

import androidx.room.withTransaction

class DatabaseRepository(private val db: AppDatabase) {
    private val dao = db.houseDao()

    //Sample data to populate the database with
    suspend fun sampleData() = db.withTransaction {
        dao.deleteAllHouses()
        dao.resetIdCounters()
        val samples = listOf(
            Triple("House 1", "123 A Street", listOf(
                "Room 1" to listOf(Triple("Window 1", 1.0, 1.0), Triple("Window 1.1", 1.0, 1.0)),
                "Room 2" to listOf(Triple("Window 2", 1.0, 1.0))
            )),
            Triple("House 2", "456 B Street", listOf(
                "Room 3" to listOf(Triple("Window 1.2", 2.0, 2.0), Triple("Window 2.2", 2.0, 2.0)),
                "Room 4" to listOf(Triple("Window 3", 2.0, 2.0))
            ))
        )
    for ((houseName, houseAddress, rooms) in samples){
        val houseId = dao.insertHouse(House(name = houseName, address = houseAddress)).toInt()
        for ((roomName, windows) in rooms){
            val roomId = dao.insertRoom(RoomEntity(houseId = houseId, roomName = roomName, numberOfWindows = 0)).toInt()
            for ((windowName, width, height) in windows){
                dao.insertWindow(Window(roomId = roomId, windowName = windowName, width = width, height = height))
            }
            dao.setWindowCount(roomId,dao.countWindows(roomId))
        }
    }

    }
    //End of sample data portion

    //Read operations below

    //Get all houses with rooms information
    val housesWithRooms = dao.getAllHousesWithRooms()

    //Get a specific house with rooms information
    fun house(id: Int) = dao.getHouseWithRooms(id)

    //Get a specific room with windows information
    fun room(id: Int) = dao.getRoomWithWindows(id)

    fun window(roomId: Int) = dao.getWindows(roomId)


    //Modifying database operations below

    //House Operations
    suspend fun addHouse(name: String, address: String) =
        dao.insertHouse(House(name = name, address = address))

    suspend fun updateHouse(house: House) = dao.updateHouse(house)
    suspend fun deleteHouse(house: House) = dao.deleteHouse(house)

    //Room Operations
    suspend fun addRoom(houseId: Int, roomName: String) =
        dao.insertRoom(RoomEntity(houseId = houseId, roomName = roomName, numberOfWindows = 0))
    suspend fun updateRoom(room: RoomEntity) = dao.updateRoom(room)
    suspend fun deleteRoom(room: RoomEntity) = dao.deleteRoom(room)

    //Window operations

    suspend fun addWindow(roomId: Int, windowName: String, width: Double, height: Double) =
        db.withTransaction {
            dao.insertWindow(Window(roomId = roomId, windowName = windowName, width = width, height = height))
            dao.setWindowCount(roomId,dao.countWindows(roomId))
        }
    suspend fun updateWindow(window: Window) = dao.updateWindow(window)
    suspend fun deleteWindow(window: Window) =
        db.withTransaction {
            dao.deleteWindow(window)
            dao.setWindowCount(window.roomId,dao.countWindows(window.roomId))
        }

}