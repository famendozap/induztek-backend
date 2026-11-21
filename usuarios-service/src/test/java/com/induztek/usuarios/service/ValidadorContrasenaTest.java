package com.induztek.usuarios.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ValidadorContrasenaTest {

    @ParameterizedTest
    @CsvSource({
            "Ab1$xyz,8 caracteres",
            "Abcdef1$ x,espacios en blanco",
            "abcdef1$x,mayúscula",
            "ABCDEF1$X,minúscula",
            "Abcdefg$x,número",
            "Abcdefg1x,carácter especial"
    })
    void rechazaClavesQueIncumplenUnaRegla(String clave, String fragmentoDelMensaje) {
        String error = ValidadorContrasena.validar(clave);
        assertTrue(error.contains(fragmentoDelMensaje));
    }

    @Test
    void rechazaClaveNula() {
        assertTrue(ValidadorContrasena.validar(null).contains("8 caracteres"));
    }

    @Test
    void aceptaClaveQueCumpleTodasLasReglas() {
        assertNull(ValidadorContrasena.validar("Induztek#2026"));
    }
}
