<#
.SYNOPSIS
Script di Misurazione Energetica per Rated-Sustainable
Sintassi EnergiBridge aggiornata: energibridge -o file.csv cmd /c ...
#>

# --- CONFIGURAZIONE ---
$ProjectDir = ".\Rated"
$OutputDir = "..\energy_results"
$EnergiBridgeName = "C:\Users\raofr\EnergiBridge\target\release\energibridge.exe" 

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

# 4. Compilazione (senza misurazione)
Write-Host "Compilazione preliminare..."
cmd /c "mvn clean compile test-compile -DskipTests"
Write-Host "Compilazione completata." -ForegroundColor Green

# 5. Cartella Output
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
}

Write-Host "--- Inizio Misurazione Operazioni ---" -ForegroundColor Cyan

# ELENCO OPERAZIONI
$Operations = @{
    "Login_e_Autenticazione" = "integration.test_Gestione_utenti.AutenticationServiceIntegrationTest";
    "Gestione_Catalogo"      = "integration.test_Gestione_catalogo.CatalogoServiceIntegrationTest";
    "Gestione_Recensioni"    = "integration.test_Gestione_recensioni.RecensioniServiceIntegrationTest";
    "Moderazione"            = "integration.test_Gestione_utenti.ModerationServiceIntegrationTest";
    "Gestione_Profilo"       = "integration.test_Gestione_utenti.ProfileServiceIntegrationTest"
}

foreach ($OpName in $Operations.Keys) {
    $TestClass = $Operations[$OpName]
    $CsvPath = Join-Path (Resolve-Path $OutputDir).Path "$OpName.csv"
    
    Write-Host "--------------------------------------------------"
    Write-Host "Misurazione: $OpName" -ForegroundColor Yellow
    
    # --- COSTRUZIONE COMANDO ---
    # Usiamo la sintassi: energibridge -o output.csv cmd /c "comando maven"
    $ArgumentList = @(
        "-o", $CsvPath,
        "cmd", "/c", "mvn -Dtest=$TestClass test"
    )

    # Debug: Mostra il comando che verrà eseguito
    # Write-Host "Eseguo: $EnergiBridgeCmd $ArgumentList" -ForegroundColor DarkGray

    try {
        Start-Process -FilePath $EnergiBridgeCmd -ArgumentList $ArgumentList -Wait -NoNewWindow -ErrorAction Stop
        Write-Host "OK -> $CsvPath" -ForegroundColor Green
    } catch {
        Write-Host "ERRORE: $_" -ForegroundColor Red
    }
    
    Start-Sleep -Seconds 2
}

Write-Host "--- Fine ---" -ForegroundColor Cyan