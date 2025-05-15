package com.isl.payments.payments_isl.services;

import com.isl.payments.payments_isl.entities.User;
import com.isl.payments.payments_isl.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public boolean updateUser(String username) {
        User user= userRepository.findByUsername(username).orElse(null);
        if(user == null)
            return false;
        user.setPurchasedKey(true);
        userRepository.save(user);
        return true;
    }
}
