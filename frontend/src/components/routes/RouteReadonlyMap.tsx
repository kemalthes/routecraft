import { useEffect, useMemo } from "react";
import { MapContainer, Marker, Polyline, TileLayer, useMap } from "react-leaflet";
import L from "leaflet";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";
import type { RouteLocationDto } from "../../types/routes";

const leafletMarkerIcon = L.icon({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const defaultCenter: [number, number] = [55.74289, 49.18171];

const decodePolyline = (encoded: string): [number, number][] => {
  let index = 0;
  let lat = 0;
  let lng = 0;
  const coordinates: [number, number][] = [];

  while (index < encoded.length) {
    let shift = 0;
    let result = 0;
    let byte;

    do {
      byte = encoded.charCodeAt(index) - 63;
      index += 1;
      result |= (byte & 0x1f) << shift;
      shift += 5;
    } while (byte >= 0x20);

    lat += result & 1 ? ~(result >> 1) : result >> 1;
    shift = 0;
    result = 0;

    do {
      byte = encoded.charCodeAt(index) - 63;
      index += 1;
      result |= (byte & 0x1f) << shift;
      shift += 5;
    } while (byte >= 0x20);

    lng += result & 1 ? ~(result >> 1) : result >> 1;
    coordinates.push([lat / 100000, lng / 100000]);
  }

  return coordinates;
};

const getGeometryPositions = (geometry?: string) => {
  if (!geometry) {
    return [];
  }

  try {
    return decodePolyline(geometry);
  } catch {
    return [];
  }
};

const RouteBounds = ({ positions }: { positions: [number, number][] }) => {
  const map = useMap();

  useEffect(() => {
    window.setTimeout(() => map.invalidateSize(), 0);

    if (positions.length === 1) {
      map.setView(positions[0], 13);
      return;
    }

    if (positions.length > 1) {
      map.fitBounds(L.latLngBounds(positions), {
        padding: [36, 36],
        animate: true,
        duration: 0.6,
      });
    }
  }, [map, positions]);

  return null;
};

interface RouteReadonlyMapProps {
  locations: RouteLocationDto[];
  geometry?: string;
}

export const RouteReadonlyMap = ({ locations, geometry }: RouteReadonlyMapProps) => {
  const markerPositions = useMemo<[number, number][]>(
    () =>
      [...locations]
        .sort((first, second) => first.orderIndex - second.orderIndex)
        .map((point) => [point.latitude, point.longitude]),
    [locations],
  );
  const routePositions = useMemo(
    () => {
      const geometryPositions = getGeometryPositions(geometry);
      return geometryPositions.length > 0 ? geometryPositions : markerPositions;
    },
    [geometry, markerPositions],
  );

  return (
    <MapContainer center={routePositions[0] ?? defaultCenter} zoom={12} style={{ width: "100%", height: "100%" }}>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <RouteBounds positions={routePositions} />
      {markerPositions.map((position, index) => (
        <Marker key={`${index}-${position[0]}-${position[1]}`} position={position} icon={leafletMarkerIcon} />
      ))}
      {routePositions.length >= 2 && (
        <Polyline positions={routePositions} pathOptions={{ color: "#1677ff", weight: 5 }} />
      )}
    </MapContainer>
  );
};
