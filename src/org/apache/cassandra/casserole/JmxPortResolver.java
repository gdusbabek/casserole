package org.apache.cassandra.casserole;

/**
 * In most cases, we just assume a cluster has the same jmx port on each node in a cluster. But when you have a cluster
 * running on 127.x.x.x interfaces, you end up specifying a different jmx port for each node.  This class recognizes
 * that and applies the mapping I use in my development environment.
 */
public class JmxPortResolver
{
    public static int getPort(String host, int defaultPort)
    {
        if (host.startsWith("127.")) {
            try {
                return 8080 +  Integer.parseInt(host.split("\\.")[3]);
            } catch (Throwable anything) {
                return defaultPort;
            }
        } else
            return defaultPort;
    }
}
