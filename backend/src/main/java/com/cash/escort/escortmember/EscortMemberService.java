package com.cash.escort.escortmember;

import com.cash.escort.common.BizException;
import com.cash.escort.common.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EscortMemberService {

    @Autowired
    private EscortMemberRepository repository;

    public List<EscortMember> list(String status) {
        if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    public EscortMember create(String name, String certNo, String level, String status, LocalDate joinDate) {
        if (name == null) {
            throw new BizException("押运员姓名不能为空");
        }
        String realStatus = status;
        if (realStatus == null) {
            realStatus = Dict.MEMBER_ONDUTY;
        }
        if (!Dict.isValidMemberStatus(realStatus)) {
            throw new BizException("非法的押运员状态");
        }
        EscortMember member = new EscortMember();
        member.setName(name);
        member.setCertNo(certNo);
        member.setLevel(level);
        member.setStatus(realStatus);
        member.setJoinDate(joinDate);
        member.setCreatedAt(LocalDateTime.now());
        return repository.save(member);
    }

    public EscortMember update(Long id, EscortMember req) {
        EscortMember member = repository.findById(id).orElse(null);
        if (member == null) {
            throw new BizException("押运员不存在");
        }
        if (req.getName() != null) {
            member.setName(req.getName());
        }
        if (req.getCertNo() != null) {
            member.setCertNo(req.getCertNo());
        }
        if (req.getLevel() != null) {
            member.setLevel(req.getLevel());
        }
        if (req.getStatus() != null) {
            if (!Dict.isValidMemberStatus(req.getStatus())) {
                throw new BizException("非法的押运员状态");
            }
            member.setStatus(req.getStatus());
        }
        if (req.getJoinDate() != null) {
            member.setJoinDate(req.getJoinDate());
        }
        member.setUpdatedAt(LocalDateTime.now());
        return repository.save(member);
    }

    public EscortMember transferStatus(Long id, String status) {
        if (status == null) {
            throw new BizException("目标状态不能为空");
        }
        if (!Dict.isValidMemberStatus(status)) {
            throw new BizException("非法的押运员状态");
        }
        EscortMember member = repository.findById(id).orElse(null);
        if (member == null) {
            throw new BizException("押运员不存在");
        }
        member.setStatus(status);
        member.setUpdatedAt(LocalDateTime.now());
        return repository.save(member);
    }
}
