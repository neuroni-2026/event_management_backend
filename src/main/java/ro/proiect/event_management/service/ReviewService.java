package ro.proiect.event_management.service;

import ro.proiect.event_management.dto.request.CreateReviewRequest;
import ro.proiect.event_management.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService
{
    void addReview(CreateReviewRequest request, Long userId);
    List<ReviewResponse> getReviewsByEvent(Long eventId);
}
