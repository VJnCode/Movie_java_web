import axios from "axios";

const api = axios.create({
  baseURL: "https://d718-2403-7340-2-d660-2db4-b5d9-c55c-23ec.ngrok-free.app",
  headers: {
    "ngrok-skip-browser-warning": "true", // Optional to bypass certain ngrok headers
  },
});

export default api;
