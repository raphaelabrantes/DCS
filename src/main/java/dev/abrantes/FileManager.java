package dev.abrantes;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class FileManager {

    public static final String FOLDER_NOT_FOUND_EX = " folder not found, check if folder exists and is a directory";
    public static final String FILE_WAS_MODIFIED_WARN = "file %s is different:  %s != %s %n";
    public static final String FILE_FIX_INFO = "File %s was fixed";
    private static final Logger LOG = Logger.getLogger(FileManager.class);
    private final File folder;
    private final Map<String, FileInfo> fileHashs = new HashMap<>();
    private final MailManager manager;

    @Inject
    public FileManager(@ConfigProperty(name = "watchdir") String folderPath, MailManager manager) throws FileNotFoundException {
        folder = new File(folderPath);
        if (isInValidFolder()) {
            var msg = folderPath + FOLDER_NOT_FOUND_EX;
            throw new FileNotFoundException(msg);
        }
        this.manager = manager;

    }

    private static String getFileHash(File file) throws IOException, NoSuchAlgorithmException {
        byte[] data = Files.readAllBytes(file.toPath());
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        return new BigInteger(1, hash).toString(16);
    }

    @Scheduled(every = "${synctime}", timeZone = "utc", identity = "FileManager", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void schedule() {
        if (isInValidFolder()) {
            //TODO: SEND EMAIL THAT FOLDER WAS DELETED
            manager.sendInvalidFolder();
        }
        var files = folder.listFiles();
        verifyFiles(files);
    }

    private void verifyFiles(File[] files) {
        for (File file : files) {
            try {
                var hash = getFileHash(file);
                var path = file.getAbsolutePath();
                if (fileHashs.containsKey(path)) {
                    var fileInfo = fileHashs.get(path);
                    if (!Objects.equals(fileInfo.getHash(), hash)) {
                        // TODO: SEND EMAIL AND SYNC FILE (reach consensus and download)?
                        manager.sendFileChanged(fileInfo); //
                        LOG.warn(String.format(FILE_WAS_MODIFIED_WARN, path, fileInfo.getHash(), hash));
                        continue;
                    }
                    if (fileInfo.isUserNotified()) {
                        fileInfo.setNotified(false);
                        LOG.info(String.format(FILE_FIX_INFO, file.getAbsoluteFile()));
                    }
                } else {
                    // TODO: SEND EMAIL TELLING ABOUT NEW FILE?
                    var fileInfo = new FileInfo(file, hash);
                    fileHashs.put(file.getAbsolutePath(), fileInfo);
                }

            } catch (IOException | NoSuchAlgorithmException ex) {
                // TODO: SEND EMAIL
                LOG.error("Fail ", ex);
            }
        }
        manager.sendEmailIfNecessary();
    }

    boolean isInValidFolder() {
        return !(folder.exists() && folder.isDirectory());
    }

}
