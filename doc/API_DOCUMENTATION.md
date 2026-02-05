# Article CRUD API

## Overview

REST API for managing articles with CRUD operations.

## Error Responses

All validation errors return a standardized error response with HTTP 400 Bad Request:

```json
{
  "timestamp": "2026-02-04T17:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Discounts would cause the article price to go below net price, resulting in a loss",
  "path": "/api/articles"
}
```

### Common Error Messages

- **Overlapping Discounts**: `"Multiple discounts have overlapping date ranges. Only one discount can be applicable at a time."`
- **Price Below Net**: `"Discounts would cause the article price to go below net price, resulting in a loss"`

## Endpoints

### Create Article

- **URL**: `POST /api/v1/articles`
- **Body**:

```json
{
  "name": "Laptop",
  "slogan": "Best laptop ever!",
  "netPrice": 500.00,
  "salesPrice": 800.00,
  "vatRatio": 0.19,
  "discounts": []
}
```

- **Response**: `201 Created`

### Get All Articles

- **URL**: `GET /api/v1/articles`
- **Query Parameters** (all optional):
  - `date` (ISO date format): Date for price calculation and discount filtering
  - `withPrices` (boolean, default: false): Return articles with calculated final prices
  - `discountOnly` (boolean, default: false): Filter to only articles with active discounts
- **Response**: `200 OK`

**Basic usage - Get all articles:**

```json
[
  {
    "id": 1,
    "name": "Laptop",
    "slogan": "Best laptop ever!",
    "netPrice": 500.00,
    "salesPrice": 800.00,
    "vatRatio": 0.19,
    "discounts": []
  }
]
```

**With prices - `GET /api/v1/articles?date=2026-02-04&withPrices=true`:**
Returns `ArticleWithPriceDTO` objects with calculated final prices:

```json
[
  {
    "id": 1,
    "name": "Laptop",
    "slogan": "Best laptop ever!",
    "netPrice": 500.00,
    "salesPrice": 800.00,
    "vatRatio": 0.19,
    "finalPrice": 720.00,
    "appliedDiscount": {
      "description": "Winter Sale",
      "discountPercentage": 10,
      "startDate": "2026-02-01",
      "endDate": "2026-02-28"
    },
    "discounts": [...]
  }
]
```

**Discount only - `GET /api/v1/articles?date=2026-02-04&discountOnly=true`:**
Returns only articles that have an active discount on the specified date.

**Combined - `GET /api/v1/articles?date=2026-02-04&withPrices=true&discountOnly=true`:**
Returns articles with prices, filtered to only those with active discounts.

### Get Article by ID

- **URL**: `GET /api/v1/articles/{id}`
- **Response**: `200 OK` or `404 Not Found`

### Update Article

- **URL**: `PUT /api/v1/articles/{id}`
- **Body**: Same as Create
- **Response**: `200 OK` or `404 Not Found`

### Delete Article

- **URL**: `DELETE /api/v1/articles/{id}`
- **Response**: `204 No Content` or `404 Not Found`

### Check Article Exists

- **URL**: `HEAD /api/v1/articles/{id}`
- **Response**: `200 OK` or `404 Not Found`

## Example Usage

### Using curl:

```bash
# Create an article
curl -X POST http://localhost:8080/api/v1/articles \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "slogan": "Best laptop ever!",
    "netPrice": 500.00,
    "salesPrice": 800.00,
    "vatRatio": 0.19
  }'

# Get all articles
curl http://localhost:8080/api/v1/articles

# Get articles with calculated prices for a specific date
curl "http://localhost:8080/api/v1/articles?date=2026-02-04&withPrices=true"

# Get only articles with active discounts on a specific date
curl "http://localhost:8080/api/v1/articles?date=2026-02-04&discountOnly=true"

# Get articles with prices, filtered to only those with discounts
curl "http://localhost:8080/api/v1/articles?date=2026-02-04&withPrices=true&discountOnly=true"

# Get article by ID
curl http://localhost:8080/api/v1/articles/1

# Update an article
curl -X PUT http://localhost:8080/api/v1/articles/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "slogan": "Ultimate gaming experience!",
    "netPrice": 600.00,
    "salesPrice": 1000.00,
    "vatRatio": 0.19
  }'

# Delete an article
curl -X DELETE http://localhost:8080/api/v1/articles/1
```
