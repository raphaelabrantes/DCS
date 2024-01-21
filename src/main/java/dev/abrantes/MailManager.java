package dev.abrantes;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MailManager {

    public static final String FILE_HEADER = "File following files were modified, please do something about this: \n";
    private static final Logger LOGGER = Logger.getLogger(FileManager.class);
    private final Mailer mailer;

    private final String email;

    private List<String> faultyFiles = new ArrayList<>();


    @Inject
    public MailManager(Mailer mailer, @ConfigProperty(name = "quarkus.mailer.from") String email) {
        this.mailer = mailer;
        this.email = email;
    }


    public void sendFileChanged(FileInfo fileInfo) {
        if (!fileInfo.isUserNotified()) {
            faultyFiles.add(fileInfo.getFile().getAbsolutePath());
            fileInfo.setNotified(true);
        }
    }

    public void sendInvalidFolder() {

    }

    public void sendEmailIfNecessary() {
        if (faultyFiles.isEmpty()) {
            return;
        }
        var mail = new Mail();
        mail.addTo(email);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(FILE_HEADER);
        faultyFiles.forEach((String filePath) -> {
            stringBuilder
                    .append(filePath)
                    .append(System.lineSeparator());
        });
        mail.setText(stringBuilder.toString());
        mail.setSubject("DCS: FILE(s) WITH PROBLEMS REQUIRE YOUR ATTENTION");
        mailer.send(mail);
        faultyFiles.clear();
        LOGGER.info("Email was sent related to fail files");
    }
}