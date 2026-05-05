# Project Report: SmartPark - Online Parking System

## Index: Development (Web/Mobile Applications, ETL)

| Chapter | Content |
| :--- | :--- |
| **Chapter 1** | **Introduction** |
| 1.1 | Problem Statement |
| 1.2 | Objectives |
| 1.3 | Scope |
| **Chapter 2** | **Design** |
| 2.1 | System Architecture |
| 2.2 | Database Design |
| **Chapter 3** | **Implementation** |
| 3.1 | Frontend Development |
| 3.2 | Backend Development |
| 3.3 | Integration |
| **Chapter 4** | **Testing** |
| 4.1 | Test Cases |
| 4.2 | Results |
| **Chapter 5** | **Conclusion** |
| 5.1 | Summary |
| 5.2 | Future Enhancements |
| **Chapter 6** | **References** |
| **Chapter 7** | **Appendices** |
| **Chapter 8** | **Annexure - Progress Sheet** |

---

<div style="page-break-after: always;"></div>

## Chapter 1: Introduction

### 1.1 Problem Statement
Finding an available parking space in densely populated urban areas and commercial complexes is a time-consuming and frustrating experience for drivers. Traditional parking systems rely on manual ticketing and on-site physical searching, leading to traffic congestion, fuel wastage, and inefficient use of parking facility resources. There is a lack of real-time visibility regarding slot availability and pricing, leading to a poor user experience.

### 1.2 Objectives
The primary objectives of the SmartPark Online Parking System are:
*   To provide real-time visibility of parking lot availability across various city locations.
*   To enable users to pre-book parking slots dynamically based on their vehicle type (2-Wheeler or 4-Wheeler).
*   To implement a secure, automated payment and invoicing system.
*   To optimize parking space utilization and reduce the time spent searching for parking.
*   To provide administrators with a centralized dashboard to manage locations, slots, and view analytics.

### 1.3 Scope
The scope of this project encompasses the development of a responsive web application that serves both end-users and administrators. 
*   **User Scope:** Registration/Login, searching for parking lots, viewing slot availability, dynamic price estimation, secure booking with a 5-minute slot lock, digital ticket generation (QR code), and viewing booking history.
*   **Admin Scope:** Managing parking locations, monitoring live slot statuses, and overseeing all booking transactions.
*   **Out of Scope:** Physical hardware integration (like automated boom barriers or IoT sensors) in this phase.

---

## Chapter 2: Design

### 2.1 System Architecture
The SmartPark system is built on a modern, decoupled **Client-Server Architecture**.
*   **Client Tier (Frontend):** A responsive web interface built using HTML5, CSS3, and Vanilla JavaScript. It communicates asynchronously with the backend via RESTful APIs.
*   **Application Tier (Backend):** A Java Spring Boot application acting as the core engine. It handles business logic, security (JWT Authentication), concurrency (slot locking mechanisms), and API endpoints.
*   **Data Tier (Database):** A relational database management system. It uses an in-memory H2 database for local development and is configured to seamlessly switch to PostgreSQL for production deployment. Flyway is used for database schema migrations.

### 2.2 Database Design
The relational database schema is designed to ensure data integrity and prevent double-booking. Key entities include:
*   **`users`**: Stores user credentials, roles (USER/ADMIN), and contact details.
*   **`parking_lots`**: Contains details of locations, total capacity, and base pricing structures.
*   **`slots`**: Represents individual parking spaces, categorized by type (e.g., REGULAR, COMPACT) and linked to a specific parking lot.
*   **`bookings`**: Records all reservations, linking a user, a slot, timestamps, and payment status.
*   **`slot_locks`**: A crucial table managing temporary reservations (5-minute locks) to prevent concurrent booking of the same slot during the checkout process.
*   **`payments`**: Tracks transaction references and statuses.

---

## Chapter 3: Implementation

### 3.1 Frontend Development
The frontend is implemented as a Single Page Application (SPA) feel utilizing standard web technologies to ensure lightweight performance and high compatibility.
*   **Technologies:** HTML5, CSS3, JavaScript (ES6+).
*   **Key Modules:** 
    *   **Authentication UI:** Secure login and registration forms with client-side validation.
    *   **Dashboard:** Interactive interface displaying available parking lots and a visual grid for slot selection.
    *   **Booking Flow:** Dynamic UI that calculates costs in real-time based on selected vehicle type and duration.

