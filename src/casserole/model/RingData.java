package casserole.model;

import org.apache.cassandra.dht.Token;

import java.util.ArrayList;
import java.util.List;

public class RingData implements RowData {
    public static final String[] COLS = {"Token", "Host", "Status", "Load", "Mode", "Schema"};
    
    private Token token;
    private String host;
    private String status;
    private String load;
    private String mode;
    private String schema;
    
    public RingData(Token token, String host, String status, String load, String mode, String schema) {
        this.token = token;
        this.host = host;
        this.status = status;
        this.load = load;
        this.mode = mode;
        this.schema = schema;
    }
    
    public String getName() {
        return token.toString();
    }
    
    public String getHost() {
        return host;
    }
    
    public Object getCol(int c)
    {
        switch (c) {
            case 0: return token.toString();
            case 1: return host;
            case 2: return status;
            case 3: return load;
            case 4: return mode;
            case 5: return schema;
            default: return "eh?";
        }
    }

    public Iterable<Integer> update(RowData rd) {
        RingData data = (RingData)rd;
        List<Integer> updated = new ArrayList<Integer>();
        if (data == null)
            return updated;
        if (!status.equals(data.status)) {
            status = data.status;
            updated.add(2);
        }
        if (!load.equals(data.load)) {
            load = data.load;
            updated.add(3);
        }
        if (!mode.equals(data.mode)) {
            mode = data.mode;
            updated.add(4);
        }
        if (!schema.equals(data.schema)) {
            schema = data.schema;
            updated.add(5);
        }
        return updated;
    }
}
