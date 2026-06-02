package io.venus.vega.data.repositories;

import io.venus.vega.data.entities.User;
import io.venus.vega.services.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;


@Repository
public class CustomUserLoginRepository {

    @Autowired
    private EntityManager entityManager;

    public User findByEmailAndPasswordSQLInj(String email, String password) {
        String sql = "SELECT * FROM vega.users WHERE email = '" + email + "' AND password = '" + password + "' LIMIT 1";

        Query query = entityManager.createNativeQuery(sql, User.class);

        try {
            return (User) query.getSingleResult();
        } catch (NoResultException e) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }
}