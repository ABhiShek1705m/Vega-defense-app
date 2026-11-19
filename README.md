# Vega Defense App

A web platform for monitoring network traffic and detecting anomalous (potentially malicious) activity on machines you register with it. It was built originally by the course instructor for **SENG 426** at the University of Victoria which was then evalauated and tested by our group (grp. 16) for the course project.

The platform has three parts:

| Part | What it is | Tech |
|------|------------|------|
| **Web frontend** (`vega-web`) | Public site, login/sign-up, admin panel, and the live *Defense App Dashboard* | React 17, React-Bootstrap, Chart.js |
| **Backend API** (`vega-spring`) | REST + WebSocket API, authentication, persistence, file hosting | Spring Boot 2.7, Java 17, MySQL 8, Flyway |
| **Defense agent** (`files/vegaDefenseApp.tar.gz`) | Python agent installed on the machine to be protected; sniffs packets and classifies them | Python 3.10, Scapy, scikit-learn, Flask |

An automated Selenium UI test suite lives in `testing/`.

## How it works

1. A user signs up in the web UI. An **admin** activates the account and assigns a role (`ADMIN`, `EMPLOYEE`, or `USER`).
2. The user downloads the Defense Agent bundle from the dashboard and installs it on the target machine (see [Installing the defense agent](#installing-the-defense-agent)).
3. `vega register` registers that machine with the backend (hostname, host IP, network interface). The agent then runs a small Flask server on **port 5000**.
4. From the dashboard, the user clicks *Start/Stop sniffing* or *Uninstall*. The backend forwards these commands to the agent's Flask server (`/start`, `/stop`, `/uninstall`).
5. While sniffing, the agent captures raw packets, extracts flow/traffic features, and runs them through a pre-trained scikit-learn model (shipped in the agent wheel) to label traffic as benign or anomalous. Results and features are uploaded to the backend (`POST /api/v1/defenseApp/uploadData`) and stored in MySQL.
6. The backend pushes live metrics and the machine's security status to the dashboard over **WebSockets** (`/api/v1/ws/defenseClient/Metrics/{appId}` and `/SecStatus/{appId}`).

Authentication is stateless JWT (Spring Security); roles gate both API endpoints and frontend routes (e.g. `/adminpanel` is admin-only).

## Project structure

```
Vega-defense-app/
├── vega-spring/              # Backend (Spring Boot, Maven, WAR)
│   ├── src/main/java/io/venus/vega/
│   │   ├── api/v1/           # REST controllers (generated interfaces from api.yaml) + WebSocket handlers
│   │   ├── services/         # Business logic (users, roles, files, defense app, metrics, sec status)
│   │   ├── data/             # JPA entities, repositories, DB seeder
│   │   ├── security/         # JWT filter/generator, Spring Security config
│   │   └── config/           # WebSocket, JPA auditing, exception handling
│   ├── src/main/resources/
│   │   ├── static/api.yaml   # OpenAPI spec – API interfaces/models are generated from this at build time
│   │   ├── db/migration/     # Flyway SQL migrations
│   │   └── application*.properties
│   └── Dockerfile
├── vega-web/                 # Frontend (React, Create React App)
│   ├── src/components/       # Atomic-design UI: atoms → molecules → organisms → templates → pages
│   ├── src/service/          # API clients (login, admin, defense app, …)
│   ├── src/auth/             # Auth provider and role-protected routes
│   └── Dockerfile
├── files/vegaDefenseApp.tar.gz  # Defense agent bundle (Python wheel + installer), served to users for download
├── testing/                  # Selenium + TestNG end-to-end tests (Maven)
├── docker-compose.yml        # Local build-from-source stack (frontend + backend; MySQL is commented out)
├── docker-compose-cd.yml     # Stack using pre-built Docker Hub images, includes MySQL
└── .gitlab-ci.yml            # Legacy CI from the original GitLab repo (build + Docker push)
```

## Prerequisites

- **Java 17** and **Maven 3.8+** (the backend also ships `mvnw`)
- **Node.js 16** and npm (the Dockerfile and CI use Node 16)
- **MySQL 8** (or Docker to run it)
- **Docker + Docker Compose** (optional, for the containerized setup)
- A **Linux machine/VM** with root access for the defense agent (raw packet capture)

## Build and run

### Option A: Docker Compose (recommended)

`docker-compose.yml` expects a MySQL host named `mysql`, but that service is commented out. Pick one:
- **Build from source:** in `docker-compose.yml`, make the backend's `SPRING_DATASOURCE_PASSWORD` match `MYSQL_ROOT_PASSWORD`. Then run `docker compose up --build` from project root.
- **With bundled MySQL (pre-built images):** `docker compose -f docker-compose-cd.yml up -d`. This pulls the images published by the original team (`eafolaranmi/vega_back`, `eafolaranmi/vega_front`), (note that this may be out of date relative to this repo).


Before building the frontend, set the backend URL in the configuration. The stack serves the frontend on <http://localhost:3000> (port 80 in `docker-compose-cd.yml`) and the API on <http://localhost:8080>.

### Option B: Run locally

**1. Database**: create an empty `vega` database. Flyway creates all tables on first start.

```bash
mysql -u root -p -e "CREATE DATABASE vega;"
```

**2. Backend**: the default profile connects to `localhost:3306` as `root`; change the credentials in `vega-spring/src/main/resources/application.properties` (or override with `SPRING_DATASOURCE_*` environment variables) to match your MySQL.

```bash
cd vega-spring
mvn clean package -DskipTests        # produces target/vega-1.0.0.war
cd ..
java -jar vega-spring/target/vega-1.0.0.war
```

Run it from the repository root: the default `defense.app.path=./files/vegaDefenseApp.tar.gz` and `files.basic-folder-path=./uploads` are relative to the working directory. The API listens on <http://localhost:8080/api/v1>.

**3. Frontend**

```bash
cd vega-web
npm install
npm start                            # http://localhost:3000
```

`npm run build` creates a production bundle in `vega-web/build/`.

### First login

On first start the backend seeds a few accounts (see `DatabaseSeeder.java`), including an admin: `Admin@vega.com` / `123456` (configurable via `main-account.*` in `application.properties`). **These are development defaults; change them before exposing the app anywhere.**

### Installing the defense agent

Extract `files/vegaDefenseApp.tar.gz` (or download it from the dashboard) on a Linux machine. It contains a Python 3.10 source tarball, the `vega-0.1.0` wheel, `install.sh`, and its own README.

```bash
./install.sh                          # builds a local Python 3.10, creates a venv, installs the wheel, grants packet-capture capability
source vegaApp/bin/activate
vega register --email <your-vega-email> --password '<password>' --ip <backend-server-ip>
vega-api                              # starts the agent's Flask server (port 5000)
```

After this, start/stop sniffing and uninstall from the web dashboard. The backend must be able to reach the agent on port 5000, and the agent must be able to reach the backend on port 8080.

### Running the tests

| Suite | Command |
|-------|---------|
| Backend unit tests | `cd vega-spring && mvn test` (requires a running MySQL) |
| Frontend tests | `cd vega-web && npm test` |
| End-to-end (Selenium/TestNG) | `cd testing && mvn test` |

The end-to-end suite drives a real browser (Chrome via WebDriverManager) against a running stack. Set the target URL, accounts, and agent settings in `testing/.env`. `testing/testng.xml` selects the suite: by default it excludes tests tagged `requiresVM` (start/stop/uninstall of a live agent); switch to the first block when a VM with the agent is running.

## Notes on this repository

- This repository was imported from the university GitLab. `.gitlab-ci.yml` and the Docker Hub image names still reflect the original setup and may not work from GitHub as-is.
- `vega-web/README.md` is a leftover from the original template and describes an Express server that is not part of this repo.

## References and citations

**Frameworks and libraries**
- Spring Boot, Spring Security, Spring Data JPA: <https://spring.io/projects/spring-boot>
- Flyway database migrations: <https://flywaydb.org/>
- OpenAPI Generator (API code generation from `api.yaml`): <https://openapi-generator.tech/>
- MapStruct and Project Lombok: <https://mapstruct.org/>, <https://projectlombok.org/>
- JJWT (JSON Web Tokens for Java): <https://github.com/jwtk/jjwt>
- React: <https://react.dev/>; React-Bootstrap: <https://react-bootstrap.github.io/>; Chart.js: <https://www.chartjs.org/>
- Selenium and TestNG: <https://www.selenium.dev/>, <https://testng.org/>
- Scapy (packet capture/parsing): <https://scapy.net/>; scikit-learn (classification model): Pedregosa et al., "Scikit-learn: Machine Learning in Python," *Journal of Machine Learning Research* 12 (2011), 2825–2830.

**Course**
- SENG 426, University of Victoria (Group 16 course project). The application originated as a GitLab course repository (`seng426_gp16`), which this repository was imported from.
