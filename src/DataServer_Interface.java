import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataServer_Interface extends Remote
{
    long uploadFile(int fileId, DataServer_Interface replicaServer1, DataServer_Interface replicaServer2, byte[] file) throws RemoteException;
    long uploadReplica(int fileId, byte[] file) throws RemoteException;
    Object[] downloadFile(int fileId) throws RemoteException;
    int heartBeat() throws RemoteException;
    long calculateChecksum(byte[] data) throws RemoteException;
}
