package br.imd.ufrn.tourai.service;

import java.util.List;
import java.util.Optional;

import br.imd.ufrn.tourai.model.NotificationType;
import org.springframework.stereotype.Service;

import br.imd.ufrn.tourai.dto.CreateInviteRequest;
import br.imd.ufrn.tourai.exception.BadRequestException;
import br.imd.ufrn.tourai.exception.ConflictException;
import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.model.Invite;
import br.imd.ufrn.tourai.model.Itinerary;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.repository.InviteRepository;
import br.imd.ufrn.tourai.repository.ItineraryRepository;
import br.imd.ufrn.tourai.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class InviteService {
    private final InviteRepository inviteRepository;
    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public InviteService(
            InviteRepository inviteRepository,
            ItineraryRepository itineraryRepository,
            UserRepository userRepository,
            NotificationService notificationService) {

        this.notificationService = notificationService;
        this.inviteRepository = inviteRepository;
        this.itineraryRepository = itineraryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Invite create(CreateInviteRequest request) {

        // TODO: adicionar verificação se o usuário que está enviando o convite é o dono do itinerário

        Itinerary itinerary = itineraryRepository
            .findById(request.itineraryId())
            .orElseThrow(() ->
                new ResourceNotFoundException("Itinerary with ID " + request.itineraryId() + " not found")
            );

        User inviter = itinerary.getUser();

        if (itinerary.getUser().getId().equals(request.userId())) {
            throw new BadRequestException("Cannot invite the owner of the itinerary");
        }

        User user = userRepository
            .findById(request.userId())
            .orElseThrow(() ->
                new ResourceNotFoundException("User with ID " + request.userId() + " not found")
            );

        if (itinerary.getParticipants().contains(user)) {
            throw new ConflictException("User is already a participant of the itinerary");
        }

        Optional<Invite> existingInvite = inviteRepository.findByItineraryIdAndUserId(itinerary.getId(), user.getId());

        if (existingInvite.isPresent()) {
            throw new ConflictException("An invite for this user and itinerary already exists");
        }

        Invite invite = new Invite();
        invite.setItinerary(itinerary);
        invite.setUser(user);
        inviteRepository.save(invite);

        notificationService.create(
                user, inviter, NotificationType.ROADMAP_INVITATION,
                itinerary.getRoadmap().getDescription(), itinerary.getId());

        return invite;
    }

    public List<Invite> list(Long userId) {
        return inviteRepository.findByUserId(userId);
    }

    @Transactional
    public void accept(Long inviteId) {
        Invite invite = inviteRepository
            .findById(inviteId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Invite with ID " + inviteId + " not found")
            );

        Itinerary itinerary = invite.getItinerary();
        User user = invite.getUser();

        itinerary.getParticipants().add(user);

        itineraryRepository.save(itinerary);
        inviteRepository.delete(invite);
    }

    @Transactional
    public void decline(Long inviteId) {
        Invite invite = inviteRepository
            .findById(inviteId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Invite with ID " + inviteId + " not found")
            );

        inviteRepository.delete(invite);
    }
}

