package casserole;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class ConnectionTest
{
    @Test
    public void testStore1() {
        Connection con = new Connection();
        con.setHost("store-1.flow.racklabs.com");
        con.setJmxPort(8080);
        con.setThriftPort(9160);
        try {
            con.connect();
        } catch (Exception ex) {
            throw new AssertionError(ex.getMessage());
        }
    }
    
    @Test
    public void testStore2() {
        Connection con = new Connection();
        con.setHost("store-2.flow.racklabs.com");
        con.setJmxPort(8080);
        con.setThriftPort(9160);
        try {
            con.connect();
            throw new AssertionError("Shouldn't have succeeded");
        } catch (TimeoutException ex) {
            // this should happen.
        } catch (Exception ex) {
            throw new AssertionError(ex.getMessage());
        }
    }
}
