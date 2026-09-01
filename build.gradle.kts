import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar

plugins {
	id("com.gtnewhorizons.gtnhconvention")
	id("com.mrleonardos.codesides") version "1.0.0"
}

codeSides {
	archiveBaseName.set(project.property("modName") as String)
	inputJar.set(
		tasks.named<AbstractArchiveTask>("reobfJar")
			.flatMap { it.archiveFile })
}

// Переименование зашейдженных классов не трогает списки в META-INF/services, а ImageIO читает их
// через ServiceLoader и падает ServiceConfigurationError на имени класса, которого в jar уже нет.
// Поэтому имена в этих файлах переписываются тем же префиксом, что и сами классы.
tasks.named<Jar>("shadowJar") {
	filesMatching("META-INF/services/*") {
		filter { line: String ->
			if (line.startsWith("com.twelvemonkeys.") || line.startsWith("com.electronwill."))
				"com.mrleonardos.codecore.shadow." + line
			else line
		}
	}

	// night-config 3.8 приехала multi-release jar: в META-INF/versions/17 лежат два класса пакета
	// serde под Java 17. Мод грузится на Java 8, которая этот каталог не читает вовсе, а сериализация
	// в ядре идёт через gson, поэтому классы выброшены. Строку Multi-Release в манифесте shadow ставит
	// по входному jar и чужие значения затирает своим, но без каталога versions она ничего не значит:
	// виртуальная машина ищет версии только в нём.
	exclude("META-INF/versions/**")
}
