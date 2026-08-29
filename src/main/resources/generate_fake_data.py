#!/usr/bin/env python3
"""
Genera data-fake.sql con datos de prueba para la BD misfichasDB.
Ejecutar: python3 generate_fake_data.py
Requiere: pip install bcrypt
"""

import random
import datetime
import bcrypt

random.seed(42)

# --- IDs hardcodeados (AdminSeeder) ---
CAT_INGRESOS = 1
CAT_GASTOS_FIJOS = 2
CAT_ALIMENTACION = 3
CAT_TRANSPORTE = 4
CAT_VIVIENDA = 5
CAT_SALUD = 6
CAT_EDUCACION = 7
CAT_COMPRAS = 8
CAT_OCIO = 9
CAT_FAMILIA = 10
CAT_FINANZAS = 11
CAT_DONACIONES = 12
CAT_ESPECIALES = 13

# Subcategorías INCOME
SUB_SALARIO = 1
SUB_HORAS_EXTRAS = 2
SUB_BONIFICACIONES = 3
SUB_FREELANCE = 4
SUB_NEGOCIO_PROPIO = 5
SUB_INVERSIONES_ING = 6
SUB_ALQUILERES_RECIBIDOS = 7
SUB_PENSIONES = 8
SUB_PRESTACIONES = 9
SUB_REGALOS_RECIBIDOS = 10
SUB_REEMBOLSOS = 11
SUB_VENTA_ARTICULOS = 12

# Subcategorías EXPENSE
SUB_ALQUILER_HIPO = 13
SUB_COMUNIDAD = 14
SUB_ELECTRICIDAD = 15
SUB_AGUA = 16
SUB_GAS = 17
SUB_INTERNET = 18
SUB_TELEFONIA = 19
SUB_SEGUROS = 20
SUB_IMPUESTOS = 21
SUB_SUSCRIPCIONES = 22
SUB_SUPERMERCADO = 23
SUB_RESTAURANTES = 24
SUB_COMIDA_RAPIDA = 25
SUB_CAFETERIAS = 26
SUB_DELIVERY = 27
SUB_SNACKS = 28
SUB_COMBUSTIBLE = 29
SUB_TRANSPORTE_PUBLICO = 30
SUB_TAXI = 31
SUB_APARCAMIENTO = 32
SUB_PEAJES = 33
SUB_MANT_VEHICULO = 34
SUB_SEGURO_VEHICULO = 35
SUB_ALQUILER_VEHICULOS = 36
SUB_MUEBLES = 37
SUB_ELECTRODOMESTICOS = 38
SUB_DECORACION = 39
SUB_REPARACIONES = 40
SUB_JARDINERIA = 41
SUB_LIMPIEZA = 42
SUB_MEDICO = 43
SUB_DENTISTA = 44
SUB_FARMACIA = 45
SUB_SEGURO_MEDICO = 46
SUB_TERAPIAS = 47
SUB_GIMNASIO = 48
SUB_BIENESTAR = 49
SUB_MATRICULAS = 50
SUB_CURSOS = 51
SUB_LIBROS = 52
SUB_MATERIAL_ESCOLAR = 53
SUB_CERTIFICACIONES = 54
SUB_ROPA = 55
SUB_CALZADO = 56
SUB_ACCESORIOS = 57
SUB_COSMETICA = 58
SUB_TECNOLOGIA = 59
SUB_ELECTRONICA = 60
SUB_CINE = 61
SUB_STREAMING = 62
SUB_VIDEOJUEGOS = 63
SUB_EVENTOS = 64
SUB_MUSICA = 65
SUB_HOBBIES = 66
SUB_VIAJES = 67
SUB_HIJOS = 68
SUB_GUARDERIA = 69
SUB_COLEGIOS = 70
SUB_OTROS_FAMILIARES = 71
SUB_MASCOTAS = 72
SUB_ALIM_MASCOTAS = 73
SUB_VETERINARIO = 74
SUB_AHORRO = 75
SUB_INVERSIONES = 76
SUB_PAGO_PRESTAMOS = 77
SUB_TARJETAS_CREDITO = 78
SUB_COMISIONES_BANCARIAS = 79
SUB_TRANSFERENCIAS = 80
SUB_DONACIONES = 81
SUB_REGALOS_REALIZADOS = 82
SUB_AYUDA_FAMILIAR = 83
SUB_EMERGENCIAS = 84
SUB_GASTOS_TRABAJO = 85
SUB_GASTOS_REEMBOLSABLES = 86
SUB_IMPREVISTOS = 87

