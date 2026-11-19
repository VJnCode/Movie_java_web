# 🎬 Movie Java Web

A full-stack movie web application with a React frontend (Bootstrap styled) and a Spring Boot backend. The backend is containerized with Docker and connected to MongoDB.

---

## 🔧 Tech Stack

### Backend
- Java 21
- Spring Boot 3.4+
- MongoDB
- Maven
- Docker

### Frontend
- React (Vite)
- Bootstrap 5
- Axios

---

## 🚀 Features

- RESTful APIs for movies
- MongoDB integration (cloud/local)
- React UI for browsing movie data
- Dockerized backend for deployment
- CORS-ready frontend-backend connection

---

## 📁 Project Structure

```
Movie_java_web/
├── Dockerfile
├── Server/               # Spring Boot backend
│   ├── pom.xml
│   └── src/
└── client/               # React frontend
    ├── index.html
    └── src/
```

---

## 🐳 Docker Deployment (Backend)

```bash
# From root folder where Dockerfile exists
docker build -t movie-backend .
docker run -p 8000:8000 movie-backend
```

---

## ⚙️ MongoDB Config

In `application.properties`:

```properties
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster>.mongodb.net/<dbname>?retryWrites=true&w=majority
```

---

## 🌐 Frontend Setup (React + Bootstrap)

```bash
cd client
npm install
npm run dev
```

Make sure Axios `baseURL` in `client/src/api.js` points to:

```js
http://localhost:8000
# or your Render backend URL
```

---

## 🧑‍💻 Author

- **VJnCode** – [github.com/VJnCode](https://github.com/VJnCode)

---
