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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Lob
    private String descricao;

    private String local;

    @Lob
    private String midiaURL;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "atividade_tags", joinColumns = @JoinColumn(name = "atividade_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAtividade tipo;

    @Enumerated(EnumType.STRING)
    private StatusModeracao statusModeracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criador_id")
    @ToString.Exclude
    private User criador;

    @ManyToMany(mappedBy = "atividades", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Roteiro> roteiros = new HashSet<>();

    // Construtor específico para atividades do sistema (sem criador)
    public Atividade(String nome, String descricao, String local, String midiaUrl, Set<String> tags) {
        this.nome = nome;
        this.descricao = descricao;
        this.local = local;
        this.midiaURL = midiaUrl;
        if (tags != null) this.tags = tags;
        this.tipo = TipoAtividade.SISTEMA;
        this.statusModeracao = StatusModeracao.APROVADO; // Atividades do sistema já são "aprovadas"
    }

    // Construtor específico para atividades personalizadas (com criador)
    public Atividade(String nome, String descricao, String local, String midiaUrl, Set<String> tags, TipoAtividade tipo, User criador) {
        if (tipo == TipoAtividade.SISTEMA) {
            throw new IllegalArgumentException("Use o construtor apropriado para atividades do sistema.");
        }
        this.nome = nome;
        this.descricao = descricao;
        this.local = local;
        this.midiaURL = midiaUrl;
        if (tags != null) this.tags = tags;
        this.criador = criador;
        this.tipo = tipo;
        if (tipo == TipoAtividade.PERSONALIZADA_PUBLICA) {
            this.statusModeracao = StatusModeracao.PENDENTE;
        } else {
            this.statusModeracao = null; // Atividade PRIVADA não precisa de moderação visível
        }
    }


}
