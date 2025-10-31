Infra local (docker-compose) - IA AGENT
=======================================

Este stack local utiliza Docker Compose para exponer un clúster de Kafka listo para ser consumido tanto desde otros contenedores como desde tu máquina anfitriona.

## Servicios incluidos

| Servicio            | Imagen                                 | Puerto host |
|---------------------|-----------------------------------------|-------------|
| Zookeeper           | `confluentinc/cp-zookeeper:7.6.1`       | `2181`      |
| Kafka               | `confluentinc/cp-kafka:7.6.1`           | `9092`, `29092` |
| Kafka UI            | `provectuslabs/kafka-ui:latest`         | `8080`      |
| Inicialización de topics | `confluentinc/cp-kafka:7.6.1`           | `-`         |

Los listeners de Kafka están configurados con dos puertos:

- `9092` para conexiones desde tu máquina (`spring.kafka.bootstrap-servers=localhost:9092`).
- `29092` para conexiones internas entre contenedores (`kafka:29092`).

Si necesitas publicar el broker con un hostname distinto a `localhost`, define la variable `KAFKA_ADVERTISED_HOST_NAME` en un archivo `.env` en esta carpeta o en la terminal antes de levantar los servicios.

## Cómo ejecutar

```bash
cd infra
docker compose up -d
```

El comando anterior levantará los contenedores en segundo plano. Puedes validar el estado con `docker compose ps`.

Durante el arranque se crearán (si no existen) los topics `transacciones` y `alertas`, cada uno con una partición y factor de
replicación igual a 1.

Para detener y eliminar los contenedores:

```bash
docker compose down
```

## Verificación rápida

1. Abre [http://localhost:8080](http://localhost:8080) para ingresar a Kafka UI y comprobar que el clúster está disponible.
2. Verifica en Kafka UI o ejecutando `docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --list` que existen los topics `transacciones` y `alertas`.
3. En otra terminal, desde la raíz del proyecto, exporta `KAFKA_BOOTSTRAP_SERVERS=localhost:9092` si quieres sobreescribir la configuración del ingestor.
4. Ejecuta el servicio `ingestion-java` y verifica que puede listar/crear topics sin errores de conexión.

Con este ajuste, los puertos quedan expuestos correctamente tanto para procesos locales como para otros contenedores conectados a la red de Docker.
