package integration.test_Gestione_catalogo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import integration.DatabaseSetupForTest;
import sottosistemi.Gestione_Catalogo.service.CatalogoService;
import model.DAO.FilmDAO;
import model.DAO.FilmGenereDAO;
import model.DAO.GenereDAO;
import model.DAO.UtenteDAO;
import model.DAO.PreferenzaDAO;
import model.DAO.VistoDAO;
import model.DAO.InteresseDAO;
import model.DAO.ValutazioneDAO;
import model.DAO.RecensioneDAO;
import model.Entity.FilmBean;
import model.Entity.FilmGenereBean;
import model.Entity.GenereBean;
import model.Entity.UtenteBean;
import model.Entity.PreferenzaBean;
import model.Entity.VistoBean;
import model.Entity.InteresseBean;
import model.Entity.ValutazioneBean;
import model.Entity.RecensioneBean;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.HashMap;

public class CatalogoServiceIntegrationTest {

    private static DataSource testDataSource; 
    private FilmDAO filmDAO;
    private FilmGenereDAO filmGenereDAO;
    private GenereDAO genereDAO;
    private UtenteDAO utenteDAO;
    private PreferenzaDAO preferenzaDAO;

    
    private CatalogoService catalogoService;

    @BeforeAll
    static void beforeAll() {
    	testDataSource = DatabaseSetupForTest.getH2DataSource();
    }

