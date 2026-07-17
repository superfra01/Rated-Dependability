<#
.SYNOPSIS
Script di Misurazione Energetica per Singola Funzionalità (Metodo Test) - Multi Run (10x)
#>

# --- CONFIGURAZIONE ---
$ProjectDir = ".\Rated"
$OutputDir = "..\energy_results_func" 
$EnergiBridgeName = "C:\Users\raofr\EnergiBridge\target\release\energibridge.exe" 
$MaxRuns = 10 # Numero di ripetizioni per ogni test
# 1. Trova EnergiBridge
if (Test-Path $EnergiBridgeName) {
    $EnergiBridgeCmd = (Resolve-Path $EnergiBridgeName).Path
} else {
    Write-Host "ERRORE: $EnergiBridgeName non trovato in $PWD" -ForegroundColor Red
    exit
}

# 2. Vai nella cartella del codice
if (Test-Path $ProjectDir) {
    Set-Location $ProjectDir
} else {
    Write-Host "Errore: Cartella $ProjectDir non trovata." -ForegroundColor Red
    exit
}

# 3. Preparazione
Write-Host "--- Preparazione ---" -ForegroundColor Cyan
# Compilazione veloce
cmd /c "mvn clean compile test-compile -DskipTests" | Out-Null
Write-Host "Compilazione completata." -ForegroundColor Green

if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
}

Write-Host "--- Inizio Misurazione (10 Iterazioni) ---" -ForegroundColor Cyan

$Operations = @{
    "Funz_Registrazione"      = "integration.test_Gestione_utenti.AutenticationServiceIntegrationTest#testRegister_NewUser_ShouldSucceed";
    "Funz_Login"              = "integration.test_Gestione_utenti.AutenticationServiceIntegrationTest#testLogin_CorrectCredentials_ShouldReturnUser";
    "Funz_VisualizzaCatalogo" = "integration.test_Gestione_catalogo.CatalogoServiceIntegrationTest#testGetFilms_ReturnsAllFilms";
    "Funz_RicercaFilm"        = "integration.test_Gestione_catalogo.CatalogoServiceIntegrationTest#testRicercaFilm_ExistingTitle_ShouldReturnResult";
    "Funz_AggiungiFilm"       = "integration.test_Gestione_catalogo.CatalogoServiceIntegrationTest#testAggiungiFilm_ValidData_ShouldSaveToDatabase";
    "Funz_RimuoviFilm"        = "integration.test_Gestione_catalogo.CatalogoServiceIntegrationTest#testRimuoviFilm_ShouldDeleteFromDB";
    "Funz_AggiungiRecensione" = "integration.test_Gestione_recensioni.RecensioniServiceIntegrationTest#testAddRecensione_Valid_ShouldCreateAndUpdateFilmRating";
    "Funz_MettiLike"          = "integration.test_Gestione_recensioni.RecensioniServiceIntegrationTest#testAddValutazione_NewLike_ShouldIncrementNLike";
    "Funz_EliminaRecensione"  = "integration.test_Gestione_recensioni.RecensioniServiceIntegrationTest#testDeleteRecensione_ShouldRemoveAndUpdateFilmRating"
}

foreach ($OpName in $Operations.Keys) {
    $TestMethod = $Operations[$OpName]
    
    Write-Host "--------------------------------------------------"
    Write-Host "Target: $OpName" -ForegroundColor Yellow
    
    for ($i = 1; $i -le $MaxRuns; $i++) {
        $CsvFileName = "${OpName}_run_${i}.csv"
        $CsvPath = Join-Path (Resolve-Path $OutputDir).Path $CsvFileName

        # Pulizia preventiva
        if (Test-Path $CsvPath) { Remove-Item $CsvPath }

        Write-Host "   > Esecuzione $i di $MaxRuns..." -NoNewline

        $ArgumentList = @(
            "-o", $CsvPath,
            "cmd", "/c", "mvn -Dtest=$TestMethod test"
        )

        try {
            # PassThru permette di ottenere l'oggetto processo per controllarne l'ExitCode
            $Process = Start-Process -FilePath $EnergiBridgeCmd -ArgumentList $ArgumentList -Wait -NoNewWindow -PassThru -ErrorAction Stop
            
            # --- CONTROLLO INTEGRITA' ---
            if ($Process.ExitCode -ne 0) {
                Write-Host " [CRASH]" -ForegroundColor Red
                Write-Host "     EnergiBridge è uscito con codice errore: $($Process.ExitCode)" -ForegroundColor Red
                # Se fallisce il driver, è inutile continuare il ciclo 10 volte
                Write-Host "     INTERRUZIONE CICLO: Risolvi il problema del driver." -ForegroundColor Magenta
                break 
            }
            elseif (-not (Test-Path $CsvPath)) {
                Write-Host " [MISSING FILE]" -ForegroundColor Red
                Write-Host "     File CSV non creato." -ForegroundColor Gray
            }
            else {
                # Controllo dimensione file > 0
                $size = (Get-Item $CsvPath).Length
                if ($size -gt 100) {
                     Write-Host " [OK] ($size bytes)" -ForegroundColor Green
                } else {
                     Write-Host " [VUOTO]" -ForegroundColor Red
                }
            }

        } catch {
            Write-Host " [ERRORE SYSTEM]" -ForegroundColor Red
            Write-Host "   $_" -ForegroundColor Red
        }
        
        Start-Sleep -Seconds 3
    }
}

Write-Host "--- Fine ---"