package org.mozilla.rocket.content

import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import org.mozilla.focus.R
import org.mozilla.focus.fragment.PanelFragment
import org.mozilla.focus.fragment.PanelFragmentStatusListener
import org.mozilla.lite.partner.NewsItem

class ContentAdapter<T : NewsItem>(private val listener: ContentPanelListener) : RecyclerView.Adapter<NewsViewHolder<T>>() {
    private lateinit var items: List<T>

//    init {
//        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                super.onItemRangeInserted(positionStart, itemCount)
//                if (itemCount == 0) {
//                    listener.onStatus(PanelFragment.VIEW_TYPE_EMPTY)
//                } else {
//                    listener.onStatus(PanelFragment.VIEW_TYPE_NON_EMPTY)
//                }
//            }
//        })
//    }
//
//    object COMPARATOR : DiffUtil.ItemCallback<BhaskarItem>() {
//
//        override fun areItemsTheSame(oldItem: BhaskarItem, newItem: BhaskarItem): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: BhaskarItem, newItem: BhaskarItem): Boolean {
//            return oldItem == newItem
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder<T> {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(v)
    }

    override fun onBindViewHolder(holder: NewsViewHolder<T>, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item, View.OnClickListener { listener.onItemClicked(item.newsUrl) })
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun getItem(index: Int): T? {
        return if (index >= 0 && items.size > index) {
            items[index]
        } else {
            null
        }
    }

    fun setData(data: List<T>) {
//        val startPos = items.size
//        val count = data.size - items.size
        this.items = data
        if (itemCount == 0) {
            listener.onStatus(PanelFragment.VIEW_TYPE_EMPTY)
        } else {
            listener.onStatus(PanelFragment.VIEW_TYPE_NON_EMPTY)
        }
        notifyDataSetChanged()
    }

    interface ContentPanelListener : PanelFragmentStatusListener {
        fun onItemClicked(url: String)

        fun tryLoadMore()
    }
}

class NewsViewHolder<T : NewsItem>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        var requestOptions = RequestOptions().apply { transforms(CenterCrop(), RoundedCorners(16)) }
    }

    fun bind(item: T, listener: View.OnClickListener) {
        itemView.findViewById<View>(R.id.news_item).setOnClickListener(listener)
        itemView.findViewById<TextView>(R.id.news_item_headline).text = item.title
        itemView.findViewById<TextView>(R.id.news_item_source).text = "" // don't show for now
        itemView.findViewById<TextView>(R.id.news_item_time).text =
            DateUtils.getRelativeTimeSpanString(
                item.time, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS
            )
        Glide.with(itemView.context)
                .load(item.imageUrl)
                .apply(requestOptions)
                .into(itemView.findViewById(R.id.news_item_image))
    }
}