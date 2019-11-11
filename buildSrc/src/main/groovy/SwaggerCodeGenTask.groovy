import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputDirectory

class SwaggerCodeGenTask extends JavaExec {

    private static final CLASS_NAME = 'io.swagger.codegen.SwaggerCodegen'

    @InputFile
    File apiFile
    @InputFile
    File configFile

    String lang
    String templateDir

    @OutputDirectory
    File output = new File("$project.buildDir/generated")

    SwaggerCodeGenTask() {
        main = CLASS_NAME
        classpath = project.configurations.swaggerCodegen
        args += "generate"
        println("Building")
    }

    @Override
    void exec() {
        if (verifierParametres()) {
            args += ["--output", output]
            args += ["--lang", lang]
            args += ["--input-spec", apiFile]

            if (templateDir) {
                args += ["--template-dir", templateDir]
            }

            if (configFile) {
                args += ["--config", configFile]
            }

            println(args)
            super.exec()
        }
    }

    boolean verifierParametres() {
        if (!lang) {
            println("Le langage cible n'est pas spécifié")
            return false
        }

        return true
    }

}
