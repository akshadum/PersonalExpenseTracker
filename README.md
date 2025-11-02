# Expense Tracker  
A Spring Boot-based RESTful Expense Tracker application to help users record, categorize, and monitor their daily expenses — with real-time budget tracking, analytics and alerts.

---

## 🚀 Features  
- Full CRUD operations for expenses: add, update, delete, list.  
- Filter expenses by category, payment mode, date range, amount above/below.  
- Analytics endpoint:  
  - Monthly total spent  
  - Top 3 expense categories  
  - Average daily spending  
- Recurring expense support: mark expenses as recurring; scheduled job automatically adds them monthly.  
- Budget alerts: automatic email notifications when a user’s spending reaches 80 %, 90 %, 100 % & 120 % of their budget.  
- REST API documentation via Swagger UI.  
- Application monitoring via Spring Boot Actuator.  
- Logging with SLF4J & proper exception handling.

---

## 🛠️ Tech Stack  
- Java 8  
- Spring Boot (2.x)  
- Spring Data JPA / Hibernate  
- RESTful APIs  
- Maven  
- H2 (in-memory) or any SQL database  
- Spring Boot Actuator (monitoring & metrics)  
- OpenAPI / Swagger UI (API docs)  
- SLF4J / Logback (logging)  
- JUnit 5 & Mockito (unit testing)  
- (Optional) Docker + Docker-Compose for containerisation and database setup  

---

## 🧩 Getting Started  

### Prerequisites  
- JDK 8 installed  
- Maven installed  
- (Optional) Docker if using a non-H2 database  

### Setup & Run  
1. Clone this repository:  
   ```bash
   git clone https://github.com/akshadum/ExpenseTracker.git
   ```

2. Navigate into project directory:
   ```bash
   cd ExpenseTracker
   ```
   
3. Build and run the application:
   ```bash
   mvn clean spring-boot:run
   ```
4. Open the API documentation in your browser:
   ```bash
   http://localhost:8080/swagger-ui/index.html
   ```
5. Access actuator endpoints for monitoring:
   ```bash
   http://localhost:8080/actuator
   ```
---

## 📂 Project Structure

```
src/
  main/
    java/
      com.application.expenseTracker/
        Controller/        ← REST controllers  
        Service/           ← Business logic  
        Repository/        ← Spring Data JPA repositories  
        Entity/            ← JPA entities  
        Exception/         ← Custom exceptions + handlers  
    resources/
      application.properties  ← Application configuration  
```
---

## ✅ API Highlights

| Method | Endpoint | Description |
|--------|-----------|-------------|
| `POST` | `/api/v1/expenses` | Add a new expense |
| `GET` | `/api/v1/expenses` | Get all expenses |
| `PUT` | `/api/v1/expenses/{id}` | Update expense by ID |
| `DELETE` | `/api/v1/expense/{id}` | Delete expense by ID |
| `DELETE` | `/api/v1/expense/delete` | Delete all expenses |
| `GET` | `/api/v1/list/category/{category}` | Get expenses by category |
| `GET` | `/api/v1/expenses/payment/{paymentMode}` | Get expenses by payment mode |
| `GET` | `/api/v1/expenses/date-range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` | Get expenses by date range |
| `GET` | `/api/v1/expenses/amount-above/{amount}` | Get expenses above amount |
| `GET` | `/api/v1/expenses/amount-below/{amount}` | Get expenses below amount |
| `GET` | `/api/v1/expenses/recent/` | Get recent expenses |
| `GET` | `/api/v1/expenses/recurring/` | Get recurring expenses |
| `GET` | `/api/v1/expenses/total/` | Get total expenses of current month |
| `GET` | `/api/v1/expenses/summary` | Get expense summary (total + top categories + average) |
| `POST` | `/api/v1/expenses/{id}/newdate?newdate=YYYY-MM-DD` | Update expense date |
| `@Scheduled` | `createRecurringEntries()` | Auto-creates recurring expenses monthly |

> For full list and request/response samples, see the Swagger UI.

---
## 🧪 Test Cases  

### 🧩 Controller Layer (CalculationControllerTest)

#### **1️⃣ Add Expense**
- ✅ `testAddExpense_Success`
- 🚫 `testAddExpense_MissingEmail`
- 🚫 `testAddExpense_InvalidBudget`
- 💥 `testAddExpense_ExceptionHandling`

