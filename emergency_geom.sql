SELECT 
    emergencyRoomName,
    address,
    ST_Distance_Sphere(geom, ST_GeomFromText('POINT(127.034073 37.522289)')) AS distance_m
FROM emergency_geom
ORDER BY distance_m ASC
LIMIT 10;
