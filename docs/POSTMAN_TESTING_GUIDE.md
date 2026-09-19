# StreamSwitch API — Postman Testing Guide

A complete, step-by-step guideline for testing **every endpoint** of the StreamSwitch API with Postman, including **validation cases** and **error cases**.

---

## Table of Contents

1. [Before You Start](#1-before-you-start)
2. [Postman Workspace Setup](#2-postman-workspace-setup)
3. [API Overview — All 19 Endpoints](#3-api-overview--all-19-endpoints)
4. [Seed Data Reference (Known IDs)](#4-seed-data-reference-known-ids)
5. [Global Error Response Formats](#5-global-error-response-formats)
6. [Phase 1 — TV Provider Endpoints (`/api/providers`)](#6-phase-1--tv-provider-endpoints)
7. [Phase 2 — Channel Endpoints (`/api/channels`)](#7-phase-2--channel-endpoints)
8. [Phase 3 — Subscription Package Endpoints (`/api/packages`)](#8-phase-3--subscription-package-endpoints)
9. [Phase 4 — Cross-Resource & Business Rule Tests](#9-phase-4--cross-resource--business-rule-tests)
10. [Phase 5 — HTTP-Level Error Tests](#10-phase-5--http-level-error-tests)
11. [Phase 6 — Run Everything with Collection Runner](#11-phase-6--run-everything-with-collection-runner)
12. [Bug Log Template](#12-bug-log-template)
13. [Appendix A — Validation Constraint Cheat Sheet](#appendix-a--validation-constraint-cheat-sheet)
14. [Appendix B — Known Gaps Found While Reviewing the Code](#appendix-b--known-gaps-found-while-reviewing-the-code)

---

## 1. Before You Start

### 1.1 Start the application

1. Make sure **PostgreSQL** is running on `localhost:5432` with database `streamswitch2_db`.
2. Start the app (port **8080**, from `application.properties`):

   ```bash
   mvn spring-boot:run
   ```

   (or run `StreamSwitchApplication` from your IDE)

3. Verify it is up:

   ```bash
   curl http://localhost:8080/api/providers
   ```

   If you get a JSON array back, you are good to go.

### 1.2 Test strategy (read this first)

| Rule | Why |
|---|---|
| **Never mutate seed data.** Create your own entities (names prefixed `QA-`) for PUT/DELETE tests. | Seed data is re-inserted on every startup; deleting it makes later runs confusing. |
| **Test in phases** (Providers → Channels → Packages → Cross-resource → HTTP errors). | Packages depend on Providers and Channels existing. |
| **Capture IDs with Postman test scripts** into collection variables. | So requests reference each other automatically. |
| **Restart the app before a full regression run** for a clean state. | `data.sql` reseeds providers & channels (idempotently) on startup. |

>  **Data warning:** `data.sql` re-inserts the 6 subscription packages on **every** startup (they have no unique constraint and no `ON CONFLICT` clause). After several restarts you may see **duplicate packages with increasing IDs**. Always fetch real IDs with a GET before assuming IDs — don't trust the table in section 4 blindly.

---

## 2. Postman Workspace Setup

### 2.1 Create environment

Create an environment called **`StreamSwitch Local`** with:

| Variable | Type | Initial value |
|---|---|---|
| `baseUrl` | default | `http://localhost:8080` |

### 2.2 Create collection structure

Create a collection called **`StreamSwitch API`** with these folders:

```
StreamSwitch API/
├── 00 Setup & Discovery
├── 01 Providers
├── 02 Channels
├── 03 Packages
├── 04 Cross-Resource Rules
└── 05 HTTP-Level Errors
```

### 2.3 Collection variables (created automatically by test scripts)

These will be set by the scripts in this guide — you don't create them by hand:

| Variable | Meaning |
|---|---|
| `qaProviderId` | ID of the QA provider created by tests |
| `qaChannelId` | ID of the QA channel created by tests |
| `qaPackageId` | ID of the QA package created by tests |
| `seedProviderId` | ID of an existing seed provider (e.g. DStv) |
| `seedChannelId` | ID of an existing seed channel |
| `seedPackageId` | ID of an existing seed package |

### 2.4 Standard request settings (use for every request)

- **Headers:** `Content-Type: application/json` (for every request with a body)
- **URL prefix:** always `{{baseUrl}}`
- **Auth:** none (the API has no security layer)

---

## 3. API Overview — All 19 Endpoints

| # | Method | Endpoint | Success | Purpose |
|---|---|---|---|---|
| 1 | POST | `/api/providers` | **201** | Create a TV provider |
| 2 | GET | `/api/providers` | **200** | List all providers |
| 3 | GET | `/api/providers/{id}` | **200** | Get one provider |
| 4 | PUT | `/api/providers/{id}` | **200** | Update a provider |
| 5 | DELETE | `/api/providers/{id}` | **204** | Delete a provider (cascades to its packages!) |
| 6 | POST | `/api/channels` | **201** | Create a channel |
| 7 | GET | `/api/channels` | **200** | List all channels |
| 8 | GET | `/api/channels/{id}` | **200** | Get one channel |
| 9 | GET | `/api/channels/genre/{genre}` | **200** | Filter channels by genre (case-insensitive) |
| 10 | PUT | `/api/channels/{id}` | **200** | Update a channel |
| 11 | DELETE | `/api/channels/{id}` | **204** | Delete a channel |
| 12 | POST | `/api/packages/provider/{providerId}` | **201** | Create a package under a provider |
| 13 | GET | `/api/packages` | **200** | List all packages |
| 14 | GET | `/api/packages/{id}` | **200** | Get one package |
| 15 | GET | `/api/packages/provider/{providerId}` | **200** | List packages of one provider |
| 16 | PUT | `/api/packages/{id}` | **200** | Update a package |
| 17 | DELETE | `/api/packages/{id}` | **204** | Delete a package |
| 18 | POST | `/api/packages/{packageId}/channels/{channelId}` | **200** | Add a channel to a package |
| 19 | DELETE | `/api/packages/{packageId}/channels/{channelId}` | **200** | Remove a channel from a package |

**Request naming convention used in this guide** (prefix + number, e.g. `P01`, `C07`, `K12`, `X01`, `E01`):

- `P` = Provider, `C` = Channel, `K` = pac**K**age, `X` = cross-resource/business rule, `E` = HTTP-level error

---

## 4. Seed Data Reference (Known IDs)

On a **fresh database**, `data.sql` produces:

**Providers** (IDs are stable across restarts — unique name + `ON CONFLICT DO NOTHING`):

| id | name | country |
|---|---|---|
| 1 | DStv | Nigeria |
| 2 | Canal+ | France |
| 3 | StarTimes | China |

**Channels** (IDs stable, in insert order):

| id | name | genre |
|---|---|---|
| 1 | SuperSport | Sports |
| 2 | Canal+ Sport | Sports |
| 3 | CNN | News |
| 4 | BBC News | News |
| 5 | Al Jazeera | News |
| 6 | Cartoon Network | Kids |
| 7 | Nickelodeon | Kids |
| 8 | Disney Channel | Kids |
| 9 | M-Net | Entertainment |
| 10 | Showmax | Entertainment |
| 11 | Canal+ Cinema | Entertainment |
| 12 | StarTimes Novela | Entertainment |
| 13 | MTV | Music |
| 14 | Channel O | Music |

**Packages** (IDs are **NOT stable** — re-inserted on every restart, see warning in §1.2):

| id (fresh DB) | name | provider | price |
|---|---|---|---|
| 1 | DStv Isange | DStv | 1500.00 |
| 2 | DStv Access | DStv | 4500.00 |
| 3 | Canal+ Access | Canal+ | 3000.00 |
| 4 | Canal+ Family | Canal+ | 6500.00 |
| 5 | StarTimes Basic | StarTimes | 900.00 |
| 6 | StarTimes Classic | StarTimes | 2200.00 |

**Request `00-S1 Discover IDs`** — run these 3 GETs at the start of every session and write down the real IDs:

- `GET {{baseUrl}}/api/providers`
- `GET {{baseUrl}}/api/channels`
- `GET {{baseUrl}}/api/packages`

Capture script for the provider list (Tests tab):

```js
pm.test("200 OK", () => pm.response.to.have.status(200));
const providers = pm.response.json();
pm.test("At least 1 provider seeded", () => pm.expect(providers.length).to.be.above(0));
const dstv = providers.find(p => p.name === "DStv");
pm.collectionVariables.set("seedProviderId", dstv.id);
```

Analogous scripts capture `seedChannelId` (find `"SuperSport"`) and `seedPackageId` (find `"DStv Access"`, or just take `packages[0]` if names are duplicated).

---

## 5. Global Error Response Formats

Every error returns this envelope (from `GlobalExceptionHandler`). Learn these shapes — many tests below assert on them.

### 5.1 Validation error — **400 Bad Request**

```json
{
  "timestamp": "2026-09-18T10:15:30.123",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "fieldErrors": {
    "name": "Channel name is required",
    "description": "Description must not exceed 500 characters"
  }
}
```

### 5.2 Resource not found — **404 Not Found**

```json
{
  "timestamp": "2026-09-18T10:15:30.123",
  "status": 404,
  "error": "Not Found",
  "message": "Channel not found with id: 999"
}
```

### 5.3 Unknown URL — **404 Not Found** (generic message)

```json
{
  "timestamp": "2026-09-18T10:15:30.123",
  "status": 404,
  "error": "Not Found",
  "message": "The requested URL was not found on this server. Check the URL and try again."
}
```

### 5.4 Wrong HTTP method — **405 Method Not Allowed**

```json
{
  "timestamp": "2026-09-18T10:15:30.123",
  "status": 405,
  "error": "Method Not Allowed",
  "message": "HTTP method 'PATCH' is not supported for this endpoint."
}
```

### 5.5 Unexpected error — **500 Internal Server Error**

```json
{
  "timestamp": "2026-09-18T10:15:30.123",
  "status": 500,
  "error": "Internal Server Error",
  "message": "<exception message>"
}
```

>  Several *client mistakes* currently fall into this 500 bucket (duplicate names, malformed JSON, non-numeric IDs). They are intentional test cases below, and are listed as improvement candidates in **Appendix B**.

---

## 6. Phase 1 — TV Provider Endpoints

### `P01 — Create provider (happy path)`  expect 201

- **POST** `{{baseUrl}}/api/providers`
- **Body:**

```json
{
  "name": "QA Test Provider",
  "description": "Provider created by automated Postman tests",
  "websiteUrl": "https://qa.example.com",
  "country": "Kenya"
}
```

- **Tests tab (paste this):**

```js
pm.test("Status is 201 Created", () => pm.response.to.have.status(201));
const provider = pm.response.json();
pm.test("Response has generated id", () => pm.expect(provider.id).to.be.a("number"));
pm.test("Name echoed correctly", () => pm.expect(provider.name).to.eql("QA Test Provider"));
pm.collectionVariables.set("qaProviderId", provider.id);
```

### `P02 — Create provider: validation cases`  expect 400 with `fieldErrors`

Send each body to **POST** `/api/providers`. Check status **and** the exact `fieldErrors` entry.

| # | Body | Expect status | Expect `fieldErrors` |
|---|---|---|---|
| P02a | `{}` | 400 | `"name": "Provider name is required"` |
| P02b | `{"name": ""}` | 400 | `"name": "Provider name is required"` |
| P02c | `{"name": "   "}` (spaces only) | 400 | `"name": "Provider name is required"` (@NotBlank rejects whitespace) |
| P02d | `{"name": "<101 chars>"}` | 400 | `"name": "Provider name must not exceed 100 characters"` |
| P02e | `{"name": "OK", "description": "<501 chars>"}` | 400 | `"description": "Description must not exceed 500 characters"` |
| P02f | `{"name": "OK", "websiteUrl": "<256 chars>"}` | 400 | `"websiteUrl": "Website URL must not exceed 255 characters"` |
| P02g | `{"name": "OK", "country": "<101 chars>"}` | 400 | `"country": "Country must not exceed 100 characters"` |
| P02h | Name 101 chars **and** description 501 chars together | 400 | **both** entries in `fieldErrors` |

>  Tip: in Postman you can generate a long string inline, e.g. `"a".repeat(101)` is not valid JSON — instead paste a pre-made 101-character string, or use a Pre-request Script variable:
> ```js
> pm.collectionVariables.set("long101", "x".repeat(101));
> ```
> then use `"{{long101}}"` in the body.

### `P03 — Create provider: duplicate name (case-insensitive)`  expect 500 (known gap)

- **POST** `/api/providers` with body `{"name": "dstv"}` (lowercase version of the seed provider **DStv**).
- **Expected today:** `500` with `"message": "A TV provider with the name 'dstv' already exists"` — because `IllegalArgumentException` is not specifically mapped (see Appendix B). The **business rule works**, but the status code is wrong (should be 400/409).
- **What to record:** rule enforced?  / status code sensible? 

### `P04 — List all providers`  expect 200

- **GET** `/api/providers`
- Expect 200, JSON array, each item has `id, name, description, websiteUrl, country, packages`.
- Confirm the provider you created in P01 appears.

### `P05 — Get provider by ID`  expect 200 /  expect 404

| # | Request | Expect |
|---|---|---|
| P05a | **GET** `/api/providers/{{qaProviderId}}` | 200, correct name |
| P05b | **GET** `/api/providers/99999` | 404, `"message": "TV Provider not found with id: 99999"` |

### `P06 — Update provider`  200 /  400 /  404 /  500

| # | Request | Expect |
|---|---|---|
| P06a | **PUT** `/api/providers/{{qaProviderId}}` with full valid body (change `country` to `"Uganda"`) | 200, updated values echoed |
| P06b | **PUT** `/api/providers/{{qaProviderId}}` with `{"name": ""}` | 400, `"name": "Provider name is required"` |
| P06c | **PUT** `/api/providers/99999` with valid body | 404 |
| P06d | **PUT** `/api/providers/{{qaProviderId}}` with `{"name": "DStv"}` (another provider's name) | **500** — duplicate-name rule (should be 409, Appendix B) |
| P06e | **PUT** `{{qaProviderId}}` with `{"name": "QA Test Provider"}` (its *own* name, same case or different case e.g. `qa test provider`) | 200 — updating with your own name is allowed |

### `P07 — Delete provider`  204 /  404

| # | Request | Expect |
|---|---|---|
| P07a | **DELETE** `/api/providers/99999` | 404 |
| P07b | **DELETE** `/api/providers/{{qaProviderId}}` *(do this at the END of provider testing, see X01)* | 204, empty body |
| P07c | **GET** `/api/providers/{{qaProviderId}}` afterwards | 404 (really gone) |

---

## 7. Phase 2 — Channel Endpoints

### `C01 — Create channel (happy path)`  expect 201

- **POST** `{{baseUrl}}/api/channels`
- **Body:**

```json
{
  "name": "QA Test Channel",
  "channelNumber": "999",
  "genre": "Testing",
  "description": "Channel created by automated Postman tests",
  "logoUrl": "https://example.com/qa.png"
}
```

- **Tests tab:**

```js
pm.test("Status is 201 Created", () => pm.response.to.have.status(201));
const channel = pm.response.json();
pm.test("Has id", () => pm.expect(channel.id).to.be.a("number"));
pm.collectionVariables.set("qaChannelId", channel.id);
```

### `C02 — Create channel: validation cases`  expect 400

| # | Body | Expect `fieldErrors` |
|---|---|---|
| C02a | `{}` | `"name": "Channel name is required"` |
| C02b | `{"name": ""}` / `{"name": "  "}` | `"name": "Channel name is required"` |
| C02c | `{"name": "<101 chars>"}` | `"name": "Channel name must not exceed 100 characters"` |
| C02d | `{"name": "OK", "channelNumber": "<21 chars>"}` | `"channelNumber": "Channel number must not exceed 20 characters"` |
| C02e | `{"name": "OK", "genre": "<51 chars>"}` | `"genre": "Genre must not exceed 50 characters"` |
| C02f | `{"name": "OK", "description": "<501 chars>"}` | `"description": "Description must not exceed 500 characters"` |
| C02g | `{"name": "OK", "logoUrl": "<256 chars>"}` | `"logoUrl": "Logo URL must not exceed 255 characters"` |
| C02h | Several violations at once | **all** matching entries in `fieldErrors` |

Optional fields (`channelNumber`, `genre`, `description`, `logoUrl`) may be **omitted entirely** → should still be 201. Add one happy-path variant:

- `C02i` — body `{"name": "QA Minimal Channel"}` → **201**, other fields `null`.

### `C03 — Create channel: duplicate name`  expect 500 (known gap)

- **POST** `/api/channels` with `{"name": "cnn"}` → **500**, message `"A channel with the name 'cnn' already exists"` (case-insensitive rule works; code is wrong — Appendix B).

### `C04 — List & filter channels`  200

| # | Request | Expect |
|---|---|---|
| C04a | **GET** `/api/channels` | 200, array contains seed channels + `QA Test Channel` |
| C04b | **GET** `/api/channels/genre/Sports` | 200, only `SuperSport` and `Canal+ Sport` |
| C04c | **GET** `/api/channels/genre/sports` (lowercase) | 200, **same result** (case-insensitive filter) |
| C04d | **GET** `/api/channels/genre/DoesNotExist` | 200 with **empty array `[]`** (not 404!) |
| C04e | **GET** `/api/channels/99999` | 404, `"Channel not found with id: 99999"` |

### `C05 — Update channel`  200 /  400 /  404 /  500

| # | Request | Expect |
|---|---|---|
| C05a | **PUT** `/api/channels/{{qaChannelId}}` — valid body, change `genre` to `"QA Updated"` | 200, updated |
| C05b | **PUT** `{{qaChannelId}}` — `{"name": ""}` | 400 |
| C05c | **PUT** `/api/channels/99999` — valid body | 404 |
| C05d | **PUT** `{{qaChannelId}}` — `{"name": "CNN"}` (existing name) | **500** duplicate rule |
| C05e | **PUT** `{{qaChannelId}}` — its own name in different case (`qa test channel`) | 200 |

### `C06 — Delete channel`  204 /  404 /  500 if in use

| # | Request | Expect |
|---|---|---|
| C06a | **DELETE** `/api/channels/99999` | 404 |
| C06b | **DELETE** `/api/channels/{{qaChannelId}}` (not linked to any package) | 204, then C06c → 404 |
| C06d | **DELETE** a channel that belongs to a package (see X03 — run it there) |  likely **500** FK-constraint violation (Appendix B) |

---

## 8. Phase 3 — Subscription Package Endpoints

> A package **must** belong to a provider (passed in the URL, not the body).

### `K01 — Create package (happy path)`  expect 201

- **POST** `{{baseUrl}}/api/packages/provider/{{seedProviderId}}`
- **Body:**

```json
{
  "name": "QA Test Package",
  "description": "Package created by automated Postman tests",
  "monthlyPrice": 1999.99,
  "billingCycle": "monthly",
  "active": true
}
```

- **Tests tab:**

```js
pm.test("Status is 201 Created", () => pm.response.to.have.status(201));
const pkg = pm.response.json();
pm.test("Has id", () => pm.expect(pkg.id).to.be.a("number"));
pm.test("Provider attached from URL", () => pm.expect(pkg.provider.id).to.eql(Number(pm.collectionVariables.get("seedProviderId"))));
pm.test("active defaults applied", () => pm.expect(pkg.active).to.eql(true));
pm.collectionVariables.set("qaPackageId", pkg.id);
```

### `K02 — Create package: active default`  201

- **POST** `/api/packages/provider/{{seedProviderId}}` with body **without** `active`:

```json
{ "name": "QA Default Active Package", "monthlyPrice": 50.00 }
```

- Expect **201** and `active === true` (service default). Note its ID — you can delete it in K07.

### `K03 — Create package: validation cases`  expect 400

| # | Body | Expect `fieldErrors` |
|---|---|---|
| K03a | `{}` | `"name": "Package name is required"` **and** `"monthlyPrice": "Monthly price is required"` |
| K03b | `{"name": ""}` | `"name": "Package name is required"` |
| K03c | `{"name": "<101 chars>", "monthlyPrice": 10}` | `"name": "Package name must not exceed 100 characters"` |
| K03d | `{"name": "OK"}` (no price) | `"monthlyPrice": "Monthly price is required"` |
| K03e | `{"name": "OK", "monthlyPrice": 0}` | `"monthlyPrice": "Monthly price must be greater than 0"` |
| K03f | `{"name": "OK", "monthlyPrice": -5.00}` | `"monthlyPrice": "Monthly price must be greater than 0"` |
| K03g | `{"name": "OK", "monthlyPrice": 9.999}` (3 decimals) | `"monthlyPrice": "Monthly price format is invalid"` |
| K03h | `{"name": "OK", "monthlyPrice": 12345678901}` (11 integer digits) | `"monthlyPrice": "Monthly price format is invalid"` |
| K03i | `{"name": "OK", "monthlyPrice": 0.01}` | **201** — boundary value just above 0 is valid |
| K03j | `{"name": "OK", "monthlyPrice": 10, "billingCycle": "<51 chars>"}` | `"billingCycle": "Billing cycle must not exceed 50 characters"` |

> Note K03i: price **0.01** and **10.00** pass; **0** and **-0.01** fail. Test the boundary explicitly.

### `K04 — Create package: provider does not exist`  expect 404

- **POST** `/api/packages/provider/99999` with a valid body → **404**, `"TV Provider not found with id: 99999"`.

### `K05 — Get / list packages`  200 /  404

| # | Request | Expect |
|---|---|---|
| K05a | **GET** `/api/packages` | 200, array; each item embeds `provider` (without its packages) and `channels` array |
| K05b | **GET** `/api/packages/{{qaPackageId}}` | 200, correct package |
| K05c | **GET** `/api/packages/99999` | 404, `"Subscription Package not found with id: 99999"` |
| K05d | **GET** `/api/packages/provider/{{seedProviderId}}` | 200, only that provider's packages |
| K05e | **GET** `/api/packages/provider/99999` | 404, `"TV Provider not found with id: 99999"` |
| K05f | **GET** `/api/packages/provider/{{qaProviderId}}` of a provider with **no** packages (before K01 created one, or use a fresh provider) | 200, empty array `[]` |

> Routing note: `/api/packages/{id}` and `/api/packages/provider/{providerId}` coexist because Spring matches the literal `provider` segment first. Verify **K05d** returns packages, not a 500.

### `K06 — Update package`  200 /  400 /  404

| # | Request | Expect |
|---|---|---|
| K06a | **PUT** `/api/packages/{{qaPackageId}}` — change `name` to `"QA Test Package v2"` and `monthlyPrice` to `2499.50` | 200, values updated |
| K06b | **PUT** `{{qaPackageId}}` — `{"name": "OK", "monthlyPrice": 0}` | 400, price error |
| K06c | **PUT** `/api/packages/99999` — valid body | 404 |
| K06d | **PUT** `{{qaPackageId}}` — body that also contains a `provider` object for a *different* provider | 200 — **provider must NOT change** (update ignores the body's provider) |

### `K07 — Delete package`  204 /  404

| # | Request | Expect |
|---|---|---|
| K07a | **DELETE** `/api/packages/99999` | 404 |
| K07b | **DELETE** the package from K02 | 204 |
| K07c | **GET** it again | 404 |

---

## 9. Phase 4 — Cross-Resource & Business Rule Tests

### `X01 — Deleting a provider cascades to its packages`  critical

1. **POST** `/api/providers` → `{"name": "QA Cascade Provider"}` → capture `cascadeProviderId`.
2. **POST** `/api/packages/provider/{{cascadeProviderId}}` → `{"name": "QA Cascade Package", "monthlyPrice": 100}` → capture `cascadePackageId`.
3. **DELETE** `/api/providers/{{cascadeProviderId}}` → expect **204**.
4. **GET** `/api/packages/{{cascadePackageId}}` → expect **404** — the package was deleted **with** the provider (JPA `cascade = ALL, orphanRemoval = true`).
5. **Record:** is cascading deletion the desired product behavior? (Customers' packages disappear silently.)

### `X02 — Add channel to package`  200 /  duplicate

| # | Request | Expect |
|---|---|---|
| X02a | **POST** `/api/packages/{{qaPackageId}}/channels/{{seedChannelId}}` | 200, response body contains the channel in `channels` |
| X02b | **POST** same URL **again** (duplicate add) | **500**, `"Channel 'SuperSport' is already in package '...'"` — rule enforced, code should be 409 (Appendix B) |
| X02c | **POST** `/api/packages/99999/channels/1` | 404, package not found |
| X02d | **POST** `/api/packages/{{qaPackageId}}/channels/99999` | 404, `"Channel not found with id: 99999"` |

### `X03 — Delete a channel that is linked to a package`  likely 500 (known gap)

1. Ensure channel `seedChannelId` is still linked to `qaPackageId` (from X02a).
2. **DELETE** `/api/channels/{{seedChannelId}}`.
3. **Expected today:** most likely **500** (`DataIntegrityViolationException` from the `package_channels` foreign key). Confirm the actual result and record it.
4. Clean up first instead: **DELETE** `/api/packages/{{qaPackageId}}/channels/{{seedChannelId}}` → 200, then delete the channel → 204.

### `X04 — Remove channel from package`  200 (also for non-linked)

| # | Request | Expect |
|---|---|---|
| X04a | **DELETE** `/api/packages/{{qaPackageId}}/channels/{{seedChannelId}}` (it IS linked) | 200, `channels` no longer contains it |
| X04b | **DELETE** same URL again (channel no longer linked) | **200**, package returned unchanged — removal of a non-linked channel is a silent no-op, **not** an error. Record whether product wants a 404 instead. |
| X04c | **DELETE** `/api/packages/99999/channels/1` | 404 |
| X04d | **DELETE** `/api/packages/{{qaPackageId}}/channels/99999` | 404, channel not found |

### `X05 — Cleanup`

Delete your QA entities in this order (children first):

1. `DELETE /api/packages/{{qaPackageId}}` → 204
2. `DELETE /api/channels/{{qaChannelId}}` → 204 (only if it was unlinked in X04a)
3. Any leftover QA providers → 204

---

## 10. Phase 5 — HTTP-Level Error Tests

These verify the `GlobalExceptionHandler` mappings for framework-level errors.

| # | Request | Expect today | Notes |
|---|---|---|---|
| E01 | **PATCH** `/api/providers` (no body needed) | **405** | Wrong method on a mapped path |
| E02 | **POST** `/api/providers/1` | **405** | POST on an id path that has no POST |
| E03 | **GET** `/api/items` (nonexistent resource) | **404** | Generic message from §5.3 |
| E04 | **GET** `/api/providers/abc` (non-numeric id) | **500**  | Type mismatch — should be 400 (Appendix B) |
| E05 | **GET** `/api/channels/genre/` (empty path variable) | 404 (no match) | Trailing-slash path behavior |
| E06 | **POST** `/api/channels` with **malformed JSON** (`{"name": "X",`) | **500**  | `HttpMessageNotReadableException` — should be 400 |
| E07 | **POST** `/api/channels` with **no body at all** | **500**  | "Required request body is missing" — should be 400 |
| E08 | **POST** `/api/channels` with valid JSON but header `Content-Type: text/plain` | **500**  (record actual) | Should be 415 |
| E09 | **POST** `/api/channels` with `{"name": "X", "monthlyPrice": "abc"}` on a package endpoint | **500**  | Non-numeric price fails JSON deserialization, not bean validation |
| E10 | **GET** `/api/providers/` (trailing slash) | record actual (likely **404**) | Spring Boot 3 no longer matches trailing slashes by default |

---

## 11. Phase 6 — Run Everything with Collection Runner

1. **Restart the app** (fresh reseed of providers/channels).
2. In Postman: open the collection → **Run** (Collection Runner).
3. Order the folders: `00 Setup` → `01 Providers` → `02 Channels` → `03 Packages` → `04 Cross-Resource` → `05 HTTP Errors`.
4. Set **delay = 200 ms** between requests (avoids DB race noise).
5. Review the run summary: every request should show **all tests green**, *except* the requests that intentionally expect 4xx/5xx — their `pm.test("Status is 500 ...")` assertions should still pass.

### Standard assertion template (reuse in every request's Tests tab)

```js
// Replace the expected code per request
pm.test("Correct status code", () => pm.response.to.have.status(200));
pm.test("Response time < 1000ms", () => pm.expect(pm.response.responseTime).to.be.below(1000));
pm.test("Content-Type is JSON", () => pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json"));
```

For error responses, also assert the envelope:

```js
pm.test("Error envelope is correct", () => {
  const body = pm.response.json();
  pm.expect(body).to.have.property("timestamp");
  pm.expect(body).to.have.property("status", 404);
  pm.expect(body).to.have.property("error", "Not Found");
  pm.expect(body).to.have.property("message").that.is.a("string").and.not.empty;
});
```

For validation responses, also assert field errors:

```js
pm.test("fieldErrors contains name", () => {
  const body = pm.response.json();
  pm.expect(body.status).to.eql(400);
  pm.expect(body.fieldErrors).to.have.property("name");
});
```

---

## 12. Bug Log Template

Keep a table while testing. Example rows show the kinds of things this guide is designed to surface:

| ID | Endpoint | Case | Expected | Actual | Severity | Status |
|---|---|---|---|---|---|---|
| BUG-1 | POST /api/providers | Duplicate name | 400/409 | 500 | Medium | Open |
| BUG-2 | DELETE /api/channels/{id} | Channel linked to package | 204 or 409 | 500 (FK) | High | Open |
| BUG-3 | POST /api/packages (bad JSON) | Malformed body | 400 | 500 | Medium | Open |

---

## Appendix A — Validation Constraint Cheat Sheet

### TVProvider

| Field | Rules | Error messages |
|---|---|---|
| `name` | `@NotBlank`, `@Size(max=100)`, DB unique | "Provider name is required" / "Provider name must not exceed 100 characters" |
| `description` | `@Size(max=500)` | "Description must not exceed 500 characters" |
| `websiteUrl` | `@Size(max=255)` | "Website URL must not exceed 255 characters" |
| `country` | `@Size(max=100)` | "Country must not exceed 100 characters" |

### Channel

| Field | Rules | Error messages |
|---|---|---|
| `name` | `@NotBlank`, `@Size(max=100)`, DB unique | "Channel name is required" / "Channel name must not exceed 100 characters" |
| `channelNumber` | `@Size(max=20)` | "Channel number must not exceed 20 characters" |
| `genre` | `@Size(max=50)` | "Genre must not exceed 50 characters" |
| `description` | `@Size(max=500)` | "Description must not exceed 500 characters" |
| `logoUrl` | `@Size(max=255)` | "Logo URL must not exceed 255 characters" |

### SubscriptionPackage

| Field | Rules | Error messages |
|---|---|---|
| `name` | `@NotBlank`, `@Size(max=100)` | "Package name is required" / "Package name must not exceed 100 characters" |
| `monthlyPrice` | `@NotNull`, `@DecimalMin("0.0", exclusive)`, `@Digits(integer=10, fraction=2)` | "Monthly price is required" / "Monthly price must be greater than 0" / "Monthly price format is invalid" |
| `description` | `@Size(max=500)` | "Description must not exceed 500 characters" |
| `billingCycle` | `@Size(max=50)` | "Billing cycle must not exceed 50 characters" |
| `active` | optional Boolean; **defaults to `true`** if omitted | — |

---

## Appendix B — Known Gaps Found While Reviewing the Code

These are the reasons some tests above expect a 500 where a 4xx would be correct. If you fix them, **update the expected status codes in this guide** accordingly.

| # | Current behavior | Why | Recommended fix |
|---|---|---|---|
| 1 | Duplicate provider/channel name → **500** | `IllegalArgumentException` from services falls through to the generic `Exception` handler | Add `@ExceptionHandler(IllegalArgumentException.class)` → **400** or **409** |
| 2 | Malformed/missing JSON body → **500** | `HttpMessageNotReadableException` unhandled | Add handler → **400** |
| 3 | Non-numeric path variable (`/api/providers/abc`) → **500** | `MethodArgumentTypeMismatchException` unhandled | Add handler → **400** |
| 4 | Wrong `Content-Type` → **500** (verify) | `HttpMediaTypeNotSupportedException` unhandled | Add handler → **415** |
| 5 | `DELETE /api/channels/{id}` of a channel used by packages → likely **500** FK violation | `package_channels` join rows are not cleaned up (Channel is the *inverse* side of the ManyToMany) | Remove join rows first, or return **409**, or cascade link cleanup |
| 6 | `DELETE /api/providers/{id}` silently deletes all of the provider's packages | `cascade = ALL, orphanRemoval = true` | Decide if this is desired; otherwise block deletion when packages exist (**409**) |
| 7 | Removing a non-linked channel from a package returns 200 no-op | `List.remove()` on absent element is silent | Consider **404** for clarity |
| 8 | Seed packages duplicate on every restart | `data.sql` package INSERTs lack `ON CONFLICT DO NOTHING` and `name` has no unique constraint | Add unique constraint + `ON CONFLICT` clause |

---

*Generated for the StreamSwitch project (Spring Boot 3, port 8080). Keep this document next to the code and update expected results whenever you change controllers, entities, or the exception handler.*
