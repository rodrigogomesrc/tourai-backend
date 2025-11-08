package br.imd.ufrn.tourai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
public class FavoriteRoadmap {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "roteiro_id")
    private Roteiro roadmap;

}
