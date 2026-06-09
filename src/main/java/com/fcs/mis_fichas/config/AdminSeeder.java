package com.fcs.mis_fichas.config;

import com.fcs.mis_fichas.entities.Category;
import com.fcs.mis_fichas.entities.Subcategory;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.enums.Type;
import com.fcs.mis_fichas.repositories.CategoryRepository;
import com.fcs.mis_fichas.repositories.SubcategoryRepository;
import com.fcs.mis_fichas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    CommandLineRunner seedAdmin() {
        return args -> {
            User admin = createAdminIfNotExists();
            seedCategoriesAndSubcategories(admin);
        };
    }

    private User createAdminIfNotExists() {
        String adminEmail = "admin@mis-fichas.fcs";
        return userRepository.findByEmail(adminEmail)
                .orElseGet(() -> {
                    User admin = User.builder()
                            .email(adminEmail)
                            .passwordHash(passwordEncoder.encode("Admin123!"))
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

    private Category createCategoryIfNotExists(String name, Type type, User admin) {
        return categoryRepository.findByName(name)
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

    private void createSubcategoryIfNotExists(Category category, String name, boolean isSystem, User admin) {
        subcategoryRepository.findByName(name)
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
}
