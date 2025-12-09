package br.imd.ufrn.tourai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
public class Notification {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter
    @Setter
    private NotificationType type;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_id", referencedColumnName = "id")
    private User source;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_id", referencedColumnName = "id")
    private User destination;

    @Getter
    @Setter
    @Column(
            name = "received",
            nullable = false,
            columnDefinition = "boolean default false"
    )
    private boolean received = false;

    @Getter
    @Setter
    private Instant createdAt;

    @Getter
    @Setter
    private String payload;

    @Getter
    @Setter
    private Long entityId;

    @Getter
    @Setter
    @Column(
            name = "action_completed",
            nullable = false,
            columnDefinition = "boolean default false"
    )
    private boolean actionCompleted = false;

}
