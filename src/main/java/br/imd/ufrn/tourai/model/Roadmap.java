package br.imd.ufrn.tourai.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "roadmap_tags", joinColumns = @JoinColumn(name = "roadmap_id"))
    @Column
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoadmapVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column
    private ModerationStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "roadmap_activity",
            joinColumns = @JoinColumn(name = "roadmap_id"),
            inverseJoinColumns = @JoinColumn(name = "activity_id")
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

