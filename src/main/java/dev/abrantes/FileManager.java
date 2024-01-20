package dev.abrantes;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
    private final File folder;

    private final Map<String, String> fileHashs = new HashMap<>();

    @Inject
    public FileManager(@ConfigProperty(name = "watchdir") String folderPath) throws FileNotFoundException {
        folder = new File(folderPath);
        if (isInValidFolder()) {
            var msg = folderPath + FOLDER_NOT_FOUND_EX;
            throw new FileNotFoundException(msg);
        }

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
        }
        var files = folder.listFiles();
        verifyFiles(files);
    }

    private void verifyFiles(File[] files) {
        for (File file : files) {
            try {
                var hash = getFileHash(file);
                if (fileHashs.containsKey(file.getAbsolutePath())) {
                    var oldHash = fileHashs.get(file.getAbsolutePath());
                    if (!Objects.equals(oldHash, hash)) {
                        // TODO: SEND EMAIL AND SYNC FILE?
                    } else {
                        // TODO: SEND EMAIL TELLING ABOUT NEW FILE?
                        fileHashs.put(file.getAbsolutePath(), hash);
                    }

                }

            } catch (IOException | NoSuchAlgorithmException ex) {
                // TODO: SEND EMAIL
            }
        }
    }

    boolean isInValidFolder() {
        return !(folder.exists() && folder.isDirectory());
    }

}
