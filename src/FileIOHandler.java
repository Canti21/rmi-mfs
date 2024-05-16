import java.io.*;

public class FileIOHandler
{
    public static synchronized void writeObjectToFile(Object obj, String file)
    {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file)))
        {
            outputStream.writeObject(obj);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static Object readObjectFromFile(String file)
    {
        Object obj = null;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file)))
        {
            obj = objectInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException ex)
        {
            ex.printStackTrace();
        }
        return obj;
    }

    public static void writeByteArrayToFile(byte[] byteArray, String filename)
    {
        try (FileOutputStream outputStream = new FileOutputStream(filename))
        {
            outputStream.write(byteArray);
            System.out.println("Byte array written to file successfully.");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static byte[] readByteArrayFromFile(String filename)
    {
        byte[] byteArray = null;
        try (FileInputStream inputStream = new FileInputStream(filename))
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, length);
            }
            byteArray = outputStream.toByteArray();
            System.out.println("Byte array read from file successfully.");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return byteArray;
    }
}
