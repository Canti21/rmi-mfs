import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class DataServer extends UnicastRemoteObject implements DataServer_Interface {
    private String serverName;
    private int totalSpace;
    private int availableSpace;
    private Map<Integer, byte[]> fileStorage;

    public DataServer(String serverName, int totalSpace) throws RemoteException {
        super();
        this.serverName = serverName;
        this.totalSpace = totalSpace;
        this.availableSpace = totalSpace;
        this.fileStorage = new HashMap<>();
    }

    @Override
    public int uploadFile(int fileId, DataServer_Interface replicaServer1, DataServer_Interface replicaServer2, byte[] file) throws RemoteException {
        if (availableSpace >= file.length) {
            fileStorage.put(fileId, file);
            availableSpace -= file.length;
            // Upload replicas to replica servers if available
            if (replicaServer1 != null && replicaServer2 != null) {
                replicaServer1.uploadReplica(fileId, file);
                replicaServer2.uploadReplica(fileId, file);
            }
            return calculateChecksum(file);
        } else {
            throw new RemoteException("Insufficient space on the server to upload the file.");
        }
    }

    @Override
    public int uploadReplica(int fileId, byte[] file) throws RemoteException {
        if (!fileStorage.containsKey(fileId)) {
            fileStorage.put(fileId, file);
            return calculateChecksum(file);
        } else {
            throw new RemoteException("File replica with the same ID already exists on the server.");
        }
    }

    @Override
    public Object[] downloadFile(int fileId) throws RemoteException {
        byte[] file = fileStorage.get(fileId);
        if (file != null) {
            int checksum = calculateChecksum(file);
            return new Object[]{checksum, file};
        } else {
            throw new RemoteException("File with the specified ID not found on the server.");
        }
    }

    @Override
    public int heartBeat() throws RemoteException {
        return availableSpace;
    }

    private int calculateChecksum(byte[] data) {
        // Placeholder for checksum calculation logic
        return data.length; // Dummy checksum (length of data)
    }
}



