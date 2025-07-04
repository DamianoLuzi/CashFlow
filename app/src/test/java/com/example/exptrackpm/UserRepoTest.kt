package com.example.exptrackpm

import com.example.exptrackpm.data.users.UserRepository
import com.example.exptrackpm.domain.model.NotificationPreferences
import com.example.exptrackpm.domain.model.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserRepoTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var repo: UserRepository

    private lateinit var firestoreMockStatic: MockedStatic<FirebaseFirestore>
    private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>
    private lateinit var firebaseAppMockStatic: MockedStatic<FirebaseApp>

    @Before
    fun setup() {
        firestore = mock(FirebaseFirestore::class.java)
        firestoreMockStatic = mockStatic(FirebaseFirestore::class.java)
        firestoreMockStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(firestore)
        auth = mock(FirebaseAuth::class.java)
        firebaseAuthMockStatic = mockStatic(FirebaseAuth::class.java)
        firebaseAuthMockStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(auth)
        firebaseAppMockStatic = mockStatic(FirebaseApp::class.java)
        firebaseAppMockStatic.`when`<FirebaseApp> { FirebaseApp.getInstance() }.thenReturn(mock(FirebaseApp::class.java))
        firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("mockUid")
        `when`(auth.currentUser).thenReturn(firebaseUser)
    }

    @After
    fun tearDown() {
        firestoreMockStatic.close()
        firebaseAuthMockStatic.close()
        firebaseAppMockStatic.close()
    }

    @Test
    fun `getUser returns User object`() {
        val usersCollection = mock(CollectionReference::class.java)
        val documentRef = mock(DocumentReference::class.java)
        val task = mock(Task::class.java) as Task<DocumentSnapshot>
        val docSnapshot = mock(DocumentSnapshot::class.java)

        `when`(firestore.collection("users")).thenReturn(usersCollection)
        `when`(usersCollection.document("mockUid")).thenReturn(documentRef)
        `when`(documentRef.get()).thenReturn(task)
        val dummyUser = User(
            id = "mockUid",
            email = "john@example.com",
            notificationPreferences = NotificationPreferences()
        )

        `when`(docSnapshot.toObject(User::class.java)).thenReturn(dummyUser)
        `when`(docSnapshot.id).thenReturn("mockUid")
        `when`(docSnapshot.exists()).thenReturn(true)

        doAnswer {
            val listener = it.getArgument<OnSuccessListener<DocumentSnapshot>>(0)
            listener.onSuccess(docSnapshot)
            task
        }.`when`(task).addOnSuccessListener(any())

        repo = UserRepository
        var result: User? = null

        repo.getUser {
            result = it
        }

        assertNotNull(result)
        assertEquals("mockUid", result!!.id)
        assertEquals("john@example.com", result!!.email)
    }
}
