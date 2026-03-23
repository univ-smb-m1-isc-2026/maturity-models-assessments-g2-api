package fr.univ_smb.info803.maturitymodelsassessmentsapi.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://mmag2.oups.net"})
public class HelloController {

    @GetMapping("/hello")
    public String index() {
        System.out.println("/hello route called");
        return "Greetings from Spring Boot!";
    }
}
