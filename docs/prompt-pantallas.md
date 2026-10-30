# Prompt para crear una pantalla

Usen este prompt si le piden su pantalla a una IA. Peguen el prompt completo y,
debajo, el contenido de estos archivos del proyecto:

1. `src/main/resources/autoparts/view/ListTemplate.fxml`
2. `src/main/java/autoparts/view/ListTemplateController.java`
3. La clase del modelo que va a mostrar (por ejemplo `Component.java`)
4. El servicio que va a usar (por ejemplo `ComponentService.java`)
5. `src/main/java/autoparts/AppServices.java`

Llenen lo que está entre `[corchetes]`.

---

```
Estoy haciendo una pantalla JavaFX (FXML + controlador) para un proyecto en Java 21
con arquitectura en capas. Te paso una PLANTILLA que debes copiar, no inventar otra.

Pantalla que necesito: [nombre, por ejemplo Catalog.fxml + CatalogController]
Qué muestra: [por ejemplo: los componentes del catálogo con buscador por nombre o referencia]
Clase del modelo de cada fila: [por ejemplo Component]
Columnas de la tabla: [por ejemplo: Nombre, Referencia, Marca (brand.getName()), Categoría, Procedencia, Estado]
Servicio y método: [por ejemplo AppServices.components().search(texto) → List<Component>]
Filtro (ComboBox): [por ejemplo: Todos / ACTIVO / INACTIVO, o "no lleva filtro"]
Resaltar la primera fila: [sí (solo el comparador) / no]

Reglas obligatorias:
1. Copia la estructura de ListTemplate.fxml: el mismo VBox, encabezado, barra (toolbar),
   TableView y lblMessage. Solo cambia textos, fx:id de columnas y el fx:controller.
2. No uses style="..." ni colores en el FXML. Usa solo estas clases de styles.css:
   page-title, secondary-text, field-label, toolbar, primary-button, secondary-button,
   error-message, success-message, highlight-row.
3. Copia la estructura de ListTemplateController: @FXML para cada componente,
   initialize() configura las columnas con setCellValueFactory(row -> new SimpleStringProperty(...))
   y search() llama al servicio.
4. El controlador solo llama al servicio a través de AppServices. Nunca usa DAO ni FileUtil.
5. Captura InvalidDataException y PersistenceException y muestra el mensaje en lblMessage.
6. Borra ExampleItem, exampleData() y filter() de la plantilla.
7. Paquete autoparts.view; nombres en inglés; textos de pantalla y Javadoc en español;
   Javadoc en la clase con @author [su nombre].
8. Código simple: sin librerías extra, sin streams complicados, sin clases internas
   innecesarias. Lo tengo que explicar en clase.

Si el servicio todavía no está en AppServices, dime también qué agregar ahí.
Devuélveme los archivos completos: el .fxml y el .java.
```
