package com.gitlab.j_m_hoffmann.meditate.db

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    @PrimaryKey val date: Long,
    @NonNull val duration: Long
) {}