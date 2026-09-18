package com.makers.prestamos.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Convierte las reglas de la arquitectura hexagonal en tests: si alguien rompe la dirección
 * de las dependencias, el build falla.
 */
@AnalyzeClasses(packages = "com.makers.prestamos", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule layersRespectDependencyDirection = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Dominio").definedBy("..domain..")
            .layer("Aplicación").definedBy("..application..")
            .layer("Infraestructura").definedBy("..infrastructure..")
            .whereLayer("Infraestructura").mayNotBeAccessedByAnyLayer()
            .whereLayer("Aplicación").mayOnlyBeAccessedByLayers("Infraestructura")
            .whereLayer("Dominio").mayOnlyBeAccessedByLayers("Aplicación", "Infraestructura");

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..", "com.fasterxml..");

    @ArchTest
    static final ArchRule applicationDoesNotKnowAboutWebOrPersistence = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.web..", "org.springframework.data..", "org.springframework.security..",
                    "org.springframework.cache..", "jakarta.persistence..", "jakarta.servlet..");

    @ArchTest
    static final ArchRule portsAreInterfaces = classes().that().resideInAnyPackage("..application.port.in..", "..application.port.out..")
            .and().areTopLevelClasses()
            .should().beInterfaces();

    @ArchTest
    static final ArchRule persistenceAdaptersAreNotUsedDirectly = noClasses().that().resideOutsideOfPackage("..adapter.out.persistence..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.out.persistence..");
}
