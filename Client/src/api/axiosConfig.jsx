import axios from "axios";

const api = axios.create({
  baseURL: "https://c8f9-2403-7340-2-d660-84a6-85c3-a59f-68ae.ngrok-free.app",
  headers: {
    "ngrok-skip-browser-warning": "true", // Optional to bypass certain ngrok headers
  },
});

export default api;
