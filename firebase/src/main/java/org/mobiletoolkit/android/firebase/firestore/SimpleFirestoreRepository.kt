package org.mobiletoolkit.android.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import org.mobiletoolkit.android.repository.AsyncRepositoryCallback

/**
 * Created by Sebastian Owodzin on 10/12/2018.
 */
interface SimpleFirestoreRepository<Entity : FirestoreModel> : FirestoreRepository<Entity> {

    companion object {
        private const val TAG = "FirestoreRepository"
    }

    override fun exists(identifier: String, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "exists -> collectionPath: $collectionPath | _identifier: $identifier")
        }

        documentExists(identifier).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun get(identifier: String, callback: AsyncRepositoryCallback<Entity?>) {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath | _identifier: $identifier")
        }

        getDocument(identifier).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun create(entity: Entity, identifier: String?, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "create -> collectionPath: $collectionPath | entity: $entity | _identifier: $identifier")
        }

        createDocument(entity, identifier).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun create(vararg entities: Entity, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "create -> collectionPath: $collectionPath | entities: $entities")
        }

        createDocuments(entities.toList()).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun create(
        entities: List<Entity>,
        identifiers: List<String?>?,
        callback: AsyncRepositoryCallback<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "create -> collectionPath: $collectionPath | entities: $entities | identifiers: $identifiers")
        }

        createDocuments(entities, identifiers).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun update(entity: Entity, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "update -> collectionPath: $collectionPath | entity: $entity")
        }

        updateDocument(entity).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun update(vararg entities: Entity, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "update -> collectionPath: $collectionPath | entities: $entities")
        }

        updateDocuments(entities.toList()).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun delete(entity: Entity, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | entity: $entity")
        }

        deleteDocument(entity).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun delete(identifier: String, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | _identifier: $identifier")
        }

        deleteDocument(identifier).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun delete(vararg entities: Entity, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | entities: $entities")
        }

        deleteDocuments(entities.toList()).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun delete(vararg identifiers: String, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | identifiers: $identifiers")
        }

        deleteDocuments(identifiers = identifiers.toList()).addOnCompleteListener { callback(it.result, it.exception) }
    }

    override fun get(callback: AsyncRepositoryCallback<List<Entity>>) {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath")
        }

        getDocuments().addOnCompleteListener { callback(it.result, it.exception) }
    }

    fun get(
        identifier: String,
        callback: FirestoreRepositoryListener<Entity, Entity>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath | _identifier: $identifier")
        }

        collectionReference.document(identifier).addSnapshotListener { documentSnapshot, exception ->
            callback(
                documentSnapshot?.toObjectWithReference(entityClazz),
                null,
                exception
            )
        }
    }

    fun get(
        callback: FirestoreRepositoryListener<List<Entity>, Entity>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath")
        }

        collectionReference.addSnapshotListener { querySnapshot, exception ->
            callback(
                querySnapshot?.documents?.mapNotNull { it.toObjectWithReference(entityClazz) } ?: listOf(),
                querySnapshot?.documentChanges?.mapNotNull {
                    Change(
                        Change.Type.from(it.type),
                        it.oldIndex,
                        it.newIndex,
                        it.document.toObjectWithReference(entityClazz)
                    )
                }?.toSet(),
                exception
            )
        }
    }

    data class Change<T>(
        val type: Type,
        val oldIndex: Int = -1,
        val newIndex: Int,
        val data: T? = null
    ) {

        enum class Type {
            Added, Modified, Removed;

            companion object {
                fun from(type: DocumentChange.Type): Type {
                    return Type.values().first { it.ordinal == type.ordinal }
                }
            }
        }
    }
}

typealias FirestoreRepositoryListener<DataType, ChangeType> = (
    data: DataType?,
    changeSet: Set<SimpleFirestoreRepository.Change<ChangeType>>?,
    exception: Exception?
) -> Unit