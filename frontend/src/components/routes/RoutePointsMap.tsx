import { MapContainer, Marker, Polyline, TileLayer, useMapEvents } from "react-leaflet";
import L from "leaflet";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";
import type { RouteLocationFormPoint } from "../../types/routes";

const leafletMarkerIcon = L.icon({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const mapCenter: [number, number] = [55.74289, 49.18171];

interface MapClickCollectorProps {
  onMapClick: (lat: number, lng: number) => void;
}

const MapClickCollector = ({ onMapClick }: MapClickCollectorProps) => {
  useMapEvents({
    click(event) {
      onMapClick(event.latlng.lat, event.latlng.lng);
    },
  });
  return null;
};

interface RoutePointsMapProps {
  points: RouteLocationFormPoint[];
  onMapClick: (lat: number, lng: number) => void;
}

export const RoutePointsMap = ({ points, onMapClick }: RoutePointsMapProps) => (
  <div className="route-form-map">
    <MapContainer center={mapCenter} zoom={12} style={{ height: "100%", width: "100%" }}>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <MapClickCollector onMapClick={onMapClick} />
      {points.map((point) => (
        <Marker
          key={`${point.orderIndex}-${point.lat}-${point.lng}`}
          position={[point.lat, point.lng]}
          icon={leafletMarkerIcon}
        />
      ))}
      {points.length >= 2 && (
        <Polyline
          positions={points.map((point) => [point.lat, point.lng] as [number, number])}
          pathOptions={{ color: "#1677ff", weight: 4 }}
        />
      )}
    </MapContainer>
  </div>
);
