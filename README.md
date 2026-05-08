# Documentación Técnica: TidyUp

Guía de sistema, instalación y credenciales para el tribunal

---

## 1. Requisitos del Sistema

Especificaciones técnicas detalladas para la correcta ejecución y desarrollo del proyecto:

| Elemento | Detalle Técnico |
|---|---|
| Versión Android (Min SDK) | Android 7.0 (Nougat, API 24) |
| Versión Android (Target SDK) | Android 14 (API 34) |
| Android Gradle Plugin (AGP) | 8.1.4 |
| Gradle Wrapper | 8.0 |
| IDE Recomendado | Android Studio Giraffe \| 2022.3.1 Patch 4 |
| Java / Runtime | JDK 17 / Compatibility VERSION_1_8 |
| Espacio en Disco Mínimo | 500 MB (para compilación y caché) |

---

## 2. Instrucciones de Compilación e Instalación

### 2.1. Obtención del Código Fuente

Clone el repositorio oficial desde GitHub utilizando el siguiente comando:

```bash
git clone https://github.com/Jaime3142/TidyUp
```

### 2.2. Configuración en Android Studio

1. Inicie Android Studio Giraffe.
2. Seleccione **File > Open** y abra la carpeta raíz del proyecto clonado.
3. Permita que el IDE realice la sincronización de Gradle (necesario para descargar dependencias como Firebase BoM y Material Components).
4. **Claves de API:** Asegúrese de que el archivo `google-services.json` (proporcionado con el proyecto) esté ubicado en la ruta `/app/google-services.json` para habilitar la conexión con Firebase.

### 2.3. Despliegue

Conecte un dispositivo físico o inicie un emulador (API 24+) y presione el botón **Run ('app')** en la barra de herramientas superior.

---

## 3. Credenciales de Evaluación

> **Nota para el tribunal:** Se han pre-registrado cuentas con diferentes roles para facilitar la revisión de la lógica de negocio y las interfaces personalizadas.

**Contraseña única para todas las cuentas:** `Davante`

| Perfil de Usuario | Correo Electrónico de Prueba |
|---|---|
| Adulto (Administrador) | example@gmail.com |
| Adolescente (Perfil Principal) | example1@gmail.com |
| Adolescente (Perfil Secundario) | example2@gmail.com |
| Mayores | example3@gmail.com |

---

## 4. Información Adicional del Proyecto

### Tecnologías Principales

- **Firebase Authentication & Firestore:** Gestión de usuarios y base de datos NoSQL en tiempo real.
- **Material Design 1.13.0:** Sistema de diseño para una interfaz moderna y accesible.
- **Mockito & JUnit:** Entorno de pruebas unitarias e instrumentadas integrado.

