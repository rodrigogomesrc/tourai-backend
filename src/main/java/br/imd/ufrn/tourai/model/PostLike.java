package br.imd.ufrn.tourai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
public class PostLike {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User liker;

    @Setter
    @Getter
    private Instant date;

}
