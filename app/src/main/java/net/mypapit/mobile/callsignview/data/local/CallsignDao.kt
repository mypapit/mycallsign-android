package net.mypapit.mobile.callsignview.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

data class CallsignWithFavorite(
    @Embedded val item: CallsignEntity,
    @ColumnInfo(name = "isFavorite") val isFavorite: Boolean
)

data class CallsignStats(
    val total: Int = 0,
    val active: Int = 0,
    val expired: Int = 0,
    val expiringIn90Days: Int = 0,
    val expiredOver5Years: Int = 0,
    val count9M: Int = 0,
    val count9W2: Int = 0,
    val count9W3: Int = 0,
    val count9W4: Int = 0,
    val count9W6: Int = 0,
    val count9W8: Int = 0,
    val countOthers: Int = 0
)

enum class SearchMode {
    CALLSIGN,
    NAME,
    AA_NUMBER
}

enum class StatusFilter {
    ALL,
    ACTIVE,
    EXPIRED
}

object SearchQueryHelper {
    const val MIN_NAME_SEARCH_LENGTH = 3

    fun isNameQueryValid(rawQuery: String): Boolean {
        val trimmed = rawQuery.trim()
        return trimmed.isEmpty() || trimmed.replace("*", "").length >= MIN_NAME_SEARCH_LENGTH || trimmed.length >= MIN_NAME_SEARCH_LENGTH
    }

    fun isNameQueryTooShort(rawQuery: String): Boolean {
        val trimmed = rawQuery.trim()
        return trimmed.isNotEmpty() && trimmed.length < MIN_NAME_SEARCH_LENGTH
    }

    fun toNameWildcardSqlPattern(rawQuery: String): String {
        val cleaned = rawQuery.trim()
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
            .replace('*', '%')
        return if (cleaned.startsWith("%") || cleaned.endsWith("%")) {
            "%$cleaned%"
        } else {
            "%$cleaned%"
        }
    }

    fun toCallsignSqlPattern(rawQuery: String): String {
        val trimmed = rawQuery.trim().uppercase()
        return if (trimmed.startsWith("9")) {
            "$trimmed%"
        } else {
            "%$trimmed%"
        }
    }

    fun buildSearchQuery(
        mode: SearchMode,
        rawQuery: String,
        statusFilter: StatusFilter,
        prefixFilter: String,
        todayIso: String
    ): SupportSQLiteQuery {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any>()

        val trimmed = rawQuery.trim()
        if (trimmed.isNotEmpty()) {
            when (mode) {
                SearchMode.CALLSIGN -> {
                    whereClauses.add("c.callsign LIKE ? COLLATE NOCASE")
                    args.add(toCallsignSqlPattern(trimmed))
                }
                SearchMode.NAME -> {
                    if (trimmed.length >= MIN_NAME_SEARCH_LENGTH) {
                        whereClauses.add("c.holderName LIKE ? ESCAPE '\\' COLLATE NOCASE")
                        args.add(toNameWildcardSqlPattern(trimmed))
                    }
                }
                SearchMode.AA_NUMBER -> {
                    whereClauses.add("c.assignmentNo LIKE ? COLLATE NOCASE")
                    args.add("%$trimmed%")
                }
            }
        }

        when (statusFilter) {
            StatusFilter.ALL -> Unit
            StatusFilter.ACTIVE -> {
                whereClauses.add("c.expiryDateIso >= ?")
                args.add(todayIso)
            }
            StatusFilter.EXPIRED -> {
                whereClauses.add("c.expiryDateIso < ?")
                args.add(todayIso)
            }
        }

        if (prefixFilter != "ALL") {
            whereClauses.add("c.prefixGroup = ?")
            args.add(prefixFilter)
        }

        val whereSql = if (whereClauses.isEmpty()) "" else "WHERE " + whereClauses.joinToString(" AND ")
        val orderSql = when {
            mode == SearchMode.CALLSIGN && trimmed.isNotEmpty() ->
                "ORDER BY CASE WHEN c.callsign LIKE ? COLLATE NOCASE THEN 0 ELSE 1 END, c.callsign ASC"
            else -> "ORDER BY c.holderName ASC, c.callsign ASC"
        }
        if (mode == SearchMode.CALLSIGN && trimmed.isNotEmpty()) {
            args.add("${trimmed.uppercase()}%")
        }

        val sql = """
            SELECT c.*, (f.callsign IS NOT NULL) AS isFavorite
            FROM callsigns c
            LEFT JOIN favorites f ON c.callsign = f.callsign
            $whereSql
            $orderSql
        """.trimIndent()

        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }
}

