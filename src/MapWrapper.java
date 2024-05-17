import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MapWrapper implements Serializable
{
    private ConcurrentHashMap<String, Integer> fileToId;
    private ConcurrentHashMap<Integer, DataTable> idToData;
    private ConcurrentHashMap<Integer, String[]> idToServerNames;
    private ConcurrentMap<String, Master_DataServerStatus> serverMap;

    public MapWrapper(ConcurrentHashMap<String, Integer> fileToId,
                      ConcurrentHashMap<Integer, DataTable> idToData,
                      ConcurrentHashMap<Integer, String[]> idToServerNames,
                      ConcurrentMap<String, Master_DataServerStatus> serverMap)
    {
        this.fileToId = fileToId;
        this.idToData = idToData;
        this.idToServerNames = idToServerNames;
        this.serverMap = serverMap;
    }

    public ConcurrentMap<String, Master_DataServerStatus> getServerMap() { return serverMap; }

    public ConcurrentHashMap<String, Integer> getFileToId()
    {
        return fileToId;
    }

    public ConcurrentHashMap<Integer, DataTable> getIdToData()
    {
        return idToData;
    }

    public ConcurrentHashMap<Integer, String[]> getIdToServerNames() { return idToServerNames; }
}
