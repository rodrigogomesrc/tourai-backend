package br.imd.ufrn.tourai.dto;

public class EvaluationRequest {
    private Integer rating;
    private String comment;

    public EvaluationRequest() {}

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
