package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource (var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders?.let {return Result.Success(ArrayList(it))}
        throw Exception("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return Result.Success(reminders?.filter {
            it.id == id
        }?.first() as ReminderDTO)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}