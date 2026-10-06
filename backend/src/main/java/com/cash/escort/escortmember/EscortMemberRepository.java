package com.cash.escort.escortmember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscortMemberRepository extends JpaRepository<EscortMember, Long> {

    List<EscortMember> findByStatus(String status);
}
