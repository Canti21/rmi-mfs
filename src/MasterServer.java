import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MasterServer
{
    private static ConcurrentMap<String, Integer> filenameToId;
    private static ConcurrentMap<Integer, DataTable> idToData;
    private static ConcurrentMap<String, Master_DataServerStatus> serverMap;
    private static Registry registry;
    private static String name;

    public MasterServer()
    {
        filenameToId = new ConcurrentHashMap<>();
        idToData = new ConcurrentHashMap<>();
        serverMap = new ConcurrentHashMap<>();
        name = "masterServer";
    }

    public void start() throws Exception
    {
        registry = LocateRegistry.createRegistry(1099);
        registry.bind(name, new Master_ServerObject(filenameToId, idToData, serverMap));
    }

    private static void acquireDataServers()
    {
        try
        {
            String[] serverNames = registry.list();

            for (String serverName : serverNames)
            {
                if (!serverName.equals(name))
                {
                    try
                    {
                        DataServer_Interface server = (DataServer_Interface) registry.lookup(serverName);
                        int freeSpace = server.heartBeat();
                        Master_DataServerStatus status = new Master_DataServerStatus(freeSpace, server);
                        serverMap.put(serverName, status);
                    }
                    catch (NotBoundException e)
                    {
                        // Server with the given name is not bound in the registry
                        serverMap.computeIfPresent(serverName, (key, value) -> {
                            value.setAvailability(false);
                            return value;
                        });
                    }
                }
            }
        }
        catch (Exception ex)
        {

        }
    }

    public static void main(String[] args) throws Exception
    {
        MasterServer masterServer = new MasterServer();
        Thread serversThread = new Thread(() -> {
            while (true)
            {
                acquireDataServers();
                try
                {
                    Thread.sleep(3000);
                }
                catch (InterruptedException ex)
                {

                }
            }
        });
        masterServer.start();
    }
}
