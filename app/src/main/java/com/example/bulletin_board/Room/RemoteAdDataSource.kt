package com.example.bulletin_board.Room

import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

@Singleton
class RemoteAdDataSource
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
    ) : AdDataSource {
        override fun getAllAds(): Flow<List<Ad>> =
            flow {
                val ads =
                    firestore
                        .collection("ads") // Замените "ads" на имя вашей коллекции
                        .get()
                        .await()
                        .toObjects(Ad::class.java)
                emit(ads)
            }

        override fun getAdById(id: String): Flow<Ad> =
            flow {
                val ad =
                    firestore
                        .collection("ads")
                        .document(id)
                        .get()
                        .await()
                        .toObject(Ad::class.java)
                emit(ad!!) //  Предполагается, что объявление с таким id существует
            }

        override suspend fun insertAd(ad: Ad) {
            firestore
                .collection("ads")
                .add(ad)
                .await()
        }

        override suspend fun updateAd(ad: Ad) {
            firestore
                .collection("ads")
                .document(ad.key!!) //  Предполагается, что у объявления есть key
                .set(ad)
                .await()
        }

        override suspend fun deleteAd(ad: Ad) {
            firestore
                .collection("ads")
                .document(ad.key!!)
                .delete()
                .await()
        }
    }
