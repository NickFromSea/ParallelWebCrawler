package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final List<Pattern> ignoredUrls;
  private final int maxDepth;
  private final PageParserFactory pageParserFactory;

  @Inject
  ParallelWebCrawler(
          Clock clock,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @TargetParallelism int threadCount,
          @IgnoredUrls List<Pattern> ignoredUrls,
          @MaxDepth int maxDepth,
          PageParserFactory pageParserFactory) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.ignoredUrls = ignoredUrls;
    this.maxDepth = maxDepth;
    this.pageParserFactory = pageParserFactory;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant complitedTime = clock.instant().plus(timeout);
    ConcurrentMap<String,Integer> counts = new ConcurrentHashMap<>();
    ConcurrentSkipListSet<String> visit = new ConcurrentSkipListSet<>();
    for (String url:startingUrls){
      pool.invoke(new InCraw(url,complitedTime,maxDepth, counts, visit, clock, pageParserFactory, ignoredUrls));

    }
    if (counts.isEmpty()){
      return new CrawlResult.Builder()
              .setWordCounts(counts)
              .setUrlsVisited(visit.size())
              .build();
    }
    return new CrawlResult.Builder()
            .setWordCounts(WordCounts.sort(counts,popularWordCount))
            .setUrlsVisited(visit.size())
            .build();
    //return new CrawlResult.Builder().build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
