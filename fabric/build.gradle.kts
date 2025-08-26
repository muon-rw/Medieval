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
    maven("https://maven.ladysnake.org/snapshots")
    maven("https://maven.jamieswhiteshirt.com/libs-release")
    maven("https://maven.parchmentmc.org")
    maven("https://cursemaven.com")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.bawnorton.com/releases")
    maven("https://maven.kosmx.dev/")
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.ftb.dev/releases")
    maven("https://maven.bai.lol" )
    maven("https://maven.kosmx.dev/")
    maven("https://nexus.resourcefulbees.com/repository/maven-public/")
    maven("https://jm.gserv.me/repository/maven-public/" )
    maven("https://masa.dy.fi/maven" )
    maven("https://maven.quiltmc.org/repository/release" )
    maven("https://maven.uuid.gg/releases" )
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")
    modLocalRuntime("com.terraformersmc:modmenu:${Versions.MOD_MENU}")

    modImplementation("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.4")
    include("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.4")
    annotationProcessor(("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.4"))?.let {
        include(it)?.let {
            modImplementation(
                it
            )
        }
    }

    // Dev Env
    modLocalRuntime("curse.maven:spark-361579:5759670")
    modLocalRuntime("maven.modrinth:fabric-permissions-api:0.3.1-fabric")

    // Config
    modImplementation("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${Versions.FCAP}")
    
    // Accessories
    modImplementation("io.wispforest:accessories-fabric:${Versions.ACCESSORIES}")
    modLocalRuntime("curse.maven:accessories-tc-layer-1005680:6008871")

    // FTB
    modImplementation("dev.ftb.mods:ftb-quests-fabric:${Versions.FTB_QUESTS}")
    modImplementation("dev.ftb.mods:ftb-chunks-fabric:${Versions.FTB_CHUNKS}")
    modImplementation("dev.ftb.mods:ftb-library-fabric:${Versions.FTB_LIBRARY}")
    modImplementation("dev.ftb.mods:ftb-teams-fabric:${Versions.FTB_TEAMS}")
    modLocalRuntime("curse.maven:architectury-api-419699:5553799")
    modApi("teamreborn:energy:4.1.0") {
        exclude("net.fabricmc.fabric-api")
    }

    // Thermoo
    modImplementation("com.github.thedeathlycow:thermoo:v4.5.3")
    modLocalRuntime("curse.maven:scorchful-981400:6480177")
    modLocalRuntime("curse.maven:frostiful-715248:6529178")
    modLocalRuntime("curse.maven:thermoo-patches-1012677:6487343")
    modApi("org.ladysnake:satin:2.0.0")
    modApi("me.shedaniel.cloth:cloth-config-fabric:${Versions.CLOTH_CONFIG_VERSION}") {
        exclude("net.fabricmc.fabric-api")
    }

    // Overflowing bars
    modImplementation("curse.maven:overflowing-bars-852662:5770622")
    modImplementation("curse.maven:puzzles-lib-495476:6013577")
    modLocalRuntime("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:21.1.3")

    // Spell Engine
    modImplementation("maven.modrinth:spell-engine:${Versions.SPELL_ENGINE}+${Versions.MINECRAFT}")
    modImplementation("maven.modrinth:spell-power:${Versions.SPELL_POWER}+${Versions.MINECRAFT}")
    modLocalRuntime("dev.kosmx.player-anim:player-animation-lib-fabric:${Versions.PLAYER_ANIMATOR}")
    implementation("com.github.ZsoltMolnarrr:TinyConfig:${Versions.TINY_CONFIG}")

    // Affinity
    modImplementation("curse.maven:affinity-938918:6171019")
    modLocalRuntime("io.wispforest:owo-lib:0.12.15+1.21")
    modLocalRuntime("io.wispforest:lavender:0.1.15+1.21")
    modLocalRuntime("com.github.glitchfiend:TerraBlender-fabric:1.21-4.0.0.2")
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${Versions.CARDINAL_COMPONENTS}")
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${Versions.CARDINAL_COMPONENTS}")
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-chunk:${Versions.CARDINAL_COMPONENTS}")
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-world:${Versions.CARDINAL_COMPONENTS}")
    modLocalRuntime("dev.onyxstudios.cardinal-components-api:cardinal-components-scoreboard:${Versions.CARDINAL_COMPONENTS}")
    modLocalRuntime("io.github.ladysnake:PlayerAbilityLib:${Versions.PLAYER_ABILITY_LIB}")
    modLocalRuntime("io.wispforest:worldmesher:${Versions.WORLD_MESHER}")

    // Visual Workbench
    modImplementation("curse.maven:visual-workbench-500273:5714955")

    // Reactive Music
    modCompileOnly("curse.maven:reactive-music-960382:6132849")

    // Starter Kit
    modCompileOnly("curse.maven:starter-kit-390717:6429851")
    modCompileOnly("curse.maven:collective-342584:6429221")

    // Origins
    modCompileOnly("io.github.apace100:apoli:${Versions.APOLI}")
    modCompileOnly("io.github.apace100:origins-fabric:${Versions.ORIGINS}")
    modCompileOnly("maven.modrinth:medieval-origins-revival:7.0.3-alpha-5-fabric")


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