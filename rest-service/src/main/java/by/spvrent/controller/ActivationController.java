package by.spvrent.controller;

import by.spvrent.service.interf.UserActivationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class ActivationController {

    private final UserActivationService userActivationService;

    @RequestMapping(method = RequestMethod.GET, value = "/activation")
    public ResponseEntity<?> activation(@RequestParam("id") String id){

        boolean res = userActivationService.activation(id);
        if (res){
            return ResponseEntity.ok().body("Registration successful completed!");
        }
        return ResponseEntity.internalServerError().build();
    }
}
