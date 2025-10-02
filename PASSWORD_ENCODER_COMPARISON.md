# Password Encoder Comparison - Spring Security

## Quick Reference

| Encoder | Strength | Speed | Memory | Use Case | Recommendation |
|---------|----------|-------|--------|----------|----------------|
| **BCrypt** | ⭐⭐⭐⭐⭐ | Medium | Low | General purpose | ✅ **Best for most apps** |
| **Argon2** | ⭐⭐⭐⭐⭐ | Slow | High | High security | ✅ Modern alternative |
| **SCrypt** | ⭐⭐⭐⭐ | Slow | High | Cryptocurrency | ⚠️ Good but complex |
| **PBKDF2** | ⭐⭐⭐ | Fast | Low | Standards compliance | ⚠️ Okay but outdated |
| **SHA-256** | ⭐⭐ | Very Fast | Low | Never for passwords | ❌ Too fast, not secure |
| **MD5** | ⭐ | Instant | Low | Checksums only | ❌ Broken, never use |
| **Plain Text** | ❌ | Instant | Low | Never! | ❌ Never ever use |

## Detailed Comparison

### 1. BCrypt (⭐ Recommended)

#### Configuration
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

#### Pros
✅ Industry standard (most widely used)
✅ Adaptive (cost factor adjustable)
✅ Built-in salt generation
✅ Spring Security default
✅ Well-tested (20+ years)
✅ Good balance of security and speed
✅ Easy to implement

#### Cons
⚠️ Not memory-hard
⚠️ Vulnerable to GPU attacks (with high cost)

#### Performance
```
Strength 10: ~65ms per hash
Strength 12: ~260ms per hash (recommended)
Strength 14: ~1000ms per hash
```

#### Best For
- Web applications
- Mobile apps
- Microservices
- General authentication

---

### 2. Argon2 (⭐ Modern Choice)

#### Configuration
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
}

// Or custom:
@Bean
public PasswordEncoder passwordEncoder() {
    return new Argon2PasswordEncoder(
        16,    // salt length
        32,    // hash length
        1,     // parallelism
        4096,  // memory (KB)
        3      // iterations
    );
}
```

#### Pros
✅ Winner of Password Hashing Competition 2015
✅ Memory-hard (resistant to GPU/ASIC attacks)
✅ Configurable (memory, time, parallelism)
✅ Most modern algorithm
✅ Recommended by OWASP (2023)

#### Cons
⚠️ Newer (less adoption)
⚠️ More complex configuration
⚠️ Requires more memory

#### Performance
```
Default: ~500ms per hash
Memory: ~4MB per hash
```

#### Best For
- High-security applications
- Financial services
- Healthcare systems
- Government applications

---

### 3. SCrypt

#### Configuration
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8();
}

// Or custom:
@Bean
public PasswordEncoder passwordEncoder() {
    return new SCryptPasswordEncoder(
        16384,  // CPU cost
        8,      // memory cost
        1,      // parallelization
        32,     // key length
        64      // salt length
    );
}
```

#### Pros
✅ Memory-hard algorithm
✅ Used in cryptocurrencies (Litecoin)
✅ Good security properties
✅ Configurable parameters

#### Cons
⚠️ Complex configuration
⚠️ Slower than BCrypt
⚠️ High memory usage
⚠️ Less adoption than BCrypt

#### Performance
```
Default: ~600ms per hash
Memory: ~16MB per hash
```

#### Best For
- Cryptocurrency applications
- High-security scenarios
- When GPU resistance is critical

---

### 4. PBKDF2

#### Configuration
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
}

