package org.screaper.db

import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import screaper.db.DataRepository
import screaper.db.WithId
import kotlin.reflect.KClass


abstract class DataRepositoryMongoImpl<T : WithId>(
    client: MongoClient,
    clazz: KClass<T>,
    dbName: String = "screaperlog",
) : DataRepository<T> {
    protected val database = client.getDatabase(dbName)
    protected val collectionName: String = clazz.simpleName!!
    protected val collection = database.getCollection(collectionName, clazz.java)
    protected val findOneAndReplaceOptionsUpsert = FindOneAndReplaceOptions().upsert(true)

    override suspend fun delete(id: String) {
        collection.deleteOne(Filters.eq("id", id))
    }

    override suspend fun deleteAll() {
        collection.deleteMany(Filters.empty())
    }

    override suspend fun find(id: String) = collection.find(Filters.eq("id", id)).singleOrNull()

    override suspend fun findAll() = collection.find().toList()

    override suspend fun upsert(entity: T) {
        collection.findOneAndReplace(Filters.eq("id", entity.id), entity, findOneAndReplaceOptionsUpsert)
    }

    override suspend fun upsert(entities: Iterable<T>) = entities.forEach {
        upsert(it)
    }

    override suspend fun insert(entities: Iterable<T>) {
        collection.insertMany(entities.toList())
    }

    override suspend fun count() = collection.countDocuments()
}
