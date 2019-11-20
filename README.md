# printer
A Spring-Boot micro-service for generating documents (odt, docx, doc, pdf).
The service internally uses [XDocReport](https://github.com/opensagres/xdocreport/wiki) to generate and convert documents, we encourage you to read about it!

The generation process takes in input :
- A template (odt, docx, doc) with Freemarker placeholders
- A data model<br>

Then merge it to produce a document containing the data.

![Generation process!](assets/process_generation.png "Generation process") 


## How to run

Pull the latest docker image :
```
docker pull pcscol/printer-server
```
Run the image :
```
docker run -u root -e printer.template.base-url=$TEMPLATE_BASE_URL pcscol/printer-server
```

## How to use

The printer-server exposes a swagger UI for testing accessible at: [http://${CONTAINER_HOST}:8080/swagger-ui.html](http://${CONTAINER_HOST}:8080/swagger-ui.html)
You can use it to try the WS :

1. Provide a body

```
{
  // 1 : Tell if the generated document must be converted to pdf or just keep the template format  
  "convert": true,

  // 2 : The path of the template. It can be absolute or relative, if so it will be prefixed with the value of $TEMPLATE_BASE_URL. default is 'classpath:/'  
  "templateUrl": "classpath:/certificat.odt"   

  // 3 : The data to merge within the document  
  "data": {"firstName" : "John", "lastName" : "Doe"},
  // 4 : Some metadata about fields : will be detailed below  
  "fieldsMetadata": [
    
  ]
}
```
2. Set the expected response content type (application/pdf in our case)
3. Click 'Execute'. You should receive a 200 http response, and a link to download the generated document should appear.

NB : In the case where the downloaded file is corrupted use curl instead.

### The fieldsMetadata purpose

Fields metadata are used to add styling and rendering behaviour to some fields.
You can find here some documentation about it :

- [Text Styling](https://github.com/opensagres/xdocreport/wiki/DocxReportingJavaMainTextStyling)
- [Dynamic image rendering](https://github.com/opensagres/xdocreport/wiki/DocxReportingJavaMainDynamicImage)
- List rendering : [here](https://github.com/opensagres/xdocreport/wiki/DocxReportingJavaMainListFieldInTable) and [here](https://github.com/opensagres/xdocreport/wiki/DocxReportingJavaMainListFieldAdvancedTable)

### Input template formatting

The Printer Service expects input templates using the [FreeMarker](https://freemarker.apache.org/) syntax.

```
L’étudiant ${firstName!"Prénom"} ${lastName!"Nom"} est bien inscrit !
```

So to exploit the full templating capabilities, you need to learn about FreeMarker ! 

NB : Sometimes it's not sufficient to just add the marker __${field}__ in the document text, you need to use a [MergeField in Word](https://www.systemonesoftware.com/en/support/article/38-merge-fields-in-word-for-windows)
or a [Input-field in OpenOffice](https://wiki.openoffice.org/wiki/Documentation/OOo3_User_Guides/Writer_Guide/Using_input_fields).

## How to generate a client for the API

You can easily generate any type of client for the API by passing the YAML definition to the [SwaggerCodegen](https://github.com/swagger-api/swagger-codegen) plugin.
The YAML definition is released to mavenCentral under the arfifact : __fr.pcscol:printer-api:$VERSION__

## How to build the sources 
 
1. Build the API : gradle :printer-api:build
2. Build the Server : gradle :printer-server:build :printer-server:jibDockerBuild
3. Run the integration test : gradle :integration-test:build

 