@Dao
interface CallsignDao {

    @RawQuery(observedEntities = [CallsignEntity::class, FavoriteEntity::class])
    fun observeFiltered(query: SupportSQLiteQuery): Flow<List<CallsignWithFavorite>>

    @Query(
        """
        SELECT c.*, (f.callsign IS NOT NULL) AS isFavorite
        FROM callsigns c
        LEFT JOIN favorites f ON c.callsign = f.callsign
        WHERE c.callsign = :callsign
        LIMIT 1
        """
    )
    fun observeByCallsign(callsign: String): Flow<CallsignWithFavorite?>

    @Query(
        """
        SELECT c.*, 1 AS isFavorite
        FROM callsigns c
        INNER JOIN favorites f ON c.callsign = f.callsign
        ORDER BY c.callsign ASC
        """
    )
    fun observeFavorites(): Flow<List<CallsignWithFavorite>>

    @Query(
        """
        SELECT c.*, (f.callsign IS NOT NULL) AS isFavorite
        FROM callsigns c
        LEFT JOIN favorites f ON c.callsign = f.callsign
        WHERE c.expiryDateIso != '' AND c.expiryDateIso < :fiveYearsAgoIso
        ORDER BY c.expiryDateIso ASC, c.callsign ASC
        """
    )
    fun observeExpiredOver5Years(fiveYearsAgoIso: String): Flow<List<CallsignWithFavorite>>

    @Query(
        """
        SELECT
            COUNT(*) AS total,
            COALESCE(SUM(CASE WHEN expiryDateIso >= :todayIso THEN 1 ELSE 0 END), 0) AS active,
            COALESCE(SUM(CASE WHEN expiryDateIso != '' AND expiryDateIso < :todayIso THEN 1 ELSE 0 END), 0) AS expired,
            COALESCE(SUM(CASE WHEN expiryDateIso >= :todayIso AND expiryDateIso <= :in90DaysIso THEN 1 ELSE 0 END), 0) AS expiringIn90Days,
            COALESCE(SUM(CASE WHEN expiryDateIso != '' AND expiryDateIso < :fiveYearsAgoIso THEN 1 ELSE 0 END), 0) AS expiredOver5Years,
            COALESCE(SUM(CASE WHEN prefixGroup IN ('9M2', '9M4', '9M6', '9M8') THEN 1 ELSE 0 END), 0) AS count9M,
            COALESCE(SUM(CASE WHEN prefixGroup = '9W2' THEN 1 ELSE 0 END), 0) AS count9W2,
            COALESCE(SUM(CASE WHEN prefixGroup = '9W3' THEN 1 ELSE 0 END), 0) AS count9W3,
            COALESCE(SUM(CASE WHEN prefixGroup = '9W4' THEN 1 ELSE 0 END), 0) AS count9W4,
            COALESCE(SUM(CASE WHEN prefixGroup = '9W6' THEN 1 ELSE 0 END), 0) AS count9W6,
            COALESCE(SUM(CASE WHEN prefixGroup = '9W8' THEN 1 ELSE 0 END), 0) AS count9W8,
            COALESCE(SUM(CASE WHEN prefixGroup = 'OTHER' THEN 1 ELSE 0 END), 0) AS countOthers
        FROM callsigns
        """
    )
    fun observeStats(
        todayIso: String,
        in90DaysIso: String,
        fiveYearsAgoIso: String
    ): Flow<CallsignStats>

    @Query("SELECT COUNT(*) FROM callsigns")
    suspend fun count(): Int

    @Query("DELETE FROM callsigns")
    suspend fun deleteAllCallsigns()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CallsignEntity>)

    @Transaction
    suspend fun replaceAllCallsigns(items: List<CallsignEntity>) {
        deleteAllCallsigns()
        items.chunked(500).forEach { chunk ->
            insertAll(chunk)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE callsign = :callsign")
    suspend fun removeFavorite(callsign: String)
}
