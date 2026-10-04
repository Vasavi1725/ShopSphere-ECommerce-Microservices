# API examples

## Create an order

`POST http://localhost:8080/api/orders`

```json
{
  "customerId": "C1001",
  "productId": "P100",
  "quantity": 2,
  "amount": 199.98
}
```

Expected: HTTP `201 Created`, with a JSON order containing an assigned `id` and `PENDING` status. An `OrderCreatedEvent` is published to Kafka.

## List orders

`GET http://localhost:8080/api/orders`

## Get one order

`GET http://localhost:8080/api/orders/1`

A missing ID returns HTTP `404 Not Found`.

## Validation

`quantity` must be at least 1; customer and product IDs must not be blank; amount must be greater than zero.
