package br.imd.ufrn.tourai.dto;

public class EvaluationResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private Long itineraryActivityId;

    public EvaluationResponse() {}

    public EvaluationResponse(Long id, Integer rating, String comment, Long itineraryActivityId) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.itineraryActivityId = itineraryActivityId;
    }

    public Long getId() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Long getItineraryActivityId() {
        return itineraryActivityId;
    }
}
