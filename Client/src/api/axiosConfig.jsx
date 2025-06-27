import axios from "axios";

const baseURL =
  import.meta.env.MODE === "development"
    ? "http://localhost:8000"
    : "https://your-backend.onrender.com"; // 👈 replace with actual URL

const api = axios.create({
  baseURL,
});


export default api;
