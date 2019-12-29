package com.gitlab.j_m_hoffmann.meditate

import android.app.Application
import com.gitlab.j_m_hoffmann.meditate.db.Db
import com.gitlab.j_m_hoffmann.meditate.db.getDatabase

class MeditateApplication : Application() {

    val database: Db
        get() = getDatabase(this)
}