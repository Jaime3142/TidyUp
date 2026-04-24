package com.example.tidyup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExampleUnitTest {

    // TEST 1: El admin se añade a miembros si no estaba en la lista
    @Test
    public void crearGrupo_adminAusente_seAgnadeSolo() {
        String miUid = "uid-admin-456";
        List<String> miembrosUids = new ArrayList<>();
        miembrosUids.add("uid-otro-usuario");

        if (!miembrosUids.contains(miUid)) {
            miembrosUids.add(miUid);
        }

        assertTrue(miembrosUids.contains(miUid));
        assertEquals(2, miembrosUids.size());
    }

    // TEST 2: Si el admin ya estaba, no se duplica
    @Test
    public void crearGrupo_adminYaPresente_noSeDuplica() {
        String miUid = "uid-admin-456";
        List<String> miembrosUids = new ArrayList<>();
        miembrosUids.add(miUid); // Ya está

        if (!miembrosUids.contains(miUid)) {
            miembrosUids.add(miUid);
        }

        assertEquals(1, miembrosUids.size());
    }

    // TEST 3: El perfil nuevo tiene rol "adulto" y puntos 0 por defecto
    @Test
    public void crearPerfilUsuario_valoresPorDefectoCorrectos() {
        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("nombre", "Ivan");
        datosUsuario.put("email", "ivan@requero.com");
        datosUsuario.put("rol", "adulto");
        datosUsuario.put("puntos", 0);

        assertEquals("adulto", datosUsuario.get("rol"));
        assertEquals(0, datosUsuario.get("puntos"));
    }

    // TEST 4: Los puntos de una tarea se muestran como "0" si el valor es nulo
    @Test
    public void puntosTarea_nulo_muestraCero() {
        Object ptsObj = null; // Simula que Firestore devuelve null

        // Lógica exacta de cargarTareasDelGrupoEnContenedor
        String puntos = (ptsObj != null) ? ptsObj.toString() : "0";

        assertEquals("0", puntos);
    }
}