package fr.univ_smb.info803.maturitymodelsassessmentsapi;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost")
public class HelloController {

    @GetMapping("/hello")
    public String index() {
        return "Greetings from Spring Boot!";
    }
}
