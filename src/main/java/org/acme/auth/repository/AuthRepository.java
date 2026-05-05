package org.acme.auth.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.auth.entity.User;

import java.util.List;

@ApplicationScoped
public class AuthRepository implements PanacheRepository<User> {

    public User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public List<User> listUsers(int pageIndex, int pageSize) {
        return findAll()
                .page(Page.of(pageIndex, pageSize))
                .list();
    }
}