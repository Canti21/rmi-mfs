import java.util.concurrent.ConcurrentHashMap;

public class MapWrapper
{
    private ConcurrentHashMap<String, Integer> fileToId;
    private ConcurrentHashMap<Integer, DataTable> idToData;

    public MapWrapper(ConcurrentHashMap<String, Integer> fileToId, ConcurrentHashMap<Integer, DataTable> idToData)
    {
        this.fileToId = fileToId;
        this.idToData = idToData;
    }

    public ConcurrentHashMap<String, Integer> getFileToId()
    {
        return fileToId;
    }

    public ConcurrentHashMap<Integer, DataTable> getIdToData()
    {
        return idToData;
    }
}
