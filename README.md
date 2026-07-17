<p align="center">
  <img src="RATED_icon.png" alt="Logo di Rated" width="180">
</p>

<h1 align="center">Rated</h1>

<p align="center">
  Piattaforma web per scoprire film e condividere recensioni.
</p>

<p align="center">
  <a href="https://github.com/superfra01/Rated-Dependability/actions/workflows/ci.yml"><img src="https://github.com/superfra01/Rated-Dependability/actions/workflows/ci.yml/badge.svg" alt="Stato della CI"></a>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Tomcat-9-F8DC75?logo=apachetomcat&logoColor=black" alt="Tomcat 9">
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white" alt="MySQL 8">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker Compose">
</p>

## Il progetto

Rated è un'applicazione Java basata su Servlet e JSP. Offre un catalogo di film consultabile pubblicamente e funzionalità dedicate a recensori, moderatori e gestori del catalogo

Il repository comprende anche test automatici, benchmark, specifiche JML, pipeline di sicurezza e gli elaborati prodotti per gli esami di software engineering, sustainability, e dependability.

## Architettura

L'applicazione segue una separazione a livelli:

```text
Browser
  └── JSP + Servlet
        └── Service
              └── DAO + Entity
                    └── DataSource JNDI
                          └── MySQL
```

- **Presentation layer:** JSP, CSS, JavaScript e Servlet.
- **Business layer:** servizi per catalogo, recensioni, autenticazione, profili e moderazione.
- **Persistence layer:** DAO JDBC, entity Java e connection pool gestito da Tomcat tramite JNDI.

## Stack tecnologico

| Area | Tecnologie |
| --- | --- |
| Backend | Java 17, Servlet API 4.0, JSP |
| Build e runtime | Maven, WAR, Apache Tomcat 9 |
| Database | MySQL 8, JDBC, JNDI DataSource |
| Test | JUnit 5, Mockito, H2 |
| Qualità | JaCoCo, PIT, JMH, OpenJML |
| Sicurezza e CI | GitHub Actions, GitGuardian, Snyk, SonarCloud |
| Deploy | Docker, Docker Compose |

## Struttura della repository

```text
Rated-Dependability/
├── Rated/                         # Applicazione, test e configurazione Docker
│   ├── src/main/java/             # Entity, DAO, servizi e Servlet
│   ├── src/main/webapp/           # JSP e risorse frontend
│   ├── src/test/java/             # Test unitari, integrazione e benchmark
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── init.sql
│   └── pom.xml
├── Deliverables/                  # Elaborati finali organizzati per esame
├── Diagrams/                      # Diagrammi UML, navigazionali e di sequenza
├── Raw Docs Files/                # Documetazione in formato docx non finale
├── DB.sql                         # Schema SQL storico del progetto
└── RATED_icon.png
```

## Avvio rapido con Docker

### Requisiti

- Git
- Docker con il plugin Compose

### 1. Clona il repository

```bash
git clone https://github.com/superfra01/Rated-Dependability.git
cd Rated-Dependability/Rated
```

### 2. Configurazione dell'ambiente

Creare un file `.env` nella cartella `Rated`:

```dotenv
DB_USERNAME=rated
DB_PASSWORD=password-sicura
MYSQL_ROOT_PASSWORD=password-root-sicura
DB_NAME=RatedDB
APP_PORT=8080
```

### 3. Avviare i servizi

```bash
docker compose up --build -d
docker compose ps
```

Rated sarà disponibile su [http://localhost:8080](http://localhost:8080).

## Build e sviluppo locale

Per compilare il progetto senza Docker sono necessari JDK 17 e Maven 3.9 o successivo:

```bash
cd Rated
mvn clean package
```

Il WAR viene generato in `Rated/target/rated-app.war` e può essere distribuito su Tomcat 9. Il runtime deve esporre le proprietà `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD` usate dal DataSource definito in `META-INF/context.xml`.

## Test e analisi di qualità

Eseguire i test unitari e di integrazione:

```bash
cd Rated
mvn test
```

Eseguire l'intera fase di verifica e generare il report JaCoCo:

```bash
mvn clean verify
```

Il report di copertura sarà disponibile in `target/site/jacoco/index.html`.

Eseguire il mutation testing con PIT:

```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

Il report sarà disponibile in `target/pit-reports`.

Le specifiche formali JML possono essere controllate con gli script inclusi. OpenJML e Maven devono essere disponibili nel `PATH`:

```bash
# Linux o WSL
./run-openjml.sh
```

```powershell
# Windows; lo script usa la distribuzione WSL Ubuntu-24.04
.\run-openjml.cmd
```

La pipeline GitHub Actions esegue inoltre scansione dei segreti, analisi delle dipendenze e SAST, verifica SonarCloud, JaCoCo, PIT e build dell'immagine Docker.

## Documentazione

- [Software Engineering](Deliverables/1.Deliverables-IS)
- [Sustainability](Deliverables/2.Deliverables-Sustainability)
- [Software Testing](Deliverables/3.Deliverables-ISTA)
- [Dependability](Deliverables/4.Deliverables-Dependability)
- [Diagrammi di progetto](Diagrams)
