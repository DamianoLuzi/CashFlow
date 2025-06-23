package com.example.exptrackpm.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.example.exptrackpm.data.users.UserRepository
import com.example.exptrackpm.domain.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class ProfileViewModel : ViewModel() {
    private val auth = Firebase.auth
    val userId = auth.currentUser?.uid
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadUser() {
        _loading.value = true
        UserRepository.getUser() { fetchedUser ->
            _user.value = fetchedUser
            _loading.value = false
        }
    }

    fun updateUser(updatedUser: User) {
        _loading.value = true
        UserRepository.updateUser(updatedUser) { success ->
            _loading.value = false
            if (success) {
                _user.value = updatedUser
            }
            // else handle failure (show error etc)
        }
    }
}


//class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
//
//    private val _user = MutableStateFlow<User?>(null)
//    val user: StateFlow<User?> = _user
//
//    private val _loading = MutableStateFlow(false)
//    val loading: StateFlow<Boolean> = _loading
//
//    fun loadUser(userId: String) {
//        _loading.value = true
//        userRepository.getUser(userId) { fetchedUser ->
//            _user.value = fetchedUser
//            _loading.value = false
//        }
//    }
//
//    fun updateUser(updatedUser: User) {
//        _loading.value = true
//        userRepository.updateUser(updatedUser) { success ->
//            _loading.value = false
//            if (success) {
//                _user.value = updatedUser
//            }
//            // else handle failure (show error etc)
//        }
//    }
//}
