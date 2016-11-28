# GatherContent AEM integration
![Image of Integration](https://github.com/AntoniBertel/GatherContentAssets/blob/master/GC-AEM.png)

GatherContent integration plugin allows you to map each field in your GatherContent Templates with AEM template fields.

- [x] Migrate content into AEM
- [x] Update migrated content in AEM
- [ ] Import content into GatherContent (soon)
- [ ] Publish content to AEM (soon)

## Modules

The main parts of the template are:

* core: Java bundle containing all core functionality like OSGi services, listeners or schedulers, as well as component-related Java code such as servlets or request filters.
* ui.apps: contains the /apps (and /etc) parts of the project, ie JS&CSS clientlibs, components, templates, runmode specific configs as well as Hobbes-tests
* ui.content: contains sample content using the components from the ui.apps
* ui.tests: Java bundle containing JUnit tests that are executed server-side. This bundle is not to be deployed onto production.
* ui.launcher: contains glue code that deploys the ui.tests bundle (and dependent bundles) to the server and triggers the remote JUnit execution

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

In progress

## Maven settings

The project comes with the auto-public repository configured. To setup the repository in your Maven settings, refer to:

    http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html
    
## Produced by
![Axamit](https://github.com/AntoniBertel/GatherContentAssets/blob/master/GC-AEM.png)