### 3.2 Backend Development
The robust backend is engineered using the Spring ecosystem to provide secure and scalable APIs.
*   **Technologies:** Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, Maven.
*   **Key Implementations:**
    *   **Security:** Implemented stateless authentication using JSON Web Tokens (JWT). All endpoints (except public auth routes) are secured.
    *   **Concurrency Control:** Implemented a locking mechanism where selecting a slot creates a temporary record in `slot_locks`. If the booking is not completed within 5 minutes, a scheduled task (`SlotCleanupScheduler`) automatically releases the lock.
    *   **Dynamic Pricing Engine:** Calculates accurate parking fees factoring in base rates, vehicle multipliers (e.g., higher rates for 4-wheelers), and duration.

### 3.3 Integration
*   **Database Integration:** Spring Data JPA provides the ORM layer, mapping Java objects to relational tables effortlessly.
*   **Payment Gateway Preparation:** The system is integrated with the Stripe Java SDK to handle secure financial transactions.
*   **QR Code Generation:** Integrated the ZXing library to generate unique QR codes for confirmed bookings, acting as digital passes.

---

## Chapter 4: Testing

### 4.1 Test Cases
Various testing methodologies were employed to ensure system stability:
1.  **Authentication Testing:** Verifying successful login with valid credentials, rejecting invalid credentials, and ensuring unauthorized access to protected endpoints is blocked (HTTP 401/403).
2.  **Concurrency Testing:** Simulating simultaneous booking attempts for the same slot to verify the locking mechanism correctly grants the slot to the first user and denies subsequent attempts.
3.  **Pricing Calculation:** Validating the total amount calculated for different vehicle types across varying time durations against expected manual calculations.
4.  **End-to-End Workflow:** Testing the complete flow from user registration -> logging in -> searching a lot -> selecting a slot -> checkout -> viewing the booking in history.

### 4.2 Results
*   The JWT authentication successfully secures the REST APIs.
*   The 5-minute slot lock mechanism successfully prevents double-booking scenarios under simulated concurrent loads.
*   Dynamic pricing calculations align perfectly with the defined business rules for 2-wheelers and 4-wheelers.
*   The local deployment utilizing the embedded Tomcat server and H2 database runs stably.

---

## Chapter 5: Conclusion

### 5.1 Summary
The SmartPark Online Parking System successfully addresses the core issues of manual parking management. By providing a digitized, real-time platform, users can conveniently secure parking spaces in advance, guaranteeing availability and transparent pricing. The backend architecture is robust, utilizing Spring Boot and secure JWT authentication, while implementing crucial business logic like concurrency control to ensure a seamless and error-free booking experience.

### 5.2 Future Enhancements
*   **IoT Integration:** Integrating hardware sensors at physical parking slots to update the real-time availability in the database automatically without manual intervention.
*   **Payment Gateway Activation:** Transitioning the Stripe integration from test mode to live production mode.
*   **Mobile Application:** Developing native Android and iOS applications using frameworks like React Native or Flutter for improved mobile accessibility.
*   **Advanced Analytics:** Providing administrators with predictive analytics regarding peak hours and revenue forecasting.

---

## Chapter 6: References
1.  Spring Boot Documentation: [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
2.  JSON Web Token (JWT) Introduction: [https://jwt.io/introduction/](https://jwt.io/introduction/)
3.  Flyway Database Migration: [https://flywaydb.org/](https://flywaydb.org/)
4.  Stripe API Reference: [https://stripe.com/docs/api](https://stripe.com/docs/api)
5.  ZXing (Zebra Crossing) QR Code Generation: [https://github.com/zxing/zxing](https://github.com/zxing/zxing)

---

## Chapter 7: Appendices
*   **Appendix A:** API Endpoint Documentation (available via Swagger/OpenAPI in a future iteration).
*   **Appendix B:** Database Schema Diagram.
*   **Appendix C:** Environment Setup Guide (Docker Compose configuration and Local setup instructions).

---

## Chapter 8: Annexure - Progress Sheet
| Date | Milestone | Status |
| :--- | :--- | :--- |
| Week 1 | Requirement Gathering & System Design | Completed |
| Week 2 | Database Design & Flyway Migrations | Completed |
| Week 3 | Backend Core Services & JWT Security | Completed |
| Week 4 | Frontend Development (UI/UX) | Completed |
| Week 5 | Integration: Slot Locking & Dynamic Pricing | Completed |
| Week 6 | Testing, Bug Fixing, and Local Deployment | Completed |
| Week 7 | Production Deployment Preparation | Pending |
