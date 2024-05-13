import java.io.Serializable;

public class FileMetadata implements Serializable {
    private String fileName;
    private String modifiedDate;
    private int fileSizeInBytes;

    public FileMetadata(String fileName, String modifiedDate, int fileSizeInBytes) {
        this.fileName = fileName;
        this.modifiedDate = modifiedDate;
        this.fileSizeInBytes = fileSizeInBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public int getFileSizeInBytes() {
        return fileSizeInBytes;
    }
}