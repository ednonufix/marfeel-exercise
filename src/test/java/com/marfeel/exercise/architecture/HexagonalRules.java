package com.marfeel.exercise.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class HexagonalRules {
	@ArchTest
	static final ArchRule domainIsIndependent = noClasses().that().resideInAPackage("..domain..")
			.should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..",
					"org.springframework..", "jakarta..");

	@ArchTest
	static final ArchRule applicationIsIndependentOfInfrastructure = noClasses().that().resideInAPackage("..application..")
			.should().dependOnClassesThat().resideInAPackage("..infrastructure..");

	@ArchTest
	static final ArchRule inputAdaptersDoNotDependOnOutputAdapters = noClasses().that().resideInAPackage("..adapter.rest.input..")
			.should().dependOnClassesThat().resideInAPackage("..adapter.rest.output..");

	@ArchTest
	static final ArchRule outputAdaptersDoNotDependOnInputAdapters = noClasses().that().resideInAPackage("..adapter.rest.output..")
			.should().dependOnClassesThat().resideInAPackage("..adapter.rest.input..");

	@ArchTest
	static final ArchRule portsAreInterfaces = classes().that().resideInAPackage("..application.ports..")
			.and().areTopLevelClasses().should().beInterfaces();

	@ArchTest
	static final ArchRule servicesImplementInputPorts = classes().that().resideInAPackage("..application.service..")
			.and().areTopLevelClasses().should().implement(resideInAPackage("..application.ports.input.."));
}
