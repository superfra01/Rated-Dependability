-- Creazione del database e delle tabelle
DROP DATABASE IF EXISTS RatedDB;
CREATE DATABASE RatedDB;
USE RatedDB;

-- 1. Tabella Dizionario dei Generi
CREATE TABLE Genere (
    Nome VARCHAR(50) NOT NULL PRIMARY KEY
);

-- 2. Tabella Utente_Registrato
CREATE TABLE Utente_Registrato (
    email VARCHAR(255) NOT NULL PRIMARY KEY,
    Icona LONGBLOB,
    username VARCHAR(50) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    Tipo_Utente VARCHAR(50),
    N_Warning INT DEFAULT 0,
    Biografia TEXT
);

-- 3. Tabella Film (SENZA colonna Generi)
CREATE TABLE Film (
    ID_Film INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    Locandina LONGBLOB,
    Nome VARCHAR(255) NOT NULL,
    Anno YEAR NOT NULL,
    Durata INT NOT NULL,
    Regista VARCHAR(255),
    Trama VARCHAR(255),
    Valutazione INT DEFAULT 1 CHECK (Valutazione BETWEEN 1 AND 5),
    Attori TEXT
);

-- 4. Tabella Recensione
CREATE TABLE Recensione (
    Titolo VARCHAR(255) NOT NULL,
    Contenuto TEXT,
    Valutazione INT NOT NULL CHECK (Valutazione BETWEEN 1 AND 5),
    N_Like INT DEFAULT 0,
    N_DisLike INT DEFAULT 0,
    N_Reports INT DEFAULT 0,
    email VARCHAR(255) NOT NULL,
    ID_Film INT NOT NULL,
    PRIMARY KEY (email, ID_Film),
    FOREIGN KEY (email) REFERENCES Utente_Registrato(email) ON DELETE CASCADE,
    FOREIGN KEY (ID_Film) REFERENCES Film(ID_Film) ON DELETE CASCADE
);

-- 5. Tabella Valutazione (Like/Dislike alle recensioni)
CREATE TABLE Valutazione (
    Like_Dislike BOOLEAN NOT NULL,
    email VARCHAR(255) NOT NULL,
    email_Recensore VARCHAR(255) NOT NULL,
    ID_Film INT NOT NULL,
    PRIMARY KEY (email, email_Recensore, ID_Film),
    FOREIGN KEY (email) REFERENCES Utente_Registrato(email),
    FOREIGN KEY (email_Recensore, ID_Film) REFERENCES Recensione(email, ID_Film) ON DELETE CASCADE
);

-- 6. Tabella Report
CREATE TABLE Report (
    email VARCHAR(255) NOT NULL,
    email_Recensore VARCHAR(255) NOT NULL,
    ID_Film INT NOT NULL,
    PRIMARY KEY (email, email_Recensore, ID_Film),
    FOREIGN KEY (email) REFERENCES Utente_Registrato(email) ON DELETE CASCADE,
    FOREIGN KEY (email_Recensore, ID_Film) REFERENCES Recensione(email, ID_Film) ON DELETE CASCADE
);

-- 7. Tabella Visto
CREATE TABLE Visto (
    email VARCHAR(255) NOT NULL,
    ID_Film INT NOT NULL,
    PRIMARY KEY (email, ID_Film),
    FOREIGN KEY (email) REFERENCES Utente_Registrato(email) ON DELETE CASCADE,
    FOREIGN KEY (ID_Film) REFERENCES Film(ID_Film) ON DELETE CASCADE
);

-- 8. Tabella Interesse
CREATE TABLE Interesse (
    email VARCHAR(255) NOT NULL,
    ID_Film INT NOT NULL,
    interesse BOOLEAN NOT NULL, 
    PRIMARY KEY (email, ID_Film),
    FOREIGN KEY (email) REFERENCES Utente_Registrato(email) ON DELETE CASCADE,
    FOREIGN KEY (ID_Film) REFERENCES Film(ID_Film) ON DELETE CASCADE
);

