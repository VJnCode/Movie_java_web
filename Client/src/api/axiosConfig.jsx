import axios from "axios";

const api = axios.create({
  baseURL:  "https://eb7d-2403-7340-2-d660-6805-6de2-9560-848a.ngrok-free.app",
  headers: {
    "ngrok-skip-browser-warning": "true", // Optional to bypass certain ngrok headers
  },
});

export default api;
