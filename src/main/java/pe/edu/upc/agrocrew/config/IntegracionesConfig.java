package pe.edu.upc.agrocrew.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Cliente HTTP compartido por las integraciones, activación de tareas programadas
 * y formato estable para respuestas paginadas.
 */
@Configuration
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class IntegracionesConfig {

    @Bean
    public RestClient restClient(@Value("${app.integraciones.timeout-segundos:15}") int timeoutSegundos) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "AgroCrew/1.0 (proyecto academico UPC)")
                .build();
    }
}