#### **2️⃣ Get All Expenses**
- ✅ `testGetAllExpense_Success`
- 🚫 `testGetAllExpense_EmptyList`
- 💥 `testGetAllExpense_ExceptionHandling`

#### **3️⃣ Update Expense**
- ✅ `testUpdateExpense_Success`
- 🚫 `testUpdateExpense_NotFound`
- 💥 `testUpdateExpense_ExceptionHandling`

#### **4️⃣ Delete Expense**
- ✅ `testDeleteExpenseById_Success`
- 🚫 `testDeleteExpenseById_NotFound`
- 💥 `testDeleteExpenseById_ExceptionHandling`

#### **5️⃣ Get by Category / Payment Mode / Date Range**
- ✅ `testGetExpensesByCategory_Success`
- 🚫 `testGetExpensesByCategory_Empty`
- ✅ `testGetExpensesByPaymentMode_Success`
- 🚫 `testGetExpensesByPaymentMode_Empty`
- ✅ `testGetExpensesByDateRange_Success`
- 🚫 `testGetExpensesByDateRange_Empty`

#### **6️⃣ Get Total / Recent / Recurring / Above / Below / Summary**
- ✅ `testGetTotalExpenses_Success`
- 🚫 `testGetTotalExpenses_Empty`
- ✅ `testGetRecentExpenses_Success`
- ✅ `testGetRecurringExpenses_Success`
- ✅ `testGetExpensesAbove_Success`
- ✅ `testGetExpensesBelow_Success`
- ✅ `testGetExpenseSummary_Success`

#### **7️⃣ Update Expense Date**
- ✅ `testUpdateExpenseDone_Success`
- 🚫 `testUpdateExpenseDone_NotFound`

#### **8️⃣ Scheduled Task**
- ✅ `testCreateRecurringEntries_WithExpenses`
- 🚫 `testCreateRecurringEntries_NoExpenses`
- 💥 `testCreateRecurringEntries_Exception`

---

### ⚙️ Service Layer (CalculationServiceTest)

| Test Case | Description |
|------------|-------------|
| ✅ `testAddExpense_Success` | Valid expense added |
| ✅ `testUpdateExpense_Success` | Update existing expense |
| ✅ `testFindById_Success` | Find expense by ID |
| 🚫 `testFindById_NotFound` | Handle missing expense |
| ✅ `testDeleteExpenseById_Success` | Delete expense by ID |
| ✅ `testDeleteAllExpenses_Success` | Delete all expenses |
| ✅ `testGetExpensesByCategory_Success` | Fetch by category |
| ✅ `testGetExpensesByDateRange_Success` | Fetch by date range |
| ✅ `testGetExpensesByPaymentMode_Success` | Fetch by payment mode |
| ✅ `testGetTotalExpenses_Success` | Fetch total spent |
| ✅ `testGetRecentExpenses_Success` | Fetch recent transactions |
| ✅ `testGetExpenseSummary_Success` | Summary with totals |
| ✅ `testCheckAndSendBudgetAlerts_At80Percent` | Trigger alert at 80 % |
| ✅ `testCheckAndSendBudgetAlerts_At100Percent` | Trigger alert at 100 % |
| 🚫 `testCheckAndSendBudgetAlerts_NoAlert` | No alert under threshold |
| 💥 `testCheckAndSendBudgetAlerts_Exception` | Handle email errors |

---

### 🧱 Global Exception Handler (GlobalExceptionHandlerTest)

| Test Case | Description |
|------------|-------------|
| ✅ `testHandleExpenseNotFoundException` | 404 response |
| ✅ `testHandleInvalidBudgetException` | 400 response |
| ✅ `testHandleGenericException` | 500 response |

---

## 🧪 Testing

* Unit tests are located in src/test/java/... using JUnit 5 & Mockito.
* Example test cases include: 'testAddExpense_Success()', 'testGetAllExpense_EmptyList()', 'testUpdateExpense_NotFound()', etc.
* To run tests:

  ```bash
  mvn test
  ```

---

## 🔧 Future Enhancements

* Add JWT authentication and multi-user support.
* Replace H2 with production database (PostgreSQL/MySQL) & use Flyway for migrations.
* Introduce caching for read-heavy endpoints (e.g., analytics).
* Add Docker + Docker-Compose configuration.
* Extend monitoring: custom Actuator endpoints, integrate with Prometheus/Grafana.
* Add pagination & sorting for list endpoints.
* Implement full frontend (React/Angular) for user interface.
