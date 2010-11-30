package casserole.util;

import org.junit.Test;

import java.text.Format;
import java.util.HashMap;
import java.util.Map;

public class SizeFormatTests {
    
    @Test
    public void testBytes() {
        Map<Long, String> expectedConversions = new HashMap<Long, String>() {{
            put(500L, "500 B");
            put(799L, "799 B");
            put(800L, "0.78 KB");
            put(1048576L, "1 MB");
            put(1258700L, "1.20 MB");
            put(1649267441664L, "1.5 TB");
            put(11361620153685L, "10.3333 TB");
        }};
        assertMap(new SizeFormatter(SizeFormatter.Type.Bytes), expectedConversions);
    }
    
    @Test
    public void testsDecSmall() {
        Map<Long, String> expectedConversions = new HashMap<Long, String>() {{
            put(500L, "500");
            put(799L, "799");
            put(800L, "800");
            put(1048576L, "1.05 M");
            put(1258700L, "1.26 M");
            put(1649267441664L, "1649267.44 M");
            put(11361620153685L, "11361620.15 M");
        }};
        assertMap(new SizeFormatter(SizeFormatter.Type.DecimalSmall), expectedConversions);
    }
    
    @Test
    public void testDecLarge() {
        Map<Long, String> expectedConversions = new HashMap<Long, String>() {{
            put(500L, "500");
            put(799L, "799");
            put(800L, "800");
            put(1048576L, "1.0486 M");
            put(1258700L, "1.2587 M");
            put(1649267441664L, "1649267.4417 M");
            put(11361620153685L, "11361620.1537 M");
        }};
        assertMap(new SizeFormatter(SizeFormatter.Type.DecimalLarge), expectedConversions);
    }
    
    private void assertMap(Format fmt, Map<? extends Number, String> expectations) {
        for (Map.Entry<? extends Number, String> entry : expectations.entrySet()) {
            String res = fmt.format(entry.getKey());
            assert res.equals(entry.getValue()) : String.format("%s is not %s", res, entry.getValue());
        }
    }
}
