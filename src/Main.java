import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class Main
{
    static private String miscDirFormat = "Misc MM_yyyy";

    public static void main(String[] args)
    {
        // TODO: replace parameter parsing with something more advanced - and thus learn how to include a module
        // TODO: get parameter for dry run / statistics dump

        if (args.length != 1)
        {
            System.err.println("Expected a one and only parameter!");
            System.exit(1);
        }

        // Get parameter with the directory path
        String baseDir = args[0];
        System.out.printf("Base directory is: %s%n", baseDir);

        File dir = new File(baseDir);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null)
        {
            for (File child : directoryListing)
            {
                if (child.isFile())
                    processFile(baseDir, child);
            }
        }
        else
        {
            System.err.println("Base directory is empty!");
            System.exit(1);
        }
    }

    private static void processFile(String baseDir, File aFile)
    {
        String fileName = aFile.getName();
        Path filePath = Path.of(aFile.getAbsolutePath());

        // Get creation date
        FileTime fileTime = FileTime.from(0, TimeUnit.DAYS);
        try
        {
            fileTime = Files.readAttributes(filePath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).lastModifiedTime();
        }
        catch (IOException e)
        {
            System.err.println("readAttributes failed!");
            System.exit(2);
        }

        // Compute target directory
        SimpleDateFormat df = new SimpleDateFormat(miscDirFormat);
        String subDirName = df.format(fileTime.toMillis());

        // mkdir the relevant month directory if needed with the format of Misc MM_YYYY
        File targetDir = new File(baseDir + "\\" + subDirName);
        if (!(targetDir.exists()))
        {
            if (!(targetDir.mkdir()))
            {
                System.err.printf("mkdir failed for '%s' ! %n", targetDir.getAbsolutePath());
                System.exit(3);
            }
        }

        // Move the file to that directory
        File newFileLocation = new File(targetDir.getAbsolutePath() + "\\" + fileName);
        if (!(aFile.renameTo(newFileLocation)))
        {
            System.err.printf("Failed to move file to '%s' ! %n", newFileLocation.getAbsolutePath());
            System.exit(4);
        }

        System.out.printf("Moved file: '%s' - to '%s' %n", fileName, newFileLocation.getAbsolutePath());
    }
}
