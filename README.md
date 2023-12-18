# kstreams-rocksdb-tester

This application is a Kafka Streams RocksDB test tool.
It is under construction :)

This application must currently be executed using these JVM flags (Uuuggly reflection usage :p) :

```shell
export JAVA_HOME=$(/usr/libexec/java_home -v 17.0)
java --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -jar target/kstreams-rocksdb-tester-1.0-SNAPSHOT-jar-with-dependencies.jar
```

# TODO: 
- Add description.
- Don't use reflection anymore and duplicate TestDriver code instead.
- Add a Docker build.
- Add a Docker Compose with monitoring (prometheus/grafana for instance).
