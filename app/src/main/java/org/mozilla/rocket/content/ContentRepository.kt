package org.mozilla.rocket.content

import android.annotation.SuppressLint
import android.content.Context
import org.mozilla.lite.newspoint.RepositoryNewsPoint
import org.mozilla.lite.partner.NewsItem
import org.mozilla.lite.partner.Repository
import java.lang.IllegalStateException

class ContentRepository {
    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: Repository<out NewsItem>? = null

        @JvmStatic
        fun getInstance(
            context: Context?
        ): Repository<out NewsItem> = INSTANCE ?: synchronized(this) {
            if (context == null) {
                throw IllegalStateException("can't create Content Repository with null context")
            }
            INSTANCE ?: buildRepository(context.applicationContext).also { INSTANCE = it }
        }

        private fun buildRepository(context: Context): RepositoryNewsPoint =
            RepositoryNewsPoint(context)
    }
}