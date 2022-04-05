package com.example.demo.security;

import com.example.demo.dao.idao.IDAOCustomer;
import com.example.demo.dao.idao.IDAOSecurity;
import com.example.demo.dao.idao.IDAOTravelAgency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private IDAOSecurity repository;

    public UserService() {
        try(ClassPathXmlApplicationContext context =
                    new ClassPathXmlApplicationContext("DAOContext.xml")) {
            repository = context.getBean("DAOSecurity",IDAOSecurity.class);
        } finally {
        }
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.repository.getUserByUsernameDependingOnRole(username);
    }
}