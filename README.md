# GatherContent AEM integration v2.0
[![Image of Integration](https://github.com/AntoniBertel/GatherContentAssets/blob/master/GC-AEM.png)](https://gathercontent.com/)

GatherContent’s AEM integration allows content editors to import and update content from GatherContent to AEM; export and update content from AEM to GatherContent. Editors are able to specify import or export mappings, defining which templates and fields should be mapped and then imported, exported or updated. The integration also allows content editors to update the GatherContent workflow status for all items that are successfully imported, exported or updated.

- [x] Migrate (import) content from GatherContent into AEM
- [x] Update migrated (imported) content in AEM
- [x] Migrate (export) content from AEM into GatherContent
- [x] Update migrated (exported) content in GatherContent


|  **`AEM 6.0`**   |  **`AEM 6.1`** | **`AEM 6.2`** | **`AEM 6.3`** |
|-------------------|----------------------|------------------|------------------|
|:ok_hand: Supported |:ok_hand: Supported |:ok_hand: Supported |:ok_hand: Supported |

## Modules

The main parts of the template are:

* core: Java bundle containing all core functionality like OSGi services, listeners or schedulers, as well as component-related Java code such as servlets or request filters.
* ui.apps: contains the /apps (and /etc) parts of the project, ie JS&CSS clientlibs, components, templates, runmode specific configs as well as Hobbes-tests

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

If you have a running AEM instance you can build and package the whole project and deploy into AEM with  

    mvn clean install -PautoInstallPackage
    
Or to deploy it to a publish instance, run

    mvn clean install -PautoInstallPackagePublish
    
Or to deploy only the bundle to the author, run

    mvn clean install -PautoInstallBundle

## Testing

v2.0 is in open beta.

## Maven settings

The project comes with the auto-public repository configured. To setup the repository in your Maven settings, refer to:

    http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html
    
## Produced by
[![Axamit](https://github.com/AntoniBertel/GatherContentAssets/blob/master/Axamit.png)](https://axamit.com/)

## Changelog

12/04/2018 [2.0.5-beta Bug Fix](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.5-beta "Package attached")

03/10/2017 [2.0.4-beta Bug Fix](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.4-beta "Package attached")

08/09/2017 [2.0.3-beta Bug Fix + Tech Changes](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.3-beta "Package attached")

15/08/2017 [2.0.2-beta Bug Fix](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.2-beta "Package attached")

12/08/2017 [2.0.1-beta Major Release 2](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.1-beta "Package attached")

27/02/2017 [1.0.38.1 Bug Fix](https://github.com/axamit/gathercontent-aem-integration/releases/tag/1.0.38.1 "Package attached")

28/11/2016 [1.0.38 Major Release 1](https://github.com/axamit/gathercontent-aem-integration/releases/tag/1.0.38 "Package attached")
