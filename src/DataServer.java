import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DataServer
{
    private static String serverName;
    private static int availableSpace;
    private static Registry registry;

    public static void start(String[] args) throws RemoteException
    {
        serverName = args[0];
        availableSpace = megabytesToBytes(Integer.parseInt(args[1]));
        registry = LocateRegistry.getRegistry(args[2], Integer.parseInt(args[3]));
        DataServerObject dso = new DataServerObject(availableSpace);
        registry.rebind(serverName, dso);
        System.out.println("Data server \"" + serverName + "\" READY!");
    }

    public static void main(String[] args) throws RemoteException
    {
        if (args.length >= 4)
        {
            start(args);
        }
        else
        {
            System.out.println("Usage: java DataServer <serverName> <space> <masterServerAddress> <masterServerPort>");
        }
    }

    protected static int megabytesToBytes(int megas)
    {
        return megas * 1048576;
    }
}
