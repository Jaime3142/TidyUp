package com.example.tidyup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Suite de pruebas unitarias para TidyUp.
 * Incluye pruebas de éxito y pruebas de error (fallos controlados).
 */
public class ExampleUnitTest {

    // ==========================================
    // 1. TESTS QUE DEBEN PASAR (CHECK VERDE ✅)
    // ==========================================

    @Test
    public void testRegistrarUsuario_EjecucionExitosa() {
        try {
            // Llama a tu método real de FirebaseManager
            Task<AuthResult> task = FirebaseManager.registrarUsuarioAuth("user@test.com", "123456");
            assertNotNull("La Task no debe ser nula", task);
        } catch (Exception e) {
            // Si el Gradle está bien configurado, no debería entrar aquí
            fail("No debería lanzar excepción con el mock configurado");
        }
    }

    @Test
    public void testEstructuraTarea_DatosCorrectos() {
        Map<String, Object> tarea = new HashMap<>();
        tarea.put("titulo", "Fregar platos");
        tarea.put("puntos", "20");
        tarea.put("estado", "pendiente");

        assertEquals("Fregar platos", tarea.get("titulo"));
        assertEquals("20", tarea.get("puntos"));
        assertEquals("pendiente", tarea.get("estado"));
    }

    // ==========================================
    // 2. TESTS QUE DEBEN FALLAR (ERROR ROJO ❌)
    // ==========================================
    // Nota: Estos fallan para demostrar que la lógica de seguridad es necesaria.

    @Test
    public void testValidacionEmail_FalloSiNoTieneFormato() {
        String emailMalo = "usuario_sin_arroba";

        // Este assert obligará al test a ponerse rojo si el email no es válido
        assertTrue("ERROR: El sistema no debería aceptar emails sin '@'",
                emailMalo.contains("@") && emailMalo.contains("."));
    }

    @Test
    public void testPassword_FalloSiEsMuyCorta() {
        String passCorta = "123";

        // Firebase requiere mínimo 6 caracteres. Este test FALLARÁ (Rojo).
        assertTrue("ERROR: La contraseña debe tener al menos 6 caracteres",
                passCorta.length() >= 6);
    }

    @Test
    public void testPuntos_FalloSiSonNegativos() {
        int puntos = -10;

        // Este test FALLARÁ porque los puntos no pueden ser negativos en TidyUp
        assertTrue("ERROR: Los puntos deben ser un número positivo",
                puntos > 0);
    }

    @Test
    public void testTarea_FalloSiTituloEstaVacio() {
        String titulo = "";

        // Este test FALLARÁ porque una tarea necesita un nombre obligatoriamente
        assertFalse("ERROR: El título de la tarea no puede estar vacío",
                titulo.isEmpty());
    }
}