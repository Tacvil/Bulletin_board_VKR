package com.example.bulletin_board.domain.auth

import com.google.firebase.auth.FirebaseAuth

interface AuthProvider {
    val auth: FirebaseAuth
}
