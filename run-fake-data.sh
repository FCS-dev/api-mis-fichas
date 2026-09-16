#!/usr/bin/env bash
# ============================================================
# run-fake-data.sh — Carga datos de prueba en misfichasDB.
#
# Uso:
#   ./run-fake-data.sh
#
# El script solicitará la contraseña del usuario 'admin' de MariaDB
# y ejecutará src/main/resources/data-fake.sql contra la base de datos
# misfichasDB.
#
# NOTA: data-fake.sql asume que ya existen el usuario ADMIN, las
# categorías y las subcategorías de sistema (cargados normalmente por
# AdminSeeder al arrancar la aplicación). Ejecutar este script sobre
# una BD vacía sin esas tablas pobladas fallará por violaciones de FK.
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="${SCRIPT_DIR}/src/main/resources/data-fake.sql"

if [[ ! -f "${SQL_FILE}" ]]; then
    echo "ERROR: No se encontró ${SQL_FILE}" >&2
    exit 1
fi

echo "Cargando datos fake desde ${SQL_FILE}..."
echo "Se solicitará la contraseña del usuario 'admin' de MariaDB."
mysql -u admin -p misfichasDB < "${SQL_FILE}"

echo "Datos fake cargados correctamente en misfichasDB."
