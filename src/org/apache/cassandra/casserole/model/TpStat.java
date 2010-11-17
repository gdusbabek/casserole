package org.apache.cassandra.casserole.model;

import org.apache.cassandra.concurrent.IExecutorMBean;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TpStat implements RowData
{
    public static final String[] COLS = { "Name", "Active", "Pending", "Completed" };
    private static final Pattern pattern = Pattern.compile("(.*:type=)(\\w+)(\\]\\))");
    
    // gets updates.
    private IExecutorMBean bean;
    
    private final String name;
    private int active;
    private long pending;
    private long completed;
    
    public TpStat(IExecutorMBean bean) {
        String name = null;
        try {
            Matcher m = pattern.matcher(bean.toString());
            m.matches();
            name = m.group(2);
        } catch (IllegalStateException noMatchFound) {
            name = "Don't know";
        }
        this.name = name;
        this.bean = bean;
        this.active = bean.getActiveCount();
        this.pending = bean.getPendingTasks();
        this.completed = bean.getCompletedTasks();
    }
    
    public String getName() { return name; }

    public Iterable<Integer> update(RowData rd)
    {
        TpStat stat = (TpStat)rd;
        return setAndGetUpdated(stat);
    }

    public Object getCol(int c) {
        switch (c) {
            case 0: return name;
            case 1: return active;
            case 2: return pending;
            case 3: return completed;
            default: return "eh?";
        }
    }

    private Iterable<Integer> setAndGetUpdated(TpStat stat) {
        List<Integer> list = new ArrayList<Integer>();
        if (active != stat.active) {
            list.add(1);
            active = stat.active;
        }
        if (pending != stat.pending) {
            list.add(2);
            pending = stat.pending;
        }
        if (completed != stat.completed) {
            list.add(3);
            completed = stat.completed;
        }
        return list;
    }
}
