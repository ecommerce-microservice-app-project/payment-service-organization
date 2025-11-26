# Payment Service

Payment Service para sistema de ecommerce. Gestiona los pagos asociados a las órdenes de compra.

## Características

- Spring Boot 2.5.7 con Java 11
- Base de datos: H2 (dev) / MySQL (stage/prod)
- Service Discovery: Eureka Client
- Circuit Breaker: Resilience4j para tolerancia a fallos
- Actuator para health checks
- Integración con Order Service para obtener información de órdenes

## Endpoints

Prefijo: `/payment-service`

### Payment API

```
GET    /api/payments              - Listar todos los pagos
GET    /api/payments/{paymentId}  - Obtener pago por ID
POST   /api/payments              - Crear pago
PUT    /api/payments              - Actualizar pago
DELETE /api/payments/{paymentId}  - Eliminar pago
```

**Ejemplo de payload para crear pago:**

```json
{
  "isPayed": false,
  "paymentStatus": "NOT_STARTED",
  "order": {
    "orderId": 1
  }
}
```

**Estados de pago disponibles:**

- `NOT_STARTED`: Pago no iniciado
- `IN_PROGRESS`: Pago en proceso
- `COMPLETED`: Pago completado
- `FAILED`: Pago fallido
- `CANCELLED`: Pago cancelado

## Testing

### Unit Tests

El servicio incluye pruebas unitarias para validar la lógica de negocio de pagos.

```bash
./mvnw test
```

## Ejecutar

```bash
# Opción 1: Directamente
./mvnw spring-boot:run

# Opción 2: Compilar y ejecutar
./mvnw clean package
java -jar target/payment-service-v0.1.0.jar
```

Service corre en: `http://localhost:8400/payment-service`

## Configuración

### Circuit Breaker (Resilience4j)

El servicio está configurado con circuit breaker para tolerancia a fallos:

- Failure rate threshold: 50%
- Minimum number of calls: 5
- Sliding window size: 10
- Wait duration in open state: 5s
- Sliding window type: COUNT_BASED

### Service Discovery

El servicio se registra automáticamente en Eureka Server con el nombre `PAYMENT-SERVICE`.

### Health Checks

El servicio expone endpoints de health check a través de Spring Boot Actuator:

```
GET /payment-service/actuator/health
```

## Funcionalidades Implementadas

- Gestión completa de pagos (CRUD)
- Estados de pago (NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED, CANCELLED)
- Integración con Order Service para obtener información de órdenes
- Validaciones de campos requeridos
- Manejo de excepciones personalizado
- Circuit breaker para resiliencia
- Integración con Service Discovery (Eureka)

## Comunicación con Otros Servicios

El Payment Service se comunica con otros microservicios a través del API Gateway:

- **Order Service**: Para obtener información completa de la orden asociada al pago

Todas las comunicaciones se realizan a través del API Gateway y el Service Discovery (Eureka).

## Notas Importantes

### Pagos

- Cada pago está asociado a una orden mediante `orderId`
- El campo `isPayed` indica si el pago ha sido completado (boolean)
- El campo `paymentStatus` indica el estado actual del pago (enum)
- Al obtener un pago, el servicio automáticamente obtiene la información completa de la orden desde Order Service

### Flujo de Pago

1. Se crea un pago con estado `NOT_STARTED` cuando se crea una orden
2. El pago puede pasar a `IN_PROGRESS` cuando se inicia el proceso de pago
3. El pago puede completarse (`COMPLETED`) o fallar (`FAILED`)
4. El pago puede cancelarse (`CANCELLED`) en cualquier momento

### Integración con Order Service

El servicio utiliza RestTemplate para obtener información de órdenes desde Order Service. Esto permite que los pagos incluyan información completa de la orden asociada sin necesidad de duplicar datos.
