plugins {
    id("sw.version-conventions")
}

dependencies {
    implementation("com.github.GeyserMC:MCProtocolLib:1.18-2")
    compileOnly(projects.serverwreckerCommon)
}

setupVersion("v1_14")
