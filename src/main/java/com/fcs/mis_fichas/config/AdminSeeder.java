package com.fcs.mis_fichas.config;

import com.fcs.mis_fichas.entities.Category;
import com.fcs.mis_fichas.entities.Subcategory;
import com.fcs.mis_fichas.entities.Transaction;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.enums.Type;
import com.fcs.mis_fichas.repositories.CategoryRepository;
import com.fcs.mis_fichas.repositories.SubcategoryRepository;
import com.fcs.mis_fichas.repositories.TransactionRepository;
import com.fcs.mis_fichas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Configuración de inicialización de datos del sistema.
 * Se ejecuta al arrancar la aplicacion y crea:
 * - Un usuario ADMIN por defecto si no existe
 * - Categorias y subcategorias de sistema si no existen
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    /**
     * Bean CommandLineRunner que ejecuta la semilla al inicio de la aplicación.
     * Crea el usuario admin y luego las categorias/subcategorias de sistema.
     *
     * @return instancia de CommandLineRunner
     */
    @Bean
    @Transactional
    CommandLineRunner seedAdmin() {
        return args -> {
            User admin = createAdminIfNotExists();
            seedCategoriesAndSubcategories(admin);
            seedFakeDataIfDeveloper();
        };
    }

    /**
     * Crea el usuario ADMIN por defecto si no existe en la base de datos.
     * Email: (Secret obtenido del .env)
     * Password: (Secret obtenido del .env) -hasheada con BCrypt-
     *
     * @return entidad User del usuario admin
     */
    private User createAdminIfNotExists() {
        return userRepository.findByEmailAndDeletedAtIsNull(adminEmail)
                .orElseGet(() -> {
                    User admin = User.builder()
                            .email(adminEmail)
                            .passwordHash(passwordEncoder.encode(adminPassword))
                            .name("Administrador")
                            .role(Role.ADMIN)
                            .status(Status.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .deletedAt(null)
                            .build();
                    userRepository.save(admin);
                    log.info("Usuario ADMIN creado: {}", adminEmail);
                    return admin;
                });
    }

    /**
     * Crea las categorías y subcategorías de sistema si no existen.
     * Incluye categorías de INCOME y EXPENSE con sus respectivas subcategorías.
     *
     * @param admin usuario admin que se registra como creador
     */
    private void seedCategoriesAndSubcategories(User admin) {
        // INCOME - Ingresos generales
        Category income = createCategoryIfNotExists("Ingresos generales", Type.INCOME, admin);
        createSubcategoryIfNotExists(income, "Salario", true, admin);
        createSubcategoryIfNotExists(income, "Horas extras", true, admin);
        createSubcategoryIfNotExists(income, "Bonificaciones y comisiones", true, admin);
        createSubcategoryIfNotExists(income, "Trabajo autónomo / freelance", true, admin);
        createSubcategoryIfNotExists(income, "Negocio propio", true, admin);
        createSubcategoryIfNotExists(income, "Inversiones (dividendos, intereses)", true, admin);
        createSubcategoryIfNotExists(income, "Alquileres recibidos", true, admin);
        createSubcategoryIfNotExists(income, "Pensiones", true, admin);
        createSubcategoryIfNotExists(income, "Prestaciones y ayudas", true, admin);
        createSubcategoryIfNotExists(income, "Regalos recibidos", true, admin);
        createSubcategoryIfNotExists(income, "Reembolsos", true, admin);
        createSubcategoryIfNotExists(income, "Venta de artículos", true, admin);

        // EXPENSE - Gastos fijos
        Category gastosFijos = createCategoryIfNotExists("Gastos fijos", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(gastosFijos, "Alquiler / Hipoteca", true, admin);
        createSubcategoryIfNotExists(gastosFijos, "Comunidad de propietarios", true, admin);
        createSubcategoryIfNotExists(gastosFijos, "Electricidad", true, admin);
        createSubcategoryIfNotExists(gastosFijos, "Agua", true, admin);
        createSubcategoryIfNotExists(gastosFijos, "Gas", true, admin);
        createSubcategoryIfNotExists(gastosFijos, "Internet", true, admin);
        createSubcategoryIfNotExists(gastosFijos, "Telefonía móvil", true, admin);
        createSubcategoryIfNotExists(gastosFijos, "Seguros", true, admin);
        createSubcategoryIfNotExists(gastosFijos, "Impuestos y tasas", true, admin);
        createSubcategoryIfNotExists(gastosFijos, "Suscripciones (Netflix, Spotify, etc.)", true, admin);

        // EXPENSE - Alimentación
        Category alimentacion = createCategoryIfNotExists("Alimentación", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(alimentacion, "Supermercado", true, admin);
        createSubcategoryIfNotExists(alimentacion, "Restaurantes", true, admin);
        createSubcategoryIfNotExists(alimentacion, "Comida rápida", true, admin);
        createSubcategoryIfNotExists(alimentacion, "Cafeterías", true, admin);
        createSubcategoryIfNotExists(alimentacion, "Delivery", true, admin);
        createSubcategoryIfNotExists(alimentacion, "Snacks y bebidas", true, admin);

        // EXPENSE - Transporte
        Category transporte = createCategoryIfNotExists("Transporte", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(transporte, "Combustible", true, admin);
        createSubcategoryIfNotExists(transporte, "Transporte público", true, admin);
        createSubcategoryIfNotExists(transporte, "Taxi / VTC", true, admin);
        createSubcategoryIfNotExists(transporte, "Aparcamiento", true, admin);
        createSubcategoryIfNotExists(transporte, "Peajes", true, admin);
        createSubcategoryIfNotExists(transporte, "Mantenimiento del vehículo", true, admin);
        createSubcategoryIfNotExists(transporte, "Seguro del vehículo", true, admin);
        createSubcategoryIfNotExists(transporte, "Alquiler de vehículos", true, admin);

        // EXPENSE - Vivienda y hogar
        Category vivienda = createCategoryIfNotExists("Vivienda y hogar", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(vivienda, "Muebles", true, admin);
        createSubcategoryIfNotExists(vivienda, "Electrodomésticos", true, admin);
        createSubcategoryIfNotExists(vivienda, "Decoración", true, admin);
        createSubcategoryIfNotExists(vivienda, "Reparaciones", true, admin);
        createSubcategoryIfNotExists(vivienda, "Jardinería", true, admin);
        createSubcategoryIfNotExists(vivienda, "Productos de limpieza", true, admin);

        // EXPENSE - Salud
        Category salud = createCategoryIfNotExists("Salud", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(salud, "Médico", true, admin);
        createSubcategoryIfNotExists(salud, "Dentista", true, admin);
        createSubcategoryIfNotExists(salud, "Farmacia", true, admin);
        createSubcategoryIfNotExists(salud, "Seguro médico", true, admin);
        createSubcategoryIfNotExists(salud, "Terapias", true, admin);
        createSubcategoryIfNotExists(salud, "Gimnasio", true, admin);
        createSubcategoryIfNotExists(salud, "Bienestar", true, admin);

        // EXPENSE - Educación
        Category educacion = createCategoryIfNotExists("Educación", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(educacion, "Matrículas", true, admin);
        createSubcategoryIfNotExists(educacion, "Cursos", true, admin);
        createSubcategoryIfNotExists(educacion, "Libros", true, admin);
        createSubcategoryIfNotExists(educacion, "Material escolar", true, admin);
        createSubcategoryIfNotExists(educacion, "Certificaciones", true, admin);

        // EXPENSE - Compras personales
        Category compras = createCategoryIfNotExists("Compras personales", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(compras, "Ropa", true, admin);
        createSubcategoryIfNotExists(compras, "Calzado", true, admin);
        createSubcategoryIfNotExists(compras, "Accesorios", true, admin);
        createSubcategoryIfNotExists(compras, "Cosmética", true, admin);
        createSubcategoryIfNotExists(compras, "Tecnología", true, admin);
        createSubcategoryIfNotExists(compras, "Electrónica", true, admin);

        // EXPENSE - Ocio y entretenimiento
        Category ocio = createCategoryIfNotExists("Ocio y entretenimiento", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(ocio, "Cine", true, admin);
        createSubcategoryIfNotExists(ocio, "Streaming", true, admin);
        createSubcategoryIfNotExists(ocio, "Videojuegos", true, admin);
        createSubcategoryIfNotExists(ocio, "Eventos", true, admin);
        createSubcategoryIfNotExists(ocio, "Música", true, admin);
        createSubcategoryIfNotExists(ocio, "Hobbies", true, admin);
        createSubcategoryIfNotExists(ocio, "Viajes", true, admin);

        // EXPENSE - Familia y mascotas
        Category familia = createCategoryIfNotExists("Familia y mascotas", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(familia, "Hijos", true, admin);
        createSubcategoryIfNotExists(familia, "Guardería", true, admin);
        createSubcategoryIfNotExists(familia, "Colegios", true, admin);
        createSubcategoryIfNotExists(familia, "Otros familiares", true, admin);
        createSubcategoryIfNotExists(familia, "Mascotas", true, admin);
        createSubcategoryIfNotExists(familia, "Alimentación de mascotas", true, admin);
        createSubcategoryIfNotExists(familia, "Veterinario", true, admin);

        // EXPENSE - Finanzas
        Category finanzas = createCategoryIfNotExists("Finanzas", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(finanzas, "Ahorro", true, admin);
        createSubcategoryIfNotExists(finanzas, "Inversiones", true, admin);
        createSubcategoryIfNotExists(finanzas, "Pago de préstamos", true, admin);
        createSubcategoryIfNotExists(finanzas, "Tarjetas de crédito", true, admin);
        createSubcategoryIfNotExists(finanzas, "Comisiones bancarias", true, admin);
        createSubcategoryIfNotExists(finanzas, "Transferencias", true, admin);

        // EXPENSE - Donaciones y regalos
        Category donaciones = createCategoryIfNotExists("Donaciones y regalos", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(donaciones, "Donaciones", true, admin);
        createSubcategoryIfNotExists(donaciones, "Regalos realizados", true, admin);
        createSubcategoryIfNotExists(donaciones, "Ayuda familiar", true, admin);

        // EXPENSE - Categorías especiales
        Category especiales = createCategoryIfNotExists("Categorías especiales", Type.EXPENSE, admin);
        createSubcategoryIfNotExists(especiales, "Emergencias", true, admin);
        createSubcategoryIfNotExists(especiales, "Gastos de trabajo", true, admin);
        createSubcategoryIfNotExists(especiales, "Gastos reembolsables", true, admin);
        createSubcategoryIfNotExists(especiales, "Imprevistos", true, admin);

        log.info("Categorías y subcategorías de sistema cargadas correctamente.");
    }

    /**
     * Crea una categoria si no existe una activa con el mismo nombre.
     *
     * @param name  nombre de la categoria
     * @param type  tipo de la categoria (INCOME o EXPENSE)
     * @param admin usuario admin como creador
     * @return entidad Category existente o recién creada
     */
    private Category createCategoryIfNotExists(String name, Type type, User admin) {
        return categoryRepository.findByNameAndDeletedAtIsNull(name)
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .name(name)
                            .type(type)
                            .createdBy(admin)
                            .deletedAt(null)
                            .build();
                    categoryRepository.save(category);
                    log.info("Categoría creada: {} ({})", name, type);
                    return category;
                });
    }

    /**
     * Crea una subcategoría si no existe una activa con el mismo nombre dentro de la categoria.
     *
     * @param category categoria padre
     * @param name     nombre de la subcategoría
     * @param isSystem true si es del sistema
     * @param admin    usuario admin como creador
     * @return entidad Subcategory existente o recién creada
     */
    private void createSubcategoryIfNotExists(Category category, String name, boolean isSystem, User admin) {
        subcategoryRepository.findByNameAndCategoryIdAndDeletedAtIsNull(name, category.getId())
                .orElseGet(() -> {
                    Subcategory subcategory = Subcategory.builder()
                            .category(category)
                            .name(name)
                            .comments(null)
                            .isSystem(isSystem)
                            .createdBy(admin)
                            .deletedAt(null)
                            .build();
                    subcategoryRepository.save(subcategory);
                    log.info("Subcategoría creada: {} -> {}", category.getName(), name);
                    return subcategory;
                });
    }

    // ========================================================================
    // FAKE DATA — Solo se ejecuta en perfil DEVELOPER
    // ========================================================================

    private void seedFakeDataIfDeveloper() {
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isDeveloper = activeProfiles != null && Arrays.asList(activeProfiles).contains("developer");
        if (!isDeveloper) return;

        long userCount = userRepository.countByDeletedAtIsNull();
        if (userCount > 1) {
            log.info("Ya existen usuarios fake, se omite la carga de datos de prueba.");
            return;
        }

        log.info("Perfil DEVELOPER detectado — iniciando carga de datos de prueba...");

        Category catIngresos = categoryRepository.findByNameAndDeletedAtIsNull("Ingresos generales").orElseThrow();
        Category catGastosFijos = categoryRepository.findByNameAndDeletedAtIsNull("Gastos fijos").orElseThrow();
        Category catAlimentacion = categoryRepository.findByNameAndDeletedAtIsNull("Alimentación").orElseThrow();
        Category catTransporte = categoryRepository.findByNameAndDeletedAtIsNull("Transporte").orElseThrow();
        Category catVivienda = categoryRepository.findByNameAndDeletedAtIsNull("Vivienda y hogar").orElseThrow();
        Category catSalud = categoryRepository.findByNameAndDeletedAtIsNull("Salud").orElseThrow();
        Category catEducacion = categoryRepository.findByNameAndDeletedAtIsNull("Educación").orElseThrow();
        Category catCompras = categoryRepository.findByNameAndDeletedAtIsNull("Compras personales").orElseThrow();
        Category catOcio = categoryRepository.findByNameAndDeletedAtIsNull("Ocio y entretenimiento").orElseThrow();
        Category catFamilia = categoryRepository.findByNameAndDeletedAtIsNull("Familia y mascotas").orElseThrow();
        Category catFinanzas = categoryRepository.findByNameAndDeletedAtIsNull("Finanzas").orElseThrow();
        Category catDonaciones = categoryRepository.findByNameAndDeletedAtIsNull("Donaciones y regalos").orElseThrow();
        Category catEspeciales = categoryRepository.findByNameAndDeletedAtIsNull("Categorías especiales").orElseThrow();

        Map<String, Subcategory> subMap = new HashMap<>();
        loadSubcategories(subMap, catIngresos, "Salario", "Horas extras", "Bonificaciones y comisiones",
                "Trabajo autónomo / freelance", "Negocio propio", "Inversiones (dividendos, intereses)",
                "Alquileres recibidos", "Pensiones", "Prestaciones y ayudas", "Regalos recibidos",
                "Reembolsos", "Venta de artículos");
        loadSubcategories(subMap, catGastosFijos, "Alquiler / Hipoteca", "Comunidad de propietarios",
                "Electricidad", "Agua", "Gas", "Internet", "Telefonía móvil", "Seguros",
                "Impuestos y tasas", "Suscripciones (Netflix, Spotify, etc.)");
        loadSubcategories(subMap, catAlimentacion, "Supermercado", "Restaurantes", "Comida rápida",
                "Cafeterías", "Delivery", "Snacks y bebidas");
        loadSubcategories(subMap, catTransporte, "Combustible", "Transporte público", "Taxi / VTC",
                "Aparcamiento", "Peajes", "Mantenimiento del vehículo", "Seguro del vehículo",
                "Alquiler de vehículos");
        loadSubcategories(subMap, catVivienda, "Muebles", "Electrodomésticos", "Decoración",
                "Reparaciones", "Jardinería", "Productos de limpieza");
        loadSubcategories(subMap, catSalud, "Médico", "Dentista", "Farmacia", "Seguro médico",
                "Terapias", "Gimnasio", "Bienestar");
        loadSubcategories(subMap, catEducacion, "Matrículas", "Cursos", "Libros", "Material escolar",
                "Certificaciones");
        loadSubcategories(subMap, catCompras, "Ropa", "Calzado", "Accesorios", "Cosmética",
                "Tecnología", "Electrónica");
        loadSubcategories(subMap, catOcio, "Cine", "Streaming", "Videojuegos", "Eventos", "Música",
                "Hobbies", "Viajes");
        loadSubcategories(subMap, catFamilia, "Hijos", "Guardería", "Colegios", "Otros familiares",
                "Mascotas", "Alimentación de mascotas", "Veterinario");
        loadSubcategories(subMap, catFinanzas, "Ahorro", "Inversiones", "Pago de préstamos",
                "Tarjetas de crédito", "Comisiones bancarias", "Transferencias");
        loadSubcategories(subMap, catDonaciones, "Donaciones", "Regalos realizados", "Ayuda familiar");
        loadSubcategories(subMap, catEspeciales, "Emergencias", "Gastos de trabajo",
                "Gastos reembolsables", "Imprevistos");

        Random random = new Random(42);
        List<User> fakeUsers = createFakeUsers(random);
        createFakeTransactions(random, fakeUsers, subMap, catIngresos, catGastosFijos, catAlimentacion);

        log.info("Fake data: {} usuarios y transacciones creadas (perfil DEVELOPER).", fakeUsers.size());
    }

    private void loadSubcategories(Map<String, Subcategory> subMap, Category cat, String... names) {
        for (String name : names) {
            subcategoryRepository.findByNameAndCategoryIdAndDeletedAtIsNull(name, cat.getId())
                    .ifPresent(s -> subMap.put(name, s));
        }
    }

    private List<User> createFakeUsers(Random random) {
        Object[][] data = {
                {"Carlos García", "carlos.garcia", 1}, {"María López", "maria.lopez", 1},
                {"Juan Martínez", "juan.martinez", 1}, {"Ana Rodríguez", "ana.rodriguez", 1},
                {"Pedro Sánchez", "pedro.sanchez", 1},
                {"Laura Fernández", "laura.fernandez", 2}, {"Diego Torres", "diego.torres", 2},
                {"Sofía Díaz", "sofia.diaz", 2}, {"Andrés Ruiz", "andres.ruiz", 2},
                {"Valentina Morales", "valentina.morales", 2},
                {"Martín Castro", "martin.castro", 3}, {"Camila Vargas", "camila.vargas", 3},
                {"Lucas Herrera", "lucas.herrera", 3}, {"Isabella Moreno", "isabella.moreno", 3},
                {"Felipe Ríos", "felipe.rios", 3},
                {"Gabriela Muñoz", "gabriela.munoz", 4}, {"Nicolás Romero", "nicolas.romero", 4},
                {"Paula Álvarez", "paula.alvarez", 4}, {"Alejandro Silva", "alejandro.silva", 4},
                {"Daniela Cruz", "daniela.cruz", 4},
                {"Roberto Reyes", "roberto.reyes", 5}, {"Claudia Peña", "claudia.pena", 5},
                {"Fernando Ortiz", "fernando.ortiz", 5}, {"Patricia Navarro", "patricia.navarro", 5},
                {"Mauricio Flores", "mauricio.flores", 5}, {"Carolina Jiménez", "carolina.jimenez", 5},
                {"Oscar Medina", "oscar.medina", 6}, {"Diana Paredes", "diana.paredes", 6},
                {"Enrique Suárez", "enrique.suarez", 6}, {"Mónica Vega", "monica.vega", 6},
                {"Ricardo Campos", "ricardo.campos", 6}, {"Laura Mendoza", "laura.mendoza", 6},
                {"Arturo Rojas", "arturo.rojas", 7}, {"Alejandra Delgado", "alejandra.delgado", 7},
                {"Sergio Guerrero", "sergio.guerrero", 7}, {"Natalie Cortés", "natalie.cortes", 7},
                {"Emilio Contreras", "emilio.contreras", 7}, {"Beatriz Luna", "beatriz.luna", 7},
                {"Pablo Salazar", "pablo.salazar", 8}, {"Cristina Herrera", "cristina.herrera", 8},
                {"Tomás Aguilar", "tomas.aguilar", 8}, {"Jessica Miranda", "jessica.miranda", 8},
                {"Adrián Castillo", "adrian.castillo", 8},
        };

        List<User> users = new ArrayList<>();
        for (Object[] row : data) {
            String name = (String) row[0];
            String emailPrefix = (String) row[1];
            int month = (int) row[2];
            String email = emailPrefix + "@prueba.fcs";

            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(email))
                    .name(name)
                    .role(Role.USER)
                    .status(Status.ACTIVE)
                    .createdAt(LocalDateTime.of(2026, month, 1, 0, 0, 0))
                    .updatedAt(LocalDateTime.of(2026, month, 1, 0, 0, 0))
                    .deletedAt(null)
                    .build();
            userRepository.save(user);
            users.add(user);
        }
        return users;
    }

    private void createFakeTransactions(Random random, List<User> users,
                                        Map<String, Subcategory> subMap,
                                        Category catIngresos, Category catGastosFijos,
                                        Category catAlimentacion) {
        Subcategory subSalario = subMap.get("Salario");
        Subcategory subSupermercado = subMap.get("Supermercado");
        Subcategory subAlquilerHipo = subMap.get("Alquiler / Hipoteca");

        List<Category> allExpenseCats = List.of(
                catGastosFijos, catAlimentacion,
                subMap.get("Combustible") != null ? subMap.get("Combustible").getCategory() : catGastosFijos,
                subMap.get("Médico") != null ? subMap.get("Médico").getCategory() : catGastosFijos,
                subMap.get("Ropa") != null ? subMap.get("Ropa").getCategory() : catGastosFijos,
                subMap.get("Cine") != null ? subMap.get("Cine").getCategory() : catGastosFijos,
                subMap.get("Ahorro") != null ? subMap.get("Ahorro").getCategory() : catGastosFijos,
                subMap.get("Donaciones") != null ? subMap.get("Donaciones").getCategory() : catGastosFijos,
                subMap.get("Emergencias") != null ? subMap.get("Emergencias").getCategory() : catGastosFijos
        );

        String[] incomeDescs = {
                "Cobro mensual del salario", "Ingreso por servicios profesionales",
                "Bonificación por desempeño", "Pago por horas extraordinarias",
                "Trabajo independiente", "Ingreso por alquiler de propiedad",
                "Rendimiento de inversiones", "Venta de artículos personales",
                "Reembolso de gastos", "Pensión mensual", "Regalo en efectivo", "Comisión por ventas"
        };

        Map<Category, String[]> expenseDescsMap = new HashMap<>();
        expenseDescsMap.put(catGastosFijos, new String[]{
                "Pago de alquiler mensual", "Servicio de electricidad", "Cuenta de agua",
                "Factura de gas", "Servicio de internet", "Plan de telefonía móvil",
                "Cuota de seguro del hogar", "Pago de impuestos anuales",
                "Suscripción a streaming", "Cuota de comunidad"});
        expenseDescsMap.put(catAlimentacion, new String[]{
                "Compra semanal del supermercado", "Cena en restaurante", "Almuerzo rápido",
                "Café con compañeros", "Pedido a domicilio", "Snacks y bebidas varias",
                "Compra de frutas y verduras", "Cena especial"});

        String[] defaultExpenseDescs = {"Gasto del mes", "Gasto varios", "Compra del mes"};

        List<Subcategory> incomeSubs = List.of(
                subSalario, subMap.get("Horas extras"), subMap.get("Bonificaciones y comisiones"),
                subMap.get("Trabajo autónomo / freelance"), subMap.get("Negocio propio"),
                subMap.get("Inversiones (dividendos, intereses)"), subMap.get("Alquileres recibidos"),
                subMap.get("Pensiones"), subMap.get("Prestaciones y ayudas"),
                subMap.get("Regalos recibidos"), subMap.get("Reembolsos"), subMap.get("Venta de artículos")
        );

        for (User user : users) {
            int startMonth = user.getCreatedAt().getMonthValue();
            int startYear = user.getCreatedAt().getYear();
            int cy = startYear, cm = startMonth;

            while (cy < 2026 || (cy == 2026 && cm <= 8)) {
                int numTx = 10 + random.nextInt(6);
                int numIncome = 1 + random.nextInt(Math.min(5, numTx));
                int numExpense = numTx - numIncome;

                BigDecimal monthIncome = BigDecimal.ZERO;
                List<Transaction> expenseTxs = new ArrayList<>();

                for (int i = 0; i < numIncome; i++) {
                    Subcategory sub;
                    BigDecimal amount;
                    if (random.nextDouble() < 0.5) {
                        sub = subSalario;
                        amount = BigDecimal.valueOf(500 + random.nextDouble() * 1500).setScale(2, RoundingMode.HALF_UP);
                    } else {
                        sub = incomeSubs.get(random.nextInt(incomeSubs.size()));
                        amount = BigDecimal.valueOf(200 + random.nextDouble() * 1800).setScale(2, RoundingMode.HALF_UP);
                    }
                    int day = 1 + random.nextInt(10);
                    day = Math.min(day, daysInMonth(cy, cm));
                    LocalDate txDate = LocalDate.of(cy, cm, day);
                    LocalDateTime txCreated = txDate.atTime(8 + random.nextInt(11), random.nextInt(60), random.nextInt(60))
                            .plusDays(random.nextInt(4));
                    if (txCreated.getMonthValue() != cm) {
                        txCreated = txCreated.withDayOfMonth(daysInMonth(txCreated.getYear(), txCreated.getMonthValue()))
                                .withHour(23).withMinute(59).withSecond(59);
                    }
                    Transaction tx = Transaction.builder()
                            .user(user).category(catIngresos).subcategory(sub)
                            .amount(amount).description(incomeDescs[random.nextInt(incomeDescs.length)])
                            .transactionDate(txDate).createdAt(txCreated).updatedAt(txCreated)
                            .deletedAt(null).build();
                    transactionRepository.save(tx);
                    monthIncome = monthIncome.add(amount);
                }

                BigDecimal maxExpense = monthIncome.multiply(BigDecimal.valueOf(0.9));
                BigDecimal expenseTotal = BigDecimal.ZERO;

                for (int j = 0; j < numExpense; j++) {
                    Category expCat;
                    if (j < 2 && numExpense >= 2) {
                        expCat = (j % 2 == 0) ? catGastosFijos : catAlimentacion;
                    } else if (random.nextDouble() < 0.4) {
                        expCat = random.nextBoolean() ? catGastosFijos : catAlimentacion;
                    } else {
                        expCat = allExpenseCats.get(random.nextInt(allExpenseCats.size()));
                    }

                    List<Subcategory> catSubs = new ArrayList<>();
                    for (Subcategory s : subMap.values()) {
                        if (s.getCategory().getId().equals(expCat.getId())) {
                            catSubs.add(s);
                        }
                    }
                    if (catSubs.isEmpty()) continue;
                    Subcategory expSub = catSubs.get(random.nextInt(catSubs.size()));

                    BigDecimal remaining = maxExpense.subtract(expenseTotal);
                    if (remaining.compareTo(BigDecimal.valueOf(5)) < 0) break;

                    BigDecimal amount;
                    if (expCat.getId().equals(catGastosFijos.getId()) && expSub.getId().equals(subAlquilerHipo.getId())) {
                        amount = BigDecimal.valueOf(400 + random.nextDouble() * 500).setScale(2, RoundingMode.HALF_UP);
                    } else if (expCat.getId().equals(catGastosFijos.getId())) {
                        amount = BigDecimal.valueOf(50 + random.nextDouble() * 750).setScale(2, RoundingMode.HALF_UP);
                    } else if (expCat.getId().equals(catAlimentacion.getId()) && expSub.getId().equals(subSupermercado.getId())) {
                        amount = BigDecimal.valueOf(100 + random.nextDouble() * 300).setScale(2, RoundingMode.HALF_UP);
                    } else if (expCat.getId().equals(catAlimentacion.getId())) {
                        amount = BigDecimal.valueOf(30 + random.nextDouble() * 470).setScale(2, RoundingMode.HALF_UP);
                    } else {
                        amount = BigDecimal.valueOf(5 + random.nextDouble() * 295).setScale(2, RoundingMode.HALF_UP);
                    }
                    if (amount.compareTo(remaining) > 0) {
                        amount = remaining.multiply(BigDecimal.valueOf(0.8)).setScale(2, RoundingMode.HALF_UP);
                    }
                    if (amount.compareTo(BigDecimal.valueOf(5)) < 0) amount = BigDecimal.valueOf(5);

                    int minDay = Math.min(10, daysInMonth(cy, cm));
                    int day = minDay + random.nextInt(daysInMonth(cy, cm) - minDay + 1);
                    LocalDate txDate = LocalDate.of(cy, cm, day);
                    LocalDateTime txCreated = txDate.atTime(8 + random.nextInt(11), random.nextInt(60), random.nextInt(60))
                            .plusDays(random.nextInt(4));
                    if (txCreated.getMonthValue() != cm) {
                        txCreated = txCreated.withDayOfMonth(daysInMonth(txCreated.getYear(), txCreated.getMonthValue()))
                                .withHour(23).withMinute(59).withSecond(59);
                    }

                    String[] descs = expenseDescsMap.getOrDefault(expCat, defaultExpenseDescs);
                    Transaction tx = Transaction.builder()
                            .user(user).category(expCat).subcategory(expSub)
                            .amount(amount).description(descs[random.nextInt(descs.length)])
                            .transactionDate(txDate).createdAt(txCreated).updatedAt(txCreated)
                            .deletedAt(null).build();
                    transactionRepository.save(tx);
                    expenseTxs.add(tx);
                    expenseTotal = expenseTotal.add(amount);
                }

                cm++;
                if (cm > 12) { cm = 1; cy++; }
            }
        }
    }

    private int daysInMonth(int year, int month) {
        if (month == 12) return 31;
        return LocalDate.of(year, month + 1, 1).minusDays(1).getDayOfMonth();
    }
}
