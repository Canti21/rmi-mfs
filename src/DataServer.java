import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class DataServer
{
    private static String serverName;
    private static int availableSpace;
    private static Registry registry;
    private static Master_ServerInterface masterServer;

    public static void start(String[] args) throws RemoteException
    {
        try
        {
            serverName = args[0];
            availableSpace = megabytesToBytes(Integer.parseInt(args[1])) - calculateFolderSize(new File("."));
            registry = LocateRegistry.getRegistry(args[2], Integer.parseInt(args[3]));
            masterServer = (Master_ServerInterface) registry.lookup("masterServer");
            DataServerObject dso = new DataServerObject(availableSpace, masterServer, serverName);
            registry.rebind(serverName, dso);
            System.out.println("Data server \"" + serverName + "\" READY!");
        }
        catch (Exception re)
        {
            re.printStackTrace();
        }
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

    public static int calculateFolderSize(File folder)
    {
        if (!folder.isDirectory())
        {
            return (int) folder.length();
        }

        int totalSize = 0;
        File[] files = folder.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                totalSize += calculateFolderSize(file);
            }
        }
        return totalSize;
    }
}
