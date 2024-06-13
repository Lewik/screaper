package org.screaper.db

import com.mongodb.kotlin.client.coroutine.MongoClient
import screaper.entities.ScreaperLog

class ScreaperLogDataRepository(
    mongoClient: MongoClient,
) : DataRepositoryMongoImpl<ScreaperLog>(
    mongoClient,
    ScreaperLog::class
)