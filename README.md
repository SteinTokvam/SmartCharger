# SmartCharger
![example workflow](https://github.com/steintokvam/smartcharger/actions/workflows/main.yml/badge.svg)

Program for å koble seg på Easee sin veggboks for å lade den på billigst mulig tidspunkt sett at tilkoblet bil lader med tilstrekklig hastighet. Det bil si at den lader smart dersom man kan lade raskt, mens om det blir koblet på en bil som lader tregt som for eksempel en plug in hybrid som ofte har små batterier så skal den gi strøm med en gang så det lille batteriet alltid blir ladet.

---

### Hvordan funker det?

Strømpriser blir hentet ut slik at man har dagens priser, og når de kommer morgendagens priser. Når en bil kobles på og det er en ren elbil så vil programmet oppdage dette og vil enten finne de X antall billigste timene og lade i disse timene, eller så kan man si til programmet etter at bilen er plugget i hvor mye strøm i prosent bilen har igjen og programmet vil estimere antall timer den trenger for å lade bilen basert på ladehastighet og batteristørrelse. Programmet trenger derfor også å vite hvor stort batteri bilen har.

#### Hvordan bygge

Legg først dockerfile og .jar fil på serveren og kjør fra den mappen

```
docker build -t steintokvam/smartcharger .
```
så kan man starte opp smartcharger i unraid sin docker tab. 

Pr nå så legger jeg ikke inn noen port, så når jeg har støtte for apple 
shortcuts så må det legges inn en port som mappes til den jeg bruker i appen.
