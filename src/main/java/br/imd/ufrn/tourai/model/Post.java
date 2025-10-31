package br.imd.ufrn.tourai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Post {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PostLike> postLikes = new ArrayList<>();

    @Setter
    @Getter
    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Setter
    @Getter
    private String mediaUrl;

    @Setter
    @Getter
    private Instant postDate;

    public int getTotalLikes() {
        return postLikes != null ? postLikes.size() : 0;
    }

}
