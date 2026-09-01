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

// Предупреждения об устаревшем api на исходниках ядра. Задачи Minecraft сюда не входят: там своих
// предупреждений сотни, и в этом шуме чужое устаревание не разглядеть. Ровно так пропустили, что
// night-config 3.8 объявила valueMap() устаревшим.
listOf("compileJava", "compileTestJava").forEach { name ->
	tasks.named<JavaCompile>(name) {
		options.compilerArgs.add("-Xlint:deprecation")
	}
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

	// Сериализация в ядре идёт через gson, а разбор toml в объект через него же, поэтому пакет serde
	// из night-config не зовёт никто: 59 классов и 207 КБ мёртвого груза в моде, который поедет на
	// чужие серверы. Проверено перед выбрасыванием: среди 200 классов night-config вне serde нет ни
	// одной ссылки на его классы и ни одной строки с его именами, то есть и Class.forName туда не
	// ведёт; своих файлов META-INF/services у библиотеки нет вовсе.
	exclude("**/nightconfig/core/serde/**")

	// Каталог META-INF/versions/17 из той же библиотеки держит два класса serde под Java 17. Мод
	// грузится на Java 8, которая этот каталог не читает вовсе. Строку Multi-Release в манифесте shadow
	// ставит по входному jar и чужие значения затирает своим, но без каталога versions она ничего не
	// значит: виртуальная машина ищет версии только в нём.
	exclude("META-INF/versions/**")
}
