import java.io.File;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.zip.CRC32;

public class Client {
    private Master_ServerInterface masterServer;

    public Client(String host, int port)
    {
        try
        {
            Registry registry = LocateRegistry.getRegistry(host, port);
            masterServer = (Master_ServerInterface) registry.lookup("masterServer");
        }
        catch (Exception e)
        {
            System.err.println("Cliente exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void uploadFile() throws Exception
    {
        Scanner scanner = new Scanner(System.in);
        //subir la ruta del archivo a subir
        System.out.print("Enter the full path of the file to upload: ");
        String filePath = scanner.nextLine();

        File file = new File(filePath);
        byte[] fileData = Files.readAllBytes(file.toPath());
        String fileName = file.getName();

        FileMetadata metadata = new FileMetadata(fileName, "2024-05-14", fileData.length);
        Object[] fileMetadata = new Object[]{metadata};

        Object[] response = masterServer.requestUpload(fileMetadata);
         if (response != null && response.length > 0)
         {
             int fileId = (int) response[0];
             DataServer_Interface[] servers = (DataServer_Interface[]) response[1];

             int tryCounter = 0;

             while (tryCounter < 3)
             {
                 if (servers.length > 0)
                 {
                     // Use the first server as primary
                     DataServer_Interface primaryServer = servers[0];
                     long returnedChecksum = primaryServer.uploadReplica(fileId, fileData);
                     long originalChecksum = calculateChecksum(fileData);
                     if (returnedChecksum == originalChecksum)
                     {
                         masterServer.confirmUpload(fileId);
                         System.out.println("File uploaded successfully.");
                     }
                     else
                     {
                         System.out.println("Error in file upload, retrying... (" + (tryCounter + 1) + "/3)");
                         tryCounter++;
                     }
                 }
                 else
                 {
                     System.out.println("No servers available for upload.");
                     tryCounter = 4;
                 }
             }
         }
    }


    public void downloadFile() throws Exception
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the file name to download: ");
        String fileName = scanner.nextLine();

        Object[] downloadInfo = masterServer.requestDownload(fileName);
        if (downloadInfo != null)
        {
            int fileId = (int) downloadInfo[0];
            FileMetadata metadata = (FileMetadata) downloadInfo[1];
            DataServer_Interface[] servers = (DataServer_Interface[]) downloadInfo[2];
            
            DataServer_Interface server = servers[0];
            Object[] fileData = server.downloadFile(fileId);
            masterServer.confirmDownload(metadata.getFileSizeInBytes());
            System.out.println("Downloaded file content: " + new String((byte[]) fileData[1]));
        }
        else
        {
            System.out.println("File not found.");
        }
    }

    public void showFiles() throws Exception {
        FileMetadata[] files = masterServer.getFileList();
        if (files.length > 0) {
            System.out.println("Files on the server:");
            for (FileMetadata file : files) {
                System.out.println("- " + file.getFileName());
            }
        } else {
            System.out.println("No files available on the server.");
        }
    }

    public void startClient() {
        Scanner scanner = new Scanner(System.in);
        boolean cycle = true;
        while (cycle)
        {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Upload File");
            System.out.println("2. Download File");
            System.out.println("3. Show Files on Server");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            try
            {
                switch (choice)
                {
                    case 1:
                        uploadFile();
                        break;
                    case 2:
                        downloadFile();
                        break;
                    case 3:
                        showFiles();
                        break;
                    case 0:
                        System.out.println("Exiting...");
                        System.exit(0);
                        cycle = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 1, 2, 3, or 0.");
                }
            }
            catch (IllegalMonitorStateException imse) { }
            catch (Exception e)
            {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
                cycle = false;
            }
        }
    }

    private long calculateChecksum(byte[] data)
    {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return crc32.getValue();
    }

    public static void main(String[] args) {
        try {
            /*//localhost
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter server host: ");
            String host = scanner.nextLine();
            //1099
            System.out.print("Enter server port: ");
            int port = scanner.nextInt();
            scanner.nextLine();*/

            Client client = new Client("localhost", 1099);
            client.startClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
