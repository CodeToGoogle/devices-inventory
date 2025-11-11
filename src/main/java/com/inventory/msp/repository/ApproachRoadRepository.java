package com.inventory.msp.repository;

import com.inventory.msp.model.ApproachRoad;
import com.inventory.msp.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApproachRoadRepository extends JpaRepository<ApproachRoad, Long> {

    // ✅ Fix: Always fetch location when loading all roads
    @Query("""
        SELECT ar FROM ApproachRoad ar
        JOIN FETCH ar.location
    """)
    List<ApproachRoad> findAllWithLocation();

    // ✅ Fetch by location name
    @Query("""
        SELECT ar FROM ApproachRoad ar
        JOIN FETCH ar.location loc
        WHERE LOWER(TRIM(loc.name)) = LOWER(TRIM(:locationName))
    """)
    List<ApproachRoad> findByLocationName(String locationName);

    Optional<ApproachRoad> findByRoadNameAndLocation(String roadName, Location loc);
}
