package com.example.painlessprep.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.painlessprep.data.*
import kotlinx.coroutines.launch


class DatabaseViewModel (application: Application) : AndroidViewModel(application) {
    private val DatabaseRepo = DatabaseRepository(AppDatabase.get(application))

    //Delete this init block when done with sample data
    init {
        viewModelScope.launch { DatabaseRepo.sampleData() }
    }

    //Read operations
    val housesWithRooms = DatabaseRepo.housesWithRooms
    fun house(id: Int) = DatabaseRepo.house(id)
    fun room(id: Int) = DatabaseRepo.room(id)
    fun window(roomId: Int) = DatabaseRepo.window(roomId)

    //House operations
    fun addHouse(name: String, address: String) = viewModelScope.launch {
        DatabaseRepo.addHouse(name, address)
    }
    fun updateHouse(house: House) = viewModelScope.launch {
        DatabaseRepo.updateHouse(house)
    }
    fun deleteHouse(house: House) = viewModelScope.launch {
        DatabaseRepo.deleteHouse(house)
    }

    //Room Operations
    fun addRoom(houseId: Int, roomName: String) = viewModelScope.launch {
        DatabaseRepo.addRoom(houseId, roomName)
    }
    fun updateRoom(room: RoomEntity) = viewModelScope.launch {
        DatabaseRepo.updateRoom(room)
    }
    fun deleteRoom(room: RoomEntity) = viewModelScope.launch {
        DatabaseRepo.deleteRoom(room)
    }
    //Window operations
    fun addWindow(roomId: Int, windowName: String, width: Double, height: Double) = viewModelScope.launch {
        DatabaseRepo.addWindow(roomId, windowName, width, height)
    }

    fun updateWindow(window: Window) = viewModelScope.launch {
        DatabaseRepo.updateWindow(window)
    }

    fun deleteWindow(window: Window) = viewModelScope.launch {
        DatabaseRepo.deleteWindow(window)
    }
}