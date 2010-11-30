package casserole.util;

import org.apache.cassandra.io.sstable.Component;
import org.apache.cassandra.io.sstable.Descriptor;
import org.apache.cassandra.streaming.PendingFile;
import org.apache.cassandra.utils.Pair;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParseTests {
    List<Pair<Long, Long>> sections = new ArrayList<Pair<Long, Long>>() {{
        add(new Pair<Long, Long>(234234234523234L, 234234234234L));
        add(new Pair<Long, Long>(98457983475L, 345345L));
        add(new Pair<Long, Long>(29379843579834L, 9847435762L));
        add(new Pair<Long, Long>(2384734568972346L, 384972L));
        add(new Pair<Long, Long>(45698236275L, 45976923728935761L));
        add(new Pair<Long, Long>(6797345892364345L, 2349080928342L));
        add(new Pair<Long, Long>(2389479872345L, 438957987234234L));
        add(new Pair<Long, Long>(69346290727864L, 34563L));
    }};
    
    // my hope is that if PendingFile.toString() format changes this test will break.
    @Test
    public void test() {
        String path = "/this/path/is/not/ever/going/to/be/valid";
        String keyspace = "MyTestKeyspace";
        String columnFamily = "MyTestColumnFamily";
        int generation = 3;
        Descriptor desc = new Descriptor(new File(path), keyspace, columnFamily, generation, false);
        PendingFile pf = new PendingFile(desc, Component.DATA.name(), sections);
        List<String> pieces = ParseHelp.parsePendingFile(pf.toString());
        assert pieces.size() == 3 + 2 * sections.size();
        assert pieces.get(0).equals(String.format("%s/%s-%s-%d-%s", path, columnFamily, Descriptor.CURRENT_VERSION, generation, Component.DATA.name()));
        long size = 0;
        for (Pair<Long, Long> p : sections)
            size += p.right - p.left;
        assert pieces.get(pieces.size()-2).equals("0");
        assert pieces.get(pieces.size()-1).equals(Long.toString(size));
        for (int i = 1; i < pieces.size()-2; i+= 2) {
            assert pieces.get(i).equals(Long.toString(sections.get((i-1)/2).left));
            assert pieces.get(i+1).equals(Long.toString(sections.get((i-1)/2).right));
        }
    }
}
