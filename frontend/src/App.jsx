import { useEffect, useState } from "react";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export default function App() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch(`${API_URL}/api/hello`)
      .then((res) => res.json())
      .then(setData)
      .catch((err) => setError(err.message));
  }, []);

  return (
    <div style={{ fontFamily: "sans-serif", textAlign: "center", marginTop: "4rem" }}>
      <h1>Hello World</h1>
      {error && <p style={{ color: "red" }}>Error: {error}</p>}
      {!error && !data && <p>Loading...</p>}
      {data && (
        <>
          <p>{data.message}</p>
          <p>Visits: {data.visits}</p>
        </>
      )}
    </div>
  );
}
