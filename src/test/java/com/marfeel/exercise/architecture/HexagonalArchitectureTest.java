package com.marfeel.exercise.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

@AnalyzeClasses(
	packages = "com.marfeel.exercise",
	importOptions = ImportOption.DoNotIncludeTests.class
)
public final class HexagonalArchitectureTest {

	@ArchTest
	static final ArchTests hexagonalRules = ArchTests.in(HexagonalRules.class);

	@ArchTest
	static final ArchTests codingRules = ArchTests.in(CodingRules.class);

	private HexagonalArchitectureTest() {
	}

}
