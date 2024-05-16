import java.io.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MasterServer
{
    private static ConcurrentMap<String, Master_DataServerStatus> serverMap;
    private static Registry registry;
    private static String name;
    private static String masterBackupFile;
    private static ConcurrentHashMap<String, Integer> filenameToId;
    private static ConcurrentHashMap<Integer, DataTable> idToData;
    private static FileIOHandler fileIOHandler;

    public static void start() throws Exception
    {
        filenameToId = new ConcurrentHashMap<>();
        idToData = new ConcurrentHashMap<>();
        serverMap = new ConcurrentHashMap<>();
        masterBackupFile = "masterBackup";
        name = "masterServer";
        fileIOHandler = new FileIOHandler();

        System.out.println("Looking for backup...");
        loadBackup();
        System.out.println("Retrieving data servers...");
        acquireDataServers();

        registry = LocateRegistry.createRegistry(1099);
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
                    Thread.currentThread().interrupt();
                }
            }
        });
        Thread backupThread = new Thread(() -> {
            while(true)
            {
                updateBackup();
                try
                {
                    Thread.sleep(3000);
                }
                catch (InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }
            }
        });
        serversThread.start();
        backupThread.start();
        registry.rebind(name, new Master_ServerObject(filenameToId, idToData, serverMap));
        System.out.println("Master server READY!");
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
                        if (!serverMap.containsKey(serverName))
                            System.out.println("Found new server: \"" + serverName + "\"");
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
            ex.printStackTrace();
        }
    }

    private static void loadBackup()
    {
        Object obj = fileIOHandler.readObjectFromFile(masterBackupFile);
        if (obj instanceof MapWrapper)
        {
            MapWrapper wrapper = (MapWrapper) obj;
            filenameToId.putAll(wrapper.getFileToId());
            idToData.putAll(wrapper.getIdToData());
            System.out.println("Maps loaded from backup file.");
        }
        else
        {
            System.out.println("No backup file found. Starting with empty HashMaps.");
        }
    }

    public static synchronized void updateBackup()
    {
        if (!filenameToId.isEmpty() || !idToData.isEmpty())
        {
            MapWrapper maps = new MapWrapper(filenameToId, idToData);
            fileIOHandler.writeObjectToFile(maps, masterBackupFile);
        }
    }

    public static void main(String[] args) throws Exception
    {
        start();
    }
}