-- 9. Tabella Preferenza
CREATE TABLE Preferenza (
    email VARCHAR(255) NOT NULL,
    Nome_Genere VARCHAR(50) NOT NULL,
    PRIMARY KEY (email, Nome_Genere),
    FOREIGN KEY (email) REFERENCES Utente_Registrato(email) ON DELETE CASCADE,
    FOREIGN KEY (Nome_Genere) REFERENCES Genere(Nome) ON DELETE CASCADE
);

-- 10. Tabella Film_Genere
CREATE TABLE Film_Genere (
    ID_Film INT NOT NULL,
    Nome_Genere VARCHAR(50) NOT NULL,
    PRIMARY KEY (ID_Film, Nome_Genere),
    FOREIGN KEY (ID_Film) REFERENCES Film(ID_Film) ON DELETE CASCADE,
    FOREIGN KEY (Nome_Genere) REFERENCES Genere(Nome) ON DELETE CASCADE
);


-- INSERIMENTO DATI

-- 1. Generi
INSERT IGNORE INTO Genere (Nome) VALUES 
('Azione'), ('Avventura'), ('Animazione'), ('Biografico'), ('Commedia'), 
('Crimine'), ('Documentario'), ('Drammatico'), ('Epico'), 
('Famiglia'), ('Fantascienza'), ('Fantasy'), ('Giallo'), ('Guerra'), 
('Horror'), ('Musicale'), ('Mistero'), ('Noir'), ('Poliziesco'), 
('Romantico'), ('Sentimentale'), ('Sportivo'), ('Storico'), ('Thriller'), ('Western');

