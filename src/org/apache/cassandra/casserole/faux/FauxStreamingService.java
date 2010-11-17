package org.apache.cassandra.casserole.faux;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.cassandra.streaming.StreamingServiceMBean;
import org.apache.cassandra.utils.Pair;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class FauxStreamingService implements StreamingServiceMBean {
    
    // 0@10kB/s
    // 1@100kB/s
    // 2@1000kB/s
    
    private static Random rand = new Random(System.currentTimeMillis());
    
    private final long maxTransfer;
    private final long start;
    
    private Multimap<InetAddress, Transfer> out = HashMultimap.create();
    private Multimap<InetAddress, Transfer> in = HashMultimap.create();
    
    
    public FauxStreamingService(long maxTransfer) {
        start = System.currentTimeMillis();
        this.maxTransfer = maxTransfer;
  
        // 0 -> 10000 10^3
        // 1 -> 100000 10^4
        // 2 -> 1000000 10^6
        int[] rates = new int[]{10000,100000, 1000000};
        for (int ip = 0; ip < rand.nextInt(4)+1; ip++) {
            try {
                InetAddress addr = InetAddress.getByName("127.0.0." + ip);
                for (int t = 1; t < rand.nextInt(20)+1; t++)
                    out.put(addr, new Transfer(rates[rand.nextInt(3)]));
            } catch (UnknownHostException ex) { 
                throw new RuntimeException(ex);
            }
        }
        for (int ip = 0; ip < rand.nextInt(4)+1; ip++) {
            try {
                InetAddress addr = InetAddress.getByName("127.0.0." + ip);
                for (int t = 1; t < rand.nextInt(20)+1; t++)
                    in.put(addr, new Transfer(rates[rand.nextInt(3)]));
            } catch (UnknownHostException ex) { 
                throw new RuntimeException(ex);
            }
        }
    }
    
    public Set<InetAddress> getStreamDestinations()
    {
        HashSet<InetAddress> set = new HashSet<InetAddress>();
        for (Map.Entry<InetAddress, Transfer> entry : out.entries())
            if (!entry.getValue().done())
                set.add(entry.getKey());
        return set;
    }

    public List<String> getOutgoingFiles(String s) throws IOException
    {
        InetAddress addr = InetAddress.getByName(s);
        List<String> list = new ArrayList<String>();
        for (Transfer trans : out.get(addr))
            if (!trans.done())
                list.add(trans.toString());
        return list;
    }

    public Set<InetAddress> getStreamSources()
    {
        HashSet<InetAddress> set = new HashSet<InetAddress>();
        for (Map.Entry<InetAddress, Transfer> entry : in.entries())
            if (!entry.getValue().done())
                set.add(entry.getKey());
        return set;
    }

    public List<String> getIncomingFiles(String s) throws IOException
    {
        InetAddress addr = InetAddress.getByName(s);
        List<String> list = new ArrayList<String>();
        for (Transfer trans : in.get(addr))
            if (!trans.done())
                list.add(trans.toString());
        return list;
    }

    public String getStatus()
    {
        return "This is not a valid status";
    }
    
    private class Transfer {
        private String file;
        private int bytesPerSecond;
        private List<Pair<Long,Long>> sections;
        
        Transfer(int bytesPerSecond) {
            file = "";
            for (int i = 0; i < Math.abs(rand.nextInt(30)); i++)
                file += (char)(97+rand.nextInt(25));
            file += ".db";
            this.bytesPerSecond = bytesPerSecond;
            sections = new ArrayList<Pair<Long, Long>>();
            for (int i = 0; i < rand.nextInt(5)+1; i++)
                sections.add(new Pair<Long, Long>(Math.abs(rand.nextLong()), Math.abs(rand.nextLong())));
        }
        
        // designed to mimic PendingFile.toString()
        public String toString() {
            long trans = transferred();
            return file + "/" + StringUtils.join(sections, ",") + "\n\t progress=" + trans + "/" + maxTransfer + " - " + trans*100/maxTransfer + "%";
        }
        
        long transferred() {
            return (bytesPerSecond * ((System.currentTimeMillis() - start)/1000));
        }
        
        boolean done() { return transferred() > maxTransfer; }
    }
}
