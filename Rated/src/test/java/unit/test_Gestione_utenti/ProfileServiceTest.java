package unit.test_Gestione_utenti;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.DAO.PreferenzaDAO;
import model.DAO.UtenteDAO;
import model.DAO.InteresseDAO;
import model.DAO.VistoDAO;
import model.Entity.FilmBean;
import model.Entity.InteresseBean;
import model.Entity.PreferenzaBean;
import model.Entity.VistoBean;
import model.Entity.RecensioneBean;
import model.Entity.UtenteBean;
import sottosistemi.Gestione_Utenti.service.ProfileService;

import utilities.PasswordUtility;
import org.mockito.ArgumentCaptor;

class ProfileServiceTest {

    private ProfileService profileService;
    private UtenteDAO mockUtenteDAO;
    private PreferenzaDAO mockPreferenzaDAO;
    private InteresseDAO mockInteresseDAO;
    private VistoDAO mockVistoDAO;
    private DataSource mockDataSource;
    private Connection mockConnection;

    @BeforeEach
    void setUp() throws Exception {
        // Mock del DataSource
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);

        // Configura il mock DataSource per restituire una connessione valida
        when(mockDataSource.getConnection()).thenReturn(mockConnection);

        // Mock UtenteDAO
        mockUtenteDAO = mock(UtenteDAO.class);
        mockPreferenzaDAO = mock(PreferenzaDAO.class);
        mockInteresseDAO = mock(InteresseDAO.class);
        mockVistoDAO = mock(VistoDAO.class);

