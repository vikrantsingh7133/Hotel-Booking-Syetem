# Hotel Booking System

A small Spring Boot backend for booking hotels.

- **Owners** add hotels (properties) with room types and prices.
- **Users** search for hotels, book rooms, pay, and cancel.

Data is kept in an in-memory H2 database, so it is reset every time the app restarts.

---

## 1. How to run

You need **Java 17+**.

**Build a jar, then run it**

```bash
mvn clean install -DskipTests
```

```bash
java -jar target/Hotel-Booking-Syetem-0.0.1-SNAPSHOT.jar
```

**Run the tests**

```bash
mvn clean test
```

- The app starts on `http://localhost:8081`.
- See the database: open `http://localhost:8081/h2-console` → JDBC URL `jdbc:h2:mem:hoteldb`, user `sa`, no password. Desktop MySQL tools (e.g. Sequel Ace) can't open it.

### Try it with Postman

1. Import `postman/Hotel-Booking.postman_collection.json`.
2. Set the `baseUrl` variable if your port is not 8081.
3. Run folders **1 to 7** in order with the Collection Runner.

Ids, dates and test data are filled in automatically. Folder 8 (booking expiry) needs waiting; read its description.

---

## 2. Main flow

```
Owner adds hotel ──► User searches ──► User books ──► User pays ──► Booking CONFIRMED
                                            │               │
                                            │               └─ payment fails ──► FAILED (room freed)
                                            ├─ not paid in 10 min ──► EXPIRED (room freed)
                                            └─ user cancels ──► CANCELLED (room freed, refund)
```

---

## 3. Schema design

### Diagram

