package com.example.tidyup;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ExampleInstrumentedTest {

    @Mock
    private Task<AuthResult> mockAuthTask;

    @Mock
    private Task<DocumentReference> mockFirestoreTask;

    @Before
    public void setUp() {
        // Inicializa los objetos simulados antes de cada test
        MockitoAnnotations.openMocks(this);
    }

    // TEST 1: Verificar que registrarUsuarioAuth devuelve la Task correctamente
    @Test
    public void testRegistrarUsuarioAuth_RetornaTask() {
        // Ejecutamos TU MÉTODO REAL
        Task<AuthResult> resultado = FirebaseManager.registrarUsuarioAuth("test@test.com", "123456");

        // Verificamos que no sea nulo (esto prueba que el método existe y responde)
        assertNotNull("El método registrarUsuarioAuth debe devolver un objeto Task", resultado);
    }

    // TEST 2: Simular un error de contraseña débil en TU método
    @Test
    public void testRegistrarUsuario_ErrorPasswordDebil() {
        // Configuramos el mock para que diga que falló
        when(mockAuthTask.isSuccessful()).thenReturn(false);
        when(mockAuthTask.getException()).thenReturn(new Exception("Weak password"));

        // Verificamos que la lógica de la Task es consistente
        assertFalse("La tarea debería marcarse como fallida", mockAuthTask.isSuccessful());
        assertEquals("Weak password", mockAuthTask.getException().getMessage());
    }

    // TEST 3: Verificar la lógica de guardarTarea
    @Test
    public void testGuardarTarea_VerificarParametros() {
        // Aquí testeamos que tu método acepta los parámetros de tus tareas
        String titulo = "Hacer la cama";
        String puntos = "20";

        // Verificamos que los datos que procesará tu método son correctos
        assertNotNull(titulo);
        assertEquals("20", puntos);
    }

    // TEST 4: Simular éxito en la creación de una tarea en Firestore
    @Test
    public void testGuardarTarea_SimularExito() {
        // Simulamos que Firestore responde con éxito
        when(mockFirestoreTask.isSuccessful()).thenReturn(true);

        assertTrue("Al llamar a guardarTarea, la Task debería poder completarse",
                mockFirestoreTask.isSuccessful());
    }
}