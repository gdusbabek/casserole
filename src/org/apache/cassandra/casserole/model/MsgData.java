package org.apache.cassandra.casserole.model;

import java.util.ArrayList;
import java.util.List;

public class MsgData implements RowData {
    public static final String[] COLS = { "Other Host", "Requests Completed", "Requests Pending", "Response Completed", "Response Pending" };
    
    private final String host;
    private long requestsCompleted;
    private int requestsPending;
    private long responseCompleted;
    private int responsePending;
    
    public MsgData(String host) {
        this.host = host;
        requestsCompleted = 0;
        requestsPending = 0;
        responseCompleted = 0;
        responsePending = 0;
    }
    
    public Object getCol(int c) {
        switch (c) {
            case 0: return host;
            case 1: return requestsCompleted;
            case 2: return requestsPending;
            case 3: return responseCompleted;
            case 4: return responsePending;
            default: return "eh?";
        }
    }
    
    public Iterable<Integer> update(RowData rd) {
        MsgData other = (MsgData)rd;
        List<Integer> list = new ArrayList<Integer>();
        if (other.requestsCompleted != requestsCompleted) {
            requestsCompleted = other.requestsCompleted;
            list.add(1);
        }
        if (other.requestsPending != requestsPending) {
            requestsPending = other.requestsPending;
            list.add(2);
        }
        if (other.responseCompleted != responseCompleted) {
            responseCompleted = other.responseCompleted;
            list.add(3);
        }
        if (other.responsePending != responsePending) {
            responsePending = other.responsePending;
            list.add(4);
        }
        return list;
    }

    public String getName() {
        return host;
    }

    public void setRequestsCompleted(long requestsCompleted) {
        this.requestsCompleted = requestsCompleted;
    }

    public void setRequestsPending(int requestsPending) {
        this.requestsPending = requestsPending;
    }

    public void setResponseCompleted(long responseCompleted) {
        this.responseCompleted = responseCompleted;
    }

    public void setResponsePending(int responsePending) {
        this.responsePending = responsePending;
    }
}
