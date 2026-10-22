package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.data.model.DoseLog
import com.example.data.model.DoseStatus
import com.example.data.model.InteractionResult
import com.example.data.model.InteractionSeverity
import com.example.data.model.Medicine
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val genericName: String,
    val dosage: String,
    val form: String,
    val frequency: String,
    val scheduledTimesJson: String,
    val startDate: Long,
    val endDate: Long?,
    val mealTiming: String,
    val stockCount: Int,
    val lowStockThreshold: Int,
    val notes: String,
    val colorHex: Long,
    val folder: String = "Daily Essentials",
    val active: Boolean
) {
    fun toModel(): Medicine = Medicine(
        id = id,
        name = name,
        genericName = genericName,
        dosage = dosage,
        form = form,
        frequency = frequency,
        scheduledTimes = RoomConverters.parseStringToList(scheduledTimesJson),
        startDate = startDate,
        endDate = endDate,
        mealTiming = mealTiming,
        stockCount = stockCount,
        lowStockThreshold = lowStockThreshold,
        notes = notes,
        colorHex = colorHex,
        folder = folder,
        active = active
    )

    companion object {
        fun fromModel(m: Medicine): MedicineEntity = MedicineEntity(
            id = m.id,
            name = m.name,
            genericName = m.genericName,
            dosage = m.dosage,
            form = m.form,
            frequency = m.frequency,
            scheduledTimesJson = RoomConverters.parseListToString(m.scheduledTimes),
            startDate = m.startDate,
            endDate = m.endDate,
            mealTiming = m.mealTiming,
            stockCount = m.stockCount,
            lowStockThreshold = m.lowStockThreshold,
            notes = m.notes,
            colorHex = m.colorHex,
            folder = m.folder,
            active = m.active
        )
    }
}

@Entity(tableName = "dose_logs")
data class DoseLogEntity(
    @PrimaryKey val id: String,
    val medId: String,
    val medName: String,
    val dosage: String,
    val scheduledTime: String,
    val scheduledDate: String,
    val status: String,
    val actualTime: Long?,
    val notes: String?,
    val folder: String = "Daily Essentials"
) {
    fun toModel(): DoseLog = DoseLog(
        id = id,
        medId = medId,
        medName = medName,
        dosage = dosage,
        scheduledTime = scheduledTime,
        scheduledDate = scheduledDate,
        status = runCatching { DoseStatus.valueOf(status) }.getOrDefault(DoseStatus.DUE),
        actualTime = actualTime,
        notes = notes,
        folder = folder
    )

    companion object {
        fun fromModel(d: DoseLog): DoseLogEntity = DoseLogEntity(
            id = d.id,
            medId = d.medId,
            medName = d.medName,
            dosage = d.dosage,
            scheduledTime = d.scheduledTime,
            scheduledDate = d.scheduledDate,
            status = d.status.name,
            actualTime = d.actualTime,
            notes = d.notes,
            folder = d.folder
        )
    }
}

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "primary_user",
    val name: String,
    val age: Int,
    val allergiesJson: String,
    val conditionsJson: String,
    val caregiverName: String,
    val caregiverPhone: String,
    val caregiverInviteCode: String,
    val caregiverLinked: Boolean,
    val timeZone: String,
    val largeTextEnabled: Boolean,
    val language: String,
    val onboardingCompleted: Boolean
) {
    fun toModel(): UserProfile = UserProfile(
        id = id,
        name = name,
        age = age,
        allergies = RoomConverters.parseStringToList(allergiesJson),
        conditions = RoomConverters.parseStringToList(conditionsJson),
        caregiverName = caregiverName,
        caregiverPhone = caregiverPhone,
        caregiverInviteCode = caregiverInviteCode,
        caregiverLinked = caregiverLinked,
        timeZone = timeZone,
        largeTextEnabled = largeTextEnabled,
        language = language,
        onboardingCompleted = onboardingCompleted
    )

    companion object {
        fun fromModel(p: UserProfile): UserProfileEntity = UserProfileEntity(
            id = p.id,
            name = p.name,
            age = p.age,
            allergiesJson = RoomConverters.parseListToString(p.allergies),
            conditionsJson = RoomConverters.parseListToString(p.conditions),
            caregiverName = p.caregiverName,
            caregiverPhone = p.caregiverPhone,
            caregiverInviteCode = p.caregiverInviteCode,
            caregiverLinked = p.caregiverLinked,
            timeZone = p.timeZone,
            largeTextEnabled = p.largeTextEnabled,
            language = p.language,
            onboardingCompleted = p.onboardingCompleted
        )
    }
}

class RoomConverters {
    @TypeConverter
    fun stringToList(value: String): List<String> {
        return parseStringToList(value)
    }

    @TypeConverter
    fun listToString(list: List<String>): String {
        return parseListToString(list)
    }

    companion object {
        fun parseStringToList(value: String): List<String> {
            if (value.isBlank()) return emptyList()
            val list = mutableListOf<String>()
            try {
                val array = JSONArray(value)
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
            } catch (e: Exception) {
                return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
            return list
        }

        fun parseListToString(list: List<String>): String {
            val array = JSONArray()
            list.forEach { array.put(it) }
            return array.toString()
        }
    }
}

@Dao
interface MediMindDao {
    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(med: MedicineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(meds: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(med: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicineById(id: String)

    @Query("SELECT * FROM dose_logs WHERE scheduledDate = :date ORDER BY scheduledTime ASC")
    fun getDoseLogsByDate(date: String): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs ORDER BY scheduledDate DESC, scheduledTime DESC LIMIT 100")
    fun getRecentDoseLogs(): Flow<List<DoseLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(log: DoseLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLogs(logs: List<DoseLogEntity>)

    @Update
    suspend fun updateDoseLog(log: DoseLogEntity)

    @Query("SELECT * FROM user_profile WHERE id = 'primary_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)
}

@Database(
    entities = [
        MedicineEntity::class,
        DoseLogEntity::class,
        UserProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class MediMindDatabase : RoomDatabase() {
    abstract fun dao(): MediMindDao
}
