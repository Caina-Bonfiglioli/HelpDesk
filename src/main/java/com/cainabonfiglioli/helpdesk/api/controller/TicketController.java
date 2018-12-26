package com.cainabonfiglioli.helpdesk.api.controller;

import com.cainabonfiglioli.helpdesk.api.entity.Ticket;
import com.cainabonfiglioli.helpdesk.api.entity.User;
import com.cainabonfiglioli.helpdesk.api.enums.StatusEnum;
import com.cainabonfiglioli.helpdesk.api.response.Response;
import com.cainabonfiglioli.helpdesk.api.security.jwt.JwtTokenUtil;
import com.cainabonfiglioli.helpdesk.api.service.TicketService;
import com.cainabonfiglioli.helpdesk.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/api/ticket")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    protected JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    public ResponseEntity<Response<Ticket>> createOrUpdate(HttpServletRequest request, @RequestBody Ticket ticket,
                                                   BindingResult result){

        Response<Ticket> response = new Response<Ticket>();

        try {
            validateCreateTicket(ticket, result);

            if (result.hasErrors()){
                result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(response);
            }

            ticket.setStatus(StatusEnum.getStatus("new"));
            ticket.setUser(userFromRequest(request));
            ticket.setDate(new Date());
            ticket.setNumber(genereteNumber());
            Ticket ticketPesisted = (Ticket) ticketService.createOrUpdate(ticket);
            response.setData(ticketPesisted);
        }catch (Exception e){
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    private void validateCreateTicket(Ticket ticket, BindingResult result){
        if (ticket.getTitle() == null){
            result.addError(new ObjectError("Ticket", "Title not information"));
            return;
        }
    }

    public User userFromRequest(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        String email = jwtTokenUtil.getUsernameFromToken(token);
        return userService.findByEmail(email);
    }

    private Integer genereteNumber(){
        Random random = new Random();
        return random.nextInt(9999);
    }
}
