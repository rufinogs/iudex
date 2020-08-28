package com.example.aplicacion.Repository;

import com.example.aplicacion.Entities.InNOut;
import com.example.aplicacion.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
