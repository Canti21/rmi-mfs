public interface DataServer_Interface
{
    int uploadFile(int fileId, DataServer_Interface replicaServer1, DataServer_Interface replicaServer2, byte[] file);
    int uploadReplica(int fileId, byte[] file);
    Object[] downloadFile(int fileId);
    int heartBeat();
}
