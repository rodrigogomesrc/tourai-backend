package br.imd.ufrn.tourai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Integer rating;

    private String comment;

    @OneToOne
    @JoinColumn(name = "itinerary_activity_id", unique = true, nullable = false)
    private ItineraryActivity itineraryActivity;

    public Evaluation(Integer rating, String comment, ItineraryActivity itineraryActivity) {
        this.rating = rating;
        this.comment = comment;
        this.itineraryActivity = itineraryActivity;
    }
}
