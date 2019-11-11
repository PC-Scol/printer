import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.yaml.snakeyaml.Yaml

class SwaggerAggregateFiles extends DefaultTask {

    @InputFiles
    FileTree apiFiles

    @OutputFile
    File output

    Yaml yaml = new Yaml()

    @TaskAction
    def aggregate() {
        List<Map<String, ?>> definitions = parseApiFiles()
        Map<String, ?> aggregatedDefinitions = new LinkedHashMap<>()

        aggregatedDefinitions.put("openapi", getCoherentValue(definitions, "openapi"))
        aggregatedDefinitions.put("info", getInfo(definitions))
        aggregatedDefinitions.put("servers", getCoherentValue(definitions, "servers"))
        aggregatedDefinitions.put('tags', getTags(definitions))
        aggregatedDefinitions.put('security', getSecurities(definitions))
        aggregatedDefinitions.put('paths', getPaths(definitions))
        aggregatedDefinitions.put('components', getComponents(definitions))

        println("Writing file " + output)
        output.withWriter { writer ->
            writer.append(yaml.dumpAsMap(aggregatedDefinitions))
        }
    }

    /**
     * Chargement de tous les fichiers à aggréger en mémoire.
     */
    private List<Map<String, ?>> parseApiFiles() {
        println("Aggregating files :")
        List<Map<String, ?>> definitions = new ArrayList<>()

        for (File apiFile : apiFiles.sort { it.name }) {
            println("- Adding " + apiFile)
            Map definition = yaml.loadAs(new FileReader(apiFile), Map)
            definition.put("__filename__", apiFile.getName())
            definitions.add(definition)
        }
        return definitions
    }

    /**
     * Lecture du bloc "info".
     * Tout les blocs infos de chaque fichier yml doivent être identiques.
     */
    private Map<String, String> getInfo(List<Map<String, ?>> definitions) {
        Map<String, String> info = new LinkedHashMap()
        List<Map<String, ?>> infos = select(definitions, "info")

        info.put("version", getCoherentValue(infos, "version"))
        info.put("title", getCoherentValue(infos, "title"))
        info.put("description", getCoherentValue(infos, "description"))

        return info
    }

    /**
     * Lecture des blocs "tags".
     * Aggrégation de chaque bloc "tags" de chaque fichier yml.
     */
    private List<?> getTags(List<Map<String, ?>> definitions) {
        List<List<?>> tagsAggregated = new ArrayList<>()
        List<Map<String, ?>> allTags = select(definitions, "tags")

        allTags.each { tags ->
            tagsAggregated.addAll(tags)
        }
        return tagsAggregated
    }

    /**
     * Lecture des blocs "security".
     * Aggrégation de chaque bloc "security" de chaque fichier yml.
     */
    private List<?> getSecurities(List<Map<String, ?>> definitions) {
        List<List<?>> tagsAggregated = new ArrayList<>()
        List<Map<String, ?>> allTags = select(definitions, "security")

        allTags.each { tags ->
            tagsAggregated.addAll(tags)
        }
        return tagsAggregated
    }

    /**
     * Lecture des blocs "paths".
     * Aggrégation de chaque bloc "paths" de chaque fichier yml.
     * La même route ne peut pas être définie plusieurs fois.
     */
    private Map<String, ?> getPaths(List<Map<String, ?>> definitions) {
        Map<String, ?> pathsAggregated = new LinkedHashMap<>()
        List<Map<String, ?>> allPaths = select(definitions, "paths")

        allPaths.each { paths ->
            paths.each {
                key, value ->
                    if (pathsAggregated.containsKey(key)) {
                        throw new InvalidUserDataException("Au moins 2 fichiers YAML définissent la route " + key)
                    }
                    pathsAggregated.put(key, value)
            }

        }

        return pathsAggregated
    }

    /**
     * On aggrège toutes les sous-structures de la structure components de tous les fichiers.
     *  components:
     *      schemas:
     *      parameters:
     *      responses:
     *      ...
     *
     * @param Les définitions de tous les YAML
     * @return Une structure 'components' aggrégée
     */
    private Map<String, ?> getComponents(List<Map<String, ?>> definitions) {
        Map<String, ?> aggregatedComponents = new LinkedHashMap<>()
        List<Map<String, ?>> allPaths = select(definitions, "components")

        allPaths.each { path ->
            path.each { subComponentKey, subComponentValue ->
                // On récupère ou crée la structure
                Map<String, ?> subComponentMap = aggregatedComponents.get(subComponentKey)
                if (subComponentMap == null) {
                    subComponentMap = new LinkedHashMap<>();
                    aggregatedComponents.put(subComponentKey, subComponentMap)
                }

                subComponentValue.each { key, value ->
                    if (subComponentMap.containsKey(key)) {
                        throw new InvalidUserDataException("Au moins 2 fichiers YAML contiennent le composant " + subComponentKey + "." + key)
                    }
                    subComponentMap.put(key, value)
                }
            }
        }

        return aggregatedComponents
    }


    /**
     * Lecture d'un bloc dans chaque fichier yml.
     */
    private List<Map<String, ?>> select(List<Map<String, ?>> definitions, String key) {
        definitions
                .findAll { definition -> definition[key] != null }
                .collect { definition -> definition[key] } as List<Map<String, ?>>
    }

    private Object getCoherentValue(List<Map<String, Object>> definitions, String key) {
        if (definitions.isEmpty()) {
            throw new InvalidUserDataException("Aucun fichier YAML")
        }
        Object value = getRequiredValue(definitions.first(), key)

        definitions.each { definition ->
            if (getRequiredValue(definition, key) != value) {
                throw new InvalidUserDataException("Versions incohérentes dans les fichiers swagger pour la clé " + key)
            }
        }

        return value
    }

    /**
     * Rechercher la valeur d'une clé.
     * La valeur doit être présente (obligatoire).
     *
     * @param definition Le YAML
     * @param key La clé
     * @return La valeur
     */
    Object getRequiredValue(Map<String, Object> definition, String key) {
        def value = definition[key]
        if (!value) {
            throw new InvalidUserDataException("Le fichier YAML " + definition.get("__filename__") + " ne contient pas de clé " + key)
        }
        return value
    }

}
