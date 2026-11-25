# Induztek Backend

## Requisitos

- JDK 17 o superior (el proyecto compila con `java.version=17`).
- MySQL de Laragon disponible en `localhost:3306`.
- Maven 3.8 o superior.

Docker no es necesario. Los servicios crean sus bases de datos al conectarse si el usuario de MySQL tiene permisos para crearlas.

## MySQL con Laragon

1. Inicia Laragon y enciende MySQL.
2. La configuración inicial usa `root` sin contraseña, que es el valor habitual de Laragon.
3. Si tu instalación tiene otras credenciales, define `DB_USERNAME` y `DB_PASSWORD` en la terminal desde la que ejecutarás cada servicio. También puedes cambiar `DB_HOST` y `DB_PORT`.

En PowerShell, antes de iniciar los servicios:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = ""
```

Se crean estas bases de datos por separado: `induztek_usuarios`, `induztek_equipos` e `induztek_protocolos`.

## Iniciar los microservicios

Abre una terminal en la carpeta raíz del proyecto y ejecuta cada comando en una terminal distinta, en este orden:

```powershell
mvn -pl eureka-server spring-boot:run
mvn -pl usuarios-service spring-boot:run
mvn -pl equipos-service spring-boot:run
mvn -pl protocolos-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

Espera a que Eureka esté disponible en `http://localhost:8761` antes de iniciar los servicios. La app Android se conecta al gateway en el puerto `8080`; no se conecta directamente a MySQL.

| Servicio | Puerto |
| --- | ---: |
| Eureka | 8761 |
| API Gateway (Android) | 8080 |
| Usuarios | 8081 |
| Equipos | 8082 |
| Protocolos | 8083 |
| MySQL Laragon | 3306 |

Para comprobar los tests del backend:

```powershell
mvn test
```

## Conexión Android

- Emulador de Android Studio: `http://10.0.2.2:8080/` (valor predeterminado).
- Teléfono físico: configura `API_BASE_URL` en el proyecto Android como `http://<IP-LAN-DE-TU-PC>:8080/`. El teléfono y el PC deben estar en la misma red; permite el puerto `8080` en el firewall de Windows si hace falta.

HTTP sin TLS está habilitado únicamente para desarrollo en una red local. No uses esta configuración para publicar el backend en Internet.

## Versiones

- Java 17 y Spring Boot 3.3.5.
- Spring Cloud 2023.0.3.
- MySQL Connector/J gestionado por Spring Boot.

Estas versiones se mantienen juntas como en el proyecto original; JDK 21 también puede ejecutar el bytecode de Java 17.
