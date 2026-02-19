# Business Requirement Document (BRD)
## Rural Marketplace Application (Gramin Bazar)

**Date:** 2026-01-16  
**Status:** Draft v0.1

---

## 1. Executive Summary
The Rural Marketplace Application is a digital platform designed to bridge the gap between farmers, rural citizens, and local business owners. The application aims to empower users with limited digital literacy to buy, sell, and trade agricultural products, seeds, fertilizers, equipment, and daily necessities comfortably from their homes.

## 2. Target Audience
1.  **Farmers:** 
    -   **Role:** Product Owners (Sellers) & Buyers.
    -   **Needs:** Sell crops/grains at fair prices, buy seeds/fertilizers/equipment, access market information.
    -   **Tech Literacy:** Low to Medium.
2.  **Rural Citizens:**
    -   **Role:** Buyers.
    -   **Needs:** Buy daily needs, fresh vegetables, fruits, and grains directly from farmers or local shops.
3.  **Shop Owners:**
    -   **Role:** Sellers (B2C/B2B).
    -   **Categories:** Fertilizer shops, Seed shops, Equipment rentals/sales, Dairy owners.
    -   **Needs:** Expand customer base, manage inventory, digital storefront.

## 3. High-Level Requirements

### 3.1 Functional Requirements
-   **User Management:**
    -   Simple Registration/Login (Mobile Number + OTP).
    -   Profile Management (Farmer, Shop Owner, Buyer).
    -   **Language:** English (MVP).
-   **Order Management:**
    -   Place Orders (Buy Now, Add to Cart).
    -   **Payment:** Cash on Delivery (COD) for MVP.
    -   Order History.
    -   Basic Inventory tracking for sellers.
-   **Communication:**
    -   In-app chat or "Call Seller" button.
-   **Pricing & Offers:**
    -   Discounts and localized pricing models.
    -   Daily price updates for crops.

### 3.2 Non-Functional Requirements
-   **Usability:** Simplified UI/UX for non-tech-savvy users (Big buttons, Icons, Voice support).
-   **Performance:** Low latency, optimized for low-bandwidth 2G/3G/4G networks.
-   **Reliability:** High availability (Microservices architecture with Circuit Breakers).
-   **Scalability:** Ability to handle increasing user load (Containerization, Orchestration).
-   **Security:** Secure data storage, proper authentication (OAuth2/OIDC).
-   **Offline Capabilities:** Basic functionality should work offline or sync when online.

## 4. Technical Architecture System (Proposed)

### 4.1 Technology Stack
-   **Backend Framework:** Java Spring Boot.
-   **Architecture Style:** Microservices.
-   **Communication:** 
    -   Synchronous: REST APIs / gRPC.
    -   Asynchronous: Apache Kafka or RabbitMQ (for order processing, notifications).
-   **Database:** PostgreSQL (Relational Data), optionally Redis (Caching).
-   **Security:** Spring Security, Keycloak/OAuth2.

### 4.2 Key Microservices
1.  **API Gateway:** Assessing point for all client requests, routing, and basic auth check.
2.  **Auth Service:** User registration, OTP, Token management.
3.  **User Profile Service:** Manage farmer/shop details, KYC.
4.  **Catalog Service:** Product listings, categories, pricing.
5.  **Search Service:** ElasticSearch integration for fast, fuzzy search.
6.  **Order Service:** Order lifecycle management.
7.  **Notification Service:** SMS, Push Notifications (Kafka Consumer).

## 5. Roadmap & Phasing (Draft)
-   **Phase 1 (MVP):** User Auth, Basic Product Listing, Search, Direct Calling/Connecting.
-   **Phase 2:** Digital Orders, Cart, Inventory, Multilingual UI.
-   **Phase 3:** Payments integration, Logistics, Advanced Analytics.

## 6. Open Questions (Clarified)
-   **Payments:** Cash on Delivery (COD) confirmed for MVP. Digital payments deferred.
-   **Logistics:** Undecided. System will allow Buyer/Seller to coordinate offline initially.
-   **Languages:** English only for MVP.
-   **Platform:** Undecided. Backend will be API-first to support generic clients (Mobile/Web).
