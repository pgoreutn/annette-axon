# Business platform Annette Axon


> An **axon** or nerve fiber, is a long, slender projection of a nerve cell, or neuron, in vertebrates, that typically conducts electrical impulses known as action potentials, away from the nerve cell body. The function of the axon is to transmit information to different neurons, muscles, and glands.
>
> [*Wikipedia*](https://en.wikipedia.org/wiki/Axon)

Annette Axon is the business process management system integrated with project management system. 
Annette Axon helps companies to define, execute and control their non-project and project business processes. 
The platform implements the following functionality:

* flexible business processes based on BPM engine (Camunda BPM)
* organisational structure of one or more companies and project team structures
* content management based on CMIS engine (Alfresco ECM)
* project system with Gantt diagram representation of WBS (work breakdown structure)

## Getting started

Clone repository from GitHub:
```bash
$ git clone https://github.com/valerylobachev/annette-axon.git
```

[Keycloak](https://www.keycloak.org/) is required to provide authentication service for Annette Axon. You have the following options to download and install Keycloak:
* Standalone server distribution (can be found on [Downloads](https://www.keycloak.org/downloads.html) page) or   
* Docker image (can be found on [Docker hub](https://hub.docker.com/r/jboss/keycloak/))

Install Keycloak according [installation guide](https://www.keycloak.org/docs/latest/server_installation/index.html) and run it.

To configure Keycloak perform the following steps:
* Create a new realm (for example Annette) for Annette Axon (see [Create a New Realm](https://www.keycloak.org/docs/latest/server_admin/index.html#_create-realm)) 
* Create new new client in you realm (see [OIDC Clients](https://www.keycloak.org/docs/latest/server_admin/index.html#oidc-clients)). For example you can use the following parameters:
  * Client ID: `app`
  * Client Protocol: `openid-connect`
  * Root URL: `http://localhost:9000/` (or any other URL you want to application run)
*  In created client enable authorization by clicking on `Authorization Enabled` field of `Settings` tab and save changes
* Set RSA public key to the key `annette.security.client.publicKey` in configuration file `annette-axon/axon-backend/conf/application.conf`. To set the key open `Keys` tab of your realm, locate the button `Public key` on the line with type `RSA` and press it. Copy a public key from opened popup to configuration file.
* Download `keycloak.json` file from `Installation` tab of created client (select `Keycloak OIDC JSON` in `Format Option` field)
* Copy downloaded file to folder `annette-axon/axon-backend/conf/`
* Create a new user in your realm to connect with (see [Creating New Users](https://www.keycloak.org/docs/latest/server_admin/index.html#_create-new-user)) 

Install the following software:
* `node.js` and `npm` (see [Node.js](https://nodejs.org) site)
* Java Development Kit 8 (see [Java Downloads](http://www.oracle.com/technetwork/java/javase/downloads/index.html) page)
* Sbt (see [Sbt Download](https://www.scala-sbt.org/download.html) page)

Install Angular CLI:
```bash
$ npm install -g @angular/cli
```

Download and install dependencies for frontend application:
```bash
$ cd axon-frontend
$ npm install
```

Build frontend application:
```bash
$ ng build
```

Build and run backend application:
```bash
$ cd ..
$ sbt runAll
```

Open [http://localhost:9000/](http://localhost:9000/) in your browser and use user credentials you've created in Keycloak to login in application.

## Help and documentation

* [About](docs/about.md)
* [Glossary](docs/glossary.md)
* Architecture
    * [Architecture overview](docs/architecture/overview.md)
    * [Application (frontend & backend)](docs/architecture/application.md)
    * [Authentication service](docs/architecture/authentication.md)
    * [Authorization service](docs/architecture/authorization.md)
    * [Organizational Structure service](docs/architecture/orgstructure.md)
    * [BPM service](docs/architecture/bpm.md)
    * [Project service](docs/architecture/projects.md)
    * [Forms service](docs/architecture/forms.md)
    * [Knowledge service](docs/architecture/knowledge.md)
    * [Content Management service](docs/architecture/cms.md)
    * [Search service](docs/architecture/search.md)
  

## Contributing

Contributions are *very* welcome!

If you see an issue that you'd like to see fixed, the best way to make it happen is to help out by submitting a pull request implementing it.

Refer to the [CONTRIBUTING.md](docs/CONTRIBUTING.md) and  [CODE_OF_CONDUCT.md](docs/CODE_OF_CONDUCT.md) file for more
 details about the workflow, and general hints on how to prepare your pull request. You can also ask for 
 clarifications or guidance in GitHub issues.


## License

Annette Axon is Open Source and available under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
