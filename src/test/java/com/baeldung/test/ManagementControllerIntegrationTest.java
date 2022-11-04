package com.baeldung.test;

import com.baeldung.Application;
import com.baeldung.persistence.model.Privilege;
import com.baeldung.persistence.model.Role;
import com.baeldung.persistence.model.User;
import com.baeldung.spring.TestDbConfig;
import com.baeldung.spring.TestIntegrationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { Application.class, TestDbConfig.class, TestIntegrationConfig.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
class ManagementControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;
    private User simpleUser;
    private User manager;



    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        simpleUser = createSimpleUser();
        manager = createManager();
    }

    private User createSimpleUser() {
        final Privilege readPrivilege  = new Privilege("READ_PRIVILEGE");
        entityManager.persist(readPrivilege);

        final Role userRole  = new Role();
        userRole.setPrivileges(Collections.singletonList(readPrivilege));
        userRole.setName("ROLE_USER");
        entityManager.persist(userRole);

        User user = new User();
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setPassword(UUID.randomUUID().toString());
        user.setFirstName("First");
        user.setLastName("Last");
        user.setRoles(new ArrayList<>(Collections.singletonList(userRole)));

        entityManager.persist(user);


        entityManager.flush();
        entityManager.clear();
        return user;
    }

    private User createManager() {
        final Privilege readPrivilege  = new Privilege("READ_PRIVILEGE");
        entityManager.persist(readPrivilege);
        final Privilege writePrivilege  = new Privilege("WRITE_PRIVILEGE");
        entityManager.persist(writePrivilege);

        final Role managerRole  = new Role();
        managerRole.setPrivileges(Arrays.asList(readPrivilege, writePrivilege));
        managerRole.setName("ROLE_MANAGER");
        entityManager.persist(managerRole);

        User manager = new User();
        manager.setEmail(UUID.randomUUID() + "@example.com");
        manager.setPassword(UUID.randomUUID().toString());
        manager.setFirstName("Manager");
        manager.setLastName("Test");
        manager.setRoles(new ArrayList<>(Collections.singletonList(managerRole)));

        entityManager.persist(manager);


        entityManager.flush();
        entityManager.clear();
        return manager;
    }

    @Test
    void testConsultManagementLinkForSimpleUser() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(simpleUser,null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        ResultActions resultActions = this.mockMvc.perform(get("/management"));
        resultActions.andExpect(view().name("accessDenied"));
    }

    @Test
    void testConsultManagementLinkForManagerUser() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(manager,null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        ResultActions resultActions = this.mockMvc.perform(get("/management"));
        resultActions.andExpect(view().name("management"));
    }
}