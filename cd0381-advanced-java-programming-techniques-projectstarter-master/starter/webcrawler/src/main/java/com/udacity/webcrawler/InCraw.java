package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class InCraw extends RecursiveAction {
    private String url;
    private Instant complited;
    private int maxDepth;
    private ConcurrentMap<String, Integer> counts;
    private ConcurrentSkipListSet<String> visit;
    private Clock clock;
    private PageParserFactory pageParserFactory;
    private List<Pattern> ignoredUrls;

    public InCraw(String url, Instant complited, int maxDepth, ConcurrentMap<String, Integer> counts, ConcurrentSkipListSet<String> visit, Clock clock, PageParserFactory pageParserFactory, List<Pattern> ignoredUrls) {
        this.url = url;
        this.complited = complited;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visit = visit;
        this.clock = clock;
        this.pageParserFactory = pageParserFactory;
        this.ignoredUrls = ignoredUrls;
    }

    /*@Override
    protected Boolean compute() {
        if (maxDepth == 0 || clock.instant().isAfter(complited) || !visit.add(url)) {
            return false;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return false;
            }
        }
        if (visit.contains(url)) {
            return false;
        }
        visit.add(url);
        PageParser.Result result = pageParserFactory.get(url).parse();
        for (ConcurrentMap.Entry<String, Integer> a : result.getWordCounts().entrySet()) {
            counts.compute(a.getKey(), (k, v) -> (v == null) ? a.getValue() : a.getValue() + v);
        }
        List<InCraw> tasks = new ArrayList<>();
        for (String links : result.getLinks()) {
            tasks.add(new InCraw(links, complited, maxDepth - 1, counts, visit, clock, pageParserFactory, ignoredUrls));
        }
        invokeAll(tasks);

        return true;
    }*/
    @Override
    protected void compute() {
        if (maxDepth == 0 || clock.instant().isAfter(complited) || !visit.add(url)) {
            return;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }
        if (visit.contains(url)) {
            return;
        }
        visit.add(url);
        PageParser.Result result = pageParserFactory.get(url).parse();
        for (ConcurrentMap.Entry<String, Integer> a : result.getWordCounts().entrySet()) {
            counts.compute(a.getKey(), (k, v) -> (v == null) ? a.getValue() : a.getValue() + v);
        }
        List<InCraw> tasks = new ArrayList<>();
        for (String links : result.getLinks()) {
            tasks.add(new InCraw(links, complited, maxDepth - 1, counts, visit, clock, pageParserFactory, ignoredUrls));
        }
        invokeAll(tasks);
    }
}
