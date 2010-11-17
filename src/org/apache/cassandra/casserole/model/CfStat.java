package org.apache.cassandra.casserole.model;

import org.apache.cassandra.db.ColumnFamilyStoreMBean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CfStat implements RowData {
    public static final String[] COLS = {
            "Name", 
            "Num SSTables", 
            "Live Sz", 
            "Total Sz", 
            "MT Cols",
            "MT Data Sz",
            "MT Switches",
            "Rd Ct",
            "Rd Lat",
            "Wr Ct",
            "Wr Lat",
            "Pndg Tasks",
            "Comp row min sz",
            "Comp row max sz",
            "Comp row avg sz",
    };
    
    public static final Comparator<CfStat> COMPARATOR = new Comparator<CfStat>() {
        public int compare(CfStat o1, CfStat o2) {
            return (o1.keyspace + "-" + o1.name).compareTo(o2.keyspace + o2.name);
        }
    };
    
    private final String keyspace;
    private final String name;
    private int ssTableCount;
    private long spaceUsedLive;
    private long spaceUsedTotal;
    private int memtableColCount;
    private long memtableSize;
    private long memtableSwitchCount;
    private long readCount;
    private double readLatency;
    private long writeCount;
    private double writeLatency;
    private int pendingTasks;
    private long compRowMinSize;
    private long compRowMaxSize;
    private long compRowMeanSize;
    
    public CfStat(String keyspace, ColumnFamilyStoreMBean cfs) {
        this.keyspace = keyspace;
        name = cfs.getColumnFamilyName();
        ssTableCount = cfs.getLiveSSTableCount();
        spaceUsedLive = cfs.getLiveDiskSpaceUsed();
        spaceUsedTotal = cfs.getTotalDiskSpaceUsed();
        memtableColCount = cfs.getMemtableColumnsCount();
        memtableSize = cfs.getMemtableDataSize();
        memtableSwitchCount = cfs.getMemtableSwitchCount();
        readCount = cfs.getReadCount();
        readLatency = cfs.getRecentReadLatencyMicros();
        writeCount = cfs.getWriteCount();
        writeLatency = cfs.getRecentWriteLatencyMicros();
        pendingTasks = cfs.getPendingTasks();
        compRowMinSize = cfs.getMinRowSize();
        compRowMaxSize = cfs.getMaxRowSize();
        compRowMeanSize = cfs.getMeanRowSize();
    }
    
    public String getName() {
        return keyspace + "-" + name;
    }

    public Iterable<Integer> update(RowData rd) {
        CfStat data = (CfStat)rd;
        List<Integer> list = new ArrayList<Integer>();
        if (data.ssTableCount != data.ssTableCount) {
            list.add(1);
            this.ssTableCount = data.ssTableCount;
        }
        if (data.spaceUsedLive != spaceUsedLive) {
            list.add(2);
            this.spaceUsedLive = data.spaceUsedLive;
        }
        if (data.spaceUsedTotal != spaceUsedTotal) {
            list.add(3);
            this.spaceUsedTotal = data.spaceUsedTotal;
        }
        if (data.memtableColCount != data.memtableColCount) {
            list.add(4);
            this.memtableColCount = data.memtableColCount;
        }
        if (data.memtableSize != data.memtableSize) {
            list.add(5);
            this.memtableSize = data.memtableSize;
        }
        if (data.memtableSwitchCount != data.memtableSwitchCount) {
            list.add(6);
            this.memtableSwitchCount = data.memtableSwitchCount;
        }
        if (data.readCount != data.readCount) {
            list.add(7);
            this.readCount = data.readCount;
        }
        if (data.readLatency != data.readLatency) {
            list.add(8);
            this.readLatency = data.readLatency;
        }
        if (data.writeCount != data.writeCount) {
            list.add(9);
            this.writeCount = data.writeCount;
        }
        if (data.writeLatency != data.writeLatency) {
            list.add(10);
            this.writeLatency = data.writeLatency;
        }
        if (data.pendingTasks != data.pendingTasks) {
            list.add(11);
            this.pendingTasks = data.pendingTasks;
        }
        if (data.compRowMinSize != data.compRowMinSize) {
            list.add(12);
            this.compRowMinSize = data.compRowMinSize;
        }
        if (data.compRowMaxSize != data.compRowMaxSize) {
            list.add(13);
            this.compRowMaxSize = data.compRowMaxSize;
        }
        if (data.compRowMeanSize != data.compRowMeanSize) {
            list.add(14);
            this.compRowMeanSize = data.compRowMeanSize;
        }
        return list;
    }

    public Object getCol(int c)
    {
        switch (c) {
            case 0: return keyspace + "." + name;
            case 1: return ssTableCount;
            case 2: return spaceUsedLive;
            case 3: return spaceUsedTotal;
            case 4: return memtableColCount;
            case 5: return memtableSize;
            case 6: return memtableSwitchCount;
            case 7: return readCount;
            case 8: return readLatency;
            case 9: return writeCount;
            case 10: return writeLatency;
            case 11: return pendingTasks;
            case 12: return compRowMinSize;
            case 13: return compRowMaxSize;
            case 14: return compRowMeanSize;
            default: return "eh?";
        }
    }
}
