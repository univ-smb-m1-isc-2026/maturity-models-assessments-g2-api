package fr.univ_smb.info803.maturitymodelsassessmentsapi.service;

import fr.univ_smb.info803.maturitymodelsassessmentsapi.model.User;
import fr.univ_smb.info803.maturitymodelsassessmentsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> getByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUser(final long id){
        return userRepository.findById(id);
    }

    public Iterable<User> getUsers(){
        return userRepository.findAll();
    }

    public void deleteUser(final long id){ userRepository.deleteById(id); }

    public User saveUser(User user){
        return userRepository.save(user);
    }

}
