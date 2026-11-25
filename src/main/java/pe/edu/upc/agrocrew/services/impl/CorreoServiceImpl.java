package pe.edu.upc.agrocrew.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pe.edu.upc.agrocrew.services.CorreoService;

/**
 * Envía los correos por SMTP. Spring Boot solo crea el JavaMailSender si se define SPRING_MAIL_HOST;
 * sin esa variable (desarrollo local) el enlace se escribe en el log para poder probar el flujo.
 */
@Slf4j
@Service
public class CorreoServiceImpl implements CorreoService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remitente;

    public CorreoServiceImpl(ObjectProvider<JavaMailSender> mailSender,
                             @Value("${app.correo.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviarEnlaceRecuperacion(String email, String nombres, String enlace, int minutosValidez) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("Correo no configurado (SPRING_MAIL_HOST). Enlace de restablecimiento para {}: {}", email, enlace);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("AgroCrew: restablece tu contraseña");
        mensaje.setText("Hola " + nombres + ":\n\n"
                + "Recibimos un pedido para restablecer la contraseña de tu cuenta de AgroCrew.\n"
                + "Para elegir una nueva, abre este enlace:\n\n" + enlace + "\n\n"
                + "El enlace vence en " + minutosValidez + " minutos y solo se puede usar una vez.\n"
                + "Si no pediste este cambio, puedes ignorar este correo: tu contraseña no cambiará.\n\n"
                + "Equipo AgroCrew");
        try {
            sender.send(mensaje);
            log.info("Correo de recuperación enviado a {}", email);
        } catch (MailException e) {
            // No se informa al usuario: la respuesta es la misma exista o no la cuenta.
            log.error("No se pudo enviar el correo de recuperación a {}: {}", email, e.getMessage());
        }
    }
}
