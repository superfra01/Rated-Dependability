<#
.SYNOPSIS
Script di Misurazione Energetica per Singola Funzionalità (Metodo Test)
#>

# --- CONFIGURAZIONE ---
$ProjectDir = ".\Rated"
$OutputDir = "..\energy_results_func" # Nuova cartella per risultati granulari
$EnergiBridgeName = ".\energibridge.exe" 

# 1. Trova EnergiBridge (Percorso Assoluto)
if (Test-Path $EnergiBridgeName) {
    $EnergiBridgeCmd = (Resolve-Path $EnergiBridgeName).Path
} else {
    Write-Host "ERRORE: $EnergiBridgeName non trovato in $PWD" -ForegroundColor Red
    exit
}

Write-Host "--- Inizio Preparazione Ambiente ---" -ForegroundColor Cyan
Write-Host "EnergiBridge: $EnergiBridgeCmd" -ForegroundColor Gray

# 2. Vai nella cartella del codice
if (Test-Path $ProjectDir) {
    Set-Location $ProjectDir
    Write-Host "Directory attiva: $(Get-Location)" -ForegroundColor Gray
} else {
    Write-Host "Errore: Cartella $ProjectDir non trovata." -ForegroundColor Red
    exit
}

# 3. Avvio Docker
Write-Host "Check Database..."
docker-compose up -d
Start-Sleep -Seconds 5

# 4. Compilazione e Warm-up (Fondamentale!)
Write-Host "Compilazione e Warm-up..."
# Compiliamo tutto e lanciamo un test a vuoto per 'scaldare' la JVM e il DB
cmd /c "mvn clean compile test-compile -DskipTests"
Write-Host "Compilazione completata." -ForegroundColor Green

# 5. Cartella Output
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
}

Write-Host "--- Inizio Misurazione Funzionalità Singole ---" -ForegroundColor Cyan

# MAPPING: "Nome Funzionalità" = "Classe#Metodo"
# Ho estratto questi metodi dai file Java che hai caricato
$Operations = @{
    # Autenticazione
    "Funz_Registrazione"    = "integration.test_Gestione_utenti.AutenticationServiceIntegrationTest#testRegister_NewUser_ShouldSucceed";
    "Funz_Login"            = "integration.test_Gestione_utenti.AutenticationServiceIntegrationTest#testLogin_CorrectCredentials_ShouldReturnUser";
    
    # Catalogo
    "Funz_VisualizzaCatalogo" = "integration.test_Gestione_catalogo.CatalogoServiceIntegrationTest#testGetFilms_ReturnsAllFilms";
    "Funz_RicercaFilm"        = "integration.test_Gestione_catalogo.CatalogoServiceIntegrationTest#testRicercaFilm_ExistingTitle_ShouldReturnResult";
    "Funz_AggiungiFilm"       = "integration.test_Gestione_catalogo.CatalogoServiceIntegrationTest#testAggiungiFilm_ValidData_ShouldSaveToDatabase";
    "Funz_RimuoviFilm"        = "integration.test_Gestione_catalogo.CatalogoServiceIntegrationTest#testRimuoviFilm_ShouldDeleteFromDB";

    # Recensioni
    "Funz_AggiungiRecensione" = "integration.test_Gestione_recensioni.RecensioniServiceIntegrationTest#testAddRecensione_Valid_ShouldCreateAndUpdateFilmRating";
    "Funz_MettiLike"          = "integration.test_Gestione_recensioni.RecensioniServiceIntegrationTest#testAddValutazione_NewLike_ShouldIncrementNLike";
    "Funz_EliminaRecensione"  = "integration.test_Gestione_recensioni.RecensioniServiceIntegrationTest#testDeleteRecensione_ShouldRemoveAndUpdateFilmRating"
}

foreach ($OpName in $Operations.Keys) {
    $TestMethod = $Operations[$OpName]
    $CsvPath = Join-Path (Resolve-Path $OutputDir).Path "$OpName.csv"
    
    Write-Host "--------------------------------------------------"
    Write-Host "Misurazione: $OpName" -ForegroundColor Yellow
    Write-Host "Target: $TestMethod"
    
    # --- COSTRUZIONE COMANDO ---
    # Maven accetta -Dtest=Classe#Metodo
    $ArgumentList = @(
        "-o", $CsvPath,
        "cmd", "/c", "mvn -Dtest=$TestMethod test"
    )

    try {
        Start-Process -FilePath $EnergiBridgeCmd -ArgumentList $ArgumentList -Wait -NoNewWindow -ErrorAction Stop
        Write-Host "OK -> $CsvPath" -ForegroundColor Green
    } catch {
        Write-Host "ERRORE: $_" -ForegroundColor Red
    }
    
    # Pausa per far stabilizzare la CPU
    Start-Sleep -Seconds 3
}

Write-Host "--- Fine ---" -ForegroundColor Cyan
Write-Host "Risultati salvati in: $OutputDir"