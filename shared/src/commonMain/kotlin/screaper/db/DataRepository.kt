package screaper.db

interface DataRepository<T : WithId> {
    suspend fun delete(id: String)
    suspend fun deleteAll()
    suspend fun find(id: String): T?
    suspend fun findAll(): List<T>
    suspend fun upsert(entity: T)
    suspend fun upsert(entities: Iterable<T>)
    suspend fun insert(entities: Iterable<T>)
    suspend fun count(): Long
}
