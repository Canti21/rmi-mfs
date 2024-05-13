import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

public class DataTable implements Serializable
{
    private FileMetadata metadata;
    private DataServer_Interface[] servers;
    private ReentrantLock lock;

    public DataTable(FileMetadata metadata, DataServer_Interface[] servers)
    {
        this.metadata = metadata;
        this.servers = servers;
        this.lock = new ReentrantLock();
    }

    public FileMetadata getMetadata()
    {
        return metadata;
    }

    public void setMetadata(FileMetadata metadata)
    {
        this.metadata = metadata;
    }

    public DataServer_Interface[] getServers()
    {
        return servers;
    }

    public void setServers(DataServer_Interface[] servers)
    {
        this.servers = servers;
    }

    public ReentrantLock getLock()
    {
        return lock;
    }
}
