package br.imd.ufrn.tourai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.imd.ufrn.tourai.config.CustomUserDetails;
import br.imd.ufrn.tourai.dto.CreateInviteRequest;
import br.imd.ufrn.tourai.model.Invite;
import br.imd.ufrn.tourai.service.InviteService;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/invites")
public class InviteController {
    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping
    public Invite create(@RequestBody CreateInviteRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return inviteService.create(userDetails, request);
    }

    @GetMapping
    public List<Invite> list() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return inviteService.list(userDetails);
    }

    @PostMapping("/{id}/accept")
    public void accept(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        inviteService.accept(userDetails, id);
    }

    @PostMapping("/{id}/decline")
    public void decline(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        inviteService.decline(userDetails, id);
    }

}
