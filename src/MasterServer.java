import java.io.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MasterServer
{
    private static Registry registry;
    private static String name;
    private static String masterBackupFile;
    private static ConcurrentHashMap<String, Integer> filenameToId;
    private static ConcurrentHashMap<Integer, DataTable> idToData;
    private static ConcurrentHashMap<Integer, String[]> idToServerNames;
    private static ConcurrentMap<String, Master_DataServerStatus> serverMap;
    private static FileIOHandler fileIOHandler;

    public static void start() throws Exception
    {
        filenameToId = new ConcurrentHashMap<>();
        idToData = new ConcurrentHashMap<>();
        idToServerNames = new ConcurrentHashMap<>();
        serverMap = new ConcurrentHashMap<>();
        masterBackupFile = "masterBackup";
        name = "masterServer";
        fileIOHandler = new FileIOHandler();

        System.out.println("Looking for backup...");
        loadBackup();
        unlockAllFiles();
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
        registry.rebind(name, new Master_ServerObject(filenameToId, idToData, idToServerNames, serverMap));
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
                        DataServer_Interface server = (DataServer_Interface) registry.lookup(serverName);
                        int freeSpace = server.heartBeat();
                        Master_DataServerStatus status = new Master_DataServerStatus(freeSpace, server);
                        Master_DataServerStatus previousStatus = serverMap.put(serverName, status);
                        if(previousStatus == null)
                        {
                            System.out.println("Found new server: \"" + serverName + "\"");
                        }
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
            updateServers();
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
            filenameToId = wrapper.getFileToId();
            idToData = wrapper.getIdToData();
            idToServerNames = wrapper.getIdToServerNames();
            serverMap = wrapper.getServerMap();
            System.out.println("Maps loaded from backup file.");
        }
        else
        {
            System.out.println("No backup file found. Starting with empty HashMaps.");
        }
    }

    public static synchronized void updateBackup()
    {
        if (!filenameToId.isEmpty() && !idToData.isEmpty())
        {
            MapWrapper maps = new MapWrapper(filenameToId, idToData, idToServerNames, serverMap);
            fileIOHandler.writeObjectToFile(maps, masterBackupFile);
        }
    }

    public static void unlockAllFiles()
    {
        idToData.forEach((k, v) -> v.getLock().release());
    }

    public static void updateServers()
    {
        // Iterate through each DataTable in idToData map
        idToData.forEach((id, dataTable) -> {
            // Get the server names associated with this DataTable ID
            String[] serverNames = idToServerNames.get(id);

            if (serverNames != null)
            {
                // Create an array to hold the DataServer_Interface objects
                DataServer_Interface[] servers = new DataServer_Interface[serverNames.length];

                // Fetch the corresponding DataServer_Interface objects from the serverMap
                for (int i = 0; i < serverNames.length; i++)
                {
                    Master_DataServerStatus status = serverMap.get(serverNames[i]);
                    if (status != null)
                    {
                        servers[i] = status.getServer();
                    }
                    else
                    {
                        // Handle the case where the server status is not found
                        //System.err.println("Server status not found for server name: " + serverNames[i]);
                    }
                }

                // Set the updated servers array back to the DataTable
                dataTable.setServers(servers);
            }
            else
            {
                // Handle the case where the server names are not found for this ID
                //System.err.println("Server names not found for DataTable ID: " + id);
            }
        });
    }

    public static void main(String[] args) throws Exception
    {
        start();
    }


}