SUBCAT_TO_CAT = {
    SUB_SALARIO: CAT_INGRESOS, SUB_HORAS_EXTRAS: CAT_INGRESOS,
    SUB_BONIFICACIONES: CAT_INGRESOS, SUB_FREELANCE: CAT_INGRESOS,
    SUB_NEGOCIO_PROPIO: CAT_INGRESOS, SUB_INVERSIONES_ING: CAT_INGRESOS,
    SUB_ALQUILERES_RECIBIDOS: CAT_INGRESOS, SUB_PENSIONES: CAT_INGRESOS,
    SUB_PRESTACIONES: CAT_INGRESOS, SUB_REGALOS_RECIBIDOS: CAT_INGRESOS,
    SUB_REEMBOLSOS: CAT_INGRESOS, SUB_VENTA_ARTICULOS: CAT_INGRESOS,
    SUB_ALQUILER_HIPO: CAT_GASTOS_FIJOS, SUB_COMUNIDAD: CAT_GASTOS_FIJOS,
    SUB_ELECTRICIDAD: CAT_GASTOS_FIJOS, SUB_AGUA: CAT_GASTOS_FIJOS,
    SUB_GAS: CAT_GASTOS_FIJOS, SUB_INTERNET: CAT_GASTOS_FIJOS,
    SUB_TELEFONIA: CAT_GASTOS_FIJOS, SUB_SEGUROS: CAT_GASTOS_FIJOS,
    SUB_IMPUESTOS: CAT_GASTOS_FIJOS, SUB_SUSCRIPCIONES: CAT_GASTOS_FIJOS,
    SUB_SUPERMERCADO: CAT_ALIMENTACION, SUB_RESTAURANTES: CAT_ALIMENTACION,
    SUB_COMIDA_RAPIDA: CAT_ALIMENTACION, SUB_CAFETERIAS: CAT_ALIMENTACION,
    SUB_DELIVERY: CAT_ALIMENTACION, SUB_SNACKS: CAT_ALIMENTACION,
    SUB_COMBUSTIBLE: CAT_TRANSPORTE, SUB_TRANSPORTE_PUBLICO: CAT_TRANSPORTE,
    SUB_TAXI: CAT_TRANSPORTE, SUB_APARCAMIENTO: CAT_TRANSPORTE,
    SUB_PEAJES: CAT_TRANSPORTE, SUB_MANT_VEHICULO: CAT_TRANSPORTE,
    SUB_SEGURO_VEHICULO: CAT_TRANSPORTE, SUB_ALQUILER_VEHICULOS: CAT_TRANSPORTE,
    SUB_MUEBLES: CAT_VIVIENDA, SUB_ELECTRODOMESTICOS: CAT_VIVIENDA,
    SUB_DECORACION: CAT_VIVIENDA, SUB_REPARACIONES: CAT_VIVIENDA,
    SUB_JARDINERIA: CAT_VIVIENDA, SUB_LIMPIEZA: CAT_VIVIENDA,
    SUB_MEDICO: CAT_SALUD, SUB_DENTISTA: CAT_SALUD, SUB_FARMACIA: CAT_SALUD,
    SUB_SEGURO_MEDICO: CAT_SALUD, SUB_TERAPIAS: CAT_SALUD,
    SUB_GIMNASIO: CAT_SALUD, SUB_BIENESTAR: CAT_SALUD,
    SUB_MATRICULAS: CAT_EDUCACION, SUB_CURSOS: CAT_EDUCACION,
    SUB_LIBROS: CAT_EDUCACION, SUB_MATERIAL_ESCOLAR: CAT_EDUCACION,
    SUB_CERTIFICACIONES: CAT_EDUCACION,
    SUB_ROPA: CAT_COMPRAS, SUB_CALZADO: CAT_COMPRAS,
    SUB_ACCESORIOS: CAT_COMPRAS, SUB_COSMETICA: CAT_COMPRAS,
    SUB_TECNOLOGIA: CAT_COMPRAS, SUB_ELECTRONICA: CAT_COMPRAS,
    SUB_CINE: CAT_OCIO, SUB_STREAMING: CAT_OCIO, SUB_VIDEOJUEGOS: CAT_OCIO,
    SUB_EVENTOS: CAT_OCIO, SUB_MUSICA: CAT_OCIO, SUB_HOBBIES: CAT_OCIO,
    SUB_VIAJES: CAT_OCIO,
    SUB_HIJOS: CAT_FAMILIA, SUB_GUARDERIA: CAT_FAMILIA,
    SUB_COLEGIOS: CAT_FAMILIA, SUB_OTROS_FAMILIARES: CAT_FAMILIA,
    SUB_MASCOTAS: CAT_FAMILIA, SUB_ALIM_MASCOTAS: CAT_FAMILIA,
    SUB_VETERINARIO: CAT_FAMILIA,
    SUB_AHORRO: CAT_FINANZAS, SUB_INVERSIONES: CAT_FINANZAS,
    SUB_PAGO_PRESTAMOS: CAT_FINANZAS, SUB_TARJETAS_CREDITO: CAT_FINANZAS,
    SUB_COMISIONES_BANCARIAS: CAT_FINANZAS, SUB_TRANSFERENCIAS: CAT_FINANZAS,
    SUB_DONACIONES: CAT_DONACIONES, SUB_REGALOS_REALIZADOS: CAT_DONACIONES,
    SUB_AYUDA_FAMILIAR: CAT_DONACIONES,
    SUB_EMERGENCIAS: CAT_ESPECIALES, SUB_GASTOS_TRABAJO: CAT_ESPECIALES,
    SUB_GASTOS_REEMBOLSABLES: CAT_ESPECIALES, SUB_IMPREVISTOS: CAT_ESPECIALES,
}