        // ProfileService utilizza un DAO mockato
        profileService = new ProfileService(mockUtenteDAO, mockPreferenzaDAO, mockInteresseDAO, mockVistoDAO); 
    }

    @Test
    void testProfileUpdate_Success() {
        final String email = "test@example.com";
        final String username = "newUsername";
        final String password = "newPassword"; // Password in chiaro
        final String biografia = "New biography";
        final byte[] icon = new byte[]{1, 2, 3};

        // Simula un utente esistente
        final UtenteBean existingUser = new UtenteBean();
        existingUser.setEmail(email);
        existingUser.setPassword("oldPassword"); 

        when(mockUtenteDAO.findByEmail(email)).thenReturn(existingUser);
        // Importante: simulo che il *nuovo* username non esista già per un altro utente
        when(mockUtenteDAO.findByUsername(username)).thenReturn(null);

        // Esegui il metodo
        final UtenteBean updatedUser = profileService.ProfileUpdate(username, email, password, biografia, icon);

        // Verifica
        assertNotNull(updatedUser);
        assertEquals(username, updatedUser.getUsername());
        
        // Verifica hash password
        final String expectedHash = PasswordUtility.hashPassword(password);
        assertEquals(expectedHash, updatedUser.getPassword()); 
        
        assertEquals(biografia, updatedUser.getBiografia());
        assertArrayEquals(icon, updatedUser.getIcona());

        verify(mockUtenteDAO).update(existingUser);
    }
    
    @Test
    void testProfileUpdate_UsernameAlreadyExists() {
        final String email = "test@example.com";
        final String username = "existingUsername";

        // Simulo che findByUsername trovi un utente diverso da quello che sta facendo l'update
        final UtenteBean anotherUser = new UtenteBean();
        anotherUser.setUsername(username);
        anotherUser.setEmail("another@example.com"); // Email DIVERSA

        when(mockUtenteDAO.findByUsername(username)).thenReturn(anotherUser);

        // Esegui il metodo
        final UtenteBean result = profileService.ProfileUpdate(username, email, "password", "bio", new byte[]{1});

        // Verifica
        assertNull(result);
    }

    @Test
    void testPasswordUpdate_Success() {
        final String email = "test@example.com";
        final String newPassword = "newPassword";

        // Simula un utente esistente
        final UtenteBean existingUser = new UtenteBean();
        existingUser.setEmail(email);
        existingUser.setPassword("oldPassword");

        when(mockUtenteDAO.findByEmail(email)).thenReturn(existingUser);

        // Esegui il metodo
        final UtenteBean updatedUser = profileService.PasswordUpdate(email, newPassword);

        // Verifica
        assertNotNull(updatedUser);
        
        final String expectedHash = PasswordUtility.hashPassword(newPassword);
        assertEquals(expectedHash, updatedUser.getPassword());

        verify(mockUtenteDAO).update(existingUser);
    }

    @Test
    void testPasswordUpdate_UserNotFound() {
        final String email = "nonexistent@example.com";

        // Simula l'assenza dell'utente
        when(mockUtenteDAO.findByEmail(email)).thenReturn(null);

        // Esegui il metodo
        final UtenteBean result = profileService.PasswordUpdate(email, "newPassword");

        // Verifica
        assertNull(result);
    }

    @Test
    void testFindByUsername() {
        final String username = "testUser";

        // Simula un utente esistente
        final UtenteBean user = new UtenteBean();
        user.setUsername(username);

        when(mockUtenteDAO.findByUsername(username)).thenReturn(user);

        // Esegui il metodo
        final UtenteBean result = profileService.findByUsername(username);

        // Verifica
        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void testGetUsers() {
        final List<RecensioneBean> recensioni = new ArrayList<>();

        // Simula una lista di recensioni
        final RecensioneBean recensione1 = new RecensioneBean();
        recensione1.setEmail("email1@example.com");
        final RecensioneBean recensione2 = new RecensioneBean();
        recensione2.setEmail("email2@example.com");
        recensioni.add(recensione1);
        recensioni.add(recensione2);

        // Simula utenti corrispondenti alle email
        final UtenteBean user1 = new UtenteBean();
        user1.setEmail("email1@example.com");
        user1.setUsername("user1");

        final UtenteBean user2 = new UtenteBean();
        user2.setEmail("email2@example.com");
        user2.setUsername("user2");

        when(mockUtenteDAO.findByEmail("email1@example.com")).thenReturn(user1);
        when(mockUtenteDAO.findByEmail("email2@example.com")).thenReturn(user2);

        // Esegui il metodo
        final HashMap<String, String> users = profileService.getUsers(recensioni);

        // Verifica
        assertEquals(2, users.size());
        assertEquals("user1", users.get("email1@example.com"));
        assertEquals("user2", users.get("email2@example.com"));
    }

    @Test
    void testGetPreferenze() {
        final String email = "user@example.com";
        final List<PreferenzaBean> preferenze = new ArrayList<>();
        preferenze.add(new PreferenzaBean(email, "Azione"));
        preferenze.add(new PreferenzaBean(email, "Drammatico"));

        when(mockPreferenzaDAO.findByEmail(email)).thenReturn(preferenze);

        final List<String> result = profileService.getPreferenze(email);

        assertEquals(List.of("Azione", "Drammatico"), result);
    }

    @Test
    void testAddPreferenza() {
        final String email = "user@example.com";
        final String genere = "Commedia";

        profileService.addPreferenza(email, genere);

        final ArgumentCaptor<PreferenzaBean> captor = ArgumentCaptor.forClass(PreferenzaBean.class);
        verify(mockPreferenzaDAO).save(captor.capture());
        assertEquals(email, captor.getValue().getEmail());
        assertEquals(genere, captor.getValue().getNomeGenere());
    }

    @Test
    void testAggiungiAllaWatchlist() {
        profileService.aggiungiAllaWatchlist("user@example.com", 10);

        final ArgumentCaptor<InteresseBean> captor = ArgumentCaptor.forClass(InteresseBean.class);
        verify(mockInteresseDAO).save(captor.capture());
        assertTrue(captor.getValue().isInteresse());
        assertEquals(10, captor.getValue().getIdFilm());
    }

    @Test
    void testIgnoreFilm() {
        profileService.ignoreFilm("user@example.com", 10);

        final ArgumentCaptor<InteresseBean> captor = ArgumentCaptor.forClass(InteresseBean.class);
        verify(mockInteresseDAO).save(captor.capture());
        assertFalse(captor.getValue().isInteresse());
        assertEquals(10, captor.getValue().getIdFilm());
    }

    @Test
    void testAggiungiFilmVisto() {
        profileService.aggiungiFilmVisto("user@example.com", 5);

        final ArgumentCaptor<VistoBean> captor = ArgumentCaptor.forClass(VistoBean.class);
        verify(mockVistoDAO).save(captor.capture());
        assertEquals("user@example.com", captor.getValue().getEmail());
        assertEquals(5, captor.getValue().getIdFilm());
    }

    @Test
    void testAggiornaPreferenzeUtente_WithGeneri() {
        final String email = "user@example.com";
        final String[] generi = new String[]{"Azione", "Giallo"};

        profileService.aggiornaPreferenzeUtente(email, generi);

        verify(mockPreferenzaDAO).deleteByEmail(email);
        verify(mockPreferenzaDAO, times(2)).save(any(PreferenzaBean.class));
    }

    @Test
    void testAggiornaPreferenzeUtente_NullGeneri() {
        final String email = "user@example.com";

        profileService.aggiornaPreferenzeUtente(email, null);

        verify(mockPreferenzaDAO).deleteByEmail(email);
        verify(mockPreferenzaDAO, never()).save(any(PreferenzaBean.class));
    }

    @Test
    void testRetrieveWatchedFilms() {
        final List<FilmBean> films = new ArrayList<>();
        films.add(new FilmBean());
        when(mockVistoDAO.doRetrieveFilmsByUtente("user")).thenReturn(films);

        final List<FilmBean> result = profileService.retrieveWatchedFilms("user");

        assertSame(films, result);
    }

    @Test
    void testRetrieveWatchlist() {
        final List<FilmBean> films = new ArrayList<>();
        films.add(new FilmBean());
        when(mockInteresseDAO.doRetrieveFilmsByUtente("user")).thenReturn(films);

        final List<FilmBean> result = profileService.retrieveWatchlist("user");

        assertSame(films, result);
    }
    
    @Test
    void testIsFilmInWatchlist_True() {
        String email = "test@example.com";
        int filmId = 10;
        
        InteresseBean bean = new InteresseBean();
        bean.setInteresse(true);
        
        when(mockInteresseDAO.findByEmailAndIdFilm(email, filmId)).thenReturn(bean);
        
        boolean result = profileService.isFilmInWatchlist(email, filmId);
        
        assertTrue(result);
        verify(mockInteresseDAO).findByEmailAndIdFilm(email, filmId);
    }

    @Test
    void testRimuoviDallaWatchlist() {
        String email = "test@example.com";
        int filmId = 10;
        
        profileService.rimuoviDallaWatchlist(email, filmId);
        
        verify(mockInteresseDAO).delete(email, filmId);
    }

    @Test
    void testIsFilmVisto_True() {
        String email = "test@example.com";
        int filmId = 5;
        
        VistoBean bean = new VistoBean();
        
        when(mockVistoDAO.findByEmailAndIdFilm(email, filmId)).thenReturn(bean);
        
        boolean result = profileService.isFilmVisto(email, filmId);
        
        assertTrue(result);
        verify(mockVistoDAO).findByEmailAndIdFilm(email, filmId);
    }
    
    @Test
    void testIsFilmVisto_False() {
        String email = "test@example.com";
        int filmId = 5;
        
        when(mockVistoDAO.findByEmailAndIdFilm(email, filmId)).thenReturn(null);
        
        boolean result = profileService.isFilmVisto(email, filmId);
        
        assertFalse(result);
    }

    @Test
    void testRimuoviFilmVisto() {
        String email = "test@example.com";
        int filmId = 5;
        
        profileService.rimuoviFilmVisto(email, filmId);
        
        verify(mockVistoDAO).delete(email, filmId);
    }
}


