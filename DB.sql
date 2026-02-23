-- Creazione del database e delle tabelle
CREATE DATABASE RatedDB;
USE RatedDB;

-- Tabella Utente_Registrato
CREATE TABLE Utente_Registrato (
    email VARCHAR(255) NOT NULL PRIMARY KEY,
    Icona LONGBLOB,
    username VARCHAR(50) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    Tipo_Utente VARCHAR(50),
    N_Warning INT,
    Biografia TEXT
);

-- Tabella Film
CREATE TABLE Film (
    ID_Film INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    Locandina LONGBLOB,
    Nome VARCHAR(255) NOT NULL,
    Anno YEAR NOT NULL,
    Durata INT NOT NULL,
    Generi VARCHAR(255),
    Regista VARCHAR(255),
    Trama VARCHAR(255),
    Valutazione INT DEFAULT 1 CHECK (Valutazione BETWEEN 1 AND 5),
    Attori TEXT
);

-- Tabella Recensione con ON DELETE CASCADE
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

-- Tabella Valutazione (nessuna modifica, ma è commentata come da tuo script originale)
CREATE TABLE Valutazione (
    Like_Dislike BOOLEAN NOT NULL,
    email VARCHAR(255) NOT NULL,
    email_Recensore VARCHAR(255) NOT NULL,
    ID_Film INT NOT NULL,
    PRIMARY KEY (email, email_Recensore, ID_Film),
    FOREIGN KEY (email) REFERENCES Utente_Registrato(email),
    FOREIGN KEY (email_Recensore, ID_Film) REFERENCES Recensione(email, ID_Film) ON DELETE CASCADE
);

-- Tabella Report con ON DELETE CASCADE
CREATE TABLE Report (
    email VARCHAR(255) NOT NULL,
    email_Recensore VARCHAR(255) NOT NULL,
    ID_Film INT NOT NULL,
    PRIMARY KEY (email, email_Recensore, ID_Film),
    FOREIGN KEY (email) REFERENCES Utente_Registrato(email) ON DELETE CASCADE,
    FOREIGN KEY (email_Recensore, ID_Film) REFERENCES Recensione(email, ID_Film) ON DELETE CASCADE
);


-- Inserimento utenti "speciali"
INSERT INTO Utente_Registrato (email, Icona, username, Password, Tipo_Utente, N_Warning, Biografia) VALUES
('gestore@catalogo.it', NULL, 'GestoreCatalogo', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'GESTORE', 0, 'Gestore Del Catalogo'),
('moderatore@forum.it', NULL, 'Moderatore', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'MODERATORE', 0, 'Moderatore');

