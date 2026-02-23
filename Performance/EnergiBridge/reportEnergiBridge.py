import pandas as pd
import os
import glob

# --- CONFIGURAZIONE PERCORSI ---
base_path = r"C:\Users\raofr\Documents\GitHub\Rated-Sustainable\Performance\EnergiBridge"

paths = {
    "Before": os.path.join(base_path, "before", "energy_results_func"),
    "After": os.path.join(base_path, "after", "energy_results_func")
}

def analyze_file(filepath):
    """Calcola le metriche per un singolo file CSV gestendo sia W (Potenza) che J (Energia)."""
    try:
        # Legge il CSV
        df = pd.read_csv(filepath, sep=None, engine='python')
        
        # Normalizza i nomi delle colonne (rimuove spazi e rende maiuscolo per ricerca)
        df.columns = df.columns.str.strip()
        
        # 1. Identifica colonna TEMPO
        # Cerca 'TIME', 'Time', 'TIME (ms)'
        time_col = next((c for c in df.columns if c.lower().startswith('time')), None)
        if not time_col:
            # Fallback se non inizia con time (es. timestamp)
            return None

        # 2. Identifica colonna DATI (Potenza o Energia)
        # Priorità: SYSTEM_POWER (totale) -> PACKAGE_ENERGY (totale cpu) -> CPU_ENERGY (totale cpu) -> CPU_POWER
        col_candidates = [
            'SYSTEM_POWER (W)', 
            'PACKAGE_ENERGY (J)', 
            'CPU_ENERGY (J)', 
            'CPU_POWER (W)'
        ]
        
        target_col = None
        for candidate in col_candidates:
            found = next((c for c in df.columns if candidate in c), None)
            if found:
                target_col = found
                break
        
        # Se ancora nullo, cerca genericamente
        if not target_col:
             target_col = next((c for c in df.columns if 'ENERGY' in c or 'POWER' in c), None)

        if not target_col:
            print(f"  [SKIP] {os.path.basename(filepath)}: Nessuna colonna energia/potenza trovata.")
            return None

        # --- CALCOLI ---
        
        # Durata (s)
        is_ms = 'ms' in time_col.lower() or df[time_col].max() > 10000 # Eeuristica per ms
        t_min = df[time_col].min()
        t_max = df[time_col].max()
        duration_ms = t_max - t_min
        duration_s = duration_ms / 1000.0 if is_ms else duration_ms
        
        if duration_s <= 0:
            return None

        total_energy = 0.0
        avg_power = 0.0
        std_power = 0.0

        # Verifica se la colonna è Energia (J) o Potenza (W)
        is_energy_metric = '(J)' in target_col or 'ENERGY' in target_col.upper()

        if is_energy_metric:
            # --- CASO ENERGIA CUMULATIVA (J) ---
            # Energia = Valore Finale - Valore Iniziale
            total_energy = df[target_col].max() - df[target_col].min()
            
            # Potenza Media = Energia Totale / Tempo Totale
            avg_power = total_energy / duration_s
            
            # Deviazione Standard: Dobbiamo stimare la potenza istantanea (dE/dt)
            df['delta_energy'] = df[target_col].diff()
            df['delta_time'] = df[time_col].diff()
            if is_ms:
                df['delta_time'] = df['delta_time'] / 1000.0
            
            # Filtra divisioni per zero o delta negativi (reset contatori)
            valid_rows = (df['delta_time'] > 0) & (df['delta_energy'] >= 0)
            df_valid = df[valid_rows].copy()
            
            if not df_valid.empty:
                df_valid['inst_power'] = df_valid['delta_energy'] / df_valid['delta_time']
                std_power = df_valid['inst_power'].std()
            
        else:
            # --- CASO POTENZA ISTANTANEA (W) ---
            avg_power = df[target_col].mean()
            std_power = df[target_col].std()
            
            # Energia = Integrale (Sommatoria Potenza * delta_t)
            df['delta_time_s'] = df[time_col].diff().fillna(0)
            if is_ms:
                df['delta_time_s'] /= 1000.0
                
            total_energy = (df[target_col] * df['delta_time_s']).sum()

        return {
            "Durata (s)": duration_s,
            "Energia Totale (J)": total_energy,
            "Potenza Media (W)": avg_power,
            "Deviazione Standard": std_power
        }
    except Exception as e:
        print(f"  [ERR] Errore su {filepath}: {e}")
        return None

# --- ESECUZIONE ---
results = []
print(f"--- Inizio Analisi v3 (Supporto J/W) ---")

for period, folder in paths.items():
    print(f"\nElaborazione cartella: {period}")
    if not os.path.exists(folder):
        print(f"  [!] Cartella non trovata: {folder}")
        continue
        
    files = glob.glob(os.path.join(folder, "*.csv"))
    # Filtra solo i file che iniziano con Funz_
    files = [f for f in files if "Funz_" in os.path.basename(f)]
    
    print(f"  File trovati: {len(files)}")

    # Raggruppa per funzione
    functions_map = {}
    for f in files:
        fname = os.path.basename(f)
        # Logica: Funz_Nome_run_X -> Funz_Nome
        if "_run_" in fname:
            func_key = fname.split("_run_")[0]
        else:
            func_key = fname.replace(".csv", "")
            
        if func_key not in functions_map:
            functions_map[func_key] = []
        functions_map[func_key].append(f)

    # Analizza ogni gruppo
    for func_name, file_list in functions_map.items():
        metrics_accum = []
        for fpath in file_list:
            m = analyze_file(fpath)
            if m:
                metrics_accum.append(m)
        
        if metrics_accum:
            df_m = pd.DataFrame(metrics_accum)
            means = df_m.mean()
            
            results.append({
                "Funzione": func_name,
                "Periodo": period,
                "Durata (s)": means["Durata (s)"],
                "Energia Totale (J)": means["Energia Totale (J)"],
                "Potenza Media (W)": means["Potenza Media (W)"],
                "Deviazione Standard": means["Deviazione Standard"],
                "Run Validate": len(metrics_accum)
            })

if results:
    final_df = pd.DataFrame(results)
    # Ordina: Prima per Funzione, poi After/Before
    final_df = final_df.sort_values(by=["Funzione", "Periodo"], ascending=[True, True])
    
    print("\n\n--- REPORT GENERATO ---")
    pd.set_option('display.max_columns', None)
    pd.set_option('display.width', 1000)
    pd.set_option('display.float_format', '{:.4f}'.format)
    print(final_df.to_string(index=False))
    
    final_df.to_csv("Report_EnergyBridge_v3.csv", index=False)
    print(f"\nSalvataggio completato: Report_EnergyBridge_v3.csv")
else:
    print("\n[ERRORE] Nessun risultato generato. Verifica i log sopra.")