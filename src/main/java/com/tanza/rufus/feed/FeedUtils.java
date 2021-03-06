package com.tanza.rufus.feed;

import com.tanza.rufus.api.Article;
import com.tanza.rufus.api.RufusFeed;
import com.tanza.rufus.api.Source;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.jsoup.Jsoup;

import com.sun.syndication.feed.synd.SyndEntry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FeedUtils {

    private FeedUtils() {
        throw new AssertionError(); //noninstantiability
    }

    public static void mergeAuthors(SyndEntry entry) {
        if (CollectionUtils.isEmpty(entry.getAuthors())) {
            entry.setAuthors(Collections.singletonList(entry.getAuthor()));
        }
    }

    public static String truncate(String value, int maxLength) {
        String ret;
        ret = value.length() > maxLength ? StringUtils.abbreviate(value, maxLength) : value;
        return ret;
    }

    public static List<Article> sort(List<Article> articles) {
        articles.sort((a, b) -> b.getPublicationDate().compareTo(a.getPublicationDate()));
        return articles;
    }

    public static List<RufusFeed> sourceToFeed(List<Source> sources) {
       return sources.stream().map(s -> RufusFeed.generate(s)).collect(Collectors.toList());
    }

    /**
     * Unescape html character entities and strip html tags from content
     *
     * @param content
     * @return
     */
    public static String clean(String content) {
        return Jsoup.parse(StringEscapeUtils.unescapeHtml4(content)).text();
    }

    public static void markBookmarks(List<Article> articles, Set<Article> bookmarks) {
        articles.stream().filter(bookmarks::contains).forEach(a -> a.setBookmark(true));
    }

    /**
     * Determines whether or not an {@link Collection}
     * contains <i>only</i> null elements.
     *
     * @param collection
     * @return
     */
    public static boolean isNull(Collection<?> collection) {
        return collection.stream().allMatch(e -> e == null);
    }

    public static List<Source> getPublicSources() {
        List<Source> sources = new ArrayList<>();
        for (String s : FeedConstants.STARTER_FEEDS.values()) {
            try {
                sources.add(new Source(new URL(s)));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return sources;
    }

    public static byte[] getVerificationKey() {
        String key = System.getenv(FeedConstants.JWT_PROPERTY);
        if (key == null) {
            throw new IllegalStateException("No JWT Environment Var Set!");
        } else {
            return key.getBytes();
        }
    }
}

