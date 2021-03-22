# AvaL-JavaScript-OAuth-Demos

Dieses Repository enthält einige Code-Beispiele zur Nutzung des zentralen OAuth-Servers durch JavaScript.

Die JavaScript Code Samples bestehen hauptsächlich aus verschiedenen Tests. Innerhalb der Tests werden verschiedene Module aufgerufen, welche die entsprechende Programmlogik enthalten. Die Tests befinden sich im Verzeichnis /js/tests, und die Module im Verzeichnis /js/modules.

Als Testframework wird node.js mit jest verwendet. Hier wird die Authentifizierung mit [keycloak-connect](https://www.npmjs.com/package/keycloak-connect "keyvloak-connect npm package") und [axios](https://www.npmjs.com/package/axios "axios npm package") beispielhaft implementiert.

Da die bis Dato bereitgestellten Samples hauptsächlich eine synchrone Implementierung  darstellen, sind diese Beispiele für eine mögliche asynchrone Implementierung aufbereitet.

## Getting Started

### node installieren

Als Erstes sollte der [node package manager (npm)](https://www.npmjs.com/) installiert sein. Er wird dazu benötigt, die für die Beispiele verwendeten Dependencies aufzulösen und die Installation für dieses Sample-Projekt durchzuführen.

Der Package Manager npm wird bei der Installation von Node.js automatisch mit installiert. Ein Installer kann zum Beispiel [hier](https://nodejs.org/en/ "OpenJS Foundation") heruntergeladen werden. Zur Installation folgen Sie bitte den Anweisungen des heruntergeladenen Installers.

Eine valide Installation von npm sollte bei der Ausführung von
```
npm -v
```
auf der Kommandozeile eine Versionsnummer liefern.

### Herunterladen der benötigten Module

Nach der Installation von npm kann Mittels der Kommandozeile **in den Ordner /javascript** (dieser Ordner) des Repositorys **gewechselt werden.**

Wenn man dort den Befehl
```
npm install
```
ausführt, lädt der node package manager alle benötigten Module (in package.json unter dependencies aufgeführt) herunter und legt diese in dem Ordner /node_Modules ab.

## Ausführung der Code Samples

Nach den oben beschriebenen Vorbereitungen unter **Getting Started** können die Tests mit

```
npm test
```
ausgeführt werden.
