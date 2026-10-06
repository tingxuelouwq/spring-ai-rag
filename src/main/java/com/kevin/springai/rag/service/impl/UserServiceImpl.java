package com.kevin.springai.rag.service.impl;

import com.kevin.springai.rag.common.ErrorCode;
import com.kevin.springai.rag.common.MessageConstant;
import com.kevin.springai.rag.common.PageResult;
import com.kevin.springai.rag.constant.PasswordConstant;
import com.kevin.springai.rag.constant.StatusConstant;
import com.kevin.springai.rag.context.BaseContext;
import com.kevin.springai.rag.dto.PasswordDTO;
import com.kevin.springai.rag.dto.UserDTO;
import com.kevin.springai.rag.dto.UserPageQueryDTO;
import com.kevin.springai.rag.entity.User;
import com.kevin.springai.rag.exception.AccountLockedException;
import com.kevin.springai.rag.exception.AccountNotFoundException;
import com.kevin.springai.rag.exception.BusinessException;
import com.kevin.springai.rag.exception.PasswordErrorException;
import com.kevin.springai.rag.repository.UserRepository;
import com.kevin.springai.rag.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User login(String userName, String password) {

        // 1. 根据用户名查询用户，校验用户是否存在
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND));

        // 2. 校验密码：对前端传入的明文密码进行 MD5 加密后比对
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!encryptedPassword.equals(user.getPassword())) {
            log.warn("登录失败：密码错误，userName={}", userName);
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 3. 校验账号状态
        if (user.getStatus().equals(StatusConstant.DISABLE)) {
            log.warn("登录失败：账号被锁定，userName={}", userName);
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        log.info("用户登录成功，userId={}，userName={}", user.getId(), userName);
        return user;
    }

    @Override
    public boolean isUsernameExists(String userName) {
        return userRepository.findByUserName(userName).isPresent();
    }

    @Override
    public void register(User user) {
        // 1. 构建待入库的用户实体
        User userResult = new User();
        BeanUtils.copyProperties(user, userResult);

        // 2. 设置账号默认状态为正常
        userResult.setStatus(StatusConstant.ENABLE);

        // 3. 对调用方传入的明文密码进行 MD5 加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(
                user.getPassword().getBytes(StandardCharsets.UTF_8));
        userResult.setPassword(encryptedPassword);

        // 4. 设置创建时间与修改时间
        LocalDateTime now = LocalDateTime.now();
        userResult.setCreateTime(now);
        userResult.setUpdateTime(now);

        // 5. 设置创建人与修改人（若为匿名注册，BaseContext 中可能为 null）
        Long currentId = BaseContext.getCurrentId();
        userResult.setCreateUser(currentId);
        userResult.setUpdateUser(currentId);

        // 6. 入库
        userRepository.save(userResult);
        log.info("用户注册成功，userId={}，userName={}", userResult.getId(), userResult.getUserName());
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND));
    }

    @Override
    public void updatePassword(PasswordDTO passwordDTO) {
        // 1. 校验两次输入的新密码是否一致
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            throw new RuntimeException("新密码与确认密码不一致");
        }

        // 2. 查询用户
        User user = userRepository.findById(passwordDTO.getId())
                .orElseThrow(() -> new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND));

        // 3. 校验旧密码
        String encryptedOldPassword = DigestUtils.md5DigestAsHex(
                passwordDTO.getOldPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(encryptedOldPassword)) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 4. 更新为新密码
        user.setPassword(DigestUtils.md5DigestAsHex(
                passwordDTO.getNewPassword().getBytes(StandardCharsets.UTF_8)));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("密码修改成功，userId={}", passwordDTO.getId());
    }

    @Override
    public void update(User user) {
        // 1. 校验 id 是否存在
        if (user.getId() == null) {
            throw new RuntimeException("用户 ID 不能为空");
        }

        // 2. 确认用户是否存在
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND));

        // 3. 更新允许修改的字段（避免覆盖密码等敏感字段）
        existing.setName(user.getName());
        existing.setPhone(user.getPhone());
        existing.setSex(user.getSex());
        existing.setIdNumber(user.getIdNumber());
        existing.setUpdateTime(LocalDateTime.now());
        existing.setUpdateUser(BaseContext.getCurrentId());

        userRepository.save(existing);
        log.info("用户信息更新成功，userId={}", user.getId());
    }

    @Override
    public PageResult pageQuery(UserPageQueryDTO userPageQueryDTO) {
        Pageable pageable = PageRequest.of(
                userPageQueryDTO.getPage(),
                userPageQueryDTO.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime") // 按创建时间倒序
        );

        // 2. 执行分页查询
        Page<User> page = userRepository.pageQuery(
                StringUtils.hasText(userPageQueryDTO.getName()) ? userPageQueryDTO.getName() : null,
                pageable);

        // 3. 封装分页结果
        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotalElements());
        pageResult.setRecords(page.getContent());
        return pageResult;
    }

    @Override
    public void saveUser(UserDTO userDTO) {
        // 1. 校验用户名是否已存在
        if (userRepository.findByUserName(userDTO.getUserName()).isPresent()) {
            throw new RuntimeException(MessageConstant.ALREADY_EXISTS);
        }

        // 2. 构建用户实体
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 3. 设置默认密码
        user.setPassword(DigestUtils.md5DigestAsHex(
                PasswordConstant.DEFAULT_PASSWORD.getBytes(StandardCharsets.UTF_8)));

        // 4. 设置默认状态与时间
        user.setStatus(StatusConstant.ENABLE);
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        // 5. 设置操作人（管理端新增，当前登录用户即操作人）
        Long currentId = BaseContext.getCurrentId();
        user.setCreateUser(currentId);
        user.setUpdateUser(currentId);

        // 6. 入库
        userRepository.save(user);
        log.info("新增用户成功，userId={}，userName={}", user.getId(), user.getUserName());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        // 1. 校验状态值合法性
        if (!StatusConstant.ENABLE.equals(status) && !StatusConstant.DISABLE.equals(status)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 查询用户
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND));

        // 3. 更新状态
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateUser(BaseContext.getCurrentId());
        userRepository.save(user);

        log.info("用户账号状态更新成功，userId={}，status={}", id, status);
    }
}