package com.example.painlessprep

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.painlessprep.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class HouseDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var houseDao: HouseDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        houseDao = db.houseDao()
    }

    @After
    fun tearDown() = db.close()

    //Creates a house and tests if it can be read back
    @Test
    fun insertedHouseCanBeReadBack() = runTest {
        houseDao.insertHouse(House(name = "Test House", address = "123 Test St"))
        val houses = houseDao.getAllHouses().first()
        assertEquals(1, houses.size)
        assertEquals("Test House", houses.single().name)
    }

    //Creates a house and loads the room and window information
    @Test
    fun houseLoadsItsRoomsAndWindows() = runTest {
        val h = houseDao.insertHouse(House(name = "Test House", address = "123 Test St")).toInt()
        val r = houseDao.insertRoom(
            RoomEntity(
                houseId = h,
                roomName = "Test Room",
                numberOfWindows = 0
            )
        ).toInt()
        houseDao.insertWindow(
            Window(
                roomId = r,
                windowName = "Test Window",
                width = 1.0,
                height = 1.0
            )
        )

        val loaded = houseDao.getHouseWithRooms(h).first()

        assertEquals("Test Room", loaded.rooms.single().room.roomName)
        assertEquals("Test Window", loaded.rooms.single().windows.single().windowName)
    }

    //Tests if deleting works
    @Test
    fun deletingHouseRemovesRoomsAndWindows() = runTest {
        val h = houseDao.insertHouse(House(name = "Test House", address = "123 Test St")).toInt()
        val r = houseDao.insertRoom(
            RoomEntity(
                houseId = h,
                roomName = "Test Room",
                numberOfWindows = 0
            )
        ).toInt()
        houseDao.insertWindow(
            Window(
                roomId = r,
                windowName = "Test Window",
                width = 1.0,
                height = 1.0
            )
        )

        houseDao.deleteHouse(houseDao.getHouseWithRooms(h).first().house)

        assertEquals(0, houseDao.countWindows(r))
        assertNull(houseDao.getRoomWithWindows(r).first())
    }

    @Test fun windowReadBackIsCorrect() = runTest {
        val h = houseDao.insertHouse(House(name = "Test House", address = "123 Test St")).toInt()
        val r1 = houseDao.insertRoom(
            RoomEntity(
                houseId = h,
                roomName = "Test Room",
                numberOfWindows = 0
            )
        ).toInt()
        val r2 = houseDao.insertRoom(
            RoomEntity(
                houseId = h,
                roomName = "Test Room 2",
                numberOfWindows = 0
            )
        ).toInt()
        houseDao.insertWindow(
            Window(
                roomId = r1,
                windowName = "Test Window 1",
                width = 1.0,
                height = 1.0
            )
        )
        houseDao.insertWindow(
            Window(
                roomId = r2,
                windowName = "Test Window 2",
                width = 1.0,
                height = 1.0
            )
        )

        val window1 = houseDao.getWindows(r1).first()
        val window2 = houseDao.getWindows(r2).first()
        assertEquals(1, window1.size)
        assertEquals("Test Window 1", window1.single().windowName)
        assertEquals(1, window2.size)
        assertEquals("Test Window 2", window2.single().windowName)
    }

    //Tests if user can create a room without a house (should raise an exception error)
    @Test(expected = SQLiteConstraintException::class)
    fun roomWithoutAHouseIsRejected() = runTest {
        houseDao.insertRoom(RoomEntity(houseId = 999, roomName = "Test Room", numberOfWindows = 0))
    }

    //Update tests go here

    //Helper function to create a house, room, and window for updates
    private suspend fun helper(): Triple<Int, Int, Int> {
        val h = houseDao.insertHouse(House(name = "Test House", address = "123 Test St")).toInt()
        val r = houseDao.insertRoom(RoomEntity(houseId = h, roomName = "Test Room", numberOfWindows = 0)).toInt()
        val w = houseDao.insertWindow(Window(roomId = r, windowName = "Test Window", width = 1.0, height = 1.0)).toInt()
        return Triple(h,r,w)
    }

    @Test fun updateHouseChangesNameAndAddress() = runTest {
        val (h, _, _) = helper()
        houseDao.updateHouse(House(id = h, name = "New Name", address = "New Address"))
        val houses = houseDao.getAllHouses().first()
        assertEquals("New Name", houses.single().name)
        assertEquals("New Address", houses.single().address)
    }
    @Test fun updateRoomName() = runTest{
        val (h,_,_) = helper()
        val room = houseDao.getHouseWithRooms(h).first().rooms.single()
        houseDao.updateRoom(RoomEntity(id = room.room.id, houseId = h, roomName = "New Room Name", numberOfWindows = 0))
        assertEquals("New Room Name", houseDao.getHouseWithRooms(h).first().rooms.single().room.roomName)

    }

    @Test fun updateWindowName() = runTest {
        val (_,r,w) = helper()
        houseDao.updateWindow(Window(id = w, roomId = r, windowName = "New Window Name", width = 1.0, height = 1.0))
        val windows = houseDao.getRoomWithWindows(r).first()
        assertEquals("New Window Name", windows?.windows?.single()?.windowName)
    }

    @Test fun setWindowCount() = runTest {
        val (h,r,_) = helper()
        houseDao.setWindowCount(r, 5)
        val room = houseDao.getHouseWithRooms(h).first().rooms.single()
        assertEquals(5, room.room.numberOfWindows)
    }
}
