package com.codesoft.edu.service.impl;

import com.codesoft.edu.exception.NullEntityReferenceException;
import com.codesoft.edu.exception.UserExistException;
import com.codesoft.edu.model.User;
import com.codesoft.edu.repository.UserRepository;
import com.codesoft.edu.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User create(User user) {
        if (user != null) {
            if(userRepository.findByEmail(user.getEmail()) != null) {
                log.error("User with email {} already exists", user.getEmail());
                throw new UserExistException("User with email " + user.getEmail() + " already exists");
            }
            return userRepository.save(user);
        }
        log.error("User create is 'null'");
        throw new NullEntityReferenceException("User cannot be 'null'");
    }

    @Override
    public User readById(long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public User update(User user) {
        if (user != null) {

            User checkedUser = userRepository.findByEmail(user.getEmail());

            if(checkedUser != null && !Objects.equals(checkedUser.getId(), user.getId())) {
                log.error("User with email {} already exists", user.getEmail());
                throw new UserExistException("User with email " + user.getEmail() + " already exists you cannot update it");
            }

            readById(user.getId());
            return userRepository.save(user);
        }
        log.error("User update 'null'");
        throw new NullEntityReferenceException("User cannot be 'null'");
    }

    @Override
    public void delete(long id) {
        User user = readById(id);
        userRepository.delete(user);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not Found!");
        }
        return user;
    }
}
