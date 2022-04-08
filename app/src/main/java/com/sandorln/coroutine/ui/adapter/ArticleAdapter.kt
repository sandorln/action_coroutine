package com.sandorln.coroutine.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sandorln.coroutine.databinding.ItemArticleBinding
import com.sandorln.coroutine.model.Article

class ArticleAdapter(
    var articles: List<Article> = mutableListOf()
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder =
        ArticleViewHolder(ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        try {
            val article = articles[position]
            with(holder.binding) {
                tvTitle.text = article.name
                tvFeed.text = article.content
                tvSummary.text = article.summary
            }
        } catch (e: Exception) {

        }
    }

    override fun getItemCount(): Int = articles.size

    class ArticleViewHolder(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root)
}