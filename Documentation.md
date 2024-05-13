# Mondongo File System

## Introducción

Mondongo File System (MFS) es un sistema de almacenamiento distribuido basado en como funciona el sistema de archivos de Google (GFS).

A diferencia de GFS, MFS almacena archivos completos en lugar de fragmentos de archivos, proporcionando un enfoque simplificado para el almacenamiento y la recuperación de archivos.

Al ser un sistema distribuido, MFS esta diseñado para proporcionar una solución escalable y tolerante a fallos para el almacenamiento de archivos.

### Convenciones del documento:

- ***Texto en negrita y cursiva:*** indica nombres de clases y metodos.
- **Texto en negrita:** Indica terminos o secciones importantes.
- `Texto en bloque`: Representa lineas o bloques de codigo.

## Arquitectura del sistema

### Arquitectura de alto nivel

MFS consta de tres componentes principales:

- **Cliente:** Realiza las solicitudes de carga y descarga de archivos.
- **Servidor Maestro:**

### Tecnologías Utilizadas

- **Java:** Lenguaje de programación utilizado para implementar el sistema
- **RMI:** Invocación de metodos remotos para la comunicación entre clientes y servidores.