EXPENSE_SUBS_BY_CAT = {}
for sub_id, cat_id in SUBCAT_TO_CAT.items():
    if cat_id != CAT_INGRESOS:
        EXPENSE_SUBS_BY_CAT.setdefault(cat_id, []).append(sub_id)

INCOME_SUBS = [SUB_SALARIO, SUB_HORAS_EXTRAS, SUB_BONIFICACIONES,
               SUB_FREELANCE, SUB_NEGOCIO_PROPIO, SUB_INVERSIONES_ING,
               SUB_ALQUILERES_RECIBIDOS, SUB_PENSIONES, SUB_PRESTACIONES,
               SUB_REGALOS_RECIBIDOS, SUB_REEMBOLSOS, SUB_VENTA_ARTICULOS]

USERS_DATA = [
    ("Carlos García", "carlos.garcia", 1),
    ("María López", "maria.lopez", 1),
    ("Juan Martínez", "juan.martinez", 1),
    ("Ana Rodríguez", "ana.rodriguez", 1),
    ("Pedro Sánchez", "pedro.sanchez", 1),
    ("Laura Fernández", "laura.fernandez", 2),
    ("Diego Torres", "diego.torres", 2),
    ("Sofía Díaz", "sofia.diaz", 2),
    ("Andrés Ruiz", "andres.ruiz", 2),
    ("Valentina Morales", "valentina.morales", 2),
    ("Martín Castro", "martin.castro", 3),
    ("Camila Vargas", "camila.vargas", 3),
    ("Lucas Herrera", "lucas.herrera", 3),
    ("Isabella Moreno", "isabella.moreno", 3),
    ("Felipe Ríos", "felipe.rios", 3),
    ("Gabriela Muñoz", "gabriela.munoz", 4),
    ("Nicolás Romero", "nicolas.romero", 4),
    ("Paula Álvarez", "paula.alvarez", 4),
    ("Alejandro Silva", "alejandro.silva", 4),
    ("Daniela Cruz", "daniela.cruz", 4),
    ("Roberto Reyes", "roberto.reyes", 5),
    ("Claudia Peña", "claudia.pena", 5),
    ("Fernando Ortiz", "fernando.ortiz", 5),
    ("Patricia Navarro", "patricia.navarro", 5),
    ("Mauricio Flores", "mauricio.flores", 5),
    ("Carolina Jiménez", "carolina.jimenez", 5),
    ("Oscar Medina", "oscar.medina", 6),
    ("Diana Paredes", "diana.paredes", 6),
    ("Enrique Suárez", "enrique.suarez", 6),
    ("Mónica Vega", "monica.vega", 6),
    ("Ricardo Campos", "ricardo.campos", 6),
    ("Laura Mendoza", "laura.mendoza", 6),
    ("Arturo Rojas", "arturo.rojas", 7),
    ("Alejandra Delgado", "alejandra.delgado", 7),
    ("Sergio Guerrero", "sergio.guerrero", 7),
    ("Natalie Cortés", "natalie.cortes", 7),
    ("Emilio Contreras", "emilio.contreras", 7),
    ("Beatriz Luna", "beatriz.luna", 7),
    ("Pablo Salazar", "pablo.salazar", 8),
    ("Cristina Herrera", "cristina.herrera", 8),
    ("Tomás Aguilar", "tomas.aguilar", 8),
    ("Jessica Miranda", "jessica.miranda", 8),
    ("Adrián Castillo", "adrian.castillo", 8),
]

