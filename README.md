Great! Here’s an updated README.md with a step-by-step guide on how to set up the application locally, including generating your own RSA key pairs and AES key.

⸻


# 🔐 Encryption Service

A Spring Boot-based secure encryption service that supports hybrid encryption using RSA and AES. It encrypts and decrypts files securely, using RSA for key exchange and AES for actual data encryption.

---

## 🚀 Features

- Hybrid Encryption (RSA + AES)
- Secure file upload and download
- JWT-based authentication (Access + Refresh tokens)
- Encrypted fields in DB using JPA converters
- Device-token based tracking
- Modular key loading (RSA, AES from classpath)
- Spring Security + JPA + Hibernate integration

---

## 🧱 Technologies Used

- Java 17
- Spring Boot
- Spring Security + JWT
- JPA (Hibernate)
- MySQL/PostgreSQL (configurable)
- AES-256, RSA-2048 encryption
- Lombok
- Maven

---

## 🖥️ Local Setup Guide

### ✅ Prerequisites

- Java 17+
- Maven 3.6+
- MySQL or PostgreSQL
- Git
- IDE (IntelliJ, VS Code, etc.)

---

## 📝 Step-by-Step Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/encryption-service.git
cd encryption-service


⸻

2. Generate RSA Keys (Access and Refresh)

🛡 Access Token Keys

# Private Key
openssl genpkey -algorithm RSA -out access-private.pem -pkeyopt rsa_keygen_bits:2048

# Public Key
openssl rsa -pubout -in access-private.pem -out access-public.pem

🔁 Refresh Token Keys

# Private Key
openssl genpkey -algorithm RSA -out refresh-private.pem -pkeyopt rsa_keygen_bits:2048

# Public Key
openssl rsa -pubout -in refresh-private.pem -out refresh-public.pem


⸻

3. Generate AES Key

AES key must be base64-encoded.

# Generate 256-bit AES key
openssl rand -base64 32 > aes.key

📌 Valid lengths (before base64 encoding): 16 bytes (128-bit), 24 bytes (192-bit), or 32 bytes (256-bit)

⸻

4. Create keys folder and place files

Inside src/main/resources/, create a keys directory:

src/main/resources/keys/
├── access-private.pem
├── access-public.pem
├── refresh-private.pem
├── refresh-public.pem
└── aes.key


⸻

5. Configure application.yml

Update src/main/resources/application.yml:

application:
  security:
    jwt:
      access-token:
        private-key: classpath:keys/access-private.pem
        public-key: classpath:keys/access-public.pem
        expiration: 3600000 # 1 hour in ms
      refresh-token:
        private-key: classpath:keys/refresh-private.pem
        public-key: classpath:keys/refresh-public.pem
        expiration: 604800000 # 7 days in ms

encryption:
  aes:
    key: classpath:keys/aes.key

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/encryption_db
    username: root
    password: yourpassword
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true


⸻

6. Build and Run the Application

./mvnw clean install
./mvnw spring-boot:run


⸻

🔑 API Endpoints

Method	Endpoint	Description
POST	/auth/register	Register new user
POST	/auth/login	Login and get JWTs
POST	/file/encrypt	Encrypt and upload file
POST	/file/decrypt	Decrypt and download file


⸻

⚠ Common Errors

❌ IllegalBlockSizeException: Data must not be longer than 245 bytes

This occurs when you try to encrypt too much data directly using RSA. Instead:
	1.	Encrypt file content using AES.
	2.	Encrypt the AES key using RSA.
	3.	Send/store the RSA-encrypted AES key + AES-encrypted file.

⸻

🔓 User Deletion With Foreign Keys

To delete a user with related UserDeviceDetailsEntity:

@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<UserDeviceDetailsEntity> deviceDetails;

Then simply call:

userRepository.deleteById(userId);


⸻

📜 License

MIT License

⸻

👨‍💻 Author

Siddharth Kumar
📍 New Delhi
📧 siddharth@example.com
🔗 LinkedIn

---

Let me know if you'd like to include Swagger docs, Postman collection, or Docker support as well.