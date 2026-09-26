package pe.edu.upc.agrocrew.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "distritos")
@Getter
@Setter
@NoArgsConstructor
public class Distrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;

    @Column(nullable = false, unique = true, length = 6)
    private String ubigeo;

    @Column(nullable = false, length = 80)
    private String nombre;

    /** Coordenadas y altitud de la capital del distrito (pueden ser nulas en algunos distritos). */
    private Double latitud;

    private Double longitud;

    private Integer altitudMsnm;
}
