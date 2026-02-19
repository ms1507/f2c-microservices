# Spring Security Filter Chain - Complete Guide

## 1. Filter Chain Sequence

Spring Security uses a **chain of filters** that process requests in a specific order. Here's the default sequence:

```
HTTP Request
    ↓
1. ChannelProcessingFilter (HTTP/HTTPS enforcement)
    ↓
2. SecurityContextPersistenceFilter (Loads/saves SecurityContext)
    ↓
3. ConcurrentSessionFilter (Session management)
    ↓
4. HeaderWriterFilter (Adds security headers)
    ↓
5. CsrfFilter (CSRF protection)
    ↓
6. LogoutFilter (Handles logout requests)
    ↓
7. UsernamePasswordAuthenticationFilter (Form login)
    ↓
8. DefaultLoginPageGeneratingFilter (Generates login page)
    ↓
9. BasicAuthenticationFilter (HTTP Basic auth)
    ↓
10. RequestCacheAwareFilter (Restores saved requests)
    ↓
11. SecurityContextHolderAwareRequestFilter (Wraps request)
    ↓
12. AnonymousAuthenticationFilter (Creates anonymous user)
    ↓
13. SessionManagementFilter (Session fixation protection)
    ↓
14. ExceptionTranslationFilter (Handles security exceptions)
    ↓
15. AuthorizationFilter (Checks permissions)
    ↓
Your Controller
```

## 2. Key Filters Explained

### 🔒 **SecurityContextPersistenceFilter**
- **What:** Loads SecurityContext from session (or creates new one)
- **When:** Every request
- **For REST APIs:** Not needed (we use STATELESS sessions)

### 🛡️ **CsrfFilter**
- **What:** Validates CSRF tokens
- **When:** POST, PUT, DELETE requests
- **For REST APIs:** ❌ Disable (we use tokens, not cookies)
```java
.csrf(AbstractHttpConfigurer::disable)
```

### 🚪 **UsernamePasswordAuthenticationFilter**
- **What:** Processes form-based login (POST to /login)
- **When:** Only on /login endpoint
- **For REST APIs:** ❌ Disable (we use JSON, not forms)
```java
.formLogin(AbstractHttpConfigurer::disable)
```

### 🔑 **BasicAuthenticationFilter**
- **What:** Processes HTTP Basic auth (Authorization: Basic base64)
- **When:** Every request with Basic auth header
- **For REST APIs:** ❌ Disable (we use JWT)
```java
.httpBasic(AbstractHttpConfigurer::disable)
```

### 👤 **AnonymousAuthenticationFilter**
- **What:** Creates anonymous user if no authentication exists
- **When:** Every unauthenticated request
- **For REST APIs:** ✅ Keep (allows public endpoints)

### ✅ **AuthorizationFilter**
- **What:** Checks if user has permission for the request
- **When:** Every request (after authentication)
- **For REST APIs:** ✅ Keep (enforces .authenticated())

## 3. Custom Filters (Your JWT Filter)

### Where to Add Custom Filters

```java
// BEFORE a specific filter
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

// AFTER a specific filter
.addFilterAfter(customFilter, BasicAuthenticationFilter.class)

// AT a specific position (replaces default)
.addFilterAt(customFilter, UsernamePasswordAuthenticationFilter.class)
```

### Your JWT Filter Position

```java
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
```

**Why?** 
- JWT filter runs BEFORE form login filter
- Extracts token and sets authentication
- If authentication is set, form login filter is skipped

## 4. How to Disable Filters

### Method 1: Using Configurers
```java
http
    .csrf(AbstractHttpConfigurer::disable)          // Disables CsrfFilter
    .formLogin(AbstractHttpConfigurer::disable)     // Disables UsernamePasswordAuthenticationFilter
    .httpBasic(AbstractHttpConfigurer::disable)     // Disables BasicAuthenticationFilter
    .logout(AbstractHttpConfigurer::disable)        // Disables LogoutFilter
```

### Method 2: Session Management
```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
// Disables: SecurityContextPersistenceFilter, SessionManagementFilter
```

### Method 3: Remove Specific Filter
```java
http.removeFilter(CsrfFilter.class)
```

