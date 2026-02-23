FROM tomcat:9.0-jdk17-openjdk

# Rimuove le app di default per pulizia
RUN rm -rf /usr/local/tomcat/webapps/*

# NOTA: Non copiamo il WAR qui con "COPY". 
# Lo inietteremo dinamicamente tramite il docker-compose.

# Espone la porta web e la porta di debug
EXPOSE 8080
EXPOSE 8000

# Avvia Tomcat in modalità debug (così puoi collegare Eclipse)
CMD ["catalina.sh", "jpda", "run"]