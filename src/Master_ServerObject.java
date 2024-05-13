import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public class Master_ServerObject extends UnicastRemoteObject implements Master_ServerInterface
{
    private ConcurrentMap<String, Integer> filenameToId;
    private ConcurrentMap<Integer, DataTable> idToData;
    private ConcurrentMap<String, Master_DataServerStatus> serverMap;

    public Master_ServerObject(ConcurrentMap<String, Integer> filenameToId, ConcurrentMap<Integer, DataTable> idToData, ConcurrentMap<String, Master_DataServerStatus> serverMap) throws RemoteException
    {
        this.filenameToId = filenameToId;
        this.idToData = idToData;
        this.serverMap = serverMap;
    }

    public DataServer_Interface[] requestUpload(Object[] fileMetadata) throws RemoteException
    {
        FileMetadata metadata = (FileMetadata) fileMetadata[0];

        // Get file name, modified date and file size
        String fileName = metadata.getFileName().toLowerCase();

        Integer existingFileId = filenameToId.get(fileName);
        if (existingFileId != null)
        {
            lockFile(existingFileId);
            DataTable data = idToData.get(existingFileId);
            return data.getServers();
        }

        int fileSizeInBytes = metadata.getFileSizeInBytes();
        // Generate Unique File ID
        int fileId = generateUniqueRandomId();

        filenameToId.put(fileName, fileId);
        DataServer_Interface[] selectedDataServers = selectDataServers(fileSizeInBytes);

        DataTable dataTable = new DataTable(metadata, selectedDataServers);

        idToData.put(fileId, dataTable);

        return selectedDataServers;
    }

    public void confirmUpload(int fileId) throws RemoteException
    {
        unlockFile(fileId);
    }

    public Object[] requestDownload(String fileName) throws RemoteException
    {
        Integer fileId = filenameToId.get(fileName);
        if (fileId != null)
        {
            lockFile(fileId);
            DataTable data = idToData.get(fileId);
            DataServer_Interface[] dataServers = data.getServers();
            if (dataServers != null)
            {
                FileMetadata metadata = data.getMetadata();
                return new Object[]{metadata, dataServers};
            }
        }
        return null; // File Not Found
    }

    public void confirmDownload(int fileId) throws RemoteException
    {
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
        Random random = new Random();
        int id;
        do {
            id = random.nextInt(Integer.MAX_VALUE); // Generate a random integer ID
        } while (filenameToId.containsValue(id)); // Check if the ID already exists in the map
        return id;
    }

    private DataServer_Interface[] selectDataServers(int fileSizeInBytes)
    {
        // Sort servers based on free space
        List<Map.Entry<String, Master_DataServerStatus>> serverEntries = new ArrayList<>(serverMap.entrySet());
        Collections.sort(serverEntries, Comparator.comparingInt(entry -> entry.getValue().getFreeSpace()));

        // Select the first three servers
        List<DataServer_Interface> selectedServers = new ArrayList<>();
        for (int i = 0; i < Math.min(3, serverEntries.size()); i++)
        {
            selectedServers.add(serverEntries.get(i).getValue().getServer());
        }
        return selectedServers.toArray(new DataServer_Interface[0]);
    }

    public void lockFile(int fileId)
    {
        DataTable data = idToData.get(fileId);
        if (data != null)
        {
            Lock lock = data.getLock();
            lock.lock(); // Acquire the lock
        }
    }

    public void unlockFile(int fileId)
    {
        DataTable data = idToData.get(fileId);
        if (data != null)
        {
            Lock lock = data.getLock();
            lock.unlock(); // Release the lock
        }
    }
}
