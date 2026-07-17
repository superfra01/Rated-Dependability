<p align="center">
  <img src="RATED_icon.png" alt="Logo di Rated" width="180">
</p>

<h1 align="center">Rated</h1>

<p align="center">
  Piattaforma web per scoprire film, condividere recensioni e gestire una community cinematografica.
</p>

<p align="center">
  <a href="https://github.com/superfra01/Rated-Dependability/actions/workflows/ci.yml"><img src="https://github.com/superfra01/Rated-Dependability/actions/workflows/ci.yml/badge.svg" alt="Stato della CI"></a>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Tomcat-9-F8DC75?logo=apachetomcat&logoColor=black" alt="Tomcat 9">
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white" alt="MySQL 8">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker Compose">
</p>

## Il progetto

Rated è un'applicazione Java basata su Servlet e JSP. Offre un catalogo di film consultabile pubblicamente e funzionalità dedicate a recensori, moderatori e gestori del catalogo.

Il repository comprende anche test automatici, benchmark, specifiche JML, pipeline di sicurezza e gli elaborati prodotti durante le attività di software engineering, sustainability, testing e dependability.

## Funzionalità

| Profilo | Funzionalità principali |
| --- | --- |
| **Ospite** | Consulta il catalogo, cerca e ordina i film, apre le schede di dettaglio e visualizza i profili pubblici. |
| **Recensore** | Si registra e accede, gestisce profilo e preferenze, riceve suggerimenti, organizza film visti e watchlist, pubblica recensioni, vota e segnala contenuti. |
| **Moderatore** | Esamina le recensioni segnalate, le approva oppure le rimuove applicando un warning all'autore. |
| **Gestore catalogo** | Aggiunge, modifica e rimuove film e relativi metadati. |

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

## Avvio rapido con Docker

### Requisiti

- Git
- Docker con il plugin Compose

### 1. Clona il repository

```bash
git clone https://github.com/superfra01/Rated-Dependability.git
cd Rated-Dependability/Rated
```

### 2. Configura l'ambiente

Crea un file `.env` nella cartella `Rated`:

```dotenv
DB_USERNAME=rated
DB_PASSWORD=scegli-una-password-sicura
MYSQL_ROOT_PASSWORD=scegli-una-password-root-sicura
DB_NAME=RatedDB
APP_PORT=8080
```

> Non versionare il file `.env`: contiene credenziali locali e deve restare fuori dal repository.
>
> `init.sql` inizializza il database `RatedDB`: mantieni questo valore per `DB_NAME`, a meno di aggiornare anche lo script SQL.

### 3. Avvia i servizi

```bash
docker compose up --build -d
docker compose ps
```

Rated sarà disponibile su [http://localhost:8080](http://localhost:8080).

Per seguire i log dell'applicazione:

```bash
docker compose logs -f webapp
```

Per arrestare i container senza eliminare il database:

```bash
docker compose down
```

Per ricreare completamente il database, elimina anche il volume con `docker compose down -v`. Questa operazione rimuove definitivamente i dati locali.

## Build e sviluppo locale

Per compilare il progetto senza Docker sono necessari JDK 17 e Maven 3.9 o successivo:

```bash
cd Rated
mvn clean package
```

Il WAR viene generato in `Rated/target/rated-app.war` e può essere distribuito su Tomcat 9. Il runtime deve esporre le proprietà `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD` usate dal DataSource definito in `META-INF/context.xml`.

## Test e analisi di qualità

Esegui i test unitari e di integrazione:

```bash
cd Rated
mvn test
```

Esegui l'intera fase di verifica e genera il report JaCoCo:

```bash
mvn clean verify
```

Il report di copertura sarà disponibile in `target/site/jacoco/index.html`.

Esegui il mutation testing con PIT:

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

## Struttura del repository

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
├── Deliverables/                  # Elaborati finali organizzati per area
├── Diagrams/                      # Diagrammi UML, navigazionali e di sequenza
├── Raw Docs Files/                # Sorgenti modificabili della documentazione
├── DB.sql                         # Schema SQL storico del progetto
└── RATED_icon.png
```

## Documentazione

- [Software Engineering](Deliverables/1.Deliverables-IS)
- [Sustainability](Deliverables/2.Deliverables-Sustainability)
- [Software Testing](Deliverables/3.Deliverables-ISTA)
- [Dependability](Deliverables/4.Deliverables-Dependability)
- [Diagrammi di progetto](Diagrams)

## Note sui dati locali

Il primo avvio di MySQL esegue `Rated/init.sql`, che crea schema e dati dimostrativi. Le modifiche successive a questo file non vengono applicate a un volume già inizializzato: per ripartire dallo script aggiornato occorre ricreare il volume del database.
