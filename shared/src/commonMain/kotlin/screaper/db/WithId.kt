package screaper.db


interface WithId {
    val id: String
}

//interface DocumentId {
//    val value: IdValue
//}
//
//interface WithId {
//    val id: DocumentId
//}
//
//@Serializable(with = IdSerializer::class)
//@JvmInline
//value class Id<T>(
//    override val value: IdValue,
//) : DocumentId {
//
//    override fun toString(): String {
//        return value.value.toString()
//    }
//
//    companion object {
//        fun <T> generate(): Id<T> = Id(IdValue(generateUuid()))
//
//        fun <T> fromUuid(uuid: Uuid): Id<T> = Id(IdValue(uuid))
//
//        fun <T> fromString(uuid: String): Id<T> = Id(IdValue(parseUuid(uuid)))
//    }
//}
//
//@Serializable
//@JvmInline
//value class IdValue(@Contextual val value: Uuid)
//
//object IdSerializer : KSerializer<Id<*>> {
//    override val descriptor = buildClassSerialDescriptor("misc.entities.Id")
//
//    override fun deserialize(decoder: Decoder): Id<*> {
//        return Id<Any>(IdValue.serializer().deserialize(decoder))
//    }
//
//    override fun serialize(encoder: Encoder, value: Id<*>) {
//        IdValue.serializer().serialize(encoder, value.value)
//    }
//
//}
