package br.imd.ufrn.tourai.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateItineraryRequest(List<UpdateItineraryRequestActivity> activities) {
    public record UpdateItineraryRequestActivity(Long itineraryActivityId, LocalDateTime time) {

    }
}
