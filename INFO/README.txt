1)Per fare il build del jar (per far partire la topologia in un cluster):
    fare il 'package' con Maven
    il jar è in target ==>> CINI_SmartLightingSystem-1.0.jar

2) per passare in modalità Locale, modificare il pom.xml sotto la dipendenza di storm-core con
        <scope>compile</scope> e poi maven package

3) per far partire una demo di storm su docker, da terminale in src/Scripts far partire
        ./start_storm_on_docker_demo
        e fermalo con ./stop_storm_on_docker_demo
