version = "1.0.1"

project.extra["PluginName"] = "ARcer" // This is the name that is used in the external plugin manager panel
project.extra["PluginDescription"] = "Crafts runes for you." // This is the description that is used in the external plugin manager panel


dependencies {
    compileOnly(project(":autils"))
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Dependencies" to
                        arrayOf(
                            nameToId("AUtils")
                        ).joinToString(),
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}