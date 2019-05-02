plugins {
    id("com.techshroom.incise-blue") version "0.3.13"
    java
}

inciseBlue {
    util {
        setJavaVersion(JavaVersion.VERSION_1_8)
    }
    lwjgl {
        lwjglVersion = "3.2.1"
        addDependency("")
        addDependency("glfw")
        addDependency("opengl")
        addDependency("nanovg")
    }
}
