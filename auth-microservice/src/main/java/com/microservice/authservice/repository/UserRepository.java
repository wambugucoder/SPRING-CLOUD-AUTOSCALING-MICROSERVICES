package com.microservice.authservice.repository;

import com.microservice.authservice.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<UserModel,String> {

        UserModel findUserModelsByUsername(String username);

        boolean existsByUsername(String username);




}
