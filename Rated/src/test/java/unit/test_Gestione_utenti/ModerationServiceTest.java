package unit.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import model.DAO.UtenteDAO;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ModerationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModerationServiceTest {

    private ModerationService moderationService;
    private UtenteDAO mockUtenteDAO;

    @BeforeEach
    void setUp() {
        // Mock del DAO
        mockUtenteDAO = mock(UtenteDAO.class);

        // Inietta il mock nel service tramite il costruttore
        moderationService = new ModerationService(mockUtenteDAO);
    }

    @Test
    void testWarn_UserExists() {
        final String email = "test@example.com";

        // Simula un utente esistente con un avvertimento iniziale
        final UtenteBean user = new UtenteBean();
        user.setEmail(email);
        user.setNWarning(1);

        when(mockUtenteDAO.findByEmail(email)).thenReturn(user);

        // Esegui il metodo
        moderationService.warn(email);

        // Verifica che il numero di avvertimenti sia incrementato
        assertEquals(2, user.getNWarning());

        // Verifica che il metodo update sia stato chiamato
        verify(mockUtenteDAO).update(user);
    }

    @Test
    void testWarn_UserNotFound() {
        final String email = "nonexistent@example.com";

        // Simula che l'utente non esista
        when(mockUtenteDAO.findByEmail(email)).thenReturn(null);

        // Esegui il metodo
        moderationService.warn(email);

        // Verifica che il metodo update non sia stato chiamato
        verify(mockUtenteDAO, never()).update(any());
    }
}