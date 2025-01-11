import dev.muon.medieval.gradle.Properties
import dev.muon.medieval.gradle.Versions
import org.apache.tools.ant.filters.LineContains
import org.gradle.jvm.tasks.Jar

plugins {
    id("conventions.loader")
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
}

tasks {
    withType<Javadoc> {
        enabled = false
    }
}

neoForge {
    version = Versions.NEOFORGE
    parchment {
        minecraftVersion = Versions.PARCHMENT_MINECRAFT
        mappingsVersion = Versions.PARCHMENT
    }
    addModdingDependenciesTo(sourceSets["test"])

    val at = project(":common").file("src/main/resources/${Properties.MOD_ID}.cfg")
    if (at.exists())
        setAccessTransformers(at)
    validateAccessTransformers = true

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            systemProperty("neoforge.enabledGameTestNamespaces", Properties.MOD_ID)
        }
        create("client") {
            client()
            gameDirectory.set(file("runs/client"))
            sourceSet = sourceSets["test"]
            jvmArguments.set(setOf("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true"))
        }
        create("server") {
            server()
            gameDirectory.set(file("runs/server"))
            programArgument("--nogui")
            sourceSet = sourceSets["test"]
            jvmArguments.set(setOf("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true"))
        }
    }

    mods {
        register(Properties.MOD_ID) {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["test"])
        }
    }
}

tasks {
    named<ProcessResources>("processResources").configure {
        filesMatching("*.mixins.json") {
            filter<LineContains>("negate" to true, "contains" to setOf("refmap"))
        }
    }
}

repositories {
    maven {
        url = uri("https://maven.shadowsoffire.dev/releases")
        content {
            includeGroup("dev.shadowsoffire")
        }
    }
    maven {
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    maven {
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven {
        url = uri("https://code.redspace.io/releases")
        content {
            includeGroup("io.redspace")
        }
    }
    maven {
        url = uri("https://code.redspace.io/snapshots")
        content {
            includeGroup("io.redspace")
        }
    }
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.blamejared.com/")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.wispforest.io/releases")
    maven("https://maven.su5ed.dev/releases")
    maven("https://maven.fabricmc.net")
    maven("https://maven.shedaniel.me/")
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.kosmx.dev/")
    maven("https://maven.octo-studios.com/releases")
}

dependencies {

    implementation("curse.maven:ftb-chunks-forge-314906:5710609")
    implementation("curse.maven:ftb-library-forge-404465:5714916")
    implementation("curse.maven:ftb-teams-forge-404468:5631446")
    runtimeOnly("curse.maven:architectury-api-419699:5553800")

    // Accessories
    implementation("io.wispforest:accessories-neoforge:${Versions.ACCESSORIES}")
    implementation("curse.maven:accessories-cc-layer-1005683:5823562")

    // TF
    compileOnly("curse.maven:the-twilight-forest-227639:5699076")

    // Iron's Spells n Spellbooks
    implementation("io.redspace:irons_spellbooks:${Versions.IRONS_SPELLBOOKS}:api")
    implementation("io.redspace:irons_spellbooks:${Versions.IRONS_SPELLBOOKS}")
    runtimeOnly("software.bernie.geckolib:geckolib-neoforge-${Versions.GECKOLIB}")
    runtimeOnly("dev.kosmx.player-anim:player-animation-lib-forge:${Versions.PLAYER_ANIMATOR}")

    // Ars Nouveau
    implementation("com.hollingsworth.ars_nouveau:ars_nouveau-${Versions.ARS_NOUVEAU}")
    implementation("com.github.glitchfiend:TerraBlender-neoforge:1.21.1-4.1.0.7")
    runtimeOnly("top.theillusivec4.curios:curios-neoforge:9.0.12+1.21")

    // Apotheosis
    implementation("dev.shadowsoffire:Placebo:${Versions.MINECRAFT}-${Versions.PLACEBO}")
    implementation("dev.shadowsoffire:Apotheosis:${Versions.MINECRAFT}-${Versions.APOTHEOSIS}")
    implementation("dev.shadowsoffire:ApothicAttributes:${Versions.MINECRAFT}-${Versions.APOTHIC_ATTRIBUTES}")
    implementation("dev.shadowsoffire:ApothicSpawners:${Versions.MINECRAFT}-${Versions.APOTHIC_SPAWNERS}")
    implementation("dev.shadowsoffire:ApothicEnchanting:${Versions.MINECRAFT}-${Versions.APOTHIC_ENCHANTING}")

    // Overflowing Bars
    implementation("curse.maven:overflowing-bars-852662:5770623")
    implementation("curse.maven:puzzles-lib-495476:6013576")
}

publishMods {
    file.set(tasks.named<Jar>("jar").get().archiveFile)
    modLoaders.add("neoforge")
    changelog = rootProject.file("CHANGELOG.md").readText()
    version = "${Versions.MOD}+${Versions.MINECRAFT}"
    type = STABLE

    curseforge {
        projectId = Properties.CURSEFORGE_PROJECT_ID
        accessToken = providers.environmentVariable("CF_TOKEN")

        minecraftVersions.add(Versions.MINECRAFT)
        javaVersions.add(JavaVersion.VERSION_21)

        clientRequired = true
        serverRequired = true
    }

    modrinth {
        projectId = Properties.MODRINTH_PROJECT_ID
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")

        minecraftVersions.add(Versions.MINECRAFT)
    }

    /*
    github {
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        parent(project(":common").tasks.named("publishGithub"))
    }
     */
}
