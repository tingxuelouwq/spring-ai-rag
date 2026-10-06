package com.kevin.springai.rag.controller;

import com.kevin.springai.rag.common.BaseResponse;
import com.kevin.springai.rag.common.ResultUtils;
import com.kevin.springai.rag.config.JwtProperties;
import com.kevin.springai.rag.constant.BizConstant;
import com.kevin.springai.rag.constant.JwtClaimsConstant;
import com.kevin.springai.rag.constant.StatusConstant;
import com.kevin.springai.rag.dto.PasswordDTO;
import com.kevin.springai.rag.dto.UserDTO;
import com.kevin.springai.rag.dto.UserPageQueryDTO;
import com.kevin.springai.rag.entity.User;
import com.kevin.springai.rag.service.UserService;
import com.kevin.springai.rag.utils.JwtUtil;
import com.kevin.springai.rag.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理接口
 */
@Tag(name = "UserController", description = "用户管理")
@Slf4j
@RestController
@RequestMapping(BizConstant.API_VERSION + "/user")
@RequiredArgsConstructor
public class UserController {

    private final JwtProperties jwtProperties;

    private final UserService userService;

    /**
     * 用户注册
     *
     * @param user 注册信息，包含用户名、密码、姓名等
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "register", description = "注册")
    public BaseResponse<Void> register(@RequestBody @Validated User user) {
        log.info("用户注册：userName={}", user.getUserName());

        if (userService.isUsernameExists(user.getUserName())) {
            return ResultUtils.error("用户名已存在");
        }

        userService.register(user);
        return ResultUtils.success(null, "注册成功");
    }

    /**
     * 用户登录
     * <p>
     * 校验用户名密码，成功后生成 JWT 令牌并返回用户基本信息。
     * </p>
     *
     * @param userName 用户名，默认 admin
     * @param password 密码，默认 123456
     * @return 包含 token 和用户信息的统一响应体
     */
    @PostMapping("/login")
    @Operation(summary = "login", description = "登录")
    public BaseResponse<UserLoginVO> login(
            @RequestParam(value = "userName", defaultValue = "admin") String userName,
            @RequestParam(value = "password", defaultValue = "123456") String password)
            throws AccountLockedException, AccountNotFoundException {

        log.info("用户登录：userName={}", userName);

        User user = userService.login(userName, password);

        // 登录成功后生成 JWT 令牌
        Map<String, Object> claims = new HashMap<>(1);
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .name(user.getName())
                .token(token)
                .build();

        return ResultUtils.success(userLoginVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "logout",description = "退出")
    public BaseResponse<String> logout() {
        return ResultUtils.success("退出成功");
    }

    /**
     * 根据 ID 查询用户信息
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "info", description = "根据id查询user信息")
    public BaseResponse<User> getById(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        return ResultUtils.success(user);
    }

    /**
     * 修改密码
     *
     * @param passwordDTO 密码修改信息
     * @return 操作结果
     */
    @PostMapping("/updatePassword")
    @Operation(summary = "updatePassword", description = "修改密码")
    public BaseResponse<Void> updatePassword(@RequestBody @Validated PasswordDTO passwordDTO) {
        log.info("修改密码：userId={}", passwordDTO.getId());
        userService.updatePassword(passwordDTO);
        return ResultUtils.success(null, "修改密码成功");
    }

    /**
     * 编辑用户信息
     *
     * @param user 待更新的用户信息，必须包含 id
     * @return 操作结果
     */
    @PutMapping("/update")
    @Operation(summary = "update", description = "编辑user信息")
    public BaseResponse<Void> update(@RequestBody @Validated User user) {
        log.info("编辑用户信息：userId={}", user.getId());
        userService.update(user);
        return ResultUtils.success(null, "编辑成功");
    }

    /**
     * 用户分页查询
     *
     * @param userPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "page", description = "user分页查询")
    public BaseResponse<Page<User>> page(@Validated UserPageQueryDTO userPageQueryDTO) {
        log.info("用户分页查询，参数为：{}", userPageQueryDTO);
        Page<User> page = userService.pageQuery(userPageQueryDTO);
        return ResultUtils.success(page);
    }

    /**
     * 新增用户
     *
     * @param userDTO 用户信息
     * @return 操作结果
     */
    @PostMapping("/addUser")
    @Operation(summary = "addUser", description = "新增user")
    public BaseResponse<Void> save(@RequestBody @Validated UserDTO userDTO) {
        log.info("新增用户：userName={}", userDTO.getUserName());
        userService.saveUser(userDTO);
        return ResultUtils.success(null, "新增成功");
    }

    /**
     * 启用/禁用用户账号
     *
     * @param status 目标状态：1-启用，0-禁用
     * @param id     用户 ID
     * @return 操作结果
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "status", description = "启用禁用账号")
    public BaseResponse<Void> startOrStop(@PathVariable("status") Integer status,
                                          @RequestParam("id") Long id) {
        log.info("启用/禁用用户账号：userId={}, status={}", id, status);
        userService.startOrStop(status, id);
        return ResultUtils.success(null, status == StatusConstant.ENABLE ? "启用成功" : "禁用成功");
    }
}