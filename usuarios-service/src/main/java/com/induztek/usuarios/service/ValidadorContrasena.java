package com.induztek.usuarios.service;

public final class ValidadorContrasena {

    private ValidadorContrasena() {
    }

    public static String validar(String clave) {
        if (clave == null || clave.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres";
        }
        if (clave.chars().anyMatch(Character::isWhitespace)) {
            return "La contraseña no puede tener espacios en blanco";
        }
        if (clave.chars().noneMatch(Character::isUpperCase)) {
            return "La contraseña debe tener al menos una letra mayúscula";
        }
        if (clave.chars().noneMatch(Character::isLowerCase)) {
            return "La contraseña debe tener al menos una letra minúscula";
        }
        if (clave.chars().noneMatch(Character::isDigit)) {
            return "La contraseña debe tener al menos un número";
        }
        if (clave.chars().allMatch(Character::isLetterOrDigit)) {
            return "La contraseña debe tener al menos un carácter especial";
        }
        return null;
    }
}
