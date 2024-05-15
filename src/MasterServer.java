import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
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

    public static void start() throws Exception
    {
        filenameToId = new ConcurrentHashMap<>();
        idToData = new ConcurrentHashMap<>();
        serverMap = new ConcurrentHashMap<>();
        masterBackupFile = "masterBackup";
        name = "masterServer";

        loadBackup();
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
            ex.printStackTrace();
        }
    }

    private static void loadBackup()
    {
        File backupFile = new File(masterBackupFile);
        if (backupFile.exists())
        {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(masterBackupFile)))
            {
                MapWrapper wrapper = (MapWrapper) inputStream.readObject();
                if (wrapper != null)
                {
                    filenameToId.putAll(wrapper.getFileToId());
                    idToData.putAll(wrapper.getIdToData());
                    System.out.println("Maps loaded from backup file.");
                }
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("No backup file found. Starting with empty HashMaps.");
        }
    }

    public static synchronized void updateBackup()
    {
        MapWrapper maps = new MapWrapper(filenameToId, idToData);
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(masterBackupFile)))
        {
            outputStream.writeObject(maps);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {
        start();
    }
}
