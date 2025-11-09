package br.imd.ufrn.tourai.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column
    private String description;

    @Column
    private String location;

    @Lob
    @Column
    private String mediaUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "activity_tags", joinColumns = @JoinColumn(name = "activity_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    @Column
    private ModerationStatus moderationStatus;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    @ToString.Exclude
    private User creator;

    @JsonIgnore
    @ManyToMany(mappedBy = "activities", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Roadmap> roadmaps = new HashSet<>();

    public Activity(String name, String description, String location, String mediaUrl, Set<String> tags) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.mediaUrl = mediaUrl;
        if (tags != null) this.tags = tags;
        this.type = ActivityType.SYSTEM;
        this.moderationStatus = ModerationStatus.APPROVED;
    }

    public Activity(String name, String description, String location, String mediaUrl, Set<String> tags, ActivityType type, User creator) {
        if (type == ActivityType.SYSTEM) {
            throw new IllegalArgumentException("Use the system activity constructor.");
        }
        this.name = name;
        this.description = description;
        this.location = location;
        this.mediaUrl = mediaUrl;
        if (tags != null) this.tags = tags;
        this.creator = creator;
        this.type = type;
        if (type == ActivityType.CUSTOM_PUBLIC) {
            this.moderationStatus = ModerationStatus.PENDING;
        } else {
            this.moderationStatus = null;
        }
    }
}
