import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

public class DataServerObject extends UnicastRemoteObject implements DataServer_Interface
{
    private int totalSpace;
    private int availableSpace;
    private FileIOHandler fileIOHandler;

    public DataServerObject(int totalSpace) throws RemoteException
    {
        super();
        this.availableSpace = totalSpace;
        this.fileIOHandler = new FileIOHandler();
    }

    @Override
    public long uploadFile(int fileId, DataServer_Interface replicaServer1, DataServer_Interface replicaServer2, byte[] file) throws RemoteException
    {
        if (availableSpace >= file.length)
        {
            String fileName = "" + fileId;
            fileIOHandler.writeByteArrayToFile(file, fileName);
            availableSpace -= file.length;
            // Upload replicas to replica servers if available
            if (replicaServer1 != null && replicaServer2 != null)
            {
                replicaServer1.uploadReplica(fileId, file);
                replicaServer2.uploadReplica(fileId, file);
            }
            return calculateChecksum(file);
        }
        else
        {
            throw new RemoteException("Insufficient space on the server to upload the file.");
        }
    }

    @Override
    public long uploadReplica(int fileId, byte[] file) throws RemoteException
    {
        if (availableSpace >= file.length)
        {
            String fileName = "" + fileId;
            fileIOHandler.writeByteArrayToFile(file, fileName);
            availableSpace -= file.length;
            return calculateChecksum(file);
        }
        else
        {
            throw new RemoteException("Insufficient space on the server to upload the replica.");
        }
    }

    @Override
    public Object[] downloadFile(int fileId) throws RemoteException
    {
        String fileName = "" + fileId;
        byte[] file = fileIOHandler.readByteArrayFromFile(fileName);
        if (file != null)
        {
            long checksum = calculateChecksum(file);
            return new Object[]{checksum, file};
        }
        else
        {
            throw new RemoteException("File with the specified ID not found on the server.");
        }
    }

    @Override
    public int heartBeat() throws RemoteException
    {
        return availableSpace;
    }

    public long calculateChecksum(byte[] data) throws RemoteException
    {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return crc32.getValue();
    }
}



