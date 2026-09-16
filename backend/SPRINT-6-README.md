# Sprint 6

## Order placement characterization tests

The Sprint 6 order-placement characterization tests live in the dedicated package:

- `org.example.backend.characterization.sprint6`

Directory:

- `src/test/java/org/example/backend/characterization/sprint6/`

These tests pin the current behavior of the order-placement path before `TradeService` is changed, including:

- affordable order response fields
- reused idempotency key response code and status
- unaffordable buy response code and status
- unknown symbol response code and status
- non-`ACTIVE` account response code and status
- accepted-order persistence for order row, cash, and position

### Characterization test classes

- `OrderPlacementEndpointCharacterizationTest`
- `OrderPlacementServiceCharacterizationTest`
- `OrderPlacementPersistenceCharacterizationTest`

### Run only the Sprint 6 characterization tests

```powershell
Set-Location "C:\Users\capstone\chennai-capstone-SE3-team3\chennai-capstone-SE3-team3\backend"
.\mvnw.cmd "-Dtest=OrderPlacementEndpointCharacterizationTest,OrderPlacementServiceCharacterizationTest,OrderPlacementPersistenceCharacterizationTest" test
```

