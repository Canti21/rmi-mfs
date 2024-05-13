import java.io.Serializable;

public class Master_DataServerStatus implements Serializable
{
    private int freeSpace;
    private boolean availability;
    private DataServer_Interface serverInterface;

    public Master_DataServerStatus(int freeSpace, DataServer_Interface serverInterface)
    {
        this.freeSpace = freeSpace;
        this.availability = true;
        this.serverInterface = serverInterface;
    }


    public int getFreeSpace()
    {
        return freeSpace;
    }

    public void setFreeSpace(int freeSpace)
    {
        this.freeSpace = freeSpace;
    }

    public boolean isAvailability()
    {
        return availability;
    }

    public void setAvailability(boolean availability)
    {
        this.availability = availability;
    }

    public DataServer_Interface getServer()
    {
        return serverInterface;
    }
}
