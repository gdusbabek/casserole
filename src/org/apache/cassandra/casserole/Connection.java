package org.apache.cassandra.casserole;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.apache.cassandra.cache.JMXInstrumentedCacheMBean;
import org.apache.cassandra.concurrent.IExecutorMBean;
import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.apache.cassandra.net.MessagingServiceMBean;
import org.apache.cassandra.casserole.model.CacheData;
import org.apache.cassandra.casserole.model.CfStat;
import org.apache.cassandra.casserole.model.MsgData;
import org.apache.cassandra.casserole.model.TpStat;
import org.apache.cassandra.service.StorageServiceMBean;
import org.apache.cassandra.streaming.StreamingServiceMBean;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Connection {
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static final String STORAGE_SERVICE = "org.apache.cassandra.db:type=StorageService";
    
    private String host;
    
    private int jmxPort;
    private JMXConnector jmxc = null;
    private MBeanServerConnection mbsc = null;
    
    private int thriftPort;
    private Cassandra.Client thriftClient;
    private Disconnect thriftDisconnect;
    
    private StreamingServiceMBean streamingService = null;
    private StorageServiceMBean storageService = null;
    private Multimap<String, ColumnFamilyStoreMBean> cfStores = null;
    private List<IExecutorMBean> threadPools = null;
    private Multimap<String, ColumnFamilyStoreMBean> indexStores = null;
    private MessagingServiceMBean messaging = null;
    
    public Connection() {
    }
    
    public void connect() throws RemoteException {
        String conUrl = "service:jmx:rmi:///jndi/rmi://" + host + ":" + jmxPort + "/jmxrmi";
        try {
            JMXServiceURL url = new JMXServiceURL(conUrl);
            jmxc = JMXConnectorFactory.connect(url, null);
            mbsc = jmxc.getMBeanServerConnection();
        } catch (MalformedURLException ex) {
            throw new RemoteException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new RemoteException("Could not establish connection: " + conUrl);
        }
        
        try {
            TSocket socket = new TSocket(host, thriftPort);
            final TTransport transport = new TFramedTransport(socket);
            TBinaryProtocol protocol = new TBinaryProtocol(transport, true, true);
            thriftClient = new Cassandra.Client(protocol);
            transport.open();
            thriftDisconnect = new Disconnect() {
                public void disconnect() {
                    if (transport.isOpen())
                        transport.close();
                    thriftClient = null;
                }
            };
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage(), ex);
        }
    }
    
    public void disconnect()
    {
        if (!isConnected())
            return;
        mbsc = null;
        storageService = null;
        if (cfStores != null)
            cfStores.clear();
        if (threadPools != null)
            threadPools.clear();
        if (indexStores != null)
            indexStores.clear();
        messaging = null;
        try {
            jmxc.close();
        } catch (IOException ex) {
            logger.warn("Couldn't close jmx connection cleanly.");
        }
        try {
            thriftDisconnect.disconnect();
        } catch (Throwable th) {
            logger.warn(th.getMessage(), th);
        }
        jmxc = null;
    }
    
    public List<MsgData> getMessageStats() throws RemoteException  {
        if (messaging == null) {
            try {
                ObjectName name = new ObjectName("org.apache.cassandra.net:type=MessagingService");
                messaging = JMX.newMBeanProxy(mbsc, name, MessagingServiceMBean.class);
            } catch (MalformedObjectNameException ex) {
                throw new RemoteException(ex.getMessage(), ex);
            }    
        }
        
        final Map<String, MsgData> msgData = new HashMap<String, MsgData>();
        class Getter {
            MsgData get(String host) {
                MsgData data = msgData.get(host);
                if (data == null) {
                    data = new MsgData(host);
                    msgData.put(host, data);
                }
                return data;
            }
        }
        Getter getter = new Getter();
        for (Map.Entry<String, Long> entry : messaging.getCommandCompletedTasks().entrySet())
            getter.get(entry.getKey()).setRequestsCompleted(entry.getValue());
        for (Map.Entry<String, Integer> entry : messaging.getCommandPendingTasks().entrySet())
            getter.get(entry.getKey()).setRequestsPending(entry.getValue());
        for (Map.Entry<String, Long> entry : messaging.getResponseCompletedTasks().entrySet())
            getter.get(entry.getKey()).setResponseCompleted(entry.getValue());
        for (Map.Entry<String, Integer> entry : messaging.getResponsePendingTasks().entrySet())
            getter.get(entry.getKey()).setResponsePending(entry.getValue());
        
        return new ArrayList<MsgData>(msgData.values());
    }
    
    public List<CacheData> getCacheStats() throws RemoteException {
        List<CacheData> list = new ArrayList<CacheData>();
        Multimap<String, ColumnFamilyStoreMBean> cfsBeans = getCfStoreMBeans();
        for (String keyspace : cfsBeans.keySet()) {
            for (ColumnFamilyStoreMBean cfs : cfsBeans.get(keyspace)) {
                String cfName = cfs.getColumnFamilyName();
                JMXInstrumentedCacheMBean keyCache = getKeyCacheMBean(keyspace, cfName);
                list.add(new CacheData(keyspace, cfName, "keys", keyCache));
                JMXInstrumentedCacheMBean rowCache = getRowCacheMBean(keyspace, cfName);
                list.add(new CacheData(keyspace, cfName, "rows", rowCache));
            }
        }
        Collections.sort(list, CacheData.COMPARATOR);
        return list;
    }
    
    public List<CfStat> getsCfStats() throws RemoteException {
        List<CfStat> list = new ArrayList<CfStat>();
        Multimap<String, ColumnFamilyStoreMBean> cfsBeans = getCfStoreMBeans();
        for (String keyspace : cfsBeans.keySet())
            for (ColumnFamilyStoreMBean cfs : cfsBeans.get(keyspace))
                list.add(new CfStat(keyspace, cfs));
        Collections.sort(list, CfStat.COMPARATOR);
        return list;
    }
    
    public List<CfStat> getIndexCfStats() throws RemoteException {
        List<CfStat> list = new ArrayList<CfStat>();
        Multimap<String, ColumnFamilyStoreMBean> cfsBeans = getIndexStoreMBeans();
        for (String keyspace : cfsBeans.keySet())
            for (ColumnFamilyStoreMBean cfs : cfsBeans.get(keyspace))
                list.add(new CfStat(keyspace, cfs));
        Collections.sort(list, CfStat.COMPARATOR);
        return list;
    }
    
    public List<TpStat> getTpStats() throws RemoteException {
        List<TpStat> list = new ArrayList<TpStat>();
        for (IExecutorMBean bean : getThreadPoolMBeans())
            list.add(new TpStat(bean));
        return list;
    }
    
    // @todo: not sure how I feel about exposing this.
    public StreamingServiceMBean getStreamingService() throws RemoteException {
        
        if (!isConnected())
            throw new RemoteException("Not connected");
        if (streamingService != null)
            return streamingService;
        try {
            streamingService = JMX.newMBeanProxy(mbsc, new ObjectName("org.apache.cassandra.net:type=StreamingService"), StreamingServiceMBean.class);
        } catch (MalformedObjectNameException ex) {
            throw new RemoteException("Malformed object name when getting Streaming Service");
        }
        return streamingService;
    }
    
    private Multimap<String, ColumnFamilyStoreMBean> getIndexStoreMBeans() throws RemoteException {
        if (!isConnected())
                throw new RemoteException("Not connected");
        if (indexStores != null)
            return indexStores;
        indexStores = HashMultimap.<String, ColumnFamilyStoreMBean>create();
        try {
            ObjectName query =  new ObjectName("org.apache.cassandra.db:type=IndexColumnFamilies,*");
            for (ObjectName name : mbsc.queryNames(query, null))
                indexStores.put(name.getKeyProperty("keyspace"), JMX.newMBeanProxy(mbsc, name, ColumnFamilyStoreMBean.class));
        } catch (MalformedObjectNameException ex) {
            throw new RemoteException("Malformed object name getting Index CFStore mbeans");
        } catch (IOException ex) {
            disconnect();
            throw new RemoteException("Disconnected", ex);
        }
        return indexStores;
    }
    
    private Multimap<String, ColumnFamilyStoreMBean> getCfStoreMBeans() throws RemoteException {
        if (!isConnected())
            throw new RemoteException("Not connected");
        if (cfStores != null)
            return cfStores;
        cfStores = HashMultimap.<String, ColumnFamilyStoreMBean>create();
        try {
            ObjectName query = new ObjectName("org.apache.cassandra.db:type=ColumnFamilies,*");
            for (ObjectName name : mbsc.queryNames(query, null))
                cfStores.put(name.getKeyProperty("keyspace"), JMX.newMBeanProxy(mbsc, name, ColumnFamilyStoreMBean.class));
        } catch (MalformedObjectNameException ex) {
            throw new RemoteException("Malformed object name when getting CFStore mbeans");
        } catch (IOException ex) {
            disconnect();
            throw new RemoteException("Disconnected", ex);
        }
        return cfStores;
    }
    
    private JMXInstrumentedCacheMBean getKeyCacheMBean(String keyspace, String columnFamily) throws RemoteException {
        try {
            return JMX.newMBeanProxy(mbsc, new ObjectName(String.format("org.apache.cassandra.db:type=Caches,keyspace=%s,cache=%sKeyCache", keyspace, columnFamily)), JMXInstrumentedCacheMBean.class);
        } catch (MalformedObjectNameException ex) {
            throw new RemoteException("Malformed object name when getting key cache mbean");
        }
    }
    
    private JMXInstrumentedCacheMBean getRowCacheMBean(String keyspace, String columnFamily) throws RemoteException {
        try {
            return JMX.newMBeanProxy(mbsc, new ObjectName(String.format("org.apache.cassandra.db:type=Caches,keyspace=%s,cache=%sRowCache", keyspace, columnFamily)), JMXInstrumentedCacheMBean.class);
        } catch (MalformedObjectNameException ex) {
            throw new RemoteException("Malformed object name when getting key cache mbean");
        }
    }
    
    private List<IExecutorMBean> getThreadPoolMBeans() throws RemoteException {
        if (threadPools != null)
            return threadPools;
        
        threadPools = new ArrayList<IExecutorMBean>();
        try {
            
            Set<ObjectName> requests = mbsc.queryNames(new ObjectName("org.apache.cassandra.request:type=*"), null);
            Set<ObjectName> internal = mbsc.queryNames(new ObjectName("org.apache.cassandra.internal:type=*"), null);
            for (ObjectName name : Iterables.concat(requests, internal))
                threadPools.add(JMX.newMBeanProxy(mbsc, name, IExecutorMBean.class));
        } catch (MalformedObjectNameException ex) {
            throw new RemoteException("Malformed object name when getting threadpool mbeans");
        } catch (IOException ex) {
            disconnect();
            throw new RemoteException("Disconnected", ex);
        }
        return threadPools;
    }
    
    public StorageServiceMBean getStorageService() throws RemoteException {
        if (!isConnected())
            throw new RemoteException("Not connected");
        if (storageService == null) {
            try {
                storageService = JMX.newMBeanProxy(mbsc, new ObjectName(STORAGE_SERVICE), StorageServiceMBean.class);
            } catch (MalformedObjectNameException ex) {
                throw new RemoteException(ex.getMessage(), ex);
            } catch (IllegalArgumentException ex) {
                throw new RemoteException("Unstable connection");
            }
        }
        return storageService;
    }
    
    Map<String, List<String>> getSchema() throws RemoteException{
        try {
            return thriftClient.describe_schema_versions();
        } catch (InvalidRequestException ex) {
            throw new RemoteException(ex.getMessage(), ex);
        } catch (TException ex) {
            throw new RemoteException(ex.getMessage(), ex);
        }
    }
    
    public boolean isConnected() {
        return jmxc != null;
    }
    
    // getters and setters below here.

    public String getHost()
    {
        return host;
    }

    public int getJmxPort()
    {
        return jmxPort;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setJmxPort(int jmxPort)
    {
        this.jmxPort = jmxPort;
    }

    public int getThriftPort()
    {
        return thriftPort;
    }

    public void setThriftPort(int thriftPort)
    {
        this.thriftPort = thriftPort;
    }
    
    private interface Disconnect {
        public void disconnect();
    }
}
