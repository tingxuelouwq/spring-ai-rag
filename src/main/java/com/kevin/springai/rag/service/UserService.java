package com.kevin.springai.rag.service;

import com.kevin.springai.rag.dto.PasswordDTO;
import com.kevin.springai.rag.dto.UserDTO;
import com.kevin.springai.rag.dto.UserPageQueryDTO;
import com.kevin.springai.rag.entity.User;
import org.springframework.data.domain.Page;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;

public interface UserService {

    User login(String userName, String password) throws AccountNotFoundException, AccountLockedException;

    boolean isUsernameExists(String userName);

    void register(User user);

    User getById(Long id);

    void updatePassword(PasswordDTO passwordDTO);

    void update(User user);

    Page<User> pageQuery(UserPageQueryDTO userPageQueryDTO);

    void saveUser(UserDTO userDTO);

    void startOrStop(Integer status, Long id);
}
