# Rated — Dependability Project 2025/2026

## Deploy the published application image

The application image is published as `superfra01/rated:latest` and contains the
compiled WAR. It can be started with the accompanying Compose definition without
mounting artifacts from the host machine.

```powershell
cd Rated
Copy-Item .env.example .env
# Edit .env and replace the two password values.
docker compose pull
docker compose up -d
```

The web application is available at `http://localhost:8080`. The MySQL database
is internal to the Compose network; it is not exposed on the host. To start the
optional Adminer interface, run `docker compose --profile tools up -d`.

To test a locally-built image instead of Docker Hub, build it from the `Rated`
directory and set `RATED_IMAGE=rated:local` in `.env`:

```powershell
docker build -t rated:local .
docker compose up -d
```
