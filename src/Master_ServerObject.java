import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class Master_ServerObject extends UnicastRemoteObject implements Master_ServerInterface
{
    private ConcurrentMap<String, Integer> filenameToId;
    private ConcurrentMap<Integer, DataTable> idToData;
    private ConcurrentMap<String, Master_DataServerStatus> serverMap;
    private ConcurrentHashMap<Integer, String[]> idToServerNames;
    private Random random;

    public Master_ServerObject(ConcurrentMap<String, Integer> filenameToId,
                               ConcurrentMap<Integer, DataTable> idToData,
                               ConcurrentHashMap<Integer, String[]> idToServerNames,
                               ConcurrentMap<String,Master_DataServerStatus> serverMap) throws RemoteException
    {
        this.filenameToId = filenameToId;
        this.idToData = idToData;
        this.idToServerNames = idToServerNames;
        this.serverMap = serverMap;
        random = new Random();
    }

    public Object[] requestUpload(Object[] fileMetadata) throws RemoteException
    {
        try
        {
            System.out.println("Upload requested from " + RemoteServer.getClientHost());
        }
        catch (ServerNotActiveException snae) { }

        FileMetadata metadata = (FileMetadata) fileMetadata[0];
        System.out.println("\tMetadata received: " + metadata.getFileName() + ", " + metadata.getFileSizeInBytes() + ", " + metadata.getModifiedDate());

        // Get file name, modified date and file size
        String fileName = metadata.getFileName().toLowerCase();

        Integer existingFileId = filenameToId.get(fileName);
        System.out.println(existingFileId);
        if (existingFileId != null)
        {
            System.out.println("\tItem already exists, returning servers");
            lockFile(existingFileId);
            DataTable data = idToData.get(existingFileId);
            return new Object[] {existingFileId, data.getServers()};
        }

        int fileSizeInBytes = metadata.getFileSizeInBytes();
        // Generate Unique File ID
        int fileId = generateUniqueRandomId();

        filenameToId.put(fileName, fileId);
        String[] selectedDataServersNames = selectDataServers(fileSizeInBytes);
        List<DataServer_Interface> selectedDataServersList = new ArrayList<>();
        for (String serverName : selectedDataServersNames)
        {
            DataServer_Interface server = serverMap.get(serverName).getServer();
            selectedDataServersList.add(server);
        }
        DataServer_Interface[] selectedDataServers = selectedDataServersList.toArray(new DataServer_Interface[0]);

        DataTable dataTable = new DataTable(metadata, selectedDataServers);

        idToData.put(fileId, dataTable);
        idToServerNames.put(fileId, selectedDataServersNames);
        lockFile(fileId);

        return new Object[]{fileId, selectedDataServers};
    }

    public void confirmUpload(int fileId) throws RemoteException
    {
        try
        {
            System.out.println("Client " + RemoteServer.getClientHost() + " has confirmed the upload of " + fileId);
        }
        catch (ServerNotActiveException snae) { }
        unlockFile(fileId);
    }

    public Object[] requestDownload(String fileName) throws RemoteException
    {
        try
        {
            System.out.println("Download requested from " + RemoteServer.getClientHost());
        }
        catch (ServerNotActiveException snae) { }
        Integer fileId = filenameToId.get(fileName);
        if (fileId != null)
        {
            lockFile(fileId);
            DataTable data = idToData.get(fileId);
            DataServer_Interface[] dataServers = data.getServers();
            if (dataServers != null)
            {
                FileMetadata metadata = data.getMetadata();
                return new Object[]{fileId, metadata, dataServers};
            }
        }
        return null; // File Not Found
    }

    public void confirmDownload(int fileId) throws RemoteException
    {
        try
        {
            System.out.println("Client " + RemoteServer.getClientHost() + " has confirmed the download of " + fileId);
        }
        catch (ServerNotActiveException snae) { }
        unlockFile(fileId);
    }

    public FileMetadata[] getFileList() throws RemoteException
    {
        List<FileMetadata> fileList = new ArrayList<>();

        for (Map.Entry<Integer, DataTable> entry : idToData.entrySet())
        {
            DataTable data = entry.getValue();
            FileMetadata metadata = data.getMetadata();
            fileList.add(metadata);
        }

        return fileList.toArray(new FileMetadata[0]);
    }

    public int generateUniqueRandomId() {
        int id;
        do
        {
            id = random.nextInt(Integer.MAX_VALUE); // Generate a random integer ID
        } while (filenameToId.containsValue(id)); // Check if the ID already exists in the map
        return id;
    }

    private String[] selectDataServers(int fileSizeInBytes)
    {
        // Sort servers based on free space
        List<Map.Entry<String, Master_DataServerStatus>> serverEntries = new ArrayList<>(serverMap.entrySet());
        Collections.sort(serverEntries, Comparator.comparingInt(entry -> entry.getValue().getFreeSpace()));

        // Select the first three servers or until there are no more servers with enough space
        List<String> selectedServers = new ArrayList<>();
        for (Map.Entry<String, Master_DataServerStatus> entry : serverEntries)
        {
            DataServer_Interface server = entry.getValue().getServer();
            int freeSpace = entry.getValue().getFreeSpace();
            if (freeSpace >= fileSizeInBytes && selectedServers.size() < 3)
            {
                selectedServers.add(entry.getKey());
            }
        }
        return selectedServers.toArray(new String[0]);
    }


    public void lockFile(int fileId)
    {
        DataTable data = idToData.get(fileId);
        if (data != null)
        {
            Semaphore lock = data.getLock();
            try
            {
                lock.acquire(); // Acquire the lock
            }
            catch (InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void unlockFile(int fileId)
    {
        DataTable data = idToData.get(fileId);
        if (data != null)
        {
            Semaphore lock = data.getLock();
            lock.release(); // Release the lock
        }
    }
}
