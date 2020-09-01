# PlantUML from source

A simple Gradle plugin that generates PlantUML class diagrams from Java source files.
It uses [QDox](https://github.com/paul-hammant/qdox) to parse the source files.

This plugin is **not ready for production**.

## Usage

### Tasks

The `buildClassDiagram` task reads all source files and generates a PlantUML diagram.

### Configuration

This plugin can be configured using the `classDiagram` extension
in your *build.gradle*.

#### Example

```groovy
classDiagram {
    sourceSet = sourceSets.main
    outputFile = project.file('build/diagrams/main.plantuml')
    visibility = "package private"
    associationFieldVisibility = "private"
    associationMethodVisibility = null
    colors = [
        'some.package.name': '#d8d4fc',
        'some.other.package.name': '#d4fcdc'
    ]
    before = "skinparam shadowing false"
}
```

### Properties

#### `sourceSet`

The source files to include in the diagram.

#### `outputFile`

The output file for the PlantUML diagram.

#### `visibility`

The minimum visibility for classes, methods and fields to be included in the diagram.

Allowed values: `"public"`, `"protected"`, `"package private"`, `"private"`

*Default value*: `"public"`

#### `associationFieldVisibility`

The minimum visibility for fields to be included in the creation of associations.
To hide all field associations set this property to `null`.

*Allowed values*: `null`, `"public"`, `"protected"`, `"package private"`, `"private"`

*Default value*: `"private"`

#### `associationMethodVisibility`

The minimum visibility for methods to be included in the creation of associations.
To hide all method associations set this property to `null`.

*Allowed values*: `null`, `"public"`, `"protected"`, `"package private"`, `"private"`

*Default value*: `"public"`

#### `colors`

A map of colors for specific packages.

*Default value*: `[]`

#### `before`

A string to include in the generated diagram file before all generated definitions.

*Default value*: `""`
