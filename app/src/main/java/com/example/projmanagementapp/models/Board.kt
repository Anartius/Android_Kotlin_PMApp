package com.example.projmanagementapp.models

import android.os.Parcel
import android.os.Parcelable

data class Board(
    var documentId: String = "",
    val name: String = "",
    val image: String = "",
    val createdBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    val taskList: ArrayList<Task> = ArrayList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.createTypedArrayList(Task.CREATOR)!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(documentId)
        writeString(name)
        writeString(image)
        writeString(createdBy)
        writeStringList(assignedTo)
        writeTypedList(taskList)
    }

    companion object CREATOR : Parcelable.Creator<Board> {
        override fun createFromParcel(parcel: Parcel): Board {
            return Board(parcel)
        }

        override fun newArray(size: Int): Array<Board?> {
            return arrayOfNulls(size)
        }
    }
}