// Or custom:
@Bean
public PasswordEncoder passwordEncoder() {
    return new Pbkdf2PasswordEncoder(
        "secret",  // secret key
        310000,    // iterations (NIST recommends 310,000+)
        256        // hash width (bits)
    );
}
```

#### Pros
✅ NIST-approved standard (SP 800-132)
✅ Simple to understand
✅ Configurable iterations
✅ Standards compliance

#### Cons
⚠️ Not memory-hard
⚠️ Vulnerable to GPU attacks
⚠️ Requires many iterations for security
⚠️ Considered outdated

#### Performance
```
310,000 iterations: ~200ms per hash
```

#### Best For
- Government compliance
- Legacy system migration
- Standards-required environments

---

### 5. SHA-256 (❌ Never for Passwords)

#### Why Not?
❌ Too fast (millions of hashes/second)
❌ No built-in salt
❌ Not designed for passwords
❌ Easily brute-forced

#### Only Use For
- File checksums
- Data integrity verification
- Digital signatures
- Never passwords!

---

### 6. MD5 (❌ Broken)

#### Why Not?
❌ Cryptographically broken
❌ Collision attacks
❌ Too fast
❌ Never use in production

---

## Decision Matrix

### Choose BCrypt If:
- Building a typical web/mobile application
- Need proven, battle-tested security
- Want simplicity and ease of use
- Performance is acceptable (~260ms)
- **Most common choice ✅**

### Choose Argon2 If:
- Building high-security applications
- Need maximum security
- Can tolerate higher latency
- Have memory to spare
- Want cutting-edge security

### Choose SCrypt If:
- Building cryptocurrency applications
- Need GPU resistance
- Have specific compliance requirements
- Can handle high memory usage

### Choose PBKDF2 If:
- Required by compliance/regulations
- Migrating from legacy systems
- NIST standards required
- Last resort option

## Real-World Usage

### Industry Adoption

**BCrypt:**
- GitHub ✅
- Stack Overflow ✅
- Reddit ✅
- Most Django apps ✅

**Argon2:**
- 1Password ✅
- Bitwarden ✅
- Modern security-focused apps ✅

**SCrypt:**
- Cryptocurrency wallets ✅
- Litecoin ✅

**PBKDF2:**
- Apple iOS ✅
- Government systems ✅

## Implementation Examples

### BCrypt (Our Implementation)
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

### Argon2 Alternative
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
```

### Multiple Encoders (Migration)
```java
@Bean
public PasswordEncoder passwordEncoder() {
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put("bcrypt", new BCryptPasswordEncoder());
    encoders.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
    encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
    
    return new DelegatingPasswordEncoder("bcrypt", encoders);
}
```

## Migration Path

### From Plain Text → BCrypt
```java
// Check if password is plain text
if (!password.startsWith("$2a$")) {
    password = bcryptEncoder.encode(password);
}
```

### From BCrypt → Argon2
```java
@Bean
public PasswordEncoder passwordEncoder() {
    Map<String, PasswordEncoder> encoders = Map.of(
        "argon2", new Argon2PasswordEncoder(),
        "bcrypt", new BCryptPasswordEncoder()
    );
    return new DelegatingPasswordEncoder("argon2", encoders);
}
```

## Performance Comparison

### Hashing Time (Single Password)
```
Plain/MD5:    < 1ms     ❌ Too fast
SHA-256:      < 1ms     ❌ Too fast
PBKDF2:       ~200ms    ⚠️ Okay
BCrypt (12):  ~260ms    ✅ Good
Argon2:       ~500ms    ✅ Better
SCrypt:       ~600ms    ✅ Good
```

### Throughput (Hashes per second)
```
MD5:          1,000,000+   ❌ Terrible for passwords
SHA-256:      500,000+     ❌ Terrible for passwords
BCrypt (12):  4           ✅ Good (slows attackers)
Argon2:       2           ✅ Better (slows attackers)
```

## Security Timeline

### Attack Resistance (2025)

**Against CPU brute-force:**
- BCrypt (12): ~10 years to crack
- Argon2: ~20 years to crack
- PBKDF2 (310k): ~5 years to crack

**Against GPU brute-force:**
- BCrypt (12): ~1 year to crack
- Argon2: ~10 years to crack (memory-hard)
- PBKDF2: ~6 months to crack

**Against ASIC:**
- BCrypt: ~3 months to crack
- Argon2: ~5 years to crack (memory-hard)
- PBKDF2: ~1 month to crack

*Times based on well-funded attacker, 8-character passwords*

## Final Recommendation

### For BVS Cloud Service: **BCrypt (Strength 12)** ✅

#### Why?
1. **Proven Track Record** - 20+ years in production
2. **Industry Standard** - Most widely adopted
3. **Good Performance** - ~260ms is acceptable
4. **Easy to Implement** - One line of code
5. **Spring Security Default** - Well-integrated
6. **Sufficient Security** - For most applications

### When to Consider Argon2?
- Handling very sensitive data (financial, health)
- High-value targets (admin accounts, privileged users)
- Compliance requirements (future-proofing)
- Have budget for extra latency

### Summary Table

| Metric | BCrypt | Argon2 | SCrypt | PBKDF2 |
|--------|--------|--------|--------|--------|
| **Security** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Speed** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ |
| **Ease of Use** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Adoption** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| **Future-proof** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |

---

**Current Implementation: BCrypt with strength 12 🔐**

Perfect balance of security, performance, and ease of use!
