# International Account

## Requirements

- Java 17 SDK installed
- Docker
- Maven 3.6

## Running locally

1. `./mvnw install`
2. `docker build -t accounts/international .`
3. `docker run -p 8080:8080 accounts/international`

## Endpoints

### Rate Service - Get Exchange Rates

If you want to return, for example, the CAD to EUR rate you could use something like:
- `http://localhost:8080/api/rates?sourceCurrency=CAD&targetCurrency=EUR`