    private void cleanDb() {
        try (final Connection conn = testDataSource.getConnection();
             final Statement stmt = conn.createStatement()) {
            
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            stmt.executeUpdate("TRUNCATE TABLE Valutazione");
            stmt.executeUpdate("TRUNCATE TABLE Report");
            stmt.executeUpdate("TRUNCATE TABLE Recensione");
            stmt.executeUpdate("TRUNCATE TABLE Preferenza");
            stmt.executeUpdate("TRUNCATE TABLE Visto");
            stmt.executeUpdate("TRUNCATE TABLE Interesse");
            stmt.executeUpdate("TRUNCATE TABLE Film_Genere");
            
            stmt.executeUpdate("TRUNCATE TABLE Film");
            stmt.executeUpdate("TRUNCATE TABLE Utente_Registrato");
            stmt.executeUpdate("TRUNCATE TABLE Genere");
            
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setUp() {
        cleanDb();
        
        filmDAO = new FilmDAO(testDataSource);
        filmGenereDAO = new FilmGenereDAO(testDataSource);
        genereDAO = new GenereDAO(testDataSource);
        utenteDAO = new UtenteDAO(testDataSource);
        preferenzaDAO = new PreferenzaDAO(testDataSource);
        
        catalogoService = new CatalogoService(filmDAO, filmGenereDAO, genereDAO);
    }

    @Test
    void testGetFilms_ReturnsAllFilms() {
        final List<FilmBean> films = catalogoService.getFilms();
        assertNotNull(films);
    }

    @Test
    void testAggiungiFilm_ValidData_ShouldSaveToDatabase() {
        final String nome = "Test Film";
        final int anno = 2022;
        final int durata = 120;
        final String generi[] = new String[]{"Azione"};
        genereDAO.save(new GenereBean("Azione")); 
        
        catalogoService.aggiungiFilm(nome, anno, durata, generi, "John Doe", "Attori", null, "Trama");

        final List<FilmBean> allFilms = filmDAO.findAll();
        final boolean found = allFilms.stream().anyMatch(f -> f.getNome().equals(nome));
        assertTrue(found);
    }

    @Test
    void testRicercaFilm_ExistingTitle_ShouldReturnResult() {
        final FilmBean film = new FilmBean();
        film.setNome("Inception");
        film.setAnno(2010);
        filmDAO.save(film); 

        final List<FilmBean> risultati = catalogoService.ricercaFilm("Inception");
        assertFalse(risultati.isEmpty());
        assertEquals("Inception", risultati.get(0).getNome());
    }

    @Test
    void testRemoveFilmByBean_ShouldDeleteFromDB() {
        final String nome = "FilmToRemove";
        genereDAO.save(new GenereBean("Azione"));
        catalogoService.aggiungiFilm(nome, 2022, 120, new String[]{"Azione"}, "Regista", "Attori", null, "Trama");

        final List<FilmBean> all = filmDAO.findAll();
        final FilmBean toRemove = all.stream().filter(f -> nome.equals(f.getNome())).findFirst().orElse(null);
        assertNotNull(toRemove);

        catalogoService.removeFilmByBean(toRemove);
        assertNull(filmDAO.findById(toRemove.getIdFilm()));
    }
    
    @Test
    void testRemoveFilm_ShouldDeleteFromDB() {
        final String nome = "FilmToRemove";
        genereDAO.save(new GenereBean("Azione"));
        catalogoService.aggiungiFilm(nome, 2022, 120, new String[]{"Azione"}, "Regista", "Attori", null, "Trama");

        final List<FilmBean> all = filmDAO.findAll();
        final FilmBean toRemove = all.stream().filter(f -> nome.equals(f.getNome())).findFirst().orElse(null);
        assertNotNull(toRemove);

        catalogoService.removeFilm(toRemove.getIdFilm());
        assertNull(filmDAO.findById(toRemove.getIdFilm()));
    }


    @Test
    void testAddFilm_AddsGeneriAssociations() {
        genereDAO.save(new GenereBean("Azione"));
        genereDAO.save(new GenereBean("Drammatico"));

        catalogoService.addFilm(2020, "Attori", 100, new String[]{"Azione", "Drammatico"}, null, "Film Generi", "Regista", "Trama");

        final FilmBean film = filmDAO.findByName("Film Generi").get(0);
        final List<FilmGenereBean> generiSalvati = filmGenereDAO.findByIdFilm(film.getIdFilm());
        
        assertTrue(generiSalvati.size() >= 2);
        assertTrue(generiSalvati.stream().anyMatch(g -> "Azione".equals(g.getNomeGenere())));
    }

    @Test
    void testModifyFilm_UpdatesGeneri() {
        genereDAO.save(new GenereBean("Azione"));
        genereDAO.save(new GenereBean("Commedia"));

        catalogoService.addFilm(2019, "Attori", 110, new String[]{"Azione"}, null, "Film Modifica", "Regista", "Trama");
        final FilmBean film = filmDAO.findByName("Film Modifica").get(0);

        catalogoService.modifyFilm(film.getIdFilm(), 2019, "Attori", 110, new String[]{"Commedia"}, null, "Film Modifica", "Regista", "Trama");

        final List<FilmGenereBean> generiAggiornati = filmGenereDAO.findByIdFilm(film.getIdFilm());
        assertEquals(1, generiAggiornati.size());
        assertEquals("Commedia", generiAggiornati.get(0).getNomeGenere());
    }
    
    @Test
    void testGetAllGeneri_Service() {
        genereDAO.save(new GenereBean("Genere1"));
        genereDAO.save(new GenereBean("Genere2"));

        List<String> result = catalogoService.getAllGeneri();

        assertNotNull(result);
        assertTrue(result.contains("Genere1"));
        assertTrue(result.contains("Genere2"));
    }

    @Test
    void testFindGeneriByIdFilm() {
        final String nomeGenere = "Fantascienza";
        genereDAO.save(new GenereBean(nomeGenere));
        
        FilmBean film = new FilmBean();
        film.setNome("Star Wars");
        film.setAnno(1977);
        filmDAO.save(film);
        final FilmBean savedFilm = filmDAO.findByName("Star Wars").get(0);
        
        filmGenereDAO.save(new FilmGenereBean(savedFilm.getIdFilm(), nomeGenere));
        
        // FIX: Uso il Service (catalogoService.getGeneri) invece del DAO diretto.
        // Il Service ritorna List<FilmGenereBean>, non List<String>.
        List<FilmGenereBean> generiTrovati = catalogoService.getGeneri(savedFilm.getIdFilm());
        
        assertNotNull(generiTrovati);
        assertFalse(generiTrovati.isEmpty());
        assertTrue(generiTrovati.stream().anyMatch(g -> g.getNomeGenere().equals(nomeGenere)));
    }

    @Test
    void testDoRetrieveConsigliati() {
        UtenteBean user = new UtenteBean();
        user.setEmail("fan@cinema.it");
        user.setUsername("MovieFan");
        user.setPassword("pass");
        utenteDAO.save(user);
        
        genereDAO.save(new GenereBean("Horror"));
        preferenzaDAO.save(new PreferenzaBean("fan@cinema.it", "Horror"));
        
        FilmBean film = new FilmBean(); 
        film.setNome("IT"); 
        film.setValutazione(5); 
        filmDAO.save(film);
        final FilmBean filmHorror = filmDAO.findByName("IT").get(0);
        filmGenereDAO.save(new FilmGenereBean(filmHorror.getIdFilm(), "Horror"));
        
        // FIX: Uso il Service (getFilmCompatibili) invece del DAO diretto.
        List<FilmBean> consigliati = catalogoService.getFilmCompatibili(user);
        
        assertNotNull(consigliati);
        assertTrue(consigliati.stream().anyMatch(f -> f.getIdFilm() == filmHorror.getIdFilm()));
    }

    @Test
    void testGetFilmsByRecensioni_ShouldReturnMap() {
        // Setup Dati
        UtenteBean user = new UtenteBean();
        user.setEmail("map@test.com");
        user.setUsername("MapUser");
        user.setPassword("pass");
        utenteDAO.save(user);

        FilmBean f1 = new FilmBean(); f1.setNome("Film A"); filmDAO.save(f1);
        f1 = filmDAO.findByName("Film A").get(0);
        
        FilmBean f2 = new FilmBean(); f2.setNome("Film B"); filmDAO.save(f2);
        f2 = filmDAO.findByName("Film B").get(0);

        RecensioneBean r1 = new RecensioneBean();
        r1.setEmail("map@test.com"); r1.setIdFilm(f1.getIdFilm()); r1.setContenuto("R1");
        
        RecensioneBean r2 = new RecensioneBean();
        r2.setEmail("map@test.com"); r2.setIdFilm(f2.getIdFilm()); r2.setContenuto("R2");
        
        List<RecensioneBean> listaRecensioni = List.of(r1, r2);

        // Act
        HashMap<Integer, FilmBean> result = catalogoService.getFilms(listaRecensioni);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(f1.getIdFilm()));
        assertTrue(result.containsKey(f2.getIdFilm()));
        assertEquals("Film A", result.get(f1.getIdFilm()).getNome());
    }
    
    @Test
    void testGetFilm_ShouldReturnSingleFilm() {
        FilmBean f = new FilmBean(); 
        f.setNome("SingleFilm"); 
        filmDAO.save(f);
        int id = filmDAO.findByName("SingleFilm").get(0).getIdFilm();

        FilmBean res = catalogoService.getFilm(id);
        assertNotNull(res);
        assertEquals("SingleFilm", res.getNome());
    }
    
    
}