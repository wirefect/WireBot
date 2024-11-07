plugins {
  idea
  `java-library`
  id("sf-license-conventions")
  id("sf-formatting-conventions")
  id("io.freefair.lombok")
  id("net.kyori.indra.git")
  id("io.freefair.javadocs")
}

tasks {
  javadoc {
    title = "SoulFire Javadocs"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
  }
  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.compilerArgs.addAll(
      listOf(
        "-parameters",
        "-nowarn",
        "-Xlint:-unchecked",
        "-Xlint:-deprecation",
        "-Xlint:-processing"
      )
    )
    options.isFork = true
  }
  test {
    reports.junitXml.required = true
    reports.html.required = true
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2).coerceAtLeast(1)
  }
  jar {
    from(rootProject.file("LICENSE"))
  }
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
  withJavadocJar()
  withSourcesJar()
}
