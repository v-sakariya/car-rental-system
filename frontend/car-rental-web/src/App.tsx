
import { useState } from "react";
import "./App.css";

type CarType = "SEDAN" | "SUV" | "VAN";

interface Availability {
  carType: CarType;
  pickupDateTime: string;
  returnDateTime: string;
  totalVehicles: number;
  availableVehicles: number;
}

interface Reservation {
  reservationId: string;
  carId: string;
  carType: CarType;
  pickupDateTime: string;
  returnDateTime: string;
  status: "CONFIRMED" | "CANCELLED";
}

interface ApiError {
  message?: string;
}

function App() {
  const [carType, setCarType] = useState<CarType>("SEDAN");
  const [pickupDateTime, setPickupDateTime] = useState("");
  const [numberOfDays, setNumberOfDays] = useState(1);

  const [availability, setAvailability] = useState<Availability | null>(null);
  const [reservation, setReservation] = useState<Reservation | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const getErrorMessage = async (response: Response) => {
    try {
      const data: ApiError = await response.json();
      return data.message || `Request failed (${response.status})`;
    } catch {
      return `Request failed (${response.status})`;
    }
  };

  const validateForm = () => {
    if (!pickupDateTime) {
      setError("Please select a pickup date and time.");
      return false;
    }

    if (new Date(pickupDateTime).getTime() <= Date.now()) {
      setError("Pickup date and time must be in the future.");
      return false;
    }

    if (!Number.isInteger(numberOfDays) || numberOfDays < 1) {
      setError("Rental duration must be at least one day.");
      return false;
    }

    return true;
  };

  const checkAvailability = async () => {
    setError("");
    setMessage("");
    setReservation(null);
    setAvailability(null);

    if (!validateForm()) return;

    setLoading(true);

    try {
      const params = new URLSearchParams({
        carType,
        pickupDateTime: pickupDateTime.replace("T", "T"),
        numberOfDays: String(numberOfDays),
      });

      const response = await fetch(
        `/api/cars/availability?${params.toString()}`
      );

      if (!response.ok) {
        throw new Error(await getErrorMessage(response));
      }

      const data: Availability = await response.json();
      setAvailability(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to check availability.");
    } finally {
      setLoading(false);
    }
  };

  const createReservation = async () => {
    setError("");
    setMessage("");

    if (!validateForm()) return;

    setLoading(true);

    try {
      const response = await fetch("/api/reservations", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          carType,
          pickupDateTime,
          numberOfDays,
        }),
      });

      if (!response.ok) {
        throw new Error(await getErrorMessage(response));
      }

      const data: Reservation = await response.json();
      setReservation(data);
      setAvailability(null);
      setMessage("Your reservation has been confirmed!");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to create reservation.");
    } finally {
      setLoading(false);
    }
  };

  const cancelReservation = async () => {
    if (!reservation) return;

    setError("");
    setMessage("");
    setLoading(true);

    try {
      const response = await fetch(
        `/api/reservations/${reservation.reservationId}`,
        { method: "DELETE" }
      );

      if (!response.ok) {
        throw new Error(await getErrorMessage(response));
      }

      const data: Reservation = await response.json();
      setReservation(data);
      setMessage("Your reservation has been cancelled.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to cancel reservation.");
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (value: string) =>
    new Date(value).toLocaleString(undefined, {
      dateStyle: "medium",
      timeStyle: "short",
    });

  return (
    <div className="app">
      <header className="navbar">
        <a className="brand" href="/">
          <span className="brand-icon">R</span>
          <span>RoadReady</span>
        </a>
        <span className="nav-label">CAR RENTAL SYSTEM</span>
      </header>

      <main>
        <section className="hero">
          <div className="hero-content">
            <p className="eyebrow">YOUR JOURNEY STARTS HERE</p>
            <h1>Find the right car for your next journey.</h1>
            <p className="hero-description">
              Simple, reliable car rentals. Choose your vehicle, check
              availability and reserve in just a few steps.
            </p>
          </div>
          <div className="hero-image" role="img" aria-label="Car on the road" />
        </section>

        <section className="booking-section">
          <div className="section-heading">
            <div>
              <p className="eyebrow">BOOK YOUR RIDE</p>
              <h2>Plan your rental</h2>
            </div>
            <span className="step-label">01 — YOUR DETAILS</span>
          </div>

          <div className="booking-card">
            <div className="form-grid">
              <label>
                Vehicle type
                <select
                  value={carType}
                  onChange={(e) => {
                    setCarType(e.target.value as CarType);
                    setAvailability(null);
                    setReservation(null);
                    setError("");
                    setMessage("");
                  }}
                >
                  <option value="SEDAN">Sedan</option>
                  <option value="SUV">SUV</option>
                  <option value="VAN">Van</option>
                </select>
              </label>

              <label>
                Pickup date and time
                <input
                  type="datetime-local"
                  value={pickupDateTime}
                  min={new Date(Date.now() - new Date().getTimezoneOffset() * 60000)
                    .toISOString()
                    .slice(0, 16)}
                  onChange={(e) => {
                    setPickupDateTime(e.target.value);
                    setAvailability(null);
                    setReservation(null);
                    setError("");
                    setMessage("");
                  }}
                />
              </label>

              <label>
                Rental duration
                <div className="days-input">
                  <input
                    type="number"
                    min="1"
                    max="365"
                    value={numberOfDays}
                    onChange={(e) => {
                      setNumberOfDays(Number(e.target.value));
                      setAvailability(null);
                      setReservation(null);
                    }}
                  />
                  <span>days</span>
                </div>
              </label>
            </div>

            <div className="form-actions">
              <button
                className="button button-secondary"
                onClick={checkAvailability}
                disabled={loading}
              >
                {loading ? "Please wait..." : "Check availability"}
              </button>
              <button
                className="button button-primary"
                onClick={createReservation}
                disabled={loading}
              >
                Reserve a car
                <span aria-hidden="true">→</span>
              </button>
            </div>

            {error && (
              <div className="alert alert-error" role="alert">
                {error}
              </div>
            )}

            {message && (
              <div className="alert alert-success" role="status">
                {message}
              </div>
            )}
          </div>
        </section>

        {availability && (
          <section className="result-card">
            <div>
              <p className="eyebrow">AVAILABILITY RESULT</p>
              <h2>{availability.availableVehicles} vehicles available</h2>
              <p>
                {availability.carType} · {formatDate(availability.pickupDateTime)}
              </p>
              <p>
                Returning: {formatDate(availability.returnDateTime)}
              </p>
              <p>
                Fleet size: {availability.totalVehicles} · Available:{" "}
                {availability.availableVehicles}
              </p>
            </div>
            {availability.availableVehicles > 0 ? (
              <button className="button button-primary" onClick={createReservation} disabled={loading}>
                Confirm reservation →
              </button>
            ) : (
              <span className="unavailable">No vehicles available</span>
            )}
          </section>
        )}

        {reservation && (
          <section className="result-card reservation-card">
            <div>
              <p className="eyebrow">RESERVATION DETAILS</p>
              <h2>
                {reservation.status === "CONFIRMED"
                  ? "You're all set!"
                  : "Reservation cancelled"}
              </h2>
              <p>Reservation ID: <strong>{reservation.reservationId}</strong></p>
              <p>Vehicle: {reservation.carType}</p>
              <p>Pickup: {formatDate(reservation.pickupDateTime)}</p>
              <p>Return: {formatDate(reservation.returnDateTime)}</p>
              <p>
                Status: <strong className={reservation.status === "CONFIRMED" ? "status-confirmed" : "status-cancelled"}>
                  {reservation.status}
                </strong>
              </p>
            </div>

            {reservation.status === "CONFIRMED" && (
              <button
                className="button button-danger"
                onClick={cancelReservation}
                disabled={loading}
              >
                {loading ? "Please wait..." : "Cancel reservation"}
              </button>
            )}
          </section>
        )}
      </main>

      <footer>
        <span>RoadReady Car Rental</span>
        <span>Reliable journeys begin here.</span>
      </footer>
    </div>
  );
}

export default App;
