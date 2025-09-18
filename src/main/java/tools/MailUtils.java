package tools;
import org.apache.log4j.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class MailUtils {
    private static final Logger log = Logger.getLogger(MailUtils.class);

    // --- SMTP ---
    private static final String HOST     = "smtp.gmail.com";
    private static final String PORT     = "587";
    private static final String USERNAME = "revecouture1990@gmail.com";
    private static final String PASSWORD = "qcukddisvzhbkdfb";

    private MailUtils() {}

    private static Properties baseProps() {
        Properties p = new Properties();
        p.put("mail.transport.protocol", "smtp");
        p.put("mail.smtp.host", HOST);
        p.put("mail.smtp.port", PORT);
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.starttls.required", "true");
        p.put("mail.smtp.ssl.trust", HOST);
        p.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return p;
    }

    private static Session newSession() {
        return Session.getInstance(baseProps(), new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    /** text email (pour reinitialisation password). */
    public static void sendText(String destEmail, String subject, String bodyText) {
        try {
            Session session = newSession();

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(USERNAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destEmail, false));
            msg.setSubject(subject, "UTF-8");
            msg.setText(bodyText, "UTF-8");

            Transport.send(msg);
            log.info("Mail texte envoyé à " + destEmail);
        } catch (Exception e) {
            log.error("Échec envoi mail texte vers " + destEmail, e);
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null) {
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Impossible d'envoyer l'e-mail.",
                        "L'opération a été réalisée, mais l'e-mail n'a pas pu être envoyé."));
            }
        }
    }

    /** Email avec pdf*/
    public static void sendWithAttachment(String destEmail, String subject, String bodyText, String absolutePath) {
        try {
            Path file = Paths.get(absolutePath);
            if (!Files.isReadable(file)) {
                throw new java.io.FileNotFoundException("Fichier introuvable: " + absolutePath);
            }

            Session session = newSession();

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(USERNAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destEmail, false));
            msg.setSubject(subject, "UTF-8");

            MimeBodyPart text = new MimeBodyPart();
            text.setText(bodyText, "UTF-8");

            MimeBodyPart attach = new MimeBodyPart();
            DataSource ds = new FileDataSource(file.toFile());
            attach.setDataHandler(new DataHandler(ds));
            attach.setFileName(file.getFileName().toString());

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(text);
            mp.addBodyPart(attach);
            msg.setContent(mp);

            Transport.send(msg);
            log.info("Mail + PJ envoyé à " + destEmail + " (" + absolutePath + ")");
        } catch (Exception e) {
            log.error("Échec envoi mail + PJ vers " + destEmail + " (" + absolutePath + ")", e);
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null) {
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Envoi du mail échoué.",
                        "Vous pouvez télécharger le document depuis l’application."));
            }
        }
    }

    @Deprecated
    public static void sendMessage(String mailDest, String Texte, String Titre) {
        // --- inline SMTP config ---
        final String host     = "smtp.gmail.com";
        final String port     = "587";
        final String username = "revecouture1990@gmail.com";
        final String password = "qcukddisvzhbkdfb";

        try {
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(username));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailDest, false));
            msg.setSubject(Titre, "UTF-8");
            msg.setText(Texte, "UTF-8"); // texte simple (pas HTML)

            Transport.send(msg);
            log.info("Mail envoyé à " + mailDest);
        } catch (Exception e) {
            log.error("Échec d’envoi e-mail vers " + mailDest, e);
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null) {
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Réinitialisation effectuée, mais l’envoi du mail a échoué.",
                        "Veuillez contacter un administrateur pour récupérer le mot de passe."
                ));
            }
        }
    }

    // Méthode qui permet l'envoi d'un mail via le mail du magasin avec la facture
    @Deprecated
    public static void sendMessage(String absolutePdfPath,
                                   String destEmail,
                                   String bodyText,
                                   String subject) {
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "revecouture1990@gmail.com";
        String password = "qcukddisvzhbkdfb";




        try {

            Path p = Paths.get(absolutePdfPath);
            if (!Files.isReadable(p)) {
                throw new FileNotFoundException("PDF introuvable: " + absolutePdfPath);
            }
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props,
                    new Authenticator() {
                        @Override protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(username));
            msg.setRecipients(MimeMessage.RecipientType.TO,
                    InternetAddress.parse(destEmail, false));
            msg.setSubject(subject, "UTF-8");

            MimeBodyPart text = new MimeBodyPart();
            text.setText(bodyText, "UTF-8");

            MimeBodyPart attach = new MimeBodyPart();
            DataSource src = new FileDataSource(absolutePdfPath);
            attach.setDataHandler(new DataHandler(src));
            attach.setFileName(new File(absolutePdfPath).getName());

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(text);
            mp.addBodyPart(attach);
            msg.setContent(mp);

            Transport.send(msg);
            log.info("Mail sent to " + destEmail + " with " + absolutePdfPath);
        } catch (Exception e) {
            log.error("Email send failed to " + destEmail + " (file: " + absolutePdfPath + ")", e);
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null) {
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Facture créée mais l’envoi du mail a échoué.",
                        "Vous pouvez télécharger la facture depuis l’application." ));
            }
        }
    }
}

