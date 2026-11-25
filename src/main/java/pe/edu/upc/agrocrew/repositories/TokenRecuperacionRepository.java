package pe.edu.upc.agrocrew.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.agrocrew.models.TokenRecuperacion;

import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);

    /** Al pedir un enlace nuevo, los anteriores del usuario dejan de servir. */
    @Modifying
    @Query("UPDATE TokenRecuperacion t SET t.usado = true WHERE t.usuario.id = :usuarioId AND t.usado = false")
    int invalidarPendientes(@Param("usuarioId") Long usuarioId);
}
