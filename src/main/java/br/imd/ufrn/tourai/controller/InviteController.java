package br.imd.ufrn.tourai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.imd.ufrn.tourai.dto.CreateInviteRequest;
import br.imd.ufrn.tourai.model.Invite;
import br.imd.ufrn.tourai.service.InviteService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/invites")
public class InviteController {
    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping
    public Invite create(@RequestBody CreateInviteRequest request) {
        return inviteService.create(request);
    }

    @GetMapping
    public List<Invite> list(@RequestParam(required = true) Long userId) {
        return inviteService.list(userId);
    }

    @PostMapping("/{id}/accept")
    public void accept(@PathVariable Long id) {
        inviteService.accept(id);
    }

    @PostMapping("/{id}/decline")
    public void decline(@PathVariable Long id) {
        inviteService.decline(id);
    }

}
