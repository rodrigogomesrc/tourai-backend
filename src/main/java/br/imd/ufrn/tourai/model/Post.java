package br.imd.ufrn.tourai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.Instant;

@Entity
public class Post {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter
    @Setter
    private String content;

    @Formula("(SELECT COUNT(pl.id) FROM post_like pl WHERE pl.post_id = id)")
    @Getter
    private Integer totalLikes;

    @Formula("(SELECT COUNT(c.id) FROM comment c WHERE c.post_id = id)")
    @Getter
    private Integer totalComments;

    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Setter
    @Getter
    private String mediaUrl;

    @Setter
    @Getter
    private Instant postDate;

}
