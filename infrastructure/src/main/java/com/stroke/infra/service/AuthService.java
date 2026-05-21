package com.stroke.infra.service;

import com.stroke.common.BusinessException;
import com.stroke.common.dto.LoginRequest;
import com.stroke.common.dto.LoginResponse;
import com.stroke.common.dto.RegisterRequest;
import com.stroke.common.util.JwtUtil;
import com.stroke.domain.entity.SysUser;
import com.stroke.infra.mapper.SysUserMapper;
import javax.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostConstruct
    public void initDefaults() {
        if (userMapper.selectCount(null) == 0) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setDisplayName("系统管理员");
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            admin.setCreatedBy("SYSTEM");
            admin.setCreatedTime(LocalDateTime.now());
            userMapper.insert(admin);

            SysUser doctor = new SysUser();
            doctor.setUsername("doctor1");
            doctor.setPassword(encoder.encode("admin123"));
            doctor.setDisplayName("张医生");
            doctor.setRole("DOCTOR");
            doctor.setStatus("ACTIVE");
            doctor.setCreatedBy("SYSTEM");
            doctor.setCreatedTime(LocalDateTime.now());
            userMapper.insert(doctor);

            SysUser nurse = new SysUser();
            nurse.setUsername("nurse1");
            nurse.setPassword(encoder.encode("admin123"));
            nurse.setDisplayName("李护士");
            nurse.setRole("NURSE");
            nurse.setStatus("ACTIVE");
            nurse.setCreatedBy("SYSTEM");
            nurse.setCreatedTime(LocalDateTime.now());
            userMapper.insert(nurse);
        }
    }

    public LoginResponse login(LoginRequest req) {
        SysUser user = userMapper.findByUsername(req.getUsername());
        if (user == null) {
            throw new BusinessException(401, "账号或密码错误");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成 Token
        String token = JwtUtil.generate(user.getId(), user.getUsername(), List.of(user.getRole()));

        LoginResponse resp = new LoginResponse(token, user.getId(), user.getUsername(),
                user.getDisplayName(), user.getRole());
        resp.setPermissions(List.of("GREENWAY_MANAGE", "NIHSS_EVALUATE", "CDS_DECISION",
                "DASHBOARD_VIEW", "FOLLOWUP_VIEW"));
        return resp;
    }

    public void register(RegisterRequest req) {
        SysUser exists = userMapper.findByUsername(req.getUsername());
        if (exists != null) {
            throw new BusinessException(400, "账号已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setDisplayName(req.getDisplayName());
        user.setRole(req.getRole() != null ? req.getRole() : "DOCTOR");
        user.setDepartment(req.getDepartment());
        user.setPhone(req.getPhone());
        user.setStatus("ACTIVE");
        userMapper.insert(user);
    }
}