## 5. Filters for Different Use Cases

### ✅ **REST API with JWT (Your Case)**
```java
Enabled:
- HeaderWriterFilter (security headers)
- JwtAuthenticationFilter (custom - validates JWT)
- AnonymousAuthenticationFilter (allows public endpoints)
- ExceptionTranslationFilter (handles 401/403)
- AuthorizationFilter (checks permissions)

Disabled:
- CsrfFilter
- UsernamePasswordAuthenticationFilter
- BasicAuthenticationFilter
- SessionManagementFilter
```

### ✅ **Traditional Web App (Form Login)**
```java
Enabled:
- CsrfFilter (protects forms)
- UsernamePasswordAuthenticationFilter (login form)
- SessionManagementFilter (session fixation)
- RememberMeAuthenticationFilter (remember me)
- LogoutFilter (logout)

Disabled:
- Nothing (use defaults)
```

### ✅ **Microservice (Internal Only)**
```java
Enabled:
- None (disable all security)

Disabled:
- Everything
```
```java
http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
```

## 6. Filter Execution Flow Example

### Request: `POST /api/v1/users/1` with `Authorization: Bearer <token>`

```
1. HeaderWriterFilter
   → Adds security headers (X-Frame-Options, etc.)

2. CsrfFilter (DISABLED in your config)
   → Skipped

3. UsernamePasswordAuthenticationFilter (DISABLED)
   → Skipped

4. JwtAuthenticationFilter (YOUR CUSTOM FILTER)
   → Extracts token from header
   → Validates token
   → Sets Authentication in SecurityContext
   
5. AnonymousAuthenticationFilter
   → Checks SecurityContext
   → Authentication exists? Skip (already authenticated)

6. ExceptionTranslationFilter
   → Wraps next filters
   → Catches AuthenticationException, AccessDeniedException

7. AuthorizationFilter
   → Checks: .anyRequest().authenticated()
   → SecurityContext has Authentication? ✅ Allow
   → No Authentication? ❌ Throw AccessDeniedException

8. Your Controller
   → UserController.updateUser() executes
```

## 7. Common Patterns

### Pattern 1: Skip Filter for Specific URLs
```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/v1/auth/");
}
```

### Pattern 2: Multiple Custom Filters
```java
http
    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
    .addFilterAfter(loggingFilter, JwtAuthenticationFilter.class)
    .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)
```

### Pattern 3: Conditional Filter
```java
public class ConditionalFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) {
        if (shouldProcess(request)) {
            // Process
        }
        filterChain.doFilter(request, response);
    }
}
```

## 8. Debugging Filters

### Enable Debug Logging
```yaml
# application.yml
logging:
  level:
    org.springframework.security: DEBUG
```

### See Filter Chain in Console
```
Security filter chain: [
  DisableEncodeUrlFilter
  WebAsyncManagerIntegrationFilter
  SecurityContextPersistenceFilter
  HeaderWriterFilter
  CsrfFilter
  LogoutFilter
  ...
]
```

## 9. Best Practices

✅ **DO:**
- Use `OncePerRequestFilter` for custom filters (runs once per request)
- Add filters at correct position in chain
- Use `shouldNotFilter()` to skip unnecessary processing
- Keep filters stateless
- Log filter execution for debugging

❌ **DON'T:**
- Add filters that block for long time (use async)
- Store state in filter instances (they're singletons)
- Forget to call `filterChain.doFilter()` (breaks chain)
- Add too many filters (performance impact)

## 10. Summary for Your Project

**Your Current Setup:**
```java
SecurityFilterChain:
1. HeaderWriterFilter (default)
2. JwtAuthenticationFilter (custom - BEFORE UsernamePasswordAuthenticationFilter)
3. AnonymousAuthenticationFilter (default)
4. ExceptionTranslationFilter (default)
5. AuthorizationFilter (default)

Disabled:
- CsrfFilter
- UsernamePasswordAuthenticationFilter
- BasicAuthenticationFilter
- SessionManagementFilter (via STATELESS)
```

**This is PERFECT for a JWT-based REST API!** ✅