INCOME_DESCRIPTIONS = [
    "Cobro mensual del salario",
    "Ingreso por servicios profesionales",
    "Bonificación por desempeño",
    "Pago por horas extraordinarias",
    "Trabajo independiente",
    "Ingreso por alquiler de propiedad",
    "Rendimiento de inversiones",
    "Venta de artículos personales",
    "Reembolso de gastos",
    "Pensión mensual",
    "Regalo en efectivo",
    "Comisión por ventas",
]

EXPENSE_DESCRIPTIONS = {
    CAT_GASTOS_FIJOS: [
        "Pago de alquiler mensual", "Servicio de electricidad",
        "Cuenta de agua", "Factura de gas", "Servicio de internet",
        "Plan de telefonía móvil", "Cuota de seguro del hogar",
        "Pago de impuestos anuales", "Suscripción a streaming", "Cuota de comunidad",
    ],
    CAT_ALIMENTACION: [
        "Compra semanal del supermercado", "Cena en restaurante",
        "Almuerzo rápido", "Café con compañeros", "Pedido a domicilio",
        "Snacks y bebidas varias", "Compra de frutas y verduras", "Cena especial",
    ],
    CAT_TRANSPORTE: [
        "Carga de combustible", "Boleto de transporte público",
        "Traslado en taxi", "Estacionamiento en centro comercial",
        "Pago de peaje", "Mantenimiento preventivo del auto", "Pago de seguro vehicular",
    ],
    CAT_VIVIENDA: [
        "Compra de mueble nuevo", "Electrodoméstico para el hogar",
        "Artículo de decoración", "Reparación de grifería",
        "Servicio de jardinería", "Compra de productos de limpieza",
    ],
    CAT_SALUD: [
        "Consulta médica general", "Control odontológico",
        "Compra de medicamentos", "Pago de seguro médico",
        "Sesión de terapia", "Cuota mensual del gimnasio", "Producto de bienestar personal",
    ],
    CAT_EDUCACION: [
        "Pago de matrícula", "Curso online especializado",
        "Compra de libros de texto", "Material escolar", "Examen de certificación profesional",
    ],
    CAT_COMPRAS: [
        "Compra de ropa nueva", "Par de zapatos", "Accesorio personal",
        "Producto de cosmética", "Dispositivo tecnológico", "Artículo electrónico",
    ],
    CAT_OCIO: [
        "Entradas al cine", "Suscripción mensual streaming",
        "Videojuego nuevo", "Entrada a evento deportivo",
        "Disco de música", "Material para hobby", "Viaje de fin de semana",
    ],
    CAT_FAMILIA: [
        "Gasto para los hijos", "Cuota de guardería", "Pago de colegio",
        "Ayuda a familiar", "Compra para mascota",
        "Alimentación de mascota", "Consulta veterinaria",
    ],
    CAT_FINANZAS: [
        "Transferencia a cuenta de ahorro", "Inversión en fondo",
        "Pago cuota préstamo", "Pago tarjeta de crédito",
        "Comisión bancaria", "Transferencia a tercero",
    ],
    CAT_DONACIONES: [
        "Donación a organización benéfica", "Regalo para amigo", "Ayuda familiar directa",
    ],
    CAT_ESPECIALES: [
        "Gasto de emergencia", "Gasto relacionado con trabajo",
        "Gasto reembolsable pendiente", "Gasto imprevisto del mes",
    ],
}


