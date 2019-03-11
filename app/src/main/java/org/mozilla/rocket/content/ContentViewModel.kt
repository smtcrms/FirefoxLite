package org.mozilla.rocket.content

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.mozilla.lite.partner.NewsItem
import org.mozilla.lite.partner.Repository

class ContentViewModel : ViewModel(), Repository.OnDataChangedListener<NewsItem> {
    var repository: Repository<out NewsItem>? = null
    val items = MutableLiveData<List<NewsItem>>()

    // the library use LiveData as callback to onDataChanged. So here will always on main thread
    override fun onDataChanged(itemPojoList: MutableList<NewsItem>?) {
        val newList = ArrayList<NewsItem>()
        // exclude existing items from itemPojoList
        items.value?.let {
            itemPojoList?.removeAll(it)
            // there are new items, add the old item to new list first
            newList.addAll(it)
        }
        // add the new items in the new list
        itemPojoList?.let {
            newList.addAll(it)
        }

        // return the new list , so diff utils will think this is something to diff
        items.value = newList
    }

    fun loadMore() {
        repository?.loadMore()
        // now wait for  OnDataChangedListener.onDataChanged to return the result
    }
}