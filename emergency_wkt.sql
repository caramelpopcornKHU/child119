SELECT 
    emogCode,
    emergencyRoomNickname,
    emergencyRoomName,
    address,
    childEmergencyAvailable,
    childEmergencyTotal,
    ST_Distance_Sphere(
        ST_GeomFromText(CONCAT('POINT(', longitude, ' ', latitude, ')')),
        ST_GeomFromText('POINT(127.034073 37.522289)')
    ) AS distance_m
FROM emergency
ORDER BY distance_m ASC
LIMIT 10;
