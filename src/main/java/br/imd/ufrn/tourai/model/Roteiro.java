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
@ToString(exclude = {"user", "atividades"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Roteiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Lob
    private String descricao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "roteiro_tags", joinColumns = @JoinColumn(name = "roteiro_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<String>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisibilidadeRoteiro visibilidade;

    @Enumerated(EnumType.STRING)
    private StatusModeracao status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "roteiro_atividade",
            joinColumns = @JoinColumn(name = "roteiro_id"),
            inverseJoinColumns = @JoinColumn(name = "atividade_id")
    )

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Atividade> atividades = new HashSet<>();

    public void addAtividade(Atividade atividade) {
        this.atividades.add(atividade);
        atividade.getRoteiros().add(this);
    }

    public void removeAtividade(Atividade atividade) {
        this.atividades.remove(atividade);
        atividade.getRoteiros().remove(this);
    }
}
