package com.example.painlessprep.data

import androidx.room.Embedded
import androidx.room.Relation

data class RoomWithWindows(
    @Embedded val room: RoomEntity,
    @Relation(parentColumn = "room_id", entityColumn = "room_id")
    val windows: List<Window>
)

data class HouseWithRooms(
    @Embedded val house: House,
    @Relation(parentColumn = "house_id", entityColumn = "house_id", entity = RoomEntity::class)
    val rooms: List<RoomWithWindows>
)