-- Inserimento dati nella tabella Utente_Registrato (tutte password uniformate)
INSERT INTO Utente_Registrato (email, Icona, username, Password, Tipo_Utente, N_Warning, Biografia) VALUES
('alice.rossi@example.com', NULL, 'AliceRossi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Recensore appassionato di libri e film.'),
('marco.bianchi@example.com', NULL, 'MarcoBianchi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Recensore esperto di tecnologia e innovazione.'),
('luca.verdi@example.com', NULL, 'LucaVerdi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 2, 'Critico di prodotti multimediali con anni di esperienza.'),
('chiara.neri@example.com', NULL, 'ChiaraNeri', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Specializzata in recensioni di narrativa contemporanea.'),
('giulia.ferri@example.com', NULL, 'GiuliaFerri', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Recensore attivo nel settore della moda e del design.'),
('andrea.fontana@example.com', NULL, 'AndreaFontana', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Appassionato di viaggi e cultura, recensisce guide e itinerari.'),
('elena.marchi@example.com', NULL, 'ElenaMarchi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 3, 'Recensore con particolare interesse per la cucina e la gastronomia.'),
('federico.ruggeri@example.com', NULL, 'FedericoRuggeri', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Esperto di recensioni nel settore dell’elettronica di consumo.'),
('simona.costa@example.com', NULL, 'SimonaCosta', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Recensore di letteratura per ragazzi e fantasy.'),
('antonio.gallo@example.com', NULL, 'AntonioGallo', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 2, 'Critico di cinema indipendente e film d’autore.'),
('sara.moretti@example.com', NULL, 'SaraMoretti', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Appassionata di arte e mostre, recensisce eventi culturali.'),
('paolo.esposito@example.com', NULL, 'PaoloEsposito', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Recensore nel settore sportivo e tempo libero.'),
('francesca.barbieri@example.com', NULL, 'FrancescaBarbieri', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Specializzata in recensioni di narrativa storica.'),
('alessio.martini@example.com', NULL, 'AlessioMartini', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Esperto di videogiochi e recensioni nel mondo gaming.'),
('marta.romani@example.com', NULL, 'MartaRomani', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 2, 'Recensore di libri di psicologia e crescita personale.'),
('giovanni.borelli@example.com', NULL, 'GiovanniBorelli', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Critico di musica classica e jazz.'),
('valentina.grassi@example.com', NULL, 'ValentinaGrassi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Recensore appassionata di moda e tendenze.'),
('carlo.bassi@example.com', NULL, 'CarloBassi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 3, 'Recensore esperto di fumetti e graphic novel.'),
('laura.rizzi@example.com', NULL, 'LauraRizzi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Specializzata in recensioni di prodotti di bellezza e skincare.'),
('roberto.mariani@example.com', NULL, 'RobertoMariani', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 2, 'Critico teatrale e cinematografico.'),
('alessandra.milani@example.com', NULL, 'AlessandraMilani', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Recensore di libri per bambini e letteratura educativa.'),
('giacomo.giorgi@example.com', NULL, 'GiacomoGiorgi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Appassionato di recensioni di scienza e tecnologia.'),
('livia.trevisan@example.com', NULL, 'LiviaTrevisan', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 3, 'Recensore di documentari e reportage giornalistici.'),
('stefano.pini@example.com', NULL, 'StefanoPini', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Esperto di recensioni nel mondo del fitness e salute.'),
('arianna.betti@example.com', NULL, 'AriannaBetti', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Recensore di narrativa rosa e commedie romantiche.'),
('claudio.vitali@example.com', NULL, 'ClaudioVitali', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 2, 'Recensore di libri fantasy e di fantascienza.'),
('irene.marconi@example.com', NULL, 'IreneMarconi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Critico esperto di cinema d’animazione.'),
('lorenzo.gentili@example.com', NULL, 'LorenzoGentili', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Appassionato di musica pop e recensioni di concerti.'),
('cecilia.mazzoni@example.com', NULL, 'CeciliaMazzoni', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Specializzata in recensioni di poesia e opere letterarie.'),
('davide.ferri@example.com', NULL, 'DavideFerri', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Recensore di sport e competizioni atletiche.'),
('beatrice.carrara@example.com', NULL, 'BeatriceCarrara', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 2, 'Appassionata di recensioni di serie TV e fiction.'),
('filippo.rinaldi@example.com', NULL, 'FilippoRinaldi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 3, 'Critico di letteratura contemporanea e saggi.'),
('matteo.russo@example.com', NULL, 'MatteoRusso', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Recensore di avventure grafiche e storie interattive.'),
('veronica.monti@example.com', NULL, 'VeronicaMonti', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Recensore nel settore del benessere e del lifestyle.'),
('franco.mancini@example.com', NULL, 'FrancoMancini', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 2, 'Critico di film di genere thriller e poliziesco.'),
('michela.zanetti@example.com', NULL, 'MichelaZanetti', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Recensore di narrativa contemporanea e gialli.'),
('fabio.riva@example.com', NULL, 'FabioRiva', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Appassionato di recensioni di cucina e tradizioni culinarie.'),
('anna.giacomini@example.com', NULL, 'AnnaGiacomini', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Specializzata in recensioni di saggistica e biografie.'),
('margherita.fontana@example.com', NULL, 'MargheritaFontana', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Recensore di viaggi e mete turistiche.'),
('emanuele.lombardi@example.com', NULL, 'EmanueleLombardi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Critico di musica indie e alternative.'),
('serena.valli@example.com', NULL, 'SerenaValli', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 2, 'Recensore di saggi scientifici e accademici.'),
('alberto.villa@example.com', NULL, 'AlbertoVilla', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 3, 'Critico cinematografico specializzato in festival internazionali.'),
('lucia.cortesi@example.com', NULL, 'LuciaCortesi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Recensore di letteratura contemporanea e narrativa breve.'),
('nicola.marchetti@example.com', NULL, 'NicolaMarchetti', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 2, 'Specializzato in recensioni di romanzi storici e saghe.'),
('gabriele.conti@example.com', NULL, 'GabrieleConti', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Appassionato di recensioni di fumetti e manga.'),
('arianna.lombardo@example.com', NULL, 'AriannaLombardo', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 0, 'Recensore esperta di narrativa per giovani adulti.'),
('carla.tosi@example.com', NULL, 'CarlaTosi', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 1, 'Specializzata in recensioni di opere teatrali e performance dal vivo.'),
('ludovico.romani@example.com', NULL, 'LudovicoRomani', 'c2FsYXRpbm/v0M/Qc9CQs6fNwqPF+h3/Aqgz0Y0EFZLsEzZFxIwHpA==', 'RECENSORE', 2, 'Critico letterario appassionato di fantasy e mitologia.');

-- Inserimento dati nella tabella Film
-- Correzione delle Valutazioni in base alla logica "se ci sono recensioni => media arrotondata, altrimenti => 1".
-- Film 1..5 e 7..8 rimangono 5 (coincidono con la media), 
-- Film 6 e 9 passano a 5, 
-- Film 10 e 11 passano a 1 (non hanno recensioni).
INSERT INTO Film (Locandina, Nome, Anno, Durata, Generi, Regista, Trama, Valutazione, Attori) VALUES
(NULL, 'Inception', 2010, 148, 'Azione, Fantascienza', 'Christopher Nolan', 'Un ladro specializzato nel rubare segreti durante il sonno.', 5, 'Leonardo DiCaprio, Joseph Gordon-Levitt'),
(NULL, 'The Matrix', 1999, 136, 'Azione, Fantascienza', 'Lana e Lilly Wachowski', 'Un hacker scopre la vera natura della realtà.', 5, 'Keanu Reeves, Laurence Fishburne'),
(NULL, 'The Godfather', 1972, 175, 'Dramma, Crimine', 'Francis Ford Coppola', 'La storia della famiglia mafiosa Corleone.', 5, 'Marlon Brando, Al Pacino'),
(NULL, 'The Dark Knight', 2008, 152, 'Azione, Dramma', 'Christopher Nolan', 'Batman combatte contro il Joker per salvare Gotham.', 5, 'Christian Bale, Heath Ledger'),
(NULL, 'Pulp Fiction', 1994, 154, 'Dramma, Crimine', 'Quentin Tarantino', 'Storie intrecciate di crimine a Los Angeles.', 5, 'John Travolta, Uma Thurman'),
(NULL, 'Fight Club', 1999, 139, 'Dramma, Thriller', 'David Fincher', 'Un uomo insoddisfatto forma un club segreto.', 5, 'Brad Pitt, Edward Norton'),
(NULL, 'Forrest Gump', 1994, 142, 'Dramma, Romantico', 'Robert Zemeckis', 'La vita straordinaria di un uomo semplice.', 5, 'Tom Hanks, Robin Wright'),
(NULL, 'Interstellar', 2014, 169, 'Fantascienza, Dramma', 'Christopher Nolan', 'Un viaggio nello spazio per salvare l’umanità.', 5, 'Matthew McConaughey, Anne Hathaway'),
(NULL, 'The Avengers', 2012, 143, 'Azione, Fantascienza', 'Joss Whedon', 'Supereroi si uniscono per salvare la Terra.', 5, 'Robert Downey Jr., Chris Evans'),
(NULL, 'Gladiator', 2000, 155, 'Azione, Dramma', 'Ridley Scott', 'Un generale romano cerca vendetta.', 1, 'Russell Crowe, Joaquin Phoenix'),
(NULL, 'Avatar', 2009, 162, 'Fantascienza, Avventura', 'James Cameron', 'Un soldato umano si immerge nel mondo di Pandora.', 1, 'Sam Worthington, Zoe Saldana');

-- Inserimento dati nella tabella Recensione
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

/*
-- Tabella Valutazione (commentata come da tuo esempio)
INSERT INTO Valutazione (Like_Dislike, email, email_Recensore, ID_Film) VALUES
(TRUE, 'alice.rossi@example.com', 'marco.bianchi@example.com', 1),
(FALSE, 'marco.bianchi@example.com', 'alice.rossi@example.com', 2),
(TRUE, 'luca.verdi@example.com', 'giulia.ferri@example.com', 3),
(TRUE, 'chiara.neri@example.com', 'andrea.fontana@example.com', 4),
(FALSE, 'giulia.ferri@example.com', 'elena.marchi@example.com', 5),
(TRUE, 'andrea.fontana@example.com', 'federico.ruggeri@example.com', 6),
(TRUE, 'elena.marchi@example.com', 'simona.costa@example.com', 7),
(FALSE, 'federico.ruggeri@example.com', 'antonio.gallo@example.com', 8),
(TRUE, 'simona.costa@example.com', 'sara.moretti@example.com', 9),
(TRUE, 'alice.rossi@example.com', 'lucia.cortesi@example.com', 2),
(TRUE, 'giovanni.borelli@example.com', 'valentina.grassi@example.com', 7);
*/

-- Inserimento nella tabella Report
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
