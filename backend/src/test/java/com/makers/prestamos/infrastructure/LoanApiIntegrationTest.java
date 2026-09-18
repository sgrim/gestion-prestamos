package com.makers.prestamos.infrastructure;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recorre la API completa (seguridad + casos de uso + persistencia + caché) sobre H2.
 * Los usuarios usuario@test.com y admin@test.com los crea DemoDataInitializer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanApiIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/loans/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"usuario@test.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRequestsLoanAndAdminApprovesIt() throws Exception {
        String userToken = login("usuario@test.com");
        String adminToken = login("admin@test.com");

        String created = mvc.perform(post("/api/loans").header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 5000, \"termMonths\": 12}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        int loanId = JsonPath.read(created, "$.id");

        // El usuario ve su préstamo (y llena la caché de consultas por usuario)
        mvc.perform(get("/api/loans/me").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(loanId)));

        // Un usuario normal NO puede aprobar
        mvc.perform(patch("/api/loans/{id}/approve", loanId).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // El admin sí
        mvc.perform(patch("/api/loans/{id}/approve", loanId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // La caché se invalidó: el usuario ve el nuevo estado
        mvc.perform(get("/api/loans/{id}", loanId).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // No se puede decidir dos veces
        mvc.perform(patch("/api/loans/{id}/reject", loanId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRequestsReturnClearErrors() throws Exception {
        String userToken = login("usuario@test.com");

        // Forma inválida -> 400 con detalle por campo
        mvc.perform(post("/api/loans").header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists())
                .andExpect(jsonPath("$.errors.termMonths").exists());

        // Regla de negocio (monto fuera de rango) -> 422
        mvc.perform(post("/api/loans").header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 50, \"termMonths\": 12}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void onlyAdminsCanListAllLoansOrManageUsers() throws Exception {
        String userToken = login("usuario@test.com");
        String adminToken = login("admin@test.com");

        mvc.perform(get("/api/loans").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/loans").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userEmail").exists());
    }

    @Test
    void userCannotReadSomeoneElsesLoan() throws Exception {
        String adminToken = login("admin@test.com");
        String userToken = login("usuario@test.com");

        // Préstamo del administrador (los admin también pueden solicitar)
        String created = mvc.perform(post("/api/loans").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1000, \"termMonths\": 6}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int loanId = JsonPath.read(created, "$.id");

        mvc.perform(get("/api/loans/{id}", loanId).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminManagesUserLifecycle() throws Exception {
        String adminToken = login("admin@test.com");

        String created = mvc.perform(post("/api/users").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"temporal@test.com\",\"password\":\"123\",\"fullName\":\"Temporal\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(created, "$.id");

        // Actualizar
        mvc.perform(put("/api/users/{id}", id).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Nombre Editado\",\"role\":\"USER\",\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nombre Editado"));

        // Baja lógica: el usuario ya no puede iniciar sesión, pero sigue existiendo
        mvc.perform(delete("/api/users/{id}", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"temporal@test.com\",\"password\":\"123\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/{id}", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void adminCannotDeleteThemselvesAndUsersCannotManageUsers() throws Exception {
        String adminToken = login("admin@test.com");
        String userToken = login("usuario@test.com");

        mvc.perform(delete("/api/users/{id}", 1).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/users/{id}", 1).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/users/{id}", 9999).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }
}