def hash_password(email):
    return bcrypt.hashpw(email.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")


def days_in_month(year, month):
    if month == 12:
        return 31
    return (datetime.date(year, month + 1, 1) - datetime.date(year, month, 1)).days


def generate_users():
    users = []
    for i, (name, email_prefix, month) in enumerate(USERS_DATA):
        email = f"{email_prefix}@prueba.fcs"
        pwd_hash = hash_password(email)
        created = datetime.datetime(2026, month, 1, 0, 0, 0)
        users.append({
            "id": i + 2,
            "name": name,
            "email": email,
            "password_hash": pwd_hash,
            "role": "USER",
            "status": "ACTIVE",
            "created_at": created,
            "updated_at": created,
        })
    return users


def generate_transactions(users):
    transactions = []
    tx_id = 1

    for user in users:
        user_id = user["id"]
        start_month = user["created_at"].month
        start_year = user["created_at"].year
        current_year = start_year
        current_month = start_month

        while (current_year, current_month) <= (2026, 8):
            num_tx = random.randint(10, 15)
            num_income = random.randint(1, min(5, num_tx - 1))
            num_expense = num_tx - num_income

            month_income_total = 0
            income_txs = []
            for _ in range(num_income):
                if random.random() < 0.5:
                    sub_id = SUB_SALARIO
                    cat_id = CAT_INGRESOS
                    amount = round(random.uniform(500, 2000), 2)
                else:
                    sub_id = random.choice(INCOME_SUBS)
                    cat_id = CAT_INGRESOS
                    amount = round(random.uniform(200, 2000), 2)

                day = random.randint(1, 10)
                max_days = days_in_month(current_year, current_month)
                day = min(day, max_days)
                tx_date = datetime.date(current_year, current_month, day)

                offset_days = random.randint(0, 3)
                tx_created = datetime.datetime(
                    tx_date.year, tx_date.month, tx_date.day,
                    random.randint(8, 18), random.randint(0, 59), random.randint(0, 59)
                ) + datetime.timedelta(days=offset_days)
                if tx_created.month != tx_date.month:
                    tx_created = tx_created.replace(
                        day=days_in_month(tx_created.year, tx_created.month),
                        hour=23, minute=59, second=59
                    )

                desc = random.choice(INCOME_DESCRIPTIONS)
                income_txs.append({
                    "id": tx_id, "user_id": user_id, "category_id": cat_id,
                    "subcategory_id": sub_id, "amount": amount, "description": desc,
                    "transaction_date": tx_date, "created_at": tx_created, "updated_at": tx_created,
                })
                month_income_total += amount
                tx_id += 1

            expense_txs = []
            priority_expense_cats = [CAT_GASTOS_FIJOS, CAT_ALIMENTACION]

            for j in range(num_expense):
                if j < 2 and num_expense >= 2:
                    cat_id = priority_expense_cats[j % 2]
                elif random.random() < 0.4:
                    cat_id = random.choice(priority_expense_cats)
                else:
                    cat_id = random.choice(list(EXPENSE_SUBS_BY_CAT.keys()))

                subs = EXPENSE_SUBS_BY_CAT[cat_id]
                sub_id = random.choice(subs)

                max_allowed = month_income_total * 0.9
                remaining_budget = max_allowed - sum(t["amount"] for t in expense_txs)
                if remaining_budget < 5:
                    break

                if cat_id == CAT_GASTOS_FIJOS:
                    amount = round(random.uniform(50, 800), 2)
                    if sub_id == SUB_ALQUILER_HIPO:
                        amount = round(random.uniform(400, 900), 2)
                elif cat_id == CAT_ALIMENTACION:
                    amount = round(random.uniform(30, 500), 2)
                    if sub_id == SUB_SUPERMERCADO:
                        amount = round(random.uniform(100, 400), 2)
                else:
                    amount = round(random.uniform(5, 300), 2)

                if amount > remaining_budget:
                    amount = round(remaining_budget * 0.8, 2)
                if amount < 5:
                    amount = 5.00

                max_days = days_in_month(current_year, current_month)
                min_day = min(10, max_days)
                day = random.randint(min_day, max_days)
                tx_date = datetime.date(current_year, current_month, day)

                offset_days = random.randint(0, 3)
                tx_created = datetime.datetime(
                    tx_date.year, tx_date.month, tx_date.day,
                    random.randint(8, 18), random.randint(0, 59), random.randint(0, 59)
                ) + datetime.timedelta(days=offset_days)
                if tx_created.month != tx_date.month:
                    tx_created = tx_created.replace(
                        day=days_in_month(tx_created.year, tx_created.month),
                        hour=23, minute=59, second=59
                    )

                descs = EXPENSE_DESCRIPTIONS.get(cat_id, ["Gasto del mes"])
                desc = random.choice(descs)

                expense_txs.append({
                    "id": tx_id, "user_id": user_id, "category_id": cat_id,
                    "subcategory_id": sub_id, "amount": amount, "description": desc,
                    "transaction_date": tx_date, "created_at": tx_created, "updated_at": tx_created,
                })
                tx_id += 1

            transactions.extend(income_txs)
            transactions.extend(expense_txs)

            current_month += 1
            if current_month > 12:
                current_month = 1
                current_year += 1

    return transactions


def format_datetime(dt):
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def format_date(d):
    return d.strftime("%Y-%m-%d")


def escape_sql(s):
    return s.replace("'", "\\'")


def generate_sql(users, transactions):
    lines = []
    lines.append("-- ============================================================")
    lines.append("-- data-fake.sql — Datos de prueba para misfichasDB")
    lines.append("-- Generado automáticamente por generate_fake_data.py")
    lines.append("-- Ejecutar: mysql -u admin -p misfichasDB < data-fake.sql")
    lines.append("-- ============================================================")
    lines.append("")
    lines.append("")

    lines.append("-- ============================================================")
    lines.append("-- USERS")
    lines.append("-- ============================================================")
    lines.append("")
    lines.append("INSERT INTO users (id, email, password_hash, name, role, status, created_at, updated_at, deleted_at)")
    lines.append("VALUES")

    user_values = []
    for u in users:
        user_values.append(
            f"  ({u['id']}, '{escape_sql(u['email'])}', '{escape_sql(u['password_hash'])}', "
            f"'{escape_sql(u['name'])}', '{u['role']}', '{u['status']}', "
            f"'{format_datetime(u['created_at'])}', '{format_datetime(u['updated_at'])}', NULL)"
        )
    lines.append(",\n".join(user_values) + ";")
    lines.append("")

    lines.append("-- ============================================================")
    lines.append("-- TRANSACTIONS")
    lines.append("-- ============================================================")
    lines.append("")
    lines.append("INSERT INTO transactions (id, user_id, category_id, subcategory_id, amount, description, transaction_date, created_at, updated_at, deleted_at)")
    lines.append("VALUES")

    tx_values = []
    for tx in transactions:
        desc = f"'{escape_sql(tx['description'])}'" if tx["description"] else "NULL"
        tx_values.append(
            f"  ({tx['id']}, {tx['user_id']}, {tx['category_id']}, {tx['subcategory_id']}, "
            f"{tx['amount']:.2f}, {desc}, '{format_date(tx['transaction_date'])}', "
            f"'{format_datetime(tx['created_at'])}', '{format_datetime(tx['updated_at'])}', NULL)"
        )
    lines.append(",\n".join(tx_values) + ";")
    lines.append("")

    lines.append("-- ============================================================")
    lines.append("-- RESUMEN")
    lines.append("-- ============================================================")
    lines.append(f"-- Usuarios creados: {len(users)}")
    lines.append(f"-- Transacciones creadas: {len(transactions)}")
    lines.append("")

    return "\n".join(lines)


def main():
    print("Generando usuarios...")
    users = generate_users()
    print(f"  {len(users)} usuarios generados")

    print("Generando transacciones...")
    transactions = generate_transactions(users)
    print(f"  {len(transactions)} transacciones generadas")

    print("Verificando balances...")
    alerts = 0
    for user in users:
        uid = user["id"]
        cy, cm = user["created_at"].year, user["created_at"].month
        while (cy, cm) <= (2026, 8):
            user_month_txs = [
                t for t in transactions
                if t["user_id"] == uid
                and t["transaction_date"].year == cy
                and t["transaction_date"].month == cm
            ]
            income = sum(t["amount"] for t in user_month_txs if t["category_id"] == CAT_INGRESOS)
            expense = sum(t["amount"] for t in user_month_txs if t["category_id"] != CAT_INGRESOS)
            if expense > income:
                print(f"  ALERTA: User {uid} mes {cy}-{cm:02d}: income={income:.2f} < expense={expense:.2f}")
                alerts += 1
            cm += 1
            if cm > 12:
                cm = 1
                cy += 1
    if alerts == 0:
        print("  Todos los balances son correctos (expense < income)")

    print("Generando SQL...")
    sql = generate_sql(users, transactions)

    output_path = "data-fake.sql"
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(sql)
    print(f"Archivo generado: {output_path}")
    print(f"Tamaño: {len(sql):,} caracteres")


if __name__ == "__main__":
    main()
