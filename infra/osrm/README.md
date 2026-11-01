# OSRM travel-time matrix

The planner needs real driving times, not straight-line distance: in the mountains a 40 km
straight line can be a 3-hour drive over a pass.

## Setup

```bash
./infra/osrm/prepare.sh
docker compose --profile osrm up -d osrm
```

Then run the API with `sayakat.routing.provider=osrm` and rebuild the matrix:

```
POST /api/admin/matrix/rebuild     (ADMIN token)
```

The matrix is stored in table `travel_leg` and loaded by the planner; OSRM is not called during solving.

## Known limitation (mention in the thesis)

OSRM's car profile assumes speed limits from OSM tags. On gravel mountain roads
(Son-Kul, Kel-Suu, Ala-Kul approach) real speeds are much lower. The API therefore applies a
calibration factor `sayakat.routing.osrm-duration-factor` (default 1.25) to every OSRM duration.
A stronger approach for future work: per-road-class factors calibrated against real trip times
(e.g. from shared-taxi schedules).

If OSRM is not available, `sayakat.routing.provider=haversine` estimates time as
great-circle distance × detour factor ÷ average speed. It is used for synthetic experiments
and as a fallback for missing pairs.
