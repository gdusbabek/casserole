package org.apache.cassandra.casserole.model;

import org.apache.cassandra.cache.JMXInstrumentedCacheMBean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CacheData implements RowData {
    public static final String[] COLS = { "Column Family", "Type", "Capacity", "Hits", "Hit Rate", "Requests", "Size"};
    public static final Comparator<CacheData> COMPARATOR = new Comparator<CacheData>() {
        public int compare(CacheData o1, CacheData o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    
    private String keyspace;
    private String cf;
    private String kind;
    private int capacity;
    private long hits;
    private double hitRate;
    private long requests;
    private long size;
    
    public CacheData(String keyspace, String cf, String kind, JMXInstrumentedCacheMBean cache) {
        this.keyspace = keyspace;
        this.cf = cf;
        this.kind = kind;
        this.capacity = cache.getCapacity();
        this.hits = cache.getHits();
        this.hitRate = cache.getRecentHitRate();
        this.requests = cache.getRequests();
        this.size = cache.getSize();
    }
    
    public Object getCol(int c)
    {
        switch (c) {
            case 0: return keyspace + "." + cf;
            case 1: return kind;
            case 2: return capacity;
            case 3: return hits;
            case 4: return hitRate;
            case 5: return requests;
            case 6: return size;
            default: return "eh?";
        }
    }

    public Iterable<Integer> update(RowData rd)
    {
        CacheData other = (CacheData)rd;
        List<Integer> list = new ArrayList<Integer>();
        
        if (other.capacity != capacity) {
            capacity = other.capacity;
            list.add(2);
        }
        if (other.hits != hits) {
            hits = other.hits;
            list.add(3);
        }
        if (other.hitRate != hitRate) {
            hitRate = other.hitRate;
            list.add(4);
        }
        if (other.requests != requests) {
            requests = other.requests;
            list.add(5);
        }
        if (other.size != size) {
            size = other.size;
            list.add(6);
        }
        return list;
    }
    
    public String getName() { return keyspace + "-" + cf + "-" + kind; }
}
