FROM ghcr.io/graalvm/native-image-community:17 AS builder
WORKDIR /build

COPY pom.xml .
COPY .mvn ./.mvn
COPY mvnw .
COPY usuarios ./usuarios
COPY reservas ./reservas
COPY pos ./pos
COPY suscripciones ./suscripciones
COPY inventario ./inventario
COPY accesos ./accesos
COPY seguimiento ./seguimiento
COPY reportes ./reportes
COPY configuracion ./configuracion
COPY notificaciones ./notificaciones

WORKDIR /build
RUN chmod +x mvnw

RUN ../mvnw dependency:go-offline -q -pl usuarios -am
RUN ../mvnw clean native:compile -Pnative -DskipTests -pl usuarios -am

FROM gcr.io/distroless/base-debian12
WORKDIR /app

COPY --from=builder /usr/lib64/libz.so.1 /lib/
COPY --from=builder /build/usuarios/target/ms-usuarios /app/ms-usuarios

EXPOSE 8081
ENTRYPOINT ["/app/ms-usuarios"]
