package com.example.pms.repository;

import com.example.pms.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AppUserRepository extends JpaRepository<AppUser, Long> {


    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmployeeId(Long employeeId);

}