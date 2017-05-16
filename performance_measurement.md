# Vorbereitung
1. MariaDB starten: `local-dev-env-start.sh`
1. Daten importieren: Datensatz von Florian anfragen, mit SQL Tool importieren
1. Backend starten: `SPRING_PROFILES_ACTIVE=somedevprofile ./gradlew bootRun`

# Performancetest
## Anzahl der Queries
1. in application-somedevprofile.properties den key `spring.jpa.show-sql=true` setzen
2. Request ausführen (e.g. `curl http://localhost:8082/posting/3452/`)
3. Logs in Textfile kopieren & mit Edit #select, #join, #as Zählen

## Loadtest
1. vegeta installieren (https://github.com/tsenart/vegeta)
2. Endpunkt mit Vegeta testen
    - Nutzung siehe Dokumentation
    - Beispiel: 
        - Ziele in targets.txt schreiben
        - Ausführen und plotten: `echo "GET http://localhost:8082/posting/3452/" | vegeta attack -duration=10s -rate=50 | vegeta report -reporter=plot > test_posting_3452.html && open test_posting_3452.html`
        - oder: `echo "GET http://localhost:8082/posting/?page=0" | vegeta attack -duration=10s -rate=50 | vegeta report -reporter=plot > test_posting_page_0.html && open test_posting_page_0.html`
