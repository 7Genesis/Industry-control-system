# 🏭 Industry Control System

Sistema web para controle de produtos e matérias-primas de uma indústria, com cálculo automático da produção possível com base no estoque disponível.

---

## 🚀 Technologies Used

### Backend
- Java 17+
- Quarkus
- Hibernate ORM with Panache
- PostgreSQL
- REST API

### Frontend
- React
- Axios
- CSS (responsive layout)

---

## 📌 Features Implemented

### ✅ RF001 – CRUD Products
- Create, list, update and delete products

### ✅ RF002 – CRUD Raw Materials
- Create, list, update and delete raw materials

### ✅ RF003 – Associate Raw Materials to Products
- Define quantity of each raw material required to produce a product

### ✅ RF004 – Production Suggestion
- Calculates how many units of each product can be produced
- Prioritizes products with higher value
- Uses virtual stock consumption
- Returns total production value

### ✅ RF005–RF008 – Frontend Interfaces
- Full CRUD UI
- Association interface
- Production suggestion screen
- Responsive layout (RNF003)

---

## 🧠 Business Rule – Production Algorithm

1. Products are ordered by highest price.
2. Available stock is copied to a virtual map.
3. For each product:
   - Calculate maximum producible quantity.
   - Reduce virtual stock.
4. Return:
   - Product name
   - Quantity possible
   - Unit price
   - Total value
   - Grand total

---

## 🗄 Database

PostgreSQL is used.

Example configuration (`application.properties`):

quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=YOUR_USER
quarkus.datasource.password=YOUR_PASSWORD
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/industry_db

---

## ▶️ How to Run

### Backend

```bash
cd backend
./mvnw quarkus:dev

Backend runs at:
http://localhost:8080

Frontend
cd frontend
npm install
npm start

Frontend runs at:
http://localhost:3000

