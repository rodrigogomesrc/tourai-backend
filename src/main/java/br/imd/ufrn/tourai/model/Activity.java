package br.imd.ufrn.tourai.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "Atividade") // keep original entity name for table mapping
@Table(name = "atividade")
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

    @Column(name = "nome", nullable = false)
    private String name;

    @Lob
    @Column(name = "descricao")
    private String description;

    @Column(name = "local")
    private String location;

    @Lob
    @Column(name = "midiaURL")
    private String mediaUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "atividade_tags", joinColumns = @JoinColumn(name = "atividade_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "statusModeracao")
    private ModerationStatus moderationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criador_id")
    @ToString.Exclude
    private User creator;

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
