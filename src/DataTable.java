import java.io.Serializable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class DataTable implements Serializable
{
    private FileMetadata metadata;
    private DataServer_Interface[] servers;
    private Semaphore semaphore;

    public DataTable(FileMetadata metadata, DataServer_Interface[] servers)
    {
        this.metadata = metadata;
        this.servers = servers;
        this.semaphore = new Semaphore(1);
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

    public Semaphore getLock()
    {
        return semaphore;
    }
}