-- 2. Utenti
INSERT INTO Utente_Registrato (email, Icona, username, Password, Tipo_Utente, N_Warning, Biografia) VALUES
('gestore@catalogo.it', NULL, 'GestoreCatalogo', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'GESTORE', 0, 'Gestore Del Catalogo'),
('moderatore@forum.it', NULL, 'Moderatore', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'MODERATORE', 0, 'Moderatore'),
('alice.rossi@example.com', NULL, 'AliceRossi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Recensore appassionato di libri e film.'),
('marco.bianchi@example.com', NULL, 'MarcoBianchi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Recensore esperto di tecnologia e innovazione.'),
('luca.verdi@example.com', NULL, 'LucaVerdi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 2, 'Critico di prodotti multimediali.'),
('chiara.neri@example.com', NULL, 'ChiaraNeri', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Specializzata in recensioni di narrativa.'),
('giulia.ferri@example.com', NULL, 'GiuliaFerri', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Recensore attivo nel settore della moda e del design.'),
('andrea.fontana@example.com', NULL, 'AndreaFontana', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Appassionato di viaggi e cultura.'),
('elena.marchi@example.com', NULL, 'ElenaMarchi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 3, 'Recensore cucina.'),
('federico.ruggeri@example.com', NULL, 'FedericoRuggeri', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Esperto elettronica.'),
('simona.costa@example.com', NULL, 'SimonaCosta', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Recensore fantasy.'),
('antonio.gallo@example.com', NULL, 'AntonioGallo', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 2, 'Critico cinema d’autore.'),
('sara.moretti@example.com', NULL, 'SaraMoretti', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Appassionata arte.'),
('paolo.esposito@example.com', NULL, 'PaoloEsposito', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Recensore sport.'),
('francesca.barbieri@example.com', NULL, 'FrancescaBarbieri', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Specializzata narrativa storica.'),
('alessio.martini@example.com', NULL, 'AlessioMartini', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Esperto videogiochi.'),
('marta.romani@example.com', NULL, 'MartaRomani', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 2, 'Recensore psicologia.'),
('giovanni.borelli@example.com', NULL, 'GiovanniBorelli', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Critico musica classica.'),
('valentina.grassi@example.com', NULL, 'ValentinaGrassi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Recensore tendenze.'),
('carlo.bassi@example.com', NULL, 'CarloBassi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 3, 'Esperto fumetti.'),
('laura.rizzi@example.com', NULL, 'LauraRizzi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Specializzata beauty.'),
('roberto.mariani@example.com', NULL, 'RobertoMariani', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 2, 'Critico teatrale.'),
('alessandra.milani@example.com', NULL, 'AlessandraMilani', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Recensore libri bambini.'),
('giacomo.giorgi@example.com', NULL, 'GiacomoGiorgi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Appassionato scienza.'),
('livia.trevisan@example.com', NULL, 'LiviaTrevisan', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 3, 'Recensore documentari.'),
('stefano.pini@example.com', NULL, 'StefanoPini', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Esperto fitness.'),
('arianna.betti@example.com', NULL, 'AriannaBetti', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Recensore narrativa rosa.'),
('claudio.vitali@example.com', NULL, 'ClaudioVitali', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 2, 'Recensore fantasy.'),
('irene.marconi@example.com', NULL, 'IreneMarconi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Critico animazione.'),
('lorenzo.gentili@example.com', NULL, 'LorenzoGentili', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Appassionato pop.'),
('cecilia.mazzoni@example.com', NULL, 'CeciliaMazzoni', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Specializzata poesia.'),
('davide.ferri@example.com', NULL, 'DavideFerri', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Recensore sport.'),
('beatrice.carrara@example.com', NULL, 'BeatriceCarrara', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 2, 'Appassionata serie TV.'),
('filippo.rinaldi@example.com', NULL, 'FilippoRinaldi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 3, 'Critico letteratura.'),
('matteo.russo@example.com', NULL, 'MatteoRusso', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Recensore avventure grafiche.'),
('veronica.monti@example.com', NULL, 'VeronicaMonti', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Recensore benessere.'),
('franco.mancini@example.com', NULL, 'FrancoMancini', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 2, 'Critico thriller.'),
('michela.zanetti@example.com', NULL, 'MichelaZanetti', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Recensore narrativa.'),
('fabio.riva@example.com', NULL, 'FabioRiva', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Appassionato cucina.'),
('anna.giacomini@example.com', NULL, 'AnnaGiacomini', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Specializzata saggistica.'),
('margherita.fontana@example.com', NULL, 'MargheritaFontana', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Recensore viaggi.'),
('emanuele.lombardi@example.com', NULL, 'EmanueleLombardi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Critico musica indie.'),
('serena.valli@example.com', NULL, 'SerenaValli', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 2, 'Recensore scienza.'),
('alberto.villa@example.com', NULL, 'AlbertoVilla', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 3, 'Critico cinema.'),
('lucia.cortesi@example.com', NULL, 'LuciaCortesi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Recensore letteratura.'),
('nicola.marchetti@example.com', NULL, 'NicolaMarchetti', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 2, 'Specializzato storici.'),
('gabriele.conti@example.com', NULL, 'GabrieleConti', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Appassionato fumetti.'),
('arianna.lombardo@example.com', NULL, 'AriannaLombardo', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 0, 'Recensore young adult.'),
('carla.tosi@example.com', NULL, 'CarlaTosi', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 1, 'Specializzata teatro.'),
('ludovico.romani@example.com', NULL, 'LudovicoRomani', 'c2FsYXRpbm//2Tx8pzmvrWaDTtUt2RhmJkQQd5KIlPUY7DngZ5c/LQ==', 'RECENSORE', 2, 'Critico fantasy.');

-- 3. Film
INSERT INTO Film (Locandina, Nome, Anno, Durata, Regista, Trama, Valutazione, Attori) VALUES
(NULL, 'Inception', 2010, 148, 'Christopher Nolan', 'Un ladro specializzato nel rubare segreti durante il sonno.', 1, 'Leonardo DiCaprio, Joseph Gordon-Levitt'),
(NULL, 'The Matrix', 1999, 136, 'Lana e Lilly Wachowski', 'Un hacker scopre la vera natura della realtà.', 1, 'Keanu Reeves, Laurence Fishburne'),
(NULL, 'The Godfather', 1972, 175, 'Francis Ford Coppola', 'La storia della famiglia mafiosa Corleone.', 1, 'Marlon Brando, Al Pacino'),
(NULL, 'The Dark Knight', 2008, 152, 'Christopher Nolan', 'Batman combatte contro il Joker per salvare Gotham.', 1, 'Christian Bale, Heath Ledger'),
(NULL, 'Pulp Fiction', 1994, 154, 'Quentin Tarantino', 'Storie intrecciate di crimine a Los Angeles.', 1, 'John Travolta, Uma Thurman'),
(NULL, 'Fight Club', 1999, 139, 'David Fincher', 'Un uomo insoddisfatto forma un club segreto.', 1, 'Brad Pitt, Edward Norton'),
(NULL, 'Forrest Gump', 1994, 142, 'Robert Zemeckis', 'La vita straordinaria di un uomo semplice.', 1, 'Tom Hanks, Robin Wright'),
(NULL, 'Interstellar', 2014, 169, 'Christopher Nolan', 'Un viaggio nello spazio per salvare l’umanità.', 1, 'Matthew McConaughey, Anne Hathaway'),
(NULL, 'The Avengers', 2012, 143, 'Joss Whedon', 'Supereroi si uniscono per salvare la Terra.', 1, 'Robert Downey Jr., Chris Evans'),
(NULL, 'Gladiator', 2000, 155, 'Ridley Scott', 'Un generale romano cerca vendetta.', 1, 'Russell Crowe, Joaquin Phoenix'),
(NULL, 'Avatar', 2009, 162, 'James Cameron', 'Un soldato umano si immerge nel mondo di Pandora.', 1, 'Sam Worthington, Zoe Saldana');

-- 4. Film_Genere (Popolazione OBBLIGATORIA)
INSERT INTO Film_Genere (ID_Film, Nome_Genere) VALUES
(1, 'Azione'), (1, 'Fantascienza'),
(2, 'Azione'), (2, 'Fantascienza'),
(3, 'Drammatico'), (3, 'Crimine'),
(4, 'Azione'), (4, 'Drammatico'),
(5, 'Drammatico'), (5, 'Crimine'),
(6, 'Drammatico'), (6, 'Thriller'),
(7, 'Drammatico'), (7, 'Romantico'),
(8, 'Fantascienza'), (8, 'Drammatico'),
(9, 'Azione'), (9, 'Fantascienza'),
(10, 'Azione'), (10, 'Drammatico'),
(11, 'Fantascienza'), (11, 'Avventura');

-- 5. Preferenze Utenti
INSERT INTO Preferenza (email, Nome_Genere) VALUES
('alice.rossi@example.com', 'Drammatico'),
('alice.rossi@example.com', 'Fantascienza'),
('marco.bianchi@example.com', 'Azione'),
('marco.bianchi@example.com', 'Thriller');

-- 6. Interesse
INSERT INTO Interesse (email, ID_Film, interesse) VALUES
('alice.rossi@example.com', 8, TRUE),
('alice.rossi@example.com', 11, TRUE),
('marco.bianchi@example.com', 10, TRUE),
('marco.bianchi@example.com', 5, FALSE);

-- 7. Recensioni
INSERT INTO Recensione (Titolo, Contenuto, Valutazione, N_Like, N_DisLike, N_Reports, email, ID_Film) VALUES
('Capolavoro assoluto', 'Questo film è stato un’esperienza unica, dalla trama agli effetti speciali.', 5, 120, 3, 2, 'alice.rossi@example.com', 1),
('Intrigante e visionario', 'Un film che ti fa riflettere sul concetto di realtà.', 5, 95, 5, 0, 'marco.bianchi@example.com', 2),
('Un classico intramontabile', 'Una pietra miliare del cinema che tutti dovrebbero vedere.', 5, 200, 2, 0, 'luca.verdi@example.com', 3),
('Oscuro e coinvolgente', 'La performance del Joker è semplicemente perfetta.', 5, 150, 6, 0, 'chiara.neri@example.com', 4),
('Divertente e originale', 'Un mix di storie che si intrecciano in modo geniale.', 5, 180, 4, 0, 'giulia.ferri@example.com', 5),
('Profondo e provocatorio', 'Un film che lascia il segno e invita a riflettere.', 4, 100, 7, 0, 'andrea.fontana@example.com', 6),
('Emozionante', 'Un racconto che tocca il cuore e ispira.', 5, 250, 1, 0, 'elena.marchi@example.com', 7),
('Un viaggio emozionante', 'Fotografia e colonna sonora eccezionali.', 5, 140, 3, 0, 'federico.ruggeri@example.com', 8),
('Azione e adrenalina pura', 'Un cast incredibile per un film indimenticabile.', 4, 110, 8, 0, 'simona.costa@example.com', 9),
('Epico e memorabile', 'Un capolavoro che rimane impresso.', 5, 300, 2, 0, 'antonio.gallo@example.com', 2),
('Visivamente spettacolare', 'Gli effetti speciali sono incredibili, ma la trama è semplice.', 4, 90, 10, 0, 'sara.moretti@example.com', 1),
('Un dramma umano', 'Una storia d’amore che ti commuove profondamente.', 5, 220, 5, 0, 'paolo.esposito@example.com', 2),
('Incredibile', 'Una storia di speranza e redenzione, interpretazioni fantastiche.', 5, 310, 1, 0, 'francesca.barbieri@example.com', 3),
('Teso e affascinante', 'Un thriller che tiene incollati alla sedia.', 5, 180, 3, 0, 'alessio.martini@example.com', 4),
('Una lezione di storia', 'Un film potente e commovente.', 5, 200, 4, 0, 'marta.romani@example.com', 5),
('Un film fantastico', 'Ogni minuto è un capolavoro, colonna sonora mozzafiato.', 5, 280, 2, 0, 'giovanni.borelli@example.com', 6),
('Perfetto', 'Le emozioni sono al centro di tutto, un’esperienza unica.', 5, 230, 1, 0, 'valentina.grassi@example.com', 7),
('Straziante e potente', 'Un capolavoro che racconta l’orrore della guerra.', 5, 220, 0, 0, 'carlo.bassi@example.com', 8),
('Divertente e ironico', 'Il film perfetto per una serata leggera.', 4, 95, 8, 0, 'laura.rizzi@example.com', 9),
('Ispirante e brillante', 'Una rappresentazione incredibile del mondo della musica.', 5, 190, 4, 0, 'roberto.mariani@example.com', 3),
('Un viaggio incredibile', 'La colonna sonora ti trasporta in un altro mondo.', 5, 150, 2, 0, 'alessandra.milani@example.com', 1),
('Rivoluzionario', 'Questo film ha cambiato il modo in cui vedo il cinema.', 5, 170, 1, 0, 'giacomo.giorgi@example.com', 2),
('Un mix perfetto', 'Storia, effetti e recitazione di alto livello.', 5, 140, 3, 0, 'livia.trevisan@example.com', 3),
('Sorprendente', 'Un finale che ti lascia senza parole.', 4, 90, 5, 0, 'stefano.pini@example.com', 4),
('Un capolavoro visivo', 'La regia e la fotografia sono straordinarie.', 5, 130, 2, 0, 'arianna.betti@example.com', 5),
('Un film emozionante', 'Ogni scena è carica di significato.', 5, 160, 1, 0, 'claudio.vitali@example.com', 6),
('Intrigante', 'Un film che ti tiene incollato fino alla fine.', 4, 110, 4, 0, 'irene.marconi@example.com', 7),
('Ben costruito', 'Un intreccio narrativo che funziona alla perfezione.', 5, 200, 3, 0, 'lorenzo.gentili@example.com', 8),
('Semplicemente fantastico', 'Un classico che non delude mai.', 5, 180, 0, 0, 'cecilia.mazzoni@example.com', 9),
('Avvincente', 'Una trama che sorprende ad ogni svolta.', 4, 95, 6, 0, 'davide.ferri@example.com', 4),
('Divertente e commovente', 'Un film che riesce a mescolare bene i toni.', 5, 190, 2, 0, 'beatrice.carrara@example.com', 1),
('Perfetto per le famiglie', 'Una storia che può essere apprezzata da tutti.', 5, 170, 1, 0, 'filippo.rinaldi@example.com', 2),
('Epico', 'Un film che definisce un genere.', 5, 240, 0, 0, 'matteo.russo@example.com', 3),
('Pieno di emozioni', 'Un film che ti resta nel cuore.', 5, 200, 2, 0, 'veronica.monti@example.com', 4),
('Avvincente e spettacolare', 'Gli effetti speciali sono incredibili.', 4, 120, 3, 0, 'franco.mancini@example.com', 5),
('Un film toccante', 'Una storia che ti fa riflettere sulla vita.', 5, 180, 1, 0, 'michela.zanetti@example.com', 6),
('Iconico', 'Un film che ha fatto la storia.', 5, 250, 0, 0, 'fabio.riva@example.com', 7),
('Drammatico', 'La performance degli attori è stata eccezionale.', 5, 210, 3, 0, 'anna.giacomini@example.com', 8),
('Una favola moderna', 'Colori, musica e storia rendono il film unico.', 5, 170, 1, 0, 'margherita.fontana@example.com', 9),
('Tenero e divertente', 'Un film che scalda il cuore.', 5, 220, 1, 0, 'serena.valli@example.com', 1),
('Un’esperienza unica', 'Ogni scena è un’opera d’arte.', 5, 190, 2, 0, 'alberto.villa@example.com', 2),
('Impeccabile', 'La sceneggiatura è scritta magistralmente.', 5, 160, 1, 0, 'lucia.cortesi@example.com', 3),
('Una gioia per gli occhi', 'Una visione spettacolare dall’inizio alla fine.', 5, 230, 0, 0, 'nicola.marchetti@example.com', 4),
('Un film straordinario', 'Mi ha lasciato senza parole.', 5, 250, 1, 0, 'gabriele.conti@example.com', 5),
('Bellissimo', 'Un mix di avventura ed emozione.', 5, 180, 0, 0, 'arianna.lombardo@example.com', 6),
('Un finale perfetto', 'La conclusione è stata fantastica.', 5, 190, 2, 0, 'carla.tosi@example.com', 7),
('Intenso e commovente', 'Un film che tocca il cuore.', 5, 200, 1, 0, 'ludovico.romani@example.com', 8),
('Un esempio di cinema', 'Ogni elemento è al posto giusto.', 5, 230, 0, 0, 'alice.rossi@example.com', 9);

-- 8. Aggiornamento Automatico Valutazione Film
UPDATE Film f
SET Valutazione = (
    SELECT COALESCE(ROUND(AVG(r.Valutazione)), 1)
    FROM Recensione r
    WHERE r.ID_Film = f.ID_Film
);

-- 9. POPOLAZIONE AUTOMATICA TABELLA VISTO
INSERT IGNORE INTO Visto (email, ID_Film)
SELECT email, ID_Film FROM Recensione;

-- 10. Report
INSERT INTO Report (email, email_Recensore, ID_Film) VALUES
('laura.rizzi@example.com', 'alessandra.milani@example.com', 1),
('roberto.mariani@example.com', 'giacomo.giorgi@example.com', 2),
('alessandra.milani@example.com', 'livia.trevisan@example.com', 3),
('giacomo.giorgi@example.com', 'stefano.pini@example.com', 4),
('livia.trevisan@example.com', 'arianna.betti@example.com', 5),
('stefano.pini@example.com', 'claudio.vitali@example.com', 6),
('arianna.betti@example.com', 'irene.marconi@example.com', 7),
('claudio.vitali@example.com', 'lorenzo.gentili@example.com', 8),
('irene.marconi@example.com', 'cecilia.mazzoni@example.com', 9);