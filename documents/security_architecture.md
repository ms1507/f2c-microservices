# Spring Security Request Flow

## 1. Current Configuration Analysis
You changed the configuration to:
```java
.requestMatchers("/api/v1/**").permitAll()
```
**Effect:** This effectively **disables security** for all your current APIs.
-   Any URL starting with `/api/v1/` (which is everything) is allowed access without checking for a user, password, or token.
-   This is fine for **testing**, but for production, we will restrict this.

---

## 2. Key Components

### A. delegatingFilterProxy (The Door)
This is a standard Servlet Filter provided by Spring. It sits at the very front of the web server (Tomcat). It says "Hey, pass this request to the Spring Security system."

### B. SecurityFilterChain (The Guard)
This is the bean we defined in `SecurityConfig.java`. It contains a list of rules and "Filters".
-   **CSRF Filter:** Checks for Cross-Site Request Forgery (Disabled by us).
-   **UsernamePasswordAuthenticationFilter:** Standard login form (We aren't using this yet).
-   **AuthorizationFilter:** The one checking your `.requestMatchers()` rules.

### C. The HTTP Request Matchers
These are the rules inside `.authorizeHttpRequests()`.
-   **Filter:** "Does the URL match `/api/v1/**`?"
-   **Action:** `permitAll()` -> "Let them pass."

---

## 3. Step-by-Step Request Flow (Scenario: Get User)

1.  **Incoming Request:**
    Client sends `GET http://localhost:8081/api/v1/users/1`
    
2.  **Tomcat/Servlet Container:**
    Receives request -> Passes to `DelegatingFilterProxy`.

3.  **Security Filter Chain:**
    The request enters the chain.
    -   *Filter 1:* Checks Header...
    -   *Filter 2:* Checks CSRF...
    -   **AuthorizationFilter (The verify step):**
        -   It looks at your config: `requestMatchers("/api/v1/**").permitAll()`
        -   It asks: "Does `/api/v1/users/1` match `/api/v1/**`?" -> **YES**.
        -   It asks: "What is the rule?" -> **PERMIT ALL**.
        -   **Decision:** "Go ahead."

4.  **DispatcherServlet:**
    Spring MVC takes over. Finds `UserController`.

5.  **Controller Execution:**
    `getUser(1)` is called -> Returns JSON.

---

## 4. What happens if we secure it? (Future State)
If we change it to `.authenticated()`:
1.  **AuthorizationFilter** runs.
2.  It asks: "Is there a User in the SecurityContext?" (e.g., from a JWT Token).
3.  **Result:** "No."
4.  **Action:** Throw `AccessDeniedException` or return **403 Forbidden**.
