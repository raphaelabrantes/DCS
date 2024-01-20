package dev.abrantes;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class FileManager {

    private final File folder;

    @Inject
    public FileManager(@ConfigProperty(name = "watchdir") String folderPath) throws FileNotFoundException {
        folder = new File(folderPath);
        if(!(folder.exists() && folder.isDirectory())){
            var msg = folderPath + " folder not found, check if folder exists and is a directory";
            throw new FileNotFoundException(msg);
        };
    }

    @Scheduled(every = "${synctime}", timeZone = "utc", identity="FileManager", concurrentExecution = Scheduled.ConcurrentExecution.SKIP )
    void schedule(){
        System.out.printf("a");
    }

}
