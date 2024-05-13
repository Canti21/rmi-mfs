import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Master_ServerInterface extends Remote
{
    DataServer_Interface[] requestUpload(Object[] fileMetadata) throws RemoteException;
    void confirmUpload(int fileId) throws RemoteException;
    Object[] requestDownload(String fileName) throws RemoteException;
    void confirmDownload(int fileId) throws RemoteException;
    FileMetadata[] getFileList() throws RemoteException;
}
