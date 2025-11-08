package br.imd.ufrn.tourai.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "Roteiro")
@Table(name = "roteiro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString // removed exclude parameter; use field-level @ToString.Exclude
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "titulo", nullable = false)
    private String title;

    @Lob
    @Column(name = "descricao")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "roteiro_tags", joinColumns = @JoinColumn(name = "roteiro_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidade", nullable = false)
    private RoadmapVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ModerationStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "roteiro_atividade",
            joinColumns = @JoinColumn(name = "roteiro_id"),
            inverseJoinColumns = @JoinColumn(name = "atividade_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Activity> activities = new HashSet<>();

    public void addActivity(Activity activity) {
        this.activities.add(activity);
        activity.getRoadmaps().add(this);
    }

    public void removeActivity(Activity activity) {
        this.activities.remove(activity);
        activity.getRoadmaps().remove(this);
    }
}

