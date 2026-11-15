package com.example.painlessprep.data

import androidx.room.*

@Entity(tableName = "houses")
data class House(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "house_id") val id: Int = 0,
    val name: String,
    val address: String
)

@Entity(
    tableName = "rooms",
    foreignKeys = [ForeignKey(
        entity = House::class,
        parentColumns = ["house_id"],
        childColumns = ["house_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("house_id")]
)
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "room_id") val id: Int = 0,
    @ColumnInfo(name = "house_id") val houseId: Int,
    @ColumnInfo(name = "room_name") val roomName: String,
    @ColumnInfo(name = "number_of_windows") val numberOfWindows: Int
)

@Entity(
    tableName = "windows",
    foreignKeys = [ForeignKey(
    entity = RoomEntity::class,
    parentColumns = ["room_id"],
    childColumns = ["room_id"],
    onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("room_id")]
)
data class Window(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "window_id") val id: Int = 0,
    @ColumnInfo(name = "room_id") val roomId: Int,
    @ColumnInfo(name = "window_name") val windowName: String,
    val width: Double,
    val height: Double
)