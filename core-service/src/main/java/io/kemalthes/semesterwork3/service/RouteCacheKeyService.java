package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.LocationDto;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("unused")
public class RouteCacheKeyService {

    public String locations(List<LocationDto> locations) {
        if (locations == null) {
            return "locations:null";
        }
        String coordinates = locations.stream()
                .sorted(Comparator.comparing(LocationDto::getOrderIndex))
                .map(location -> "%s,%s".formatted(location.getLongitude(), location.getLatitude()))
                .collect(Collectors.joining(";"));
        String shaCoords;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(coordinates.getBytes(StandardCharsets.UTF_8));
            shaCoords = HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
        return "locations:" + shaCoords;
    }
}
