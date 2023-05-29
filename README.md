# International Account

MVP of an API to create accounts with balances of different currencies, where the customers will be able to deposit, send funds to other customers and exchange ("swap") their funds between currencies.

## Requirements

- [Java 17 SDK](https://www.oracle.com/ca-en/java/technologies/downloads/)
- [Docker](https://www.docker.com/)
- [Maven](https://maven.apache.org/)

## Running locally

1. Install the libraries: `./mvnw install`
2. Run the unit tests: `./mvnw test`
3. Build a docker image: `docker build -t accounts/international .`
4. Run the docker container: `docker run -p 8080:8080 accounts/international`

### Swagger UI
You can access swagger to check the API Documentation through `http://localhost:8080/swagger-ui/index.html`

## API 
The API allows the basic functionalities of an "Account" system where customers can create accounts, balances for those accounts, send funds between each other and exchange their own funds from one currency to another. It's important to notice that for this MVP I am not using authentication and are not actually persisting the data, I instead use an in-memory service to store the data while the api is running.

Also, for simplicity's sake I am using the customers' emails as an unique property and this `email` field will be used as a key/id for the accounts.

Also to make it simple, I have limited the creation of balances for 3 currencies: USD, CAD and EUR.

Other than the documentation provided by [Swagger](http://localhost:8080/swagger-ui/index.html), I have also provide provide a [postman](https://www.postman.com/) collection containing the endpoints with their payloads: [international_accounts.postman_collection.json](international_accounts.postman_collection.json)

PS: The Swagger api documentation could be a bit better documented but due to time constraints even though not ideal it will hopefully be enough to understand how to use the API.

### Endpoints

- `POST /accounts/create` : Creates an account
- `POST /accounts/create-balance` : Creates a balance for the account specified through the email field
    - PS: when creating a balance, we have defined a field `yearlyInterestRate` where we will be paying out a certain % over the funds the customer has in his balance at the beginning of every month.
- `POST /accounts/deposit` : Deposit funds into a customer's account balance, looking for an account that matches the email on the payload and a balance with a currency provided on the payload
- `POST /accounts/send` : Allows the customers send money to each other with zero fee
- `POST /accounts/swap` : Allows the customers to exchange funds between their own balances. for this service a % fee is charged. For that there is a variable defined on the [application.properties](src/main/resources/application.properties): `service.fee=0.01`
- `GET /accounts/{email}`: given an email, Get the customer's account information
- `GET /accounts/{email}/transactions`: given an email, Get the customer's account transactions

## Jobs: Paying interest to the customers' balances on a monthly basis

the [ScheduleTasks](src/main/java/com/account/springboot/jobs/ScheduledTasks.java) class is responsible to run all jobs on the server and as of now there is only one job.

the `payoutInterestRates` method will run at the beginning of every month and will check whether the account balances were created more than a month ago
    - if so the balances will receive the full interestRate for that month (considering the amount of the balance by the time this job runs), 
    - if the account is not older than 1 month, the customer will receive the interest proportionally (for example, if the balance was open for 15 days, he will get half of the interest).

### What would be the most robust solution

- Code: The current version doesn't care if the user has added a bigger amount on the last day of the month and it would still pay him the interest on top of the current balance. I'd ideally want to pay the customer for the money that was there during the whole month, not only the last day, so it'd be better to keep track of any balance changes to then be able to know how long an amount of money was there for what period.
- Infrastructure: The first step to make this job more secure would be to extract this job from the api and run it on a different service.

## Rate Service - Get Exchange Rates

Also, for simplicity's sake I have create a simple [RatesService](src/main/java/com/account/springboot/services/RatesServiceImpl.java) with hardcoded exchange rates that simulate an api call.

## Database - in-memory service

Once again, for simplicity's sake I have create a simple [InMemoryService](src/main/java/com/account/springboot/services/InMemoryServiceImpl.java) that will store accounts and transactions in memory while the api is running. the 2 main [models](src/main/java/com/account/springboot/models) are the [Account](src/main/java/com/account/springboot/models/Account.java) and [Balance](src/main/java/com/account/springboot/models/Balance.java) models.
