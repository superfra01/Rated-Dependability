# Riepilogo test modificati/aggiunti (cambiamento precedente)

Documento di sintesi dei test **unitari** e **di integrazione** modificati o aggiunti con il cambiamento precedente, con riferimento al Test Plan (`Deliverables/TestPlanDocument_Rated.pdf`).

## Test unitari modificati/aggiunti
### Gestione recensioni
- **RecensioniServiceTest**
  - `testAddValutazione_SwitchToDislike`: cambio valutazione da like a dislike. (OPERAZIONE: AGGIUNTO)
  - `testAddValutazione_RemoveExistingLike`: rimozione della valutazione esistente. (OPERAZIONE: AGGIUNTO)
  - `testAddValutazione_RecensioneMissing`: errore se la recensione non esiste. (OPERAZIONE: AGGIUNTO)
  - `testAddRecensione_DuplicateDoesNotSave`: blocco doppia recensione. (OPERAZIONE: AGGIUNTO)
  - `testDeleteReports`: azzera report e aggiorna la recensione. (OPERAZIONE: AGGIUNTO)
  - `testGetRecensioni`: recupero recensioni per film. (OPERAZIONE: AGGIUNTO)
  - `testGetValutazioni`: recupero valutazioni per film e utente. (OPERAZIONE: AGGIUNTO)

### Gestione catalogo
- **CatalogoServiceTest**
  - `testAddFilm`: salvataggio film con generi associati. (OPERAZIONE: AGGIUNTO)
  - `testModifyFilm_UpdatesGeneri`: aggiornamento film con refresh dei generi. (OPERAZIONE: AGGIUNTO)
  - `testGetGeneri`: recupero generi associati al film. (OPERAZIONE: AGGIUNTO)
  - `testGetAllGeneri`: recupero di tutti i generi. (OPERAZIONE: AGGIUNTO)

### Gestione utenti (profilo)
- **ProfileServiceTest**
  - `testGetPreferenze`: recupero preferenze utente. (OPERAZIONE: AGGIUNTO)
  - `testAddPreferenza`: inserimento singola preferenza. (OPERAZIONE: AGGIUNTO)
  - `testAggiornaPreferenzeUtente_WithGeneri`: aggiornamento preferenze con generi selezionati. (OPERAZIONE: AGGIUNTO)
  - `testAggiornaPreferenzeUtente_NullGeneri`: reset preferenze su input nullo. (OPERAZIONE: AGGIUNTO)
  - `testAggiungiAllaWatchlist`: aggiunta film alla watchlist. (OPERAZIONE: AGGIUNTO)
  - `testIgnoreFilm`: marcatura “non mi interessa”. (OPERAZIONE: AGGIUNTO)
  - `testAggiungiFilmVisto`: aggiunta film alla watched list. (OPERAZIONE: AGGIUNTO)
  - `testRetrieveWatchlist`: recupero lista watchlist. (OPERAZIONE: AGGIUNTO)
  - `testRetrieveWatchedFilms`: recupero lista film visti. (OPERAZIONE: AGGIUNTO)

## Test di integrazione modificati/aggiunti
### Gestione recensioni
- **RecensioniServiceIntegrationTest**
  - `testReportAndDeleteReports`: report recensione e reset dei report. (OPERAZIONE: AGGIUNTO)
  - `testAddValutazione_SwitchToDislike`: cambio valutazione persistito su DB. (OPERAZIONE: AGGIUNTO)

### Gestione catalogo
- **CatalogoServiceIntegrationTest**
  - `testAddFilm_AddsGeneriAssociations`: verifica associazioni film-genere. (OPERAZIONE: AGGIUNTO)
  - `testModifyFilm_UpdatesGeneri`: aggiornamento associazioni genere. (OPERAZIONE: AGGIUNTO)
  - `testGetGeneri_ReturnsAssociations`: lettura generi per film. (OPERAZIONE: AGGIUNTO)
  - `testGetAllGeneri_ReturnsList`: recupero lista generi. (OPERAZIONE: AGGIUNTO)

### Gestione utenti (profilo)
- **ProfileServiceIntegrationTest**
  - `testWatchlistLifecycle`: inserimento/rimozione watchlist con verifica DB. (OPERAZIONE: AGGIUNTO)
  - `testFilmVistoLifecycle`: inserimento/rimozione watched list con verifica DB. (OPERAZIONE: AGGIUNTO)
  - `testAggiornaPreferenzeUtente_Persist`: persistenza preferenze utente. (OPERAZIONE: AGGIUNTO)

### Gestione utenti (moderazione)
- **ModerationServiceIntegrationTest**
  - `testWarn_UserExists`: incremento warning utente esistente. (OPERAZIONE: AGGIUNTO)

## Note
- I test di profilo richiedono DAO mockati e cleanup DB per evitare contaminazioni tra casi di test.
