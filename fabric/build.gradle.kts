import dev.muon.medieval.gradle.Properties
import dev.muon.medieval.gradle.Versions
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar

plugins {
    id("conventions.loader")
    id("fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
}

repositories {
    maven("https://maven.blamejared.com/")
    maven("https://maven.wispforest.io/releases")
    maven("https://maven.su5ed.dev/releases")
    maven("https://maven.fabricmc.net")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/")
    maven("https://jitpack.io/")
    maven("https://maven.ladysnake.org/releases")
    maven("https://maven.parchmentmc.org")
    maven("https://cursemaven.com")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.bawnorton.com/releases")
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")
    modLocalRuntime("com.terraformersmc:modmenu:${Versions.MOD_MENU}")

    modImplementation("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.2.0")
    include("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.2.0")
    annotationProcessor(("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.2.0"))?.let {
        include(it)?.let {
            modImplementation(
                it
            )
        }
    }

    // FTB
    modImplementation("curse.maven:ftb-quests-fabric-438496:5635134")
    modImplementation("curse.maven:ftb-chunks-fabric-472657:5710610")
    modImplementation("curse.maven:ftb-library-fabric-438495:5714915")
    modImplementation("curse.maven:ftb-teams-fabric-438497:5631447")
    modLocalRuntime("curse.maven:architectury-api-419699:5553799")
    modApi("teamreborn:energy:4.1.0") {
        exclude("net.fabricmc.fabric-api")
    }

    // Thermoo
    modImplementation("com.github.TheDeathlyCow:thermoo:v4.2")
    modLocalRuntime("curse.maven:scorchful-981400:5664972")
    modLocalRuntime("curse.maven:frostiful-715248:5721630")
    modLocalRuntime("curse.maven:thermoo-patches-1012677:5692250")
    modApi("org.ladysnake:satin:2.0.0")
    modApi("me.shedaniel.cloth:cloth-config-fabric:${Versions.CLOTH_CONFIG_VERSION}") {
        exclude("net.fabricmc.fabric-api")
    }

    // Dehydration
    modImplementation("curse.maven:dehydration-410830:5709714")

}

loom {
    val aw = file("src/main/resources/${Properties.MOD_ID}.accesswidener");
    if (aw.exists())
        accessWidenerPath.set(aw)
    mixin {
        defaultRefmapName.set("${Properties.MOD_ID}.refmap.json")
    }
    mods {
        register(Properties.MOD_ID) {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["test"])
        }
    }
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            vmArgs("-Dmixin.debug.verbose=true", "-Dmixin.debug.export=true")
        }
        register("datagen") {
            server()
            configName = "Fabric Datagen"
            setSource(sourceSets["test"])
            ideConfigGenerated(true)
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("../common/src/generated/resources")}")
            vmArg("-Dfabric-api.datagen.modid=${Properties.MOD_ID}")
            runDir("build/datagen")
        }
    }
}

tasks {
    named<ProcessResources>("processResources").configure {
        exclude("${Properties.MOD_ID}.cfg")
    }
}

publishMods {
    file.set(tasks.named<Jar>("remapJar").get().archiveFile)
    modLoaders.add("fabric")
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