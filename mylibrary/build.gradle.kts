plugins {
    //Significa que estás publicando una librería Android, no una aplicación.
    //
    //Esto genera un archivo .aar (Android Archive) que contiene:
    //Código compilado
    //Recursos XML
    //Archivos AndroidManifest.xml
    //Assets
    //Otros metadatos
    //Ese .aar será el artefacto que vas a publicar.
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    //Este plugin permite que cualquier módulo (app, librería, etc.) se pueda publicar en un repositorio Maven, como:
    //Tu repositorio local (build/repo)
    //Tu repositorio Maven local (~/.m2)
    //Maven Central, JitPack, Artifactory, etc.
    id("maven-publish")//-----------------------------------------------

}

android {
    namespace = "com.empresa.mylibrary"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    //-----------------------------------------------
    buildTypes {
        debug {
            //isMinifyEnabled indica si se deben aplicar herramientas como ProGuard o R8 para minimizar y ofuscar el código.
            //false significa que no se va a minimizar:
            //El código no se ofusca.
            //No se eliminan clases/métodos innecesarios.
            //El APK/AAR es más grande, pero más fácil de depurar
            isMinifyEnabled = false
        }

        release {
            //Aquí defines las reglas que usará ProGuard o R8 para saber qué cosas puede eliminar u ofuscar y qué no.
            //"proguard-android-optimize.txt": es una configuración estándar proporcionada por Android.
            //"proguard-rules.pro": es un archivo tuyo donde puedes personalizar reglas (por ejemplo, conservar clases específicas).
            isMinifyEnabled = true
            proguardFiles(
                //proguard-android-optimize.txt" es un archivo que viene con el Android SDK. Contiene reglas generales y optimizadas que funcionan para la mayoría de las apps/librería
                //proguard-rules.pro Este es un archivo personalizado tuyo para poner tus propias reglas. Está ubicado normalmente en tu módulo, por ejemplo: mylibrary ->proguard-rules.pro
                getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro"
            )
        }
        //Creamos un build type custom
        //¿Por qué usamos create() aquí y no en debug o release? -> Porque debug y release ya existen por defecto en cualquier proyecto Android.
        //Si quieres un nuevo buildType con un nombre personalizado como staging, lo tienes que crear manualmente usando create("nombre").
        create("staging") {
            isMinifyEnabled = false
        }
    }
    //-----------------------------------------------


    //--------------
    //Qué es una dimension? -> Es una categoría lógica para agrupar flavors que no son mutuamente excluyentes.
    //Por ejemplo:
    //Dimension	        Flavors dentro
    //version	        demo, full
    //market	        google, huawei, amazon
    //architecture	    arm64, x86
    //Le estás diciendo a Gradle: "Todas las flavors que voy a definir pertenecen a la dimensión version".
    flavorDimensions += "version"
    //Aquí defines tus sabores de producto, como si fueran variaciones de tu app o librería.
    //Cada flavor puede:
    //  Tener recursos diferentes (como íconos, layouts)
    //  Incluir/excluir código
    //  Definir constantes (buildConfigField)
    //  Tener su propio applicationId, versionName, etc.
    productFlavors {
        create("demo") {
            dimension = "version"
            //Notas para usar buildConfigField:
            //Si escribes "String", estás diciendo: ->Voy a crear una constante tipo String
            //Este es el nombre de la constante que se va a crear.
            //Este es el valor literal que va a tener la constante.


            //Te permite hacer cosas como esas:
            //if (BuildConfig.FLAVOR_NAME == "Demo") {
            //    mostrarAnuncios()
            //} else {
            //    mostrarPantallaDeLogin()
            //}
            buildConfigField("String", "FLAVOR_NAME", "\"Demo\"")
        }
        create("full") {
            dimension = "version"
            buildConfigField("String", "FLAVOR_NAME", "\"Full\"")
        }
    }
    //--------------





    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    //-----------------------------------------------
    buildFeatures {
        buildConfig = true
    }
    //-----------------------------------------------

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

//El bloque afterEvaluate { ... } le dice a Gradle:
//"Espera hasta que el proyecto esté completamente configurado (plugins, variantes, componentes), y luego ejecuta esto."
//Así evitamos errores como:
//Cannot access 'components["release"]': it is only available after evaluation
//-----------------------------------------------
afterEvaluate {
    extensions.configure<PublishingExtension>("publishing") {
        //publications → lo que quieres publicar
        // Crea una publicación Maven llamada "release" (puede llamarse como quieras).


        publications {
            create<MavenPublication>("demoDebug") {
                groupId = "com.empresa"
                artifactId = "mylibrary-demo-debug"
                version = "1.0.0"
                from(components["demoDebug"])
            }
            create<MavenPublication>("demoRelease") {
                groupId = "com.empresa"
                artifactId = "mylibrary-demo-release"
                version = "1.0.0"
                from(components["demoRelease"])
            }
            create<MavenPublication>("demoStaging") {
                groupId = "com.empresa"
                artifactId = "mylibrary-demo-staging"
                version = "1.0.0"
                from(components["demoStaging"])
            }
            create<MavenPublication>("fullDebug") {
                groupId = "com.empresa"
                artifactId = "mylibrary-full-debug"
                version = "1.0.0"
                from(components["fullDebug"])
            }
            create<MavenPublication>("fullRelease") {
                groupId = "com.empresa"
                artifactId = "mylibrary-full"
                version = "1.0.0"
                from(components["fullRelease"])
            }
            create<MavenPublication>("fullStaging") {
                groupId = "com.empresa"
                artifactId = "mylibrary-full-staging"
                version = "1.0.0"
                from(components["fullStaging"])
            }
        }



        //repositories → a dónde publicar
        //Esto indica que el .aar y los metadatos (.pom, etc.) se publicarán en:
        //<tu_módulo>/build/repo/com/tuempresa/mylibrary/1.0.0/


        repositories {
            maven {
                //$buildDir es una propiedad de Gradle que representa la carpeta build dentro del módulo actual.
                //Si estás en un módulo llamado mylibrary, la ruta real será:<ruta_del_proyecto>/mylibrary/build

                //Esa línea le está diciendo a Gradle:
                //"Publica el contenido Maven en la carpeta build/repo."
                //Entonces, al hacer ./gradlew publishReleasePublicationToMavenRepository, Gradle generará:
                //mylibrary/build/repo/com/tuempresa/mylibrary/1.0.0/
                //│
                //├── mylibrary-1.0.0.aar       <- tu librería Android
                //├── mylibrary-1.0.0.pom       <- metadatos Maven (nombre, versión, dependencias, etc.)
                //└── ...                       <- otros archivos si aplica

                //¿Por qué com/tuempresa/mylibrary/1.0.0/?
                //Porque usaste:
                //groupId = "com.tuempresa"
                //artifactId = "mylibrary"
                //version = "1.0.0"
                //Y en Maven, eso se traduce a una estructura de carpetas tipo:
                //[groupId con puntos reemplazados por /]/[artifactId]/[version]/

                //mylibrary/
                //├── build/
                //│   └── repo/
                //│       └── com/
                //│           └── tuempresa/
                //│               └── mylibrary/
                //│                   └── 1.0.0/
                //│                       ├── mylibrary-1.0.0.aar // la libreria
                //│                       └── mylibrary-1.0.0.pom // metadatos de maven
                url = uri("$buildDir/repo")

            }
        }
    }
}



//NOTAS
//Para compilar y generar el .ARR ->    ./gradlew :mylibrary:assembleRelease
//El .ARR se generara en ->             mylibrary/build/outputs/aar/mylibrary-release.aar
//Ahora publica la librería en el repositorio Maven local del proyecto (o de tu sistema): -> ./gradlew :mylibrary:publishReleasePublicationToMavenLocal
//El .aar se instalará en ~/.m2/repository/com/tuempresa/mylibrary/1.0.0/



//Extra: para que otro proyecto la use de manera local:
//En el proyecto que la va a usar, agrega en build.gradle.kts:
//repositories {
//    mavenLocal()
//    google()
//    mavenCentral()
//}
//
//dependencies {
//    implementation("com.tuempresa:mylibrary:1.0.0")
//}
