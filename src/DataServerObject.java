import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

public class DataServerObject extends UnicastRemoteObject implements DataServer_Interface
{
    private int totalSpace;
    private int availableSpace;
    private FileIOHandler fileIOHandler;
    private Master_ServerInterface masterServer;
    private String serverName;

    public DataServerObject(int totalSpace, Master_ServerInterface masterServer, String serverName) throws RemoteException
    {
        super();
        this.availableSpace = totalSpace;
        this.fileIOHandler = new FileIOHandler();
        this.masterServer = masterServer;
        this.serverName = serverName;
    }

    @Override
    public long uploadFile(int fileId, DataServer_Interface replicaServer1, DataServer_Interface replicaServer2, byte[] file) throws RemoteException
    {
        System.out.println("File Upload with id " + fileId + " Started");
        if (availableSpace >= file.length)
        {
            String fileName = "" + fileId;
            fileIOHandler.writeByteArrayToFile(file, fileName);
            System.out.println("\tFile stored successfully");
            availableSpace -= file.length;
            long localChecksum = calculateChecksum(file);
            long replicaChecksum = -1;
            int replicaUploadTryCounter = 0;
            // Upload replicas to replica servers if available
            if (replicaServer1 != null || replicaServer2 != null)
                System.out.println("\tSending replicas");
            else
                System.out.println("\tNo replica servers found");
            if (replicaServer1 != null)
            {
                System.out.println("\tSending replica to \"" + replicaServer1.getName() + "\"");
                while (replicaUploadTryCounter < 3)
                {
                    replicaChecksum = replicaServer1.uploadReplica(fileId, file);
                    if(localChecksum == replicaChecksum)
                    {
                        System.out.println("\tSuccessful replica upload to \"" + replicaServer1.getName() + "\"");
                        break;
                    }
                    else
                    {
                        replicaUploadTryCounter++;
                    }
                }
                if (replicaUploadTryCounter >= 3)
                    System.out.println("\tThere was an error in replica upload to \"" + replicaServer1.getName() + "\"");
            }
            replicaUploadTryCounter = 0;
            if (replicaServer2 != null)
            {
                System.out.println("\tSending replica to \"" + replicaServer2.getName() + "\"");
                while (replicaUploadTryCounter < 3)
                {
                    replicaChecksum = replicaServer2.uploadReplica(fileId, file);
                    if(localChecksum == replicaChecksum)
                    {
                        System.out.println("\tSuccessful replica upload to \"" + replicaServer2.getName() + "\"");
                        break;
                    }
                    else
                    {
                        replicaUploadTryCounter++;
                    }
                }
                if (replicaUploadTryCounter >= 3)
                    System.out.println("\tThere was an error in replica upload to \"" + replicaServer2.getName() + "\"");
            }
            masterServer.confirmUpload(fileId);
            return localChecksum;
        }
        else
        {
            throw new RemoteException("\tInsufficient space on the server to upload the file.");
        }
    }

    @Override
    public long uploadReplica(int fileId, byte[] file) throws RemoteException
    {
        System.out.println("Receiving replica upload of file with id " + fileId);
        if (availableSpace >= file.length)
        {
            String fileName = "" + fileId;
            fileIOHandler.writeByteArrayToFile(file, fileName);
            System.out.println("\tFile stored successfully");
            availableSpace -= file.length;
            return calculateChecksum(file);
        }
        else
        {
            throw new RemoteException("\tInsufficient space on the server to upload the replica.");
        }
    }

    @Override
    public Object[] downloadFile(int fileId) throws RemoteException
    {
        System.out.println("Download of file with ID " + fileId + " started");
        String fileName = "" + fileId;
        byte[] file = fileIOHandler.readByteArrayFromFile(fileName);
        if (file != null)
        {
            long checksum = calculateChecksum(file);
            System.out.println("\tFile downloaded");
            return new Object[]{checksum, file};
        }
        else
        {
            throw new RemoteException("\tFile with the specified ID not found on the server.");
        }
    }

    @Override
    public int heartBeat() throws RemoteException
    {
        return availableSpace;
    }
    public String getName() throws RemoteException
    {
        return serverName;
    }

    public long calculateChecksum(byte[] data) throws RemoteException
    {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return crc32.getValue();
    }
}