[//]: # ()

### Tables

**users**: a guest who books rooms.

| Column | Type | Notes |
|---|---|---|
| id | varchar (UUID) | primary key |
| name | varchar | required |
| email | varchar | required, **unique** |
| phone | varchar | optional |

**owners**: a hotel business. It can own one hotel or a whole chain.

| Column | Type | Notes |
|---|---|---|
| id | varchar (UUID) | primary key |
| name | varchar | required |
| email | varchar | required |

**properties**: a hotel.

| Column | Type | Notes |
|---|---|---|
| id | varchar (UUID) | primary key |
| owner_id | varchar | → owners.id |
| name | varchar | required |
| city | varchar | required |
| locality | varchar | e.g. "Colaba" |
| address | varchar | |
| star_rating | decimal(2,1) | 1.0 to 5.0, e.g. 4.5 |

**property_amenities**: the amenities of a hotel, one row each.

| Column | Type | Notes |
|---|---|---|
| property_id | varchar | → properties.id |
| amenity | varchar | WIFI, POOL, PARKING, AC, GYM, BREAKFAST |

**room_types**: a kind of room in a hotel, e.g. "Deluxe", and how many such rooms exist.

| Column | Type | Notes |
|---|---|---|
| id | varchar (UUID) | primary key |
| property_id | varchar | → properties.id |
| name | varchar | e.g. "Deluxe" |
| max_guests | int | guests per room |
| total_rooms | int | how many rooms of this type |
| base_price | decimal(12,2) | normal price per night |

**room_price_overrides**: a special price for one room type on one date. It can be higher or lower than the base price.

| Column | Type | Notes |
|---|---|---|
| id | varchar (UUID) | primary key |
| room_type_id | varchar | → room_types.id |
| date | date | the night it applies to |
| price | decimal(12,2) | |
| | | **unique (room_type_id, date)** |

**bookings**: one booking of one or more rooms of a single room type.

| Column | Type | Notes |
|---|---|---|
| id | varchar (UUID) | primary key |
| user_id | varchar | → users.id (indexed) |
| property_id | varchar | → properties.id |
| room_type_id | varchar | → room_types.id |
| guests | int | |
| rooms | int | rooms of this type |
| check_in | date | first night |
| check_out | date | leaving day (not a night) |
| amount | decimal(12,2) | total price, fixed when booked |
| refund_amount | decimal(12,2) | set on cancel |
| status | varchar | see "Booking status" below |
| expires_at | timestamp | unpaid booking is dropped after this |
| created_at, updated_at | timestamp | |
| | | index (room_type_id, check_in, check_out) for availability |

**payments**: one try at paying for a booking.

| Column | Type | Notes |
|---|---|---|
| id | varchar (UUID) | primary key |
| booking_id | varchar | → bookings.id |
| method | varchar | CARD, UPI, WALLET |
| amount | decimal(12,2) | |
| status | varchar | SUCCESS, FAILED |
| idempotency_key | varchar | **unique**, stops double charging |
| gateway_ref | varchar | reference from the (mock) gateway |
| created_at | timestamp | |



## 4. Booking status

```
PENDING_PAYMENT ──pay ok──────► CONFIRMED ──cancel──► CANCELLED
PENDING_PAYMENT ──pay fails───► FAILED
PENDING_PAYMENT ──cancel──────► CANCELLED
PENDING_PAYMENT ──not paid────► EXPIRED
```

Any other change, like paying a cancelled booking, is refused with **409**.

---

## 5. API

### Users

| Method | URL | Body / Params |
|---|---|---|
| POST | `/api/users` | `{name, email, phone}` |
| GET | `/api/users/{userId}` | |
| GET | `/api/users/{userId}/booking-history` | `status?`, `page` (default 0), `size` (default 10, max 100) |

### Owners and hotels

| Method | URL | Body / Params |
|---|---|---|
| POST | `/api/owners` | `{name, email}` |
| GET | `/api/owners/{ownerId}` | |
| POST | `/api/owners/{ownerId}/properties` | hotel with room types (see example) |
| GET | `/api/owners/{ownerId}/properties` | |
| GET | `/api/properties/{propertyId}` | |

### Search

| Method | URL | Params |
|---|---|---|
| GET | `/api/properties/search` | `city`, `checkIn`, `checkOut`, `guests`, and optional `rooms`, `locality`, `minPrice`, `maxPrice`, `amenities`, `minRating` |

Only hotels with **free rooms for those dates** are returned. For each room type you get `roomsNeeded`, `avgPricePerNight` (one room) and `totalPrice` (all rooms, all nights).

### Special prices

| Method | URL | Body / Params |
|---|---|---|
| PUT | `/api/room-types/{roomTypeId}/prices` | `{from, to, price}`, both dates included |
| GET | `/api/room-types/{roomTypeId}/prices` | `from`, `to` → price for each date |
| DELETE | `/api/room-types/{roomTypeId}/prices` | `from`, `to` → back to base price |

### Bookings

| Method | URL | Body |
|---|---|---|
| POST | `/api/bookings` | `{userId, propertyId, roomTypeId, guests, rooms, checkIn, checkOut}` (`rooms` defaults to 1) |
| GET | `/api/bookings/{bookingId}` | |
| POST | `/api/bookings/{bookingId}/payments` | `{idempotencyKey, method, details}` |
| POST | `/api/bookings/{bookingId}/cancel` | `{userId}` |

Payment `details` by method: CARD → `cardNumber`, UPI → `vpa`, WALLET → `walletId`.
Payment is mocked. Any value containing `fail` (like `"vpa": "fail@upi"`) is declined.

### Errors

| Code | When |
|---|---|
| 400 | bad input (missing field, bad dates, too many guests for the rooms…) |
| 403 | cancelling someone else's booking |
| 404 | not found |
| 409 | no free rooms, wrong status (e.g. paying twice), duplicate email |

### Example: add a hotel

```json
POST /api/owners/{ownerId}/properties
{
  "name": "Taj Palace",
  "location": { "city": "Mumbai", "locality": "Colaba", "address": "Apollo Bunder" },
  "starRating": 4.5,
  "amenities": ["WIFI", "POOL"],
  "roomTypes": [
    { "name": "Deluxe", "maxGuests": 2, "totalRooms": 10, "basePrice": 2000 },
    { "name": "Family", "maxGuests": 3, "totalRooms": 5,  "basePrice": 3000 }
  ]
}
```

---

## 6. Business rules

| Topic | Rule |
|---|---|
| **Price** | Each night costs the room type's base price, unless that date has a special price. Booking total = sum of nights × rooms. It is fixed at booking time. |
| **Many rooms** | 5 guests in 3-guest rooms need 2 rooms. Rules: `guests ≤ rooms × maxGuests` and `rooms ≤ guests`. |
| **Expiry** | An unpaid booking holds its rooms for **10 minutes** (`booking.hold-minutes`). A job runs every **30 seconds** (`booking.expiry-job-interval-ms`) and marks late ones `EXPIRED`. Paying after the deadline is refused even if the job hasn't run yet. |
| **Payment** | Sending the same `idempotencyKey` again returns the first result and does not charge twice. |
| **Refund** | Cancel 48h+ before check-in (12:00): full refund. 24h+: half. Later: nothing. Unpaid bookings get 0. No cancelling after check-in. |

---

## 7. How double booking is stopped

When two users try to book the last room at the same moment:

1. Each request locks the room type row in the database (`SELECT … FOR UPDATE`).
2. The second request **waits** until the first one finishes.
3. The first saves its booking. The second then sees no free room and gets **409**.

The lock lives in the database, so this works even with many copies of the app running.
Payment and the expiry job lock the **booking** row in the same way, so a booking can't be paid and expired at the same time.

---

## 8. Code layout

```
api/            REST controllers, request/response classes, error handler
model/          entities (User, Owner, Property, RoomType, Booking, Payment…) and their rules
repository/     database access (Spring Data JPA)
service/        the main logic: booking, payment, search, prices, availability
search/filter/  one class per search filter
pricing/        how a night's price is calculated
cancellation/   how much to refund
payment/        one class per payment method (mocked)
scheduler/      the booking expiry job
```


## 9. Assumptions

- No login. The caller passes `userId`.
- A user and an owner are separate. An owner who wants to travel signs up as a user.
- One booking = one room type (any number of rooms of it).
- Money is in INR.
- A failed payment ends the booking; the user books again to retry.
