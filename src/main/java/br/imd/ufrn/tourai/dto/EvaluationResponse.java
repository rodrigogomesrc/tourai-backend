package br.imd.ufrn.tourai.dto;

public class EvaluationResponse {
    private Long id;
    private Integer rating;
    private String comment;
    private Integer itineraryActivityId;

    public EvaluationResponse() {}

    public EvaluationResponse(Long id, Integer rating, String comment, Integer itineraryActivityId) {
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

    public Integer getItineraryActivityId() {
        return itineraryActivityId;
    }
}
