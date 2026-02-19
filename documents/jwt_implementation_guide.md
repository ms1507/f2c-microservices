# JWT + RBAC Implementation Guide

Here is the step-by-step recipe to implement **Stateless JWT Authentication** with **Method Level Security**.

## Step 1: Add Dependencies
We need a library to create and sign tokens. `jjwt` (Java JWT) is the standard choice.
**Action:** Add these to `user-service/pom.xml`:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

## Step 2: Create `JwtService` (The Ticked Printer)
This class handles all token logic.
**Capabilities:**
-   `generateToken(UserDetails)`: Creates a string token signed with a `SECRET_KEY`.
-   `extractUsername(token)`: Reads who sent the token.
-   `validateToken(token)`: Checks if the signature matches and if it has expired.

## Step 3: Create `JwtAuthenticationFilter` (The Gatekeeper)
This is a custom Filter that runs *before* the Spring Security logic.
**Logic:**
1.  Check request header: `Authorization: Bearer <token>`.
2.  If token exists -> Call `JwtService.validateToken()`.
3.  If valid -> Extract User & Roles -> Create an `Authentication` object.
4.  **Crucial Step:** Put this object into `SecurityContextHolder`.
    *   *Result:* Spring now knows "User X is logged in".

## Step 4: Update `SecurityConfig`
We need to wire everything together in `SecurityConfig.java`.
1.  **Enable RBAC:** Add annotation `@EnableMethodSecurity`.
2.  **Add Filter:** `.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`.
3.  **Lock Routes:** Change `.permitAll()` back to `.authenticated()` (except for `/auth/**`).

## Step 5: Implement Login API
Create a new endpoint `POST /auth/login`.
1.  Receive `email` and `password`.
2.  Use `AuthenticationManager` to verify password (It checks DB automatically).
3.  If correct, call `JwtService.generateToken()`.
4.  Return the Token string to the user.

## Step 6: Use It (RBAC)
Now you can protect individual methods!
**Example in Controller:**
```java
@PreAuthorize("hasRole('ADMIN')") // Only Admin can delete
@DeleteMapping("/{id}")
public void deleteUser(@PathVariable Long id) { ... }

@PreAuthorize("hasRole('FARMER')") // Only Farmer can create products
@PostMapping("/products")
public void createProduct() { ... }
